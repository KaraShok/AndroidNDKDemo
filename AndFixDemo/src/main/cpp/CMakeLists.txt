cmake_minimum_required(VERSION 3.18.1)

project("andfixdemo")

add_library(
        andfixdemo

        SHARED

        native-lib.cpp
)

target_link_libraries(
        andfixdemo

        log

)