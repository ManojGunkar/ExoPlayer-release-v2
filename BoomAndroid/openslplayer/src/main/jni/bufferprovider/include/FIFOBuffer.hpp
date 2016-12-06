#ifndef BOOMANDROID_FIFOBUFFER_HPP
#define BOOMANDROID_FIFOBUFFER_HPP

#include <unistd.h>

namespace gdpl {
    class FIFOBuffer {
    public:
        FIFOBuffer(size_t size, size_t count): _bufferSize(size), _totalSize(size * count) {
            _buffer = (uint8_t*)malloc(_totalSize);
            _readIndex = 0;
            _writeIndex = 0;
        }

        ~FIFOBuffer() {
            free(_buffer);
        }

        void append(void* buffer, size_t size) {
            uint8_t* src = (uint8_t*)buffer;
            if ( _writeIndex + size >= _totalSize ) {
                int bytesToWrite = _totalSize - _writeIndex;
                memcpy(_buffer + _writeIndex, src, bytesToWrite);
                src = src + bytesToWrite;
                size = size - bytesToWrite;
                _writeIndex = 0;
            }

            memcpy(_buffer + _writeIndex, src, size);
            _writeIndex += size;
        }

        size_t filledSize() const {
            if ( _readIndex > _writeIndex ) {
                return _totalSize - _readIndex + _writeIndex;
            }

            return _writeIndex - _readIndex;
        }

        const uint8_t* getNextBuffer()  {
            uint8_t* out = _buffer + _readIndex;
            _readIndex = (_readIndex + _bufferSize) % _totalSize;
            return out;
        }

        void reset() {
            _readIndex = 0;
            _writeIndex = 0;
        }

    private:
        uint8_t*        _buffer;
        const size_t    _totalSize;
        const size_t    _bufferSize;
        size_t          _readIndex;
        size_t          _writeIndex;
    };
}

#endif // BOOMANDROID_FIFOBUFFER_HPP