#include <iostream>
#include <vector>
#include "Block.h"
#include "Blockchain.h"

int main() {
    Blockchain blockchain;
    blockchain.difficulty = 3;

    BlockData bd1;
    bd1.numOfPeople = 5;
    bd1.timestamp = std::time(nullptr);
    bd1.longitude = 14.5058;
    bd1.latitude = 46.0569;

    // Drugi primer
    BlockData bd2;
    bd2.numOfPeople = 12;
    bd2.timestamp = std::time(nullptr) - 3600;
    bd2.longitude = 13.4170;
    bd2.latitude = 52.5200;

    // Tretji primer
    BlockData bd3;
    bd3.numOfPeople = 3;
    bd3.timestamp = std::time(nullptr) - 86400;
    bd3.longitude = -0.1276;
    bd3.latitude = 51.5074;

    blockchain.addBlock(bd1);
    blockchain.addBlock(bd2);
    blockchain.addBlock(bd3);

    blockchain.printChain();

    return 0;
}