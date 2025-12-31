#include "Blockchain.h"
#include "iostream"

Block Blockchain::getLastBlock() {
    return blockchain.back();
}

Block Blockchain::createBlock(BlockData data) {
    time_t timestamp = time(nullptr);
    Block newBlock;
    if (blockchain.empty()) {
        newBlock = Block(
                0,
                "0",
                timestamp,
                getAdjustedDifficulty(),
                data
        );
    } else {
        Block previous = getLastBlock();

        newBlock = Block(
                previous.index + 1,
                previous.hash,
                timestamp,
                getAdjustedDifficulty(),
                data
        );
    }

    return newBlock;
}

void Blockchain::addBlock(Block &block) {
    blockchain.push_back(block);
}

void Blockchain::printChain() {
    for (int i = 0; i < blockchain.size(); ++i) {
        std::cout <<"Index: "<< blockchain[i].index << " Hash: " << blockchain[i].hash << " Previous hash: "
                  << blockchain[i].previousHash << std::endl;
    }

}

bool Blockchain::validateBlock(Block block) {
    if (!blockchain.empty()) {
        Block previous = getLastBlock();

        if (block.index != previous.index + 1) {
            std::cout << "Invalid index at block: " << block.index << std::endl;
            return false;
        }

        if (block.previousHash != previous.hash) {
            std::cout << "Invalid previous hash at: " << block.index << std::endl;
            return false;
        }
    }

    std::string hash = block.createHash(block.foundNonce);

    if (hash != block.hash) {
        std::cout << "Invalid hash at: " << block.index << std::endl;
        return false;
    }

    return true;
}

bool Blockchain::validateChain() {
    for (int i = 1; i < blockchain.size(); ++i) {
        Block current = blockchain[i];
        Block previous = blockchain[i - 1];

        if (current.previousHash != previous.hash) {
            std::cout << "Invalid index in chain: " << current.index << std::endl;
            return false;
        }

        if (current.index != previous.index + 1) {
            std::cout << "Invalid index at: " << current.index << std::endl;
            return false;
        }

        std::string hash = current.createHash(current.foundNonce);
        if (hash != current.hash) {
            std::cout << "Invalid hash at: " << current.index << std::endl;
            return false;
        }
    }
    return true;
}

int Blockchain::getLength() {
    return blockchain.size();
}

int Blockchain::getAdjustedDifficulty() {
    if (blockchain.size() < DIFF_INTERVAL)
        return difficulty;

    Block& last = blockchain.back();
    Block& adjustBlock = blockchain[blockchain.size() - DIFF_INTERVAL];

    int expectedTime = BLOCK_INTERVAL * DIFF_INTERVAL;
    int actualTime = last.timestamp - adjustBlock.timestamp;

    if (actualTime < expectedTime / 2)
        return adjustBlock.difficulty + 1;
    else if (actualTime > expectedTime * 2)
        return std::max(1, adjustBlock.difficulty - 1);
    else
        return adjustBlock.difficulty;
}

