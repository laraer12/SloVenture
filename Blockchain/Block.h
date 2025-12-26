//
// Created by Lenovo on 26. 12. 2025.
//

#ifndef BLOCKCHAIN_BLOCK_H
#define BLOCKCHAIN_BLOCK_H


#include <string>
#include <sstream>
#include "BlockData.h"
#include <openssl/sha.h>
#include <iomanip>

using namespace std;

class Block {
public:
    int index;
    string previousHash;
    time_t timestamp;
    int difficulty;
    BlockData data;
    string hash;
    int foundNonce;

    string createHash(int nonce);
};


#endif //BLOCKCHAIN_BLOCK_H
