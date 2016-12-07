//
// Created by Adarsh on 05/12/16.
//

#ifndef BOOMANDROID_FIFOBUFFERPROVIDER_H
#define BOOMANDROID_FIFOBUFFERPROVIDER_H

#include "AudioBufferProvider.h"
#include "FIFOBuffer.hpp"

namespace gdpl {
    using namespace android;

    class FifoBufferProvider : public AudioBufferProvider {
    public:

        FifoBufferProvider(FIFOBuffer* fifo, uint32_t channels, uint32_t bytesPerChannel);

        virtual status_t getNextBuffer(Buffer* buffer);

        virtual void releaseBuffer(Buffer* buffer);

    private:
        FIFOBuffer* mFifo;
        const uint32_t kBytesPerChannel;
        const uint32_t kChannelCount;
    };
}

#endif //BOOMANDROID_FIFOBUFFERPROVIDER_H
