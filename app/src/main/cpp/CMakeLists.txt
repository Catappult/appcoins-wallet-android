cmake_minimum_required(VERSION 3.22)
project(native-lib C)

set(ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES ON CACHE BOOL "Support 16K pages" FORCE)

add_library(wallet-native SHARED native-lib.c)
target_link_options(wallet-native PRIVATE "-Wl,-z,max-page-size=16384")
target_link_libraries(wallet-native android log)

set(CMAKE_VERBOSE_MAKEFILE ON)
