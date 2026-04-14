cmake_minimum_required(VERSION 3.10)
project(InfraAccelerator)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Dependencies: Crow (Header only), Boost (system/asio), nlohmann_json
find_package(Threads REQUIRED)
find_package(Boost REQUIRED COMPONENTS system)

include_directories(include)

add_executable(accelerator src/main.cpp)
target_link_libraries(accelerator PRIVATE Threads::Threads Boost::system)
