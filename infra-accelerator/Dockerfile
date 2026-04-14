# Build stage: Use a modern Ubuntu base for better stability
FROM ubuntu:22.04 as builder

# Avoid prompts from apt
ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y \
    build-essential \
    cmake \
    libboost-all-dev \
    curl \
    git \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Download header-only dependencies
RUN mkdir -p include && \
    curl -L https://github.com/CrowCpp/Crow/releases/download/v1.0+5/crow_all.h -o include/crow.h && \
    curl -L https://github.com/nlohmann/json/releases/download/v3.11.2/json.hpp -o include/json.hpp

COPY . .

# Generate CMakeLists.txt dynamically to ensure correct paths
RUN echo 'cmake_minimum_required(VERSION 3.10)\n\
project(InfraAccelerator)\n\
set(CMAKE_CXX_STANDARD 17)\n\
set(CMAKE_CXX_STANDARD_REQUIRED ON)\n\
find_package(Threads REQUIRED)\n\
find_package(Boost REQUIRED COMPONENTS system)\n\
include_directories(include)\n\
add_executable(accelerator src/main.cpp)\n\
target_link_libraries(accelerator PRIVATE Threads::Threads Boost::system)' > CMakeLists.txt

RUN mkdir build && cd build && cmake .. && make

# Run stage: Use the same base to avoid library mismatch
FROM ubuntu:22.04
RUN apt-get update && apt-get install -y \
    libboost-system1.74.0 \
    libstdc++6 \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /root/
COPY --from=builder /app/build/accelerator .

EXPOSE 18080
CMD ["./accelerator"]
