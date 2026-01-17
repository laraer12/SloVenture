#ifndef BLOCKCHAIN_BLOCKDATA_H
#define BLOCKCHAIN_BLOCKDATA_H


#include <ctime>
#include <string>
#include <sstream>

class BlockData {
public:
    int numOfPeople;
    time_t timestamp;
    double longitude;
    double latitude;

    std::string toString();
};


#endif //BLOCKCHAIN_BLOCKDATA_H
