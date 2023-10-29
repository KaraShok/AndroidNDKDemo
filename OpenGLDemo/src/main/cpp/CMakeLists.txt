cmake_minimum_required(VERSION 3.18.1)

project("opengldemo")

add_library(
        opengldemo

        SHARED

        native-lib.cpp
        FaceTrack.cpp
)

include_directories(include)

add_subdirectory(FaceAlignment)

add_library( opencv_java4 SHARED IMPORTED)
set_target_properties( opencv_java4 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/armeabi-v7a/libopencv_java4.so)

target_link_libraries(
        opengldemo
        opencv_java4
        seeta_fa_lib
        android
        log
)