#include "Blockchain.h"
#include "iostream"

Block Blockchain::getLastBlock() {
    return blockchain.back();
}

void Blockchain::addBlock(BlockData data) {
    time_t timestamp = time(nullptr);
    Block newBlock;
    if (blockchain.empty()) {
        newBlock = Block(
                0,
                "0",
                timestamp,
                difficulty,
                data
        );
    } else {
        Block previous = getLastBlock();

        newBlock = Block(
                previous.index + 1,
                previous.hash,
                timestamp,
                difficulty,
                data
        );
    }

    proofOfWork(newBlock);

    if (validateBlock(newBlock)) {
        blockchain.push_back(newBlock);
        validateChain();
    }
}

void Blockchain::printChain() {
    for (int i = 0; i < blockchain.size(); ++i) {
        cout << blockchain[i].index << " hash " << blockchain[i].hash << " previous hash "
             << blockchain[i].previousHash << endl;
    }

}

void Blockchain::proofOfWork(Block &block) const {
    string startZeroes(difficulty, '0');
    int nonce = 0;

    while (true) {
        string h = block.createHash(nonce);
        if (h.starts_with(startZeroes) && h != block.previousHash) {
            block.foundNonce = nonce;
            block.hash = h;
            break;
        }
        nonce++;
    }

}

bool Blockchain::validateBlock(Block block) {
    if (!blockchain.empty()) {
        Block previous = getLastBlock();

        if (block.index != previous.index + 1) {
            cout << "Invalid index at: " << block.index << endl;
            return false;
        }

        if (block.previousHash != previous.hash) {
            cout << "Invalid previous hash at: " << block.index << endl;
            return false;
        }
    }

    string hash = block.createHash(block.foundNonce);

    if (hash != block.hash) {
        cout << "Invalid hash at: " << block.index << endl;
        return false;
    }

    return true;
}

bool Blockchain::validateChain() {
    for (int i = 1; i < blockchain.size(); ++i) {
        Block current = blockchain[i];
        Block previous = blockchain[i - 1];

        if (current.previousHash != previous.hash) {
            cout << "Invalid index at: " << current.index << endl;
            return false;
        }

        if (current.index != previous.index + 1) {
            cout << "Invalid index at: " << current.index << endl;
            return false;
        }

        string hash = current.createHash(current.foundNonce);
        if (hash != current.hash) {
            cout << "Invalid hash at: " << current.index << endl;
            return false;
        }
    }
    return true;
}
