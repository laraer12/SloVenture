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

    blockchain.push_back(newBlock);
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
