cmake_minimum_required(VERSION 3.28.1)
project(Blockchain)

set(CMAKE_CXX_STANDARD 20)

# set(OPENSSL_ROOT_DIR "C:/Program Files/OpenSSL-Win64")
set(OPENSSL_ROOT_DIR "C:/dev/vcpkg/installed/x64-windows")
find_package(OpenSSL REQUIRED)

find_package(MPI REQUIRED COMPONENTS CXX)

add_executable(Blockchain
        client/main.cpp
        client/BlockData.h
        client/Block.h
        client/Blockchain.cpp
        client/Blockchain.h
        client/Block.cpp
        client/BlockData.cpp
        client/SyncQueue.h
)

target_link_libraries(Blockchain
        OpenSSL::SSL
        OpenSSL::Crypto
        MPI::MPI_CXX
)