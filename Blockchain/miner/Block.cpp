#include "Block.h"
#include "iostream"
#include "BlockData.h"


std::string Block::createHash(int nonce) {
    std::stringstream ss;
    ss << index
       << previousHash
       << data.toString()
       << timestamp
       << difficulty
       << nonce;

    std::string str = ss.str();

    unsigned char hashChars[SHA256_DIGEST_LENGTH];
    SHA256(reinterpret_cast<const unsigned char *>(str.c_str()), str.size(), hashChars);

    std::stringstream hashStream;
    for (unsigned char i: hashChars) {
        hashStream << std::hex
                   << std::setw(2)
                   << std::setfill('0')
                   << (int) i;
    }

    return hashStream.str();
}

std::string serializeBlock(const Block &b) {
    std::stringstream ss;
    ss << b.index << ";" << b.previousHash << ";" << b.timestamp
       << ";" << b.difficulty << ";" << b.foundNonce << ";" << b.hash
       << ";" << b.data.numOfPeople << ";" << b.data.latitude
       << ";" << b.data.longitude;
    return ss.str();
}

Block deserializeBlock(const std::string &s) {
    std::stringstream ss(s);
    std::string token;
    Block b;
    BlockData data;

    std::getline(ss, token, ';'); b.index = std::stoi(token);
    std::getline(ss, token, ';'); b.previousHash = token;
    std::getline(ss, token, ';'); b.timestamp = std::stol(token);
    std::getline(ss, token, ';'); b.difficulty = std::stoi(token);
    std::getline(ss, token, ';'); b.foundNonce = std::stoi(token);
    std::getline(ss, token, ';'); b.hash = token;
    std::getline(ss, token, ';'); data.numOfPeople = std::stoi(token);
    std::getline(ss, token, ';'); data.latitude = std::stod(token);
    std::getline(ss, token, ';'); data.longitude = std::stod(token);

    b.data = data;
    return b;
}