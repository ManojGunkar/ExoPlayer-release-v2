//
// Created by Adarsh on 12/12/16.
//

#ifndef BOOMANDROID_BOOMAUDIOPROCESSOR_H
#define BOOMANDROID_BOOMAUDIOPROCESSOR_H

#include <stdlib.h>
#include "audioFx/AudioEngine.h"
#include "audioresampler/include/AudioResampler.h"
#include "OpenSLPlayer.hpp"


using namespace android;

namespace gdpl {

    class RingBuffer;
    class BoomAudioProcessor : public gdpl::IDataSource {

    public:
        BoomAudioProcessor(AudioEngine* engine, int32_t sampleRate, uint32_t channels);

        ~BoomAudioProcessor();

        size_t Write(uint8_t* data, size_t size);

        void Flush();

        bool isReady() const {
            return mIsReady;
        }

        void getNextBuffer(AudioBufferProvider::Buffer *buffer);

    private:

        void ProcessAudio(RingBuffer* buffer, void* output, int frameCount);

        void SendToPlayback(void* outBuffer, int frameCount);

    private:
        static const int kTempBufferCount = 4;

        int32_t *mResampleBuffer;
        int16_t *mBuffer;
        void   *mOutputBuffer;
        void   *mTempBuffers[kTempBufferCount];
        int     mIndex;

        AudioResampler *mAudioResampler;
        int mQueueCount;
        bool mIsReady;

        RingBuffer* mPlaybackBuffer;
        RingBuffer* mInputBuffer;
        AudioEngine* mAudioEngine;


        const int kInputSampleRate;
        const int kInputChannels;
        const int kNativeSampleRate;
        const size_t kFrameCount;
        const int kBytesPerFrame;
    };

}


#endif //BOOMANDROID_BOOMAUDIOPROCESSOR_H
