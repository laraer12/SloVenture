
cmake_minimum_required(VERSION 4.0)
project(Blockchain)

set(CMAKE_CXX_STANDARD 20)

set(OPENSSL_ROOT_DIR "C:/Program Files/OpenSSL-Win64")
find_package(OpenSSL REQUIRED)

set(MPI_CXX_INCLUDE_PATH
        "C:/Program Files (x86)/Microsoft SDKs/MPI/Include"
)

set(MPI_CXX_LIBRARIES
        "C:/Program Files (x86)/Microsoft SDKs/MPI/Lib/x64/msmpi.lib"
)

find_package(MPI REQUIRED COMPONENTS CXX)

add_executable(Blockchain
        main.cpp
        BlockData.h
        Block.h
        Blockchain.cpp
        Blockchain.h
        Block.cpp
        BlockData.cpp
        SyncQueue.h
)

target_link_libraries(Blockchain
        OpenSSL::SSL
        OpenSSL::Crypto
        MPI::MPI_CXX
)
