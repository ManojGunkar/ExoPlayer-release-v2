//
// Created by Adarsh on 21/12/16.
//

#ifndef BOOMANDROID_RINGBUFFERPROVIDER_H
#define BOOMANDROID_RINGBUFFERPROVIDER_H

#include "AudioBufferProvider.h"
#include "RingBuffer.h"


namespace gdpl {
    using namespace android;

    class RingBufferProvider : public AudioBufferProvider {
    public:

        RingBufferProvider(RingBuffer& source);

        ~RingBufferProvider();

        virtual status_t getNextBuffer(Buffer* buffer);

        virtual void releaseBuffer(Buffer* buffer);

    private:
        RingBuffer& mRingBuffer;
        uint8_t*    mBuffer;
    };
}


#endif //BOOMANDROID_RINGBUFFERPROVIDER_H
