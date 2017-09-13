//
// Created by Adarsh on 12/12/16.
//

#ifndef BOOMANDROID_BOOMAUDIOPROCESSOR_H
#define BOOMANDROID_BOOMAUDIOPROCESSOR_H

#include <stdlib.h>
#include <atomic>
#include "audioFx/AudioEngine.h"
#include "audioresampler/include/AudioResampler.h"


using namespace android;

namespace gdpl {

    class RingBuffer;
    class RingBufferProvider;

    class BoomAudioProcessor {

    public:
        BoomAudioProcessor(AudioEngine* engine, int32_t sampleRate, uint32_t channels, int32_t outputSampleRate);

        ~BoomAudioProcessor();

        size_t Write(uint8_t* data, size_t size);

        void Flush();

        void getOutputBuffer(AudioBufferProvider::Buffer *buffer);

        void reset() {
            mAvailableFrames = 0;
        }

    private:

        void ProcessAudio(void* output, int frameCount);

    private:
        int16_t *mBuffer;
        int mAvailableFrames;

        RingBuffer* mBufferQueue;
        AudioEngine* mAudioEngine;

        const int kInputSampleRate;
        const int kInputChannels;
        const size_t kOutputFrameCount;
    };

}


#endif //BOOMANDROID_BOOMAUDIOPROCESSOR_H
