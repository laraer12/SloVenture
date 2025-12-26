//
// Created by Lenovo on 26. 12. 2025.
//

#ifndef BLOCKCHAIN_BLOCK_H
#define BLOCKCHAIN_BLOCK_H


#include <string>
#include "BlockData.h"

using namespace std;

class Block {
public:
    int index;
    string hash;
    time_t timestamp;
    int difficulty;
    int nonce;
    BlockData data;
};


#endif //BLOCKCHAIN_BLOCK_H
