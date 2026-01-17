cmake_minimum_required(VERSION 3.28.1)
project(Blockchain)

set(CMAKE_CXX_STANDARD 20)

# set(OPENSSL_ROOT_DIR "C:/Program Files/OpenSSL-Win64")
set(OPENSSL_ROOT_DIR "C:/dev/vcpkg/installed/x64-windows")
find_package(OpenSSL REQUIRED)

find_package(MPI REQUIRED COMPONENTS CXX)

include(FetchContent)
FetchContent_Declare(
        json
        GIT_REPOSITORY https://github.com/nlohmann/json.git
        GIT_TAG v3.11.2
)
FetchContent_MakeAvailable(json)

add_executable(Blockchain
        miner/main.cpp
        miner/BlockData.h
        miner/Block.h
        miner/Blockchain.cpp
        miner/Blockchain.h
        miner/Block.cpp
        miner/BlockData.cpp
        miner/SyncQueue.h
        miner/BlockData.cpp
        miner/BlockIO.h
)

target_link_libraries(Blockchain
        OpenSSL::SSL
        OpenSSL::Crypto
        MPI::MPI_CXX
        nlohmann_json::nlohmann_json
)