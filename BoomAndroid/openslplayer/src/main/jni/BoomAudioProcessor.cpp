//
// Created by Adarsh on 12/12/16.
//

#include <assert.h>
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "bufferprovider/include/RingBufferProvider.h"
#include "logger/log.h"
#include "Utilities/Utility.h"
#include "BoomAudioProcessor.h"


#define LOG_TAG "BoomAudioProcessor"

using namespace gdpl;

static const uint16_t UNITY_GAIN = 0x1000;
static const int CHANNEL_COUNT = 2;
static const int OUTPUT_QUEUE_SIZE = 6;

#define MIN(A,B) (((A)<(B))? (A):(B))
#define BYTES_PER_CHANNEL ((mAudioEngine->GetOutputType() == SAMPLE_TYPE_SHORT)? sizeof(int16_t) : sizeof(float))


gdpl::BoomAudioProcessor::BoomAudioProcessor(AudioEngine* engine, int32_t sampleRate, uint32_t channels) :
        mQueueCount(0),
        mIsReady(false),
        mAudioEngine(engine),
        kInputSampleRate(sampleRate),
        kInputChannels(channels),
        kNativeSampleRate(OpenSLPlayer::getSampleRate()),
        kFrameCount((size_t)engine->GetSampleSize())
{
    mBuffer = (int16_t *)calloc(kFrameCount * CHANNEL_COUNT, sizeof(int16_t));
    mOutputBuffer = (float*)calloc(kFrameCount * CHANNEL_COUNT, BYTES_PER_CHANNEL);
    mResampleBuffer = (int32_t*)calloc(kFrameCount * CHANNEL_COUNT, sizeof(int32_t));
    for ( int i = 0; i < kTempBufferCount; i++ ) {
        mTempBuffers[i] = (float*)calloc(kFrameCount * CHANNEL_COUNT, BYTES_PER_CHANNEL);
    }
    mIndex = 0;

    mAudioResampler = AudioResampler::create(16, MIN(channels,2), kNativeSampleRate, AudioResampler::HIGH_QUALITY);
    mAudioResampler->setSampleRate(sampleRate);
    mAudioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);

    size_t inFrameCount = (kFrameCount * kInputSampleRate) / kNativeSampleRate;
    mInputQueue = new RingBuffer(inFrameCount * 2, channels, sizeof(uint16_t));
    mPlaybackQueue = new RingBuffer(kFrameCount * OUTPUT_QUEUE_SIZE, 2, BYTES_PER_CHANNEL);

    mProvider = new RingBufferProvider(*mInputQueue);
}


gdpl::BoomAudioProcessor::~BoomAudioProcessor() {
    free(mBuffer);
    free(mOutputBuffer);
    free(mResampleBuffer);
    for ( int i = 0; i < kTempBufferCount; i++ ) {
        free(mTempBuffers[i]);
    }

    delete mPlaybackQueue;
    delete mInputQueue;
    delete mAudioResampler;
    delete mProvider;
}

size_t gdpl::BoomAudioProcessor::Write(uint8_t* data, size_t size) {
    mReset = false;
    const size_t bytesPerFrame = kInputChannels * sizeof(int16_t);
    size_t inFrameCount = (kFrameCount * kInputSampleRate) / kNativeSampleRate;
    size_t count = size / bytesPerFrame;
    if ( count > inFrameCount ) {
        count = inFrameCount;
    }

    size_t inputOffset = 0;
    while ( count > inputOffset ) {
        inputOffset += mInputQueue->Write(data, inputOffset, count);
        if ( inputOffset < count ) { // if the buffer is full
            ProcessAudio(mOutputBuffer, kFrameCount);
            SendToPlayback(mOutputBuffer, kFrameCount);
        }
    }

    return (count * bytesPerFrame);
}


void BoomAudioProcessor::Flush() {
    mInputQueue->Empty();
    mPlaybackQueue->Empty();
    mAudioResampler->reset();
    mIsReady = false;
    mQueueCount = 0;
}

void gdpl::BoomAudioProcessor::getNextBuffer(AudioBufferProvider::Buffer *buffer) {
    assert(buffer != nullptr);

    buffer->raw = mTempBuffers[mIndex];
    mIndex = (mIndex + 1) % kTempBufferCount;

    size_t count = mPlaybackQueue->Read((uint8_t*)buffer->raw, buffer->frameCount);
    if ( count < buffer->frameCount ) {
        LOGW("Reading less data [requested = %d read = %d]", buffer->frameCount, count);
    }

    buffer->frameCount = count;
    if ( buffer->frameCount == 0 ) {
        LOGE("No data to enqueue!");
        buffer->raw = NULL;
    }
}


void gdpl::BoomAudioProcessor::ProcessAudio(void* output, int frameCount)
{
    memset(mResampleBuffer, 0, kFrameCount * CHANNEL_COUNT * sizeof(int32_t));
    mAudioResampler->resample(mResampleBuffer, (size_t)frameCount, mProvider);
    ditherAndClamp((int32_t *) mBuffer, mResampleBuffer, (size_t)frameCount);
    mAudioEngine->ProcessAudio(mBuffer, output, frameCount * CHANNEL_COUNT);
}

void gdpl::BoomAudioProcessor::SendToPlayback(void* outBuffer, int frameCount)
{
    size_t offset = 0;
    const uint32_t timeToWait = (uint32_t)((frameCount * 1000000ULL)/(uint64_t)(kNativeSampleRate*2));

    while ( frameCount > offset && !mReset ) {
        offset += mPlaybackQueue->Write((uint8_t*)outBuffer, offset, (size_t)frameCount);
        if ( offset < frameCount && mIsReady ) {
            usleep(timeToWait);
        }
    }

    if (!mIsReady && !mReset ) {
        mQueueCount++;
        if ( mQueueCount >= OUTPUT_QUEUE_SIZE ) {
            mIsReady = true;
            mQueueCount = 0;
            LOGD("Ready for playback!");
        }
    }
}