//
// Created by Adarsh on 21/12/16.
//

#include "logger/log.h"
#include "bufferprovider/include/RingBufferProvider.h"


using namespace gdpl;
using namespace android;

static void convert_multi_to_stereo(void* ioBuf, int inChannels, int frameCount)
{
    int16_t* ptr = (int16_t*)ioBuf;
    for ( int i = 0; i < frameCount; i++ ) {
        // L C R Lr Rr
        size_t left = i * inChannels;
        size_t center = i * inChannels + 1;
        size_t right = i * inChannels + 2;
        size_t leftSurround = (inChannels >= 5)? i * inChannels + 4 : left;
        size_t rightSurround = (inChannels >= 5)? i * inChannels + 5 : right;


        int16_t leftValue = (ptr[left] + ptr[center] + ptr[leftSurround]) / 3;
        int16_t rightValue = (ptr[right] + ptr[center]+ ptr[rightSurround]) / 3;

        int j = i*2;
        ptr[j] = leftValue;
        ptr[j+1] = rightValue;
    }
}


RingBufferProvider::RingBufferProvider(RingBuffer& source): mRingBuffer(source)
{
    mBuffer = (uint8_t*)calloc(mRingBuffer.frameCount * mRingBuffer.channelCount * mRingBuffer.bytesPerChannel,
                     sizeof(uint8_t));
}


RingBufferProvider::~RingBufferProvider()
{
    free(mBuffer);
}


status_t RingBufferProvider::getNextBuffer(Buffer* buffer) {
    size_t readFrameCount = mRingBuffer.Read(mBuffer, buffer->frameCount);
    if ( readFrameCount <  buffer->frameCount ) {
        LOGD("RingBuffer::getNextBuffer not enough data [requested = %d, read = %d]", buffer->frameCount, readFrameCount);
        buffer->frameCount = readFrameCount;
    }

    if ( mRingBuffer.channelCount > 2 ) {
        convert_multi_to_stereo(mBuffer, mRingBuffer.channelCount, buffer->frameCount);
    }

    buffer->raw = (buffer->frameCount > 0)? mBuffer : NULL;
    return  NO_ERROR;
}

void RingBufferProvider::releaseBuffer(Buffer* buffer)
{
    buffer->frameCount = 0;
    if(buffer->raw != NULL)
        buffer->raw = NULL;
};
