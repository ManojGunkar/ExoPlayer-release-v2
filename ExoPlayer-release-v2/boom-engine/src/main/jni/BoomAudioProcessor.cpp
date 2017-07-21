//
// Created by Adarsh on 12/12/16.
//

#include <assert.h>
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "logger/log.h"
#include "Utilities/Utility.h"
#include "BoomAudioProcessor.h"


#define LOG_TAG "BoomAudioProcessor"

using namespace gdpl;

static const int CHANNEL_COUNT = 2;

#define MAX(A,B) (((A)<(B))? (B):(A))
#define BYTES_PER_CHANNEL ((mAudioEngine->GetOutputType() == SAMPLE_TYPE_SHORT)? sizeof(int16_t) : sizeof(float))

gdpl::BoomAudioProcessor::BoomAudioProcessor(AudioEngine* engine, int32_t sampleRate, uint32_t channels, int32_t outputSampleRate) :
        mAvailableFrames(0),
        mAudioEngine(engine),
        kInputSampleRate(sampleRate),
        kInputChannels(channels),
        kOutputFrameCount((size_t)engine->GetSampleSize())
{
    mBuffer = (int16_t *)calloc(kOutputFrameCount * MAX(CHANNEL_COUNT, channels), sizeof(int16_t));
    mBufferQueue = new RingBuffer(kOutputFrameCount * 2, channels, sizeof(uint16_t));
}


gdpl::BoomAudioProcessor::~BoomAudioProcessor() {
    free(mBuffer);
    delete mBufferQueue;
}

size_t gdpl::BoomAudioProcessor::Write(uint8_t* data, size_t size) {
    const size_t bytesPerFrame = kInputChannels * sizeof(int16_t);
    size_t count = size / bytesPerFrame;
    size_t written = mBufferQueue->Write(data, 0, count);
    mAvailableFrames = mAvailableFrames + written;

    return (written * bytesPerFrame);
}


void BoomAudioProcessor::Flush() {
    mBufferQueue->Empty();
    mAvailableFrames = 0;
}

void gdpl::BoomAudioProcessor::getOutputBuffer(AudioBufferProvider::Buffer *buffer) {
    assert(buffer != nullptr && buffer->raw != nullptr);

    // Check if we have enough data for processing
    if ( mAvailableFrames < kOutputFrameCount || buffer->frameCount < kOutputFrameCount ) {
        buffer->frameCount = 0;
        return;
    }

    size_t count = mBufferQueue->Read((uint8_t*)mBuffer, kOutputFrameCount);
    mAvailableFrames -= count;
    if ( count == kOutputFrameCount ) {
        buffer->frameCount = count;
        if ( kInputChannels == 1 ) {
            convert_mono_to_stereo(mBuffer, kOutputFrameCount);
        }
        else if ( kInputChannels > 2 ) {
            convert_multi_to_stereo(mBuffer, kInputChannels, kOutputFrameCount);
        }

        mAudioEngine->ProcessAudio(mBuffer, buffer->raw, buffer->frameCount * CHANNEL_COUNT);
    }
    else {
        LOGE("Not enough data in Queue!");
    }
}
