//
// Created by Adarsh on 12/12/16.
//

#include <assert.h>
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "logger/log.h"
#include "Utilities/Utility.h"
#include "BoomAudioProcessor.h"

using namespace gdpl;

static const uint16_t UNITY_GAIN = 0x1000;
static const int CHANNEL_COUNT = 2;

#define BYTES_PER_CHANNEL ((mAudioEngine->GetOutputType() == SAMPLE_TYPE_SHORT)? sizeof(int16_t) : sizeof(float))


gdpl::BoomAudioProcessor::BoomAudioProcessor(AudioEngine* engine, int32_t sampleRate, uint32_t channels) :
        mQueueCount(0),
        mIsReady(false),
        mAudioEngine(engine),
        kInputSampleRate(sampleRate),
        kInputChannels(channels),
        kNativeSampleRate(OpenSLPlayer::getEngineSampleRate()),
        kFrameCount((size_t)engine->GetSampleSize()),
        kBytesPerFrame(kFrameCount * CHANNEL_COUNT * BYTES_PER_CHANNEL)
{
    mBuffer = (int16_t *)calloc(kFrameCount * CHANNEL_COUNT, sizeof(int16_t));
    mOutputBuffer = (float*)calloc(kFrameCount * CHANNEL_COUNT, BYTES_PER_CHANNEL);
    mResampleBuffer = (int32_t*)calloc(kFrameCount * CHANNEL_COUNT, sizeof(int32_t));
    for ( int i = 0; i < kTempBufferCount; i++ ) {
        mTempBuffers[i] = (float*)calloc(kFrameCount * CHANNEL_COUNT, BYTES_PER_CHANNEL);
    }
    mIndex = 0;

    mAudioResampler = AudioResampler::create(16, channels, kNativeSampleRate, AudioResampler::HIGH_QUALITY);
    mAudioResampler->setSampleRate(sampleRate);
    mAudioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);

    size_t inFrameCount = (kFrameCount * kInputSampleRate) / kNativeSampleRate;
    mInputBuffer = new RingBuffer(inFrameCount * 2, channels, sizeof(uint16_t));
    mPlaybackBuffer = new RingBuffer(kFrameCount * (kTempBufferCount + 1), 2, BYTES_PER_CHANNEL);
}


gdpl::BoomAudioProcessor::~BoomAudioProcessor() {
    free(mBuffer);
    free(mOutputBuffer);
    free(mResampleBuffer);
    for ( int i = 0; i < kTempBufferCount; i++ ) {
        free(mTempBuffers[i]);
    }

    delete mPlaybackBuffer;
    delete mInputBuffer;
    delete mAudioResampler;
}

size_t gdpl::BoomAudioProcessor::Write(uint8_t* data, size_t size) {
    size_t inputOffset = 0;
    size_t count = size / (kInputChannels * sizeof(int16_t));
    while ( count > inputOffset ) {
        inputOffset += mInputBuffer->Write(data, inputOffset, count);
        if ( inputOffset < count ) { // if the buffer is full
            ProcessAudio(mInputBuffer, mOutputBuffer, kFrameCount);
            SendToPlayback(mOutputBuffer, kFrameCount);
        }
    }

    return size;
}


void BoomAudioProcessor::Flush() {
    mInputBuffer->Empty();
    mPlaybackBuffer->Empty();
    mIsReady = false;
    mQueueCount = 0;
}

void gdpl::BoomAudioProcessor::getNextBuffer(AudioBufferProvider::Buffer *buffer) {
    assert(buffer != nullptr);

    buffer->raw = mTempBuffers[mIndex];
    mIndex = (mIndex + 1) % kTempBufferCount;

    buffer->frameCount = mPlaybackBuffer->Read((uint8_t*)buffer->raw, buffer->frameCount);
    if ( buffer->frameCount == 0 ) {
        LOGE("No data to enqueue!");
        buffer->raw = NULL;
    }
}


void gdpl::BoomAudioProcessor::ProcessAudio(RingBuffer* buffer, void* output, int frameCount)
{
    memset(mResampleBuffer, 0, kFrameCount * CHANNEL_COUNT * sizeof(int32_t));
    mAudioResampler->resample(mResampleBuffer, (size_t)frameCount, buffer);
    ditherAndClamp((int32_t *) mBuffer, mResampleBuffer, (size_t)frameCount);
    mAudioEngine->ProcessAudio(mBuffer, output, frameCount * CHANNEL_COUNT);
}

void gdpl::BoomAudioProcessor::SendToPlayback(void* outBuffer, int frameCount)
{
    size_t offset = 0;
    while ( frameCount > offset ) {
        offset += mPlaybackBuffer->Write((uint8_t*)outBuffer, offset, (size_t)frameCount);
        if ( offset < frameCount ) {
            const uint32_t timeToWait = (uint32_t)((frameCount * 1000000ULL)/(uint64_t)kNativeSampleRate);
            usleep(timeToWait/2);
        }
    }

    if (!mIsReady) {
        mQueueCount++;
        if ( mQueueCount > kTempBufferCount ) {
            mIsReady = true;
            mQueueCount = 0;
            LOGD("Ready for playback!");
        }
    }
}