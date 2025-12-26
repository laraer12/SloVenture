#include "BlockData.h"
#include "iostream"

string BlockData::toString() {
    stringstream ss;
    ss << numOfPeople
       << timestamp
       << longitude
       << latitude;
    return ss.str();
}
