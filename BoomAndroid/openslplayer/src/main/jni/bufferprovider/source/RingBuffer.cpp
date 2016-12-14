#include <android/log.h>
#include "../include/RingBuffer.h"
#include "../../logger/log.h"

#define LOG_TAG "RingBuffer"


namespace gdpl {

    static void convert_multi_to_stereo(void* ioBuf, int inChannels, int frameCount)
    {
        int16_t* ptr = (int16_t*)ioBuf;
        for ( int i = 0; i < frameCount; i++ ) {
            // L C R Lr Rr
            size_t left = i * inChannels;
            size_t center = i * inChannels + 1;
            size_t right = i * inChannels + 2;
            int j = i*2;
            ptr[j] = (ptr[left] + ptr[center]/2) / 1.5;
            ptr[j+1] = (ptr[right] + ptr[center]/2) / 1.5;
        }
    }


    RingBuffer::RingBuffer(size_t frameCount, uint32_t channels, uint32_t bytesPerChannel)
            :  kFrameCount(frameCount), kChannelCount(channels), kBytesPerChannel(bytesPerChannel)
    {
        const size_t bytesPerFrame = (size_t)(kChannelCount * kBytesPerChannel);
        _data = (uint8_t*)calloc(frameCount * bytesPerFrame, sizeof(uint8_t));
        audio_utils_fifo_init(&_fifo, frameCount, bytesPerFrame, _data);

        _tempBuffer = (uint8_t*)calloc(frameCount * bytesPerFrame, sizeof(uint8_t));
    }

    RingBuffer::~RingBuffer() {
        audio_utils_fifo_deinit(&_fifo);
        free(_data);
        free(_tempBuffer);
    }

// Set all data to 0 and flag buffer as empty.
    bool RingBuffer::Empty(void) {
        audio_utils_fifo_init(&_fifo, kFrameCount, (size_t)(kChannelCount * kBytesPerChannel), _data);
        return true;
    }


    size_t RingBuffer::Read(uint8_t *dataPtr, size_t count) {
        return (size_t)audio_utils_fifo_read(&_fifo, dataPtr, count);
    }

// Write to the ring buffer.  Do not overwrite data that has not yet
// been read.
    size_t RingBuffer::Write(uint8_t *dataPtr, size_t offset, size_t count) {
        int offsetBytes = (offset * kChannelCount * kBytesPerChannel);
        return (size_t)audio_utils_fifo_write(&_fifo, (dataPtr + offsetBytes), count-offset);
    }

    status_t RingBuffer::getNextBuffer(Buffer* buffer) {
        size_t readFrameCount = this->Read(_tempBuffer, buffer->frameCount);
        if ( readFrameCount <  buffer->frameCount ) {
            LOGD("RingBuffer::getNextBuffer not enough data [requested = %d, read = %d]", buffer->frameCount, readFrameCount);
            buffer->frameCount = readFrameCount;
        }

        if ( kChannelCount > 2 ) {
            convert_multi_to_stereo(_tempBuffer, kChannelCount, buffer->frameCount);
        }

        buffer->raw = (buffer->frameCount > 0)? _tempBuffer : NULL;
        return  NO_ERROR;
    }

    void RingBuffer::releaseBuffer(Buffer* buffer)
    {
        buffer->frameCount = 0;
        if(buffer->raw != NULL)
            buffer->raw = NULL;
    };
}