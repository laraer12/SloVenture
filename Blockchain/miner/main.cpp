/*
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
        numThreads = std::stoi(argv[2]);
        //odkomentiraj ce zelis da se uposteva st niti podano pri argumentih
        numThreads = std::thread::hardware_concurrency();
        //"Program na vsakem vozlišču zažene toliko niti kot je optimalna za arhitekturo vozlišča" odvisno kako razumes navodila
        blockchain.difficulty = std::stoi(argv[4]);
    } catch (const std::exception &e) {
        std::cerr << "Illegal arguments\n";
        return 1;
    }

    /* // TEST ČASOVNE VALIDACIJE BLOKOV
    if (mpiRank == 0) {
        std::cout << "[TEST] Timestamp validation test" << std::endl;

        // genesis blok
        BlockData genesisData{0, time(nullptr), 0.0, 0.0};
        Block genesisBlock = blockchain.createBlock(genesisData);
        genesisBlock.hash = genesisBlock.createHash(0);
        genesisBlock.foundNonce = 0;
        blockchain.addBlock(genesisBlock);

        // blok v prihodnosti
        time_t futureTs = time(nullptr) + 61;
        Block futureBlock(
                genesisBlock.index + 1,
                genesisBlock.hash,
                futureTs,
                blockchain.difficulty,
                BlockData{5, futureTs, 0.0, 0.0}
        );
        futureBlock.hash = futureBlock.createHash(0);
        futureBlock.foundNonce = 0;

        if (!blockchain.validateBlock(futureBlock))
            std::cout << "[TEST] Correctly rejected future block" << std::endl;
        else
            std::cout << "[TEST] ERROR: Future block accepted!" << std::endl;

        // blok preveč v preteklosti
        time_t pastTs = time(nullptr) - 61;
        Block pastBlock(
                genesisBlock.index + 1,
                genesisBlock.hash,
                pastTs,
                blockchain.difficulty,
                BlockData{5, pastTs, 0.0, 0.0}
        );
        pastBlock.hash = pastBlock.createHash(0);
        pastBlock.foundNonce = 0;

        if (!blockchain.validateBlock(pastBlock))
            std::cout << "[TEST] Correctly rejected past block" << std::endl;
        else
            std::cout << "[TEST] ERROR: Past block accepted!" << std::endl;

        // validacija celotne verige z neveljavnim timestampom
        // dodam blok s timestampom preveč v preteklosti, da se sproži sporočilo
        Block invalidBlock(
                genesisBlock.index + 1,
                genesisBlock.hash,
                genesisBlock.timestamp - 5000,  // NEVELJAVEN timestamp
                blockchain.difficulty,
                BlockData{1, genesisBlock.timestamp - 5000, 0.0, 0.0}
        );
        invalidBlock.hash = invalidBlock.createHash(0);
        invalidBlock.foundNonce = 0;

        blockchain.addBlock(invalidBlock);

        if (!blockchain.validateChain())
            std::cout << "[TEST] Chain validation detected invalid timestamp!" << std::endl;
    }
    // konec testa

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
*/

#include <chrono>
#include <iostream>
#include <vector>
#include <thread>
#include <atomic>
#include <mutex>
#include <condition_variable>
#include <cstdlib>
#include <mpi.h>

#include "Block.h"
#include "Blockchain.h"
#include "SyncQueue.h"
#include "BlockIO.h"

int mpiRank;
int mpiSize;

SyncQueue<Block> workQueue;
SyncQueue<Block> resultQueue;

std::atomic<bool> stop = false;
std::mutex mtx;
std::condition_variable cv;
std::atomic<bool> found = false;
std::atomic<int> index = -1;

void workerTask(int threadId, int numThreads, int rank, int size) {
    while (!stop.load()) {
        Block block = workQueue.read();

        if (block.stopBlock) break;

        int nonce = rank * numThreads + threadId;
        std::string startZeroes(block.difficulty, '0');

        while (!found.load() && !stop.load()) {
            std::string h = block.createHash(nonce);

            if (h.starts_with(startZeroes)) {
                block.foundNonce = nonce;
                block.hash = h;

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

    if (argc < 7) {
        std::cerr << "Usage: " << argv[0] << " -N <threads> -diff <difficulty> <input_json> <output_json>\n";
        return 1;
    }

    int numThreads = std::stoi(argv[2]);
    int difficulty = std::stoi(argv[4]);
    std::string inputJson = argv[5];
    std::string outputJson = argv[6];

    Blockchain blockchain;
    blockchain.difficulty = difficulty;

    std::vector<std::thread> threads;
    for (int i = 0; i < numThreads; ++i) {
        threads.emplace_back(workerTask, i, numThreads, mpiRank, mpiSize);
    }

    // Preberi blok iz JSON
    Block previousBlock = readBlockFromFile(inputJson);

    // Če je genesis block in nima hash-a, ga rudarimo
    if (previousBlock.index == 0 && previousBlock.hash.empty()) {
        std::cout << "[INFO] Mining genesis block..." << std::endl;

        previousBlock.difficulty = 1;

        int nonce = 0;
        std::string startZeroes(previousBlock.difficulty, '0');

        while (true) {
            std::string h = previousBlock.createHash(nonce);
            if (h.starts_with(startZeroes)) {
                previousBlock.foundNonce = nonce;
                previousBlock.hash = h;
                break;
            }
            nonce++;
        }

        // Shrani rudarjeni genesis block nazaj v JSON, da ga client vidi
        writeBlockToFile(previousBlock, inputJson);
        std::cout << "[INFO] Genesis block mined: " << previousBlock.hash << std::endl;
    }

    Block newBlock;
    newBlock.data = previousBlock.data;
    newBlock.timestamp = previousBlock.timestamp;
    newBlock.index = previousBlock.index + 1;
    newBlock.previousHash = previousBlock.previousHash;
    newBlock.difficulty = previousBlock.difficulty;

    auto start = std::chrono::high_resolution_clock::now();

    // Dodaj blok v vrsto za rudarjenje
    found = false;
    for (int i = 0; i < numThreads; ++i) {
        workQueue.add(newBlock);
    }

    // Čakaj da ena nit najde hash
    {
        std::unique_lock<std::mutex> lock(mtx);
        cv.wait(lock, [] { return found.load(); });
    }

    // Preberi najdeni blok
    Block minedBlock = resultQueue.read();

    if (blockchain.validateBlock(minedBlock) && blockchain.validateChain()) {
        blockchain.addBlock(minedBlock);
        index.fetch_add(1);

        // Zapiši blok v izhodno JSON datoteko
        writeBlockToFile(minedBlock, outputJson);
        if (mpiRank == 0) {
            std::cout << "[INFO] Block mined and saved to JSON: " << outputJson << std::endl;
        }
    } else {
        std::cerr << "[ERROR] Mined block is invalid!" << std::endl;
    }

    // Ustavi niti
    stop = true;
    Block stopBlock;
    stopBlock.stopBlock = true;
    for (int i = 0; i < numThreads; ++i) {
        workQueue.add(stopBlock);
    }

    for (auto &t : threads) {
        t.join();
    }

    auto end = std::chrono::high_resolution_clock::now();
    unsigned int runtime = std::chrono::duration_cast<std::chrono::milliseconds>(end - start).count();
    // std::cout << "Runtime: " << runtime << std::endl;

    MPI_Finalize();
    return 0;
}