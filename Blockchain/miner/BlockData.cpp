#include "BlockData.h"
#include "iostream"

std::string BlockData::toString() {
    std::stringstream ss;
    ss << numOfPeople
       << timestamp
       << longitude
       << latitude;
    return ss.str();
}