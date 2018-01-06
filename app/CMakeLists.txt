# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Specific to android
include_directories(src/main/cpp/ttwatch/include src/main/cpp/ttwatch/src ${CMAKE_CURRENT_BINARY_DIR})

set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_CURRENT_SOURCE_DIR}/cmake_modules/")

add_definitions(-DNOELEVATION)

# From ttwatch CMakeList.txt
include(TestBigEndian)
test_big_endian(is_big_endian)
if(is_big_endian)
  add_definitions(-DTT_BIG_ENDIAN)
else()
  add_definitions(-DTT_LITTLE_ENDIAN)
endif(is_big_endian)

set(TTBIN_SRC
    src/main/cpp/ttwatch/src/export_csv.c
    src/main/cpp/ttwatch/src/export_gpx.c
    src/main/cpp/ttwatch/src/export_kml.c
    src/main/cpp/ttwatch/src/export_tcx.c
    src/main/cpp/ttwatch/src/ttbin.c
    src/main/cpp/ttwatch/src/cycling_cadence.c)
add_library(libttbin STATIC ${TTBIN_SRC})
target_link_libraries(libttbin ${CURL_LIBRARIES})
set_target_properties(libttbin PROPERTIES OUTPUT_NAME ttbin)

add_executable(ttbincnv src/main/cpp/ttwatch/src/ttbincnv.c)
target_link_libraries(ttbincnv libttbin m)