#ifndef RINGBUFFER_H_
#define RINGBUFFER_H_

#include "AudioBufferProvider.h"
#include "audio_utils/fifo.h"


namespace gdpl {
    using namespace android;

    // Lock free ring buffer
    class RingBuffer {
    public:
        RingBuffer(size_t frameCount, uint32_t channels, uint32_t bytesPerChannel);

        ~RingBuffer();

        // Read 'count' frames from the ring buffer
        size_t Read(void *dataPtr, size_t count);

        // Write 'count' frames to the RingBuffer
        size_t Write(void *dataPtr, size_t offset, size_t count);

        // Clear the buffer
        bool Empty(void);

    public:
        const size_t  frameCount;
        const int  channelCount;
        const int  bytesPerChannel;

    private:
        uint8_t*            _data;
        audio_utils_fifo    _fifo;
    };
};

#endif