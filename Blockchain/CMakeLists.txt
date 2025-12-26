cmake_minimum_required(VERSION 4.0)
project(Blockchain)

set(CMAKE_CXX_STANDARD 20)

add_executable(Blockchain main.cpp
        BlockData.h
        Block.h)
