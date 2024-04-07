#include <cstdint>

class Bitmap {
public:
    virtual uint64_t size() = 0;
    virtual void set(uint64_t bit_index, bool value) = 0;
    virtual void setFromTo(uint64_t from, uint64_t to, uint64_t value) = 0;
    virtual bool get(uint64_t bit_index) = 0;
    virtual uint64_t getFromTo(uint64_t from, uint64_t to) = 0;
    
    static bool get_fingerprint_bit(uint64_t index, uint64_t fingerprint) {
        uint64_t mask = 1ULL << index;
        uint64_t and_op = fingerprint & mask;
        return and_op != 0;
    }
};