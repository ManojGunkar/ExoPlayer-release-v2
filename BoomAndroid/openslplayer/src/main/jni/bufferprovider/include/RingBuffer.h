#ifndef RINGBUFFER_H_
#define RINGBUFFER_H_

#include "AudioBufferProvider.h"
#include "audio_utils/fifo.h"


namespace gdpl {
    using namespace android;

    class RingBuffer {
    public:
        RingBuffer(size_t frameCount, uint32_t channels, uint32_t bytesPerChannel);

        ~RingBuffer();

        size_t Read(uint8_t *dataPtr, size_t numBytes);

        size_t Write(uint8_t *dataPtr, size_t offset, size_t numBytes);

        bool Empty(void);

    public:
        const int  channelCount;
        const int  bytesPerChannel;
        const size_t  frameCount;

    private:
        uint8_t*            _data;
        audio_utils_fifo    _fifo;
    };
};

#endif