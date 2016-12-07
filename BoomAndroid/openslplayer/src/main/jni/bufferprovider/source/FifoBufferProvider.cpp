//
// Created by Adarsh on 05/12/16.
//

#include <logger/log.h>
#include "bufferprovider/include/FifoBufferProvider.h"

namespace gdpl {
    FifoBufferProvider::FifoBufferProvider(FIFOBuffer *fifo, uint32_t channels,
                                           uint32_t bytesPerChannel):
    kBytesPerChannel(bytesPerChannel), kChannelCount(channels)
    {
        mFifo = fifo;
    }

    status_t FifoBufferProvider::getNextBuffer(Buffer *buffer)
    {
        const uint32_t kBytesPerFrame =  kBytesPerChannel * kChannelCount;

        //ALOGD("getNextBuffer", "Requested Frames = %d", buffer->frameCount);

        int requestedBytes = buffer->frameCount * kBytesPerFrame;
        if ( mFifo->filledSize() <  requestedBytes ) {
            LOGD("RingBuffer::getNextBuffer not enough data [available = %d requested= %d]", mFifo->filledSize(), requestedBytes);
            buffer->frameCount = 0;//mFifo->filledSize() / kBytesPerFrame;
        }

        buffer->raw = (buffer->frameCount != 0)? (void*)mFifo->getNextBuffer() : NULL;

        //ALOGD("getNextBuffer", "Available Frames = %d", buffer->frameCount);

        return  NO_ERROR;
    }


    void FifoBufferProvider::releaseBuffer(Buffer *buffer)
    {
        buffer->frameCount = 0;
        if(buffer->raw != NULL)
            buffer->raw = NULL;
    }
}