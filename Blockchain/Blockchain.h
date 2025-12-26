#ifndef BLOCKCHAIN_BLOCKCHAIN_H
#define BLOCKCHAIN_BLOCKCHAIN_H


#include <vector>
#include "Block.h"

class Blockchain {
public:
    int difficulty;

    Block getLastBlock();

    void addBlock(BlockData data);

    void printChain();

private:
    std::vector<Block> blockchain;

    void proofOfWork(Block &block) const;
};


#endif //BLOCKCHAIN_BLOCKCHAIN_H
