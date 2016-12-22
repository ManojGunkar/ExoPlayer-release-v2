#include <android/log.h>
#include "../include/RingBuffer.h"
#include "../../logger/log.h"

#define LOG_TAG "RingBuffer"


namespace gdpl {

    RingBuffer::RingBuffer(size_t frameCount, uint32_t channels, uint32_t bytesPerChannel)
            :  frameCount(frameCount), channelCount(channels), bytesPerChannel(bytesPerChannel)
    {
        const size_t bytesPerFrame = (size_t)(channelCount * bytesPerChannel);
        _data = (uint8_t*)calloc(frameCount * bytesPerFrame, sizeof(uint8_t));
        audio_utils_fifo_init(&_fifo, frameCount, bytesPerFrame, _data);
    }

    RingBuffer::~RingBuffer() {
        audio_utils_fifo_deinit(&_fifo);
        free(_data);
    }

// Set all data to 0 and flag buffer as empty.
    bool RingBuffer::Empty(void) {
        audio_utils_fifo_init(&_fifo, frameCount, (size_t)(channelCount * bytesPerChannel), _data);
        return true;
    }


    size_t RingBuffer::Read(void *dataPtr, size_t count) {
        return (size_t)audio_utils_fifo_read(&_fifo, dataPtr, count);
    }

// Write to the ring buffer.  Do not overwrite data that has not yet
// been read.
    size_t RingBuffer::Write(void *dataPtr, size_t offset, size_t count) {
        int offsetBytes = (offset * channelCount * bytesPerChannel);
        return (size_t)audio_utils_fifo_write(&_fifo, ((uint8_t*)dataPtr + offsetBytes), count-offset);
    }
}