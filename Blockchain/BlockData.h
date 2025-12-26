//
// Created by Lenovo on 26. 12. 2025.
//

#ifndef BLOCKCHAIN_BLOCKDATA_H
#define BLOCKCHAIN_BLOCKDATA_H


#include <ctime>

class BlockData {
public:
    int numOfPeople;
    time_t timestamp;
    double longitude;
    double latitude;
};


#endif //BLOCKCHAIN_BLOCKDATA_H
