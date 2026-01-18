#ifndef BLOCKCHAIN_BLOCKIO_H
#define BLOCKCHAIN_BLOCKIO_H

#include <fstream>
#include <string>
#include <iostream>
#include "Block.h"
#include "nlohmann/json.hpp"

using json = nlohmann::json;

// Branje bloka iz JSON datoteke
inline Block readBlockFromFile(const std::string &filename) {
    Block block;
    std::ifstream f(filename);
    if (!f.is_open()) {
        std::cerr << "[ERROR] Could not open file for reading: " << filename << std::endl;
        return block;
    }

    try {
        json j;
        f >> j;

        block.index = j.value("index", 0);
        block.previousHash = j.value("previous_hash", "");
        block.timestamp = j.at("timestamp").get<int64_t>();
        block.difficulty = j.value("difficulty", 0);
        block.foundNonce = j.value("nonce", 0);
        block.hash = j.value("hash", "");

        if (j.contains("data")) {
            block.data.numOfPeople = j["data"].value("numOfPeople", 0);
            block.data.timestamp = j["data"].at("timestamp").get<int64_t>();
            block.data.longitude = j["data"].value("longitude", 0.0);
            block.data.latitude = j["data"].value("latitude", 0.0);
        }
    } catch (const std::exception &e) {
        std::cerr << "[ERROR] Failed to parse JSON from file " << filename << ": " << e.what() << std::endl;
    }

    return block;
}

// Pisanje bloka v JSON datoteko
inline void writeBlockToFile(const Block &block, const std::string &filename) {
    json j;
    j["index"] = block.index;
    j["previous_hash"] = block.previousHash;
    j["timestamp"] = block.timestamp;
    j["difficulty"] = block.difficulty;
    j["nonce"] = block.foundNonce;
    j["hash"] = block.hash;

    j["data"] = {
            {"numOfPeople", block.data.numOfPeople},
            {"timestamp", block.data.timestamp},
            {"longitude", block.data.longitude},
            {"latitude", block.data.latitude}
    };

    std::ofstream f(filename);
    if (!f.is_open()) {
        std::cerr << "[ERROR] Could not open file for writing: " << filename << std::endl;
        return;
    }

    f << j.dump(4); // 4 = indent za lepo obliko
}

#endif // BLOCKCHAIN_BLOCKIO_H