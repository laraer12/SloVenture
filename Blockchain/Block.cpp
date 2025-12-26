#include "Block.h"
#include "iostream"


string Block::createHash(int nonce) {
    stringstream ss;
    ss << index
       << previousHash
       << data.toString()
       << timestamp
       << difficulty
       << nonce;

    string str = ss.str();

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
