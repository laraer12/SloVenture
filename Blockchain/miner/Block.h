#ifndef BLOCKCHAIN_BLOCK_H
#define BLOCKCHAIN_BLOCK_H


#include <string>
#include <sstream>
#include "BlockData.h"
#include <openssl/sha.h>
#include <iomanip>

class Block {
public:
    int index;
    std::string previousHash;
    time_t timestamp;
    int difficulty;
    BlockData data;
    std::string hash;
    int foundNonce;
    bool stopBlock = false;

    std::string createHash(int nonce);
};

std::string serializeBlock(const Block &b);
Block deserializeBlock(const std::string &s);

#endif //BLOCKCHAIN_BLOCK_H