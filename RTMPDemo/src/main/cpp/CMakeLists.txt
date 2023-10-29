cmake_minimum_required(VERSION 3.18.1)

project("rtmpdemo")

add_subdirectory(librtmp)

include_directories(include/*)

add_library(
        rtmpdemo

        SHARED

        rtmpdemo.cpp
        AudioChannel.cpp
        VideoChannel.cpp
)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -L${CMAKE_SOURCE_DIR}/libs")

target_link_libraries(
        rtmpdemo
        librtmp
        x264
        faac
        log
)