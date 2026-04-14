#include "crow.h"
#include <nlohmann/json.hpp>
#include <iostream>
#include <vector>
#include <thread>
#include <mutex>
#include <shared_mutex>
#include <unordered_map>
#include <chrono>

using json = nlohmann::json;

// --- High Performance Thread-Safe Cache ---
struct CacheEntry {
    std::string data;
    std::chrono::steady_clock::time_point expiry;
};

class UltraCache {
private:
    std::unordered_map<std::string, CacheEntry> store;
    mutable std::shared_mutex mtx;
public:
    void set(const std::string& key, const std::string& value, int ttl_sec = 600) {
        std::unique_lock lock(mtx);
        store[key] = {value, std::chrono::steady_clock::now() + std::chrono::seconds(ttl_sec)};
    }

    std::string get(const std::string& key) {
        std::shared_lock lock(mtx);
        auto it = store.find(key);
        if (it != store.end()) {
            if (std::chrono::steady_clock::now() < it->second.expiry) {
                return it->second.data;
            }
        }
        return "";
    }
};

UltraCache global_cache;

int main() {
    crow::SimpleApp app;

    // Health check for Render
    CROW_ROUTE(app, "/health")([]() { return "OK"; });

    // Optimized Data Processing for Transactions
    CROW_ROUTE(app, "/v1/accelerate-transaction").methods(crow::HTTPMethod::POST)
    ([](const crow::request& req) {
        try {
            auto body = json::parse(req.body);
            std::string cache_key = body.value("id", "default");
            
            // Return cached calculation if available
            std::string cached = global_cache.get(cache_key);
            if (!cached.empty()) return crow::response(cached);

            // simulate high-speed financial validation/computation
            // C++ is 100x faster than TS for loops and math
            double total = 0;
            if (body.contains("items")) {
                for (auto& item : body["items"]) {
                    total += item.get<double>() * 1.0; 
                }
            }

            json response = {
                {"status", "success"},
                {"computed_total", total},
                {"engine", "cpp-v1"}
            };

            std::string serialized = response.dump();
            global_cache.set(cache_key, serialized);

            return crow::response(serialized);
        } catch (...) {
            return crow::response(400, "Invalid Payload");
        }
    });

    // Support Render dynamic port
    const char* port_env = std::getenv("PORT");
    uint16_t port = port_env ? std::stoi(port_env) : 18080;

    std::cout << "Accelerator running on port " << port << std::endl;

    app.port(port)
       .multithreaded()
       .run();
}
