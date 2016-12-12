#include <pthread.h>
#include "jni.h"
#include "AudioBufferProvider.h"
#include "audio_utils/fifo.h"


#ifndef RINGBUFFER_H_
#define RINGBUFFER_H_

namespace gdpl {
    using namespace android;

    class RingBuffer : public AudioBufferProvider {
    public:
        RingBuffer(size_t frameCount, uint32_t channels, uint32_t bytesPerChannel);

        ~RingBuffer();

        size_t Read(uint8_t *dataPtr, size_t numBytes);

        size_t Write(uint8_t *dataPtr, size_t offset, size_t numBytes);

        bool Empty(void);

        virtual status_t getNextBuffer(Buffer* buffer);

        virtual void releaseBuffer(Buffer* buffer);

    private:
        uint8_t*            _data;
        uint8_t*            _tempBuffer;
        audio_utils_fifo    _fifo;

        const size_t  kFrameCount;
        const int  kChannelCount;
        const int  kBytesPerChannel;
    };
};

#endif