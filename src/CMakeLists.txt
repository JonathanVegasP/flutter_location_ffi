cmake_minimum_required(VERSION 3.10)

project(flutter_location_ffi_library VERSION 1.0.0 LANGUAGES CXX)

find_package(result_channel CONFIG REQUIRED)

add_library(flutter_location_ffi SHARED "flutter_location_ffi.cpp")

set_target_properties(flutter_location_ffi PROPERTIES PUBLIC_HEADER flutter_location_ffi.h OUTPUT_NAME "flutter_location_ffi")

target_link_libraries(flutter_location_ffi PUBLIC result_channel::result_channel)

target_compile_definitions(flutter_location_ffi PUBLIC DART_SHARED_LIB)

if (ANDROID)
    target_link_options(flutter_location_ffi PRIVATE "-Wl,-z,max-page-size=16384")
endif ()
