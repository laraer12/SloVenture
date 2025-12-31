#include <chrono>
#include <iostream>
#include <vector>
#include <random>
#include <ctime>
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

std::vector<BlockData> generateBlockData(int count) {
    std::vector<BlockData> data;
    data.reserve(count);

    std::mt19937 rng(static_cast<unsigned>(std::time(nullptr)));

    std::uniform_int_distribution<int> people(1, 15);
    std::uniform_int_distribution<int> timeOffsetDist(0, 100000);
    std::uniform_real_distribution<double> lonDist(-180.0, 180.0);
    std::uniform_real_distribution<double> latDist(-90.0, 90.0);

    time_t now = std::time(nullptr);

    for (int i = 0; i < count; ++i) {
        int value = people(rng);
        time_t timestamp = now - timeOffsetDist(rng);
        double longitude = lonDist(rng);
        double latitude = latDist(rng);

        data.emplace_back(value, timestamp, longitude, latitude);
    }

    return data;
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
        //numThreads = std::stoi(argv[2]);
        //odkomentiraj ce zelis da se uposteva st niti podano pri argumentih
        numThreads = std::thread::hardware_concurrency();
        //"Program na vsakem vozlišču zažene toliko niti kot je optimalna za arhitekturo vozlišča" odvisno kako razumes navodila
        blockchain.difficulty = std::stoi(argv[4]);
    } catch (const std::exception &e) {
        std::cerr << "Illegal arguments\n";
        return 1;
    }
    std::vector<std::thread> threads;
    std::cout << "Rank " << mpiRank << " running with " << numThreads << " threads.\n";

    std::vector<BlockData> inputData = generateBlockData(15); //za testiranje

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

        if (mpiRank == 0) {
            std::cout << "Block " << newBlock.index << " target difficulty: " << newBlock.difficulty << std::endl;
        }
        std::cout << "Rank " << mpiRank << std::endl;

        Block block = resultQueue.read();
        if (blockchain.validateBlock(block) && blockchain.validateChain()) {
            blockchain.addBlock(block);
            index.fetch_add(1);
            if (mpiRank == 0) {
                std::cout << "Block found" << std::endl;
            }
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

    if (mpiRank == 0) {
        blockchain.printChain();
    }
    MPI_Finalize();
    return 0;
}
