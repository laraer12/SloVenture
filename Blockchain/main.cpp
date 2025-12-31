#include <chrono>
#include <iostream>
#include <vector>
#include <mpi.h>
#include "Block.h"
#include "Blockchain.h"
#include "SyncQueue.h"

int mpiRank;
int mpiSize;

SyncQueue<Block> workQueue;
SyncQueue<Block> resultQueue;

//za zaustavitev niti
std::atomic<bool> stop = false;

std::mutex mtx;
std::condition_variable cv;

//ce je nit nasla ustrezen hash
std::atomic<bool> found = false;

//vcasih pride do nepravilnih indeksov zato se mora belezit trenutni
std::atomic<int> index = -1;


void workerTask(int threadId, int numThreads, int rank, int size) {
    while (!stop.load()) {
        //nit caka da je v vrsti nov blok
        Block block = workQueue.read();

        if (block.stopBlock) break;

        int nonce = rank * numThreads + threadId;

        std::string startZeroes(block.difficulty, '0');

        while (!found.load() && !stop.load()) {
            std::string h = block.createHash(nonce);

            if (h.starts_with(startZeroes)) {
                block.foundNonce = nonce;
                block.hash = h;

                //prevedi da se ni bil najden ustrezen blok
                if (block.index > index.load() && !found.exchange(true)) {
                    resultQueue.add(block);
                    {
                        std::lock_guard<std::mutex> lock(mtx);
                        cv.notify_one();
                    }
                }
                break;
            }

            nonce += size * numThreads;
        }
    }
}

int main(int argc, char **argv) {
    MPI_Init(&argc, &argv);
    MPI_Comm_rank(MPI_COMM_WORLD, &mpiRank);
    MPI_Comm_size(MPI_COMM_WORLD, &mpiSize);

    // -N stevilo niti -diff tezavnost
    if (argc < 4) {
        std::cerr << "Use: -N <unsigned int> -diff <unsigned int>\n";
        return 1;
    }

    Blockchain blockchain;
    int numThreads = 1;

    try {
        numThreads = std::stoi(argv[2]);
        blockchain.difficulty = std::stoi(argv[4]);
    } catch (const std::exception &e) {
        std::cerr << "Illegal arguments\n";
        return 1;
    }
    std::vector<std::thread> threads;
    std::vector<BlockData> inputData;

    BlockData bd1(5, std::time(nullptr), 14.5058, 46.0569);
    BlockData bd2(12, std::time(nullptr) - 3600, 13.4170, 52.5200);
    BlockData bd3(3, std::time(nullptr) - 86400, -0.1276, 51.5074);

    inputData.push_back(bd1);
    inputData.push_back(bd2);
    inputData.push_back(bd3);

    auto start = std::chrono::high_resolution_clock::now();

    for (int i = 0; i < numThreads; ++i) {
        threads.emplace_back(workerTask, i, numThreads, mpiRank, mpiSize);
    }

    for (int i = 0; i < inputData.size(); ++i) {
        found = false;
        //dodajanje blokov
        Block newBlock = blockchain.createBlock(inputData[i]);
        for (int j = 0; j < numThreads; ++j) {
            workQueue.add(newBlock);
        }
        //cakanje da prva nit najde hash
        {
            std::unique_lock<std::mutex> lock(mtx);
            cv.wait(lock, [] { return found.load(); });
        }
        //dodajanje v verigo
        Block block = resultQueue.read();
        if (blockchain.validateBlock(block) && blockchain.validateChain()) {
            blockchain.addBlock(block);
            index.fetch_add(1);
            std::cout << "Block found" << std::endl;
        }
    }

    //zaustavljanje niti
    stop = true;
    Block stopBlock;
    stopBlock.stopBlock = true;
    for (int i = 0; i < numThreads; ++i) {
        workQueue.add(stopBlock);
    }
    //zdruzevanje niti
    for (auto &t: threads) {
        t.join();
    }

    auto end = std::chrono::high_resolution_clock::now();
    unsigned int runtime = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    std::cout << "Runtime: " << runtime << std::endl;

    blockchain.printChain();
    MPI_Finalize();
    return 0;
}
