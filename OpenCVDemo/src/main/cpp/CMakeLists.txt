cmake_minimum_required(VERSION 3.18.1)

project("opencvdemo")

add_library(
        opencvdemo

        SHARED

        native-lib.cpp
)

include_directories(include)

add_library( opencv_java4 SHARED IMPORTED)
set_target_properties( opencv_java4 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/armeabi-v7a/libopencv_java4.so)

target_link_libraries(
        opencvdemo
        opencv_java4
        android
        log
)