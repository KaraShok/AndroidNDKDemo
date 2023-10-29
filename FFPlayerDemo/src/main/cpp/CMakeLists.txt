cmake_minimum_required(VERSION 3.18.1)

project("ffplayerdemo")

add_library(
        ffplayerdemo

        SHARED

        ffplayerdemo.cpp
)

target_link_libraries(
        ffplayerdemo

        log
)