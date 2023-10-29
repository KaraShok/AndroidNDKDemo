cmake_minimum_required(VERSION 3.18.1)

project("gifdemo")

add_library(
        gifdemo

        SHARED

        native-lib.cpp
        dgif_lib.c
        egif_lib.c
        gif_err.c
        gif_font.c
        gif_hash.c
        gifalloc.c
        openbsd-reallocarray.c
        quantize.c
)

target_link_libraries(
        gifdemo

        log

        jnigraphics
)