cmake_minimum_required(VERSION 3.5)
project(DiagnosticExporter)

# Set C++ standard
set(CMAKE_CXX_STANDARD 17 CACHE STRING "C++ standard to conform to")
set(CMAKE_CXX_STANDARD_REQUIRED YES)
set(CMAKE_CXX_EXTENSIONS NO)
set(CMAKE_EXPORT_COMPILE_COMMANDS ON)

file(READ ${CMAKE_CURRENT_SOURCE_DIR}/version VERSION_FILE_CONTENTS)
string(REGEX REPLACE "\n" "" VERSION_FILE_CONTENTS "${VERSION_FILE_CONTENTS}")
string(REPLACE "," ";" VERSION_PARTS ${VERSION_FILE_CONTENTS})
list(GET VERSION_PARTS 0 PROJECT_VERSION)
list(GET VERSION_PARTS 1 PROJECT_RELEASE_DATE)
string(STRIP ${PROJECT_VERSION} PROJECT_VERSION)
string(STRIP ${PROJECT_RELEASE_DATE} PROJECT_RELEASE_DATE)
set(TOOL_VERSION ${PROJECT_VERSION} CACHE STRING "Tool version" FORCE)
set(TOOL_RELEASE_DATE ${PROJECT_RELEASE_DATE} CACHE STRING "Tool release date" FORCE)
add_definitions(-DTOOL_NAME="Diagnostic Exporter")
add_definitions(-DTOOL_VERSION="${TOOL_VERSION}")
add_definitions(-DTOOL_RELEASE_DATE="${TOOL_RELEASE_DATE}")

find_package(LLVM REQUIRED CONFIG)

list(APPEND CMAKE_MODULE_PATH "${LLVM_CMAKE_DIR}")

include(AddLLVM)

# Find the Clang package
find_package(Clang REQUIRED CONFIG)

# Find the nlohmann json package
find_package(nlohmann_json REQUIRED)

# Display LLVM and Clang information
message(STATUS "Found LLVM ${LLVM_PACKAGE_VERSION}")
message(STATUS "Using LLVMConfig.cmake in: ${LLVM_DIR}")
message(STATUS "Found Clang ${Clang_VERSION}")
message(STATUS "Using ClangConfig.cmake in: ${Clang_DIR}")
# Optional: Print out the list of available LLVM targets
message(STATUS "LLVM Targets: ${LLVM_TARGETS_TO_BUILD}")
message(STATUS "LLVM Definitions: ${LLVM_DEFINITIONS}")
#message(STATUS "${LLVM_CMAKE_DIR}")

# Include the LLVM (and Clang) directories
include_directories(${LLVM_INCLUDE_DIRS})

add_definitions(${LLVM_DEFINITIONS})

# Optionally, specify which LLVM components you need
llvm_map_components_to_libnames(llvm_libs core)

set(SOURCE_FILES
        src/main.cpp
        src/DiagnosticExporterAction.cpp
        src/JsonDiagnosticConsumer.cpp)

add_executable(diag-exporter ${SOURCE_FILES})

target_link_libraries(diag-exporter ${llvm_libs})

target_link_libraries(diag-exporter clangTooling)

target_link_libraries(diag-exporter nlohmann_json::nlohmann_json)
