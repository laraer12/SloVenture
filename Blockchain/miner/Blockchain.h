#ifndef BLOCKCHAIN_BLOCKCHAIN_H
#define BLOCKCHAIN_BLOCKCHAIN_H


#include <vector>
#include "Block.h"

class Blockchain {
public:
    int difficulty;

    Block getLastBlock();

    Block createBlock(BlockData data);

    void printChain();

    bool validateBlock(Block block);

    bool validateChain();

    void addBlock(Block &block);

    int getLength();

    static constexpr int BLOCK_INTERVAL = 10;   // sekunde
    static constexpr int DIFF_INTERVAL  = 10;   // bloki

    int getAdjustedDifficulty();

private:
    std::vector<Block> blockchain;
};


#endif //BLOCKCHAIN_BLOCKCHAIN_H