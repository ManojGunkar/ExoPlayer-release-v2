#include <android/log.h>
#include "../include/RingBuffer.h"
#include "../../logger/log.h"

#define LOG_TAG "RingBuffer"


namespace gdpl {


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