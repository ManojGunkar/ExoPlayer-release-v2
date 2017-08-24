/** log */
#include <stddef.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>
#include <Utilities/Utility.h>

#include "logger/log.h"
#include "audioFx/AudioEngine.h"
#include "BoomAudioProcessor.h"
#include "Utilities/AutoLock.hpp"
#include "Utilities/ByteBuffer.h"

#define LOG_TAG "BoomPlayerJNI"

#define BYTES_PER_CHANNEL ((mEngine->GetOutputType() == SAMPLE_TYPE_SHORT)? sizeof(int16_t) : sizeof(float))

#define BOOM_ENGINE_METHOD(method) Java_com_globaldelight_boom_BoomEngine_##method

using namespace gdpl;

static const int32_t DEFAULT_FRAME_COUNT = 2048;
static const int32_t CHANNEL_COUNT = 2;


static jobject globalJavaAssetManager;

static AudioEngine *mEngine = nullptr;
static BoomAudioProcessor *mProcessor = nullptr;
static int mNativeSampleRate = 0;
static pthread_mutex_t mLock;

//#define EXPIRY_DATE makeDate(1,10,2017)
#ifdef EXPIRY_DATE

static inline struct tm makeDate(int day, int month, int year) {
    struct tm time = {};
    time.tm_mday = day;
    time.tm_mon  = month - 1; //starting with 0 (Jan is 0th month)
    time.tm_year = year - 1900; //number of years from 1900

    return time;
}

static bool hasExpired() {
    struct tm expiry = EXPIRY_DATE;
    double diff = difftime(time(NULL), mktime(&expiry));
    return ( diff > 0 );
}

#endif


// Clear all the pending data from engine
static void RinseEngine() {
    int16_t* inbuffer = (int16_t*)calloc(DEFAULT_FRAME_COUNT*CHANNEL_COUNT, sizeof(int16_t));
    float* outbuffer = (float*)calloc(DEFAULT_FRAME_COUNT*CHANNEL_COUNT, BYTES_PER_CHANNEL);
    for ( int i = 0; i < 4; i++ ) {
        mEngine->ProcessAudio(inbuffer, outbuffer, DEFAULT_FRAME_COUNT*CHANNEL_COUNT);
    }
    free(inbuffer);
    free(outbuffer);
}


extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(init)(
        JNIEnv *env,
        jclass clazz,
        jobject assetManager,
        jint sampleRate,
        jint inFrameCount)
{
#ifdef EXPIRY_DATE
    if ( hasExpired() ) {
        fprintf(stderr, "Library has expired!!!\n");
        exit(1);
    }
#endif

    globalJavaAssetManager = env->NewGlobalRef(assetManager);
    InitAssetManager(AAssetManager_fromJava(env, globalJavaAssetManager));
    mNativeSampleRate = sampleRate;
    pthread_mutex_init(&mLock, nullptr);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(finish)(JNIEnv *env, jclass clazz)
{
    env->DeleteGlobalRef(globalJavaAssetManager);
    pthread_mutex_destroy(&mLock);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(start)(
        JNIEnv *env,
        jobject obj,
        jint sampleRate,
        jint channel,
        jboolean useFloat)
{
    //ALOGD("createAudioPlayer start");
    gdpl::AutoLock lock(&mLock);

    mEngine = new AudioEngine(sampleRate, DEFAULT_FRAME_COUNT);
    mEngine->SetHeadPhoneType(eOnEar);
    if ( !useFloat ) {
        mEngine->SetOutputType(SAMPLE_TYPE_SHORT);
    }

    /*Iitialize AudioEngine*/
    mEngine->ResetEngine();
    RinseEngine();

    mProcessor = new BoomAudioProcessor(mEngine, sampleRate, (uint32_t)channel, sampleRate);
}


extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(stop)(
        JNIEnv *env,
        jobject obj)
{
    gdpl::AutoLock lock(&mLock);

    delete mEngine;
    delete mProcessor;
}


extern "C" JNIEXPORT
jint BOOM_ENGINE_METHOD(process)(
        JNIEnv *env,
        jobject obj,
        jobject input,
        jobject output,
        jint    size)
{
    gdpl::AutoLock lock(&mLock);

    ByteBuffer in(env, input);
    ByteBuffer out(env, output);

    const uint8_t* inPtr = in.bytes() + in.position();
    int written = mProcessor->Write((uint8_t*)inPtr, size);
    in.position(in.position() + written);

    uint8_t* outPtr = out.bytes() + out.position();
    AudioBufferProvider::Buffer buffer;
    int converted = 0;
    const int kBytesPerFrame = (CHANNEL_COUNT * BYTES_PER_CHANNEL);
    int maxFrameCount = (out.capacity() - out.position()) / kBytesPerFrame;
    do {
        buffer.frameCount = maxFrameCount;
        buffer.raw = outPtr + converted;
        mProcessor->getOutputBuffer(&buffer);
        maxFrameCount -= buffer.frameCount;
        converted += (buffer.frameCount * kBytesPerFrame);
    } while ( buffer.frameCount > 0);
    out.limit(out.position() + converted);

    return converted;
}


extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(flush)(
        JNIEnv *env,
        jobject obj)
{
    LOGD("Flush");
    gdpl::AutoLock lock(&mLock);
    mProcessor->Flush();
    RinseEngine();
}


extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(enableAudioEffect)(JNIEnv *env, jclass clazz, jboolean enabled)
{
    gdpl::AutoLock lock(&mLock);
    LOGD("enableAudioEffect(%d)", enabled);
    mEngine->SetEffectsState(enabled);

}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(enable3DAudio)(JNIEnv *env, jclass clazz, jboolean enabled) {
    gdpl::AutoLock lock(&mLock);
    LOGD("enable3DAudio(%d)", enabled);
    mEngine->Set3DAudioState(enabled);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(enableEqualizer)(JNIEnv *env, jclass clazz, jboolean enabled)
{
    /*openSLEqualizer->Enable(enabled);*/
    gdpl::AutoLock lock(&mLock);
    LOGD("enableEqualizer(%d)", enabled);
    mEngine->SetEffectsState(enabled);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(enableSuperBass)(JNIEnv *env, jobject instance, jboolean enable)
{
    gdpl::AutoLock lock(&mLock);
    LOGD("enableSuperBass(%d)", enable);
    mEngine->SetSuperBass(enable);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(setQuality)(JNIEnv *env, jobject instance, jint quality)
{
    gdpl::AutoLock lock(&mLock);
    LOGD("setQuality(%d)", quality);
    mEngine->SetQuality(eQualityLevel(quality));
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(setIntensity)(JNIEnv *env, jobject instance, jfloat value)
{
    gdpl::AutoLock lock(&mLock);
    LOGD("setIntensity(%g)", value);
    mEngine->SetIntensity(value);

}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(setEqualizer)(JNIEnv *env, jobject instance, jint id, jfloatArray bandGains_)
{
    jfloat *bandGains = env->GetFloatArrayElements(bandGains_, NULL);
    gdpl::AutoLock lock(&mLock);
    LOGD("setEqualizer(%d)", id);
    mEngine->SetEqualizer(id, bandGains);

    env->ReleaseFloatArrayElements(bandGains_, bandGains, 0);
}

extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(setSpeakerState)(JNIEnv *env, jobject instance, jint speakerId, jfloat value)
{
    gdpl::AutoLock lock(&mLock);
    mEngine->SetSpeakerState(SpeakerID(speakerId), value);
}

extern "C" JNIEXPORT
jboolean BOOM_ENGINE_METHOD(get3DAudioState)(JNIEnv *env, jobject instance)
{
    gdpl::AutoLock lock(&mLock);
    return (jboolean)mEngine->Get3DAudioState();
}

extern "C" JNIEXPORT
jboolean BOOM_ENGINE_METHOD(getEffectsState)(JNIEnv *env, jclass clazz)
{
    gdpl::AutoLock lock(&mLock);
    return (jboolean)mEngine->GetEffectsState();

}

extern "C" JNIEXPORT
jfloat BOOM_ENGINE_METHOD(getIntensity)(JNIEnv *env, jobject instance)
{
    gdpl::AutoLock lock(&mLock);
    return mEngine->GetIntensity();

}

extern "C" JNIEXPORT
jint BOOM_ENGINE_METHOD(getEqualizerId)(JNIEnv *env, jobject instance)
{
    gdpl::AutoLock lock(&mLock);
    return mEngine->GetEqualizerId();
}

extern "C" JNIEXPORT
jfloat BOOM_ENGINE_METHOD(getSpeakerState)(JNIEnv *env, jobject instance, jint speakerId)
{

    gdpl::AutoLock lock(&mLock);
    return mEngine->GetSpeakerState(SpeakerID(speakerId));
}


extern "C" JNIEXPORT
void BOOM_ENGINE_METHOD(setHeadPhoneType)(JNIEnv *env, jobject instance, jint headphoneType)
{
    gdpl::AutoLock lock(&mLock);
    LOGD("setHeadPhoneType(%d)", headphoneType);
    mEngine->SetHeadPhoneType(headphoneType);
}

extern "C"  JNIEXPORT
jint BOOM_ENGINE_METHOD(getHeadPhoneType)(JNIEnv *env, jobject instance)
{
    gdpl::AutoLock lock(&mLock);
    return mEngine->GetHeadPhoneType();
}

