/** log */
#include <stddef.h>
#include "logger/log.h"
#include "BoomPlayerJNI.h"
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "audioFx/AudioEngine.h"
#include "Utilities/AutoLock.hpp"
#include "BoomAudioProcessor.h"
#include "OpenSLPlayer.hpp"

#define LOG_TAG "BoomPlayerJNI"

#define BYTES_PER_CHANNEL ((mEngine->GetOutputType() == SAMPLE_TYPE_SHORT)? sizeof(int16_t) : sizeof(float))



namespace gdpl {

    using namespace android;

    class PlaybackThread;

    static const int32_t DEFAULT_FRAME_COUNT = 2048;
    static const int32_t CHANNEL_COUNT = 2;


    static jobject globalJavaAssetManager;

    static AudioEngine *mEngine = nullptr;
    static BoomAudioProcessor *mProcessor = nullptr;
    static gdpl::OpenSLPlayer *mPlayer = nullptr;
    static pthread_mutex_t mLock;



    static void RinseEngine() {
        int16_t* inbuffer = (int16_t*)calloc(DEFAULT_FRAME_COUNT*CHANNEL_COUNT, sizeof(int16_t));
        float* outbuffer = (float*)calloc(DEFAULT_FRAME_COUNT*CHANNEL_COUNT, BYTES_PER_CHANNEL);
        for ( int i = 0; i < 4; i++ ) {
            mEngine->ProcessAudio(inbuffer, outbuffer, DEFAULT_FRAME_COUNT*CHANNEL_COUNT);
        }
        free(inbuffer);
        free(outbuffer);
    }


    /* Checks for error. If any errors exit the application! */
    void CheckErr(SLresult res) {
        if (res != SL_RESULT_SUCCESS) {
            // Debug printing to be placed here
            exit(1);
        }
    }




/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createEngine
 * Signature: ()V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_createEngine(JNIEnv *env, jclass clazz,
                                                                 jobject assetManager, jint sampleRate, jint inFrameCount, jboolean useFloat) {
        globalJavaAssetManager = env->NewGlobalRef(assetManager);
        InitAssetManager(AAssetManager_fromJava(env, globalJavaAssetManager));

        uint32_t frameCount = inFrameCount;
        if ( DEFAULT_FRAME_COUNT > inFrameCount ) {
            frameCount = inFrameCount * (DEFAULT_FRAME_COUNT/inFrameCount);
        }
        OpenSLPlayer::setupEngine(sampleRate, frameCount, useFloat);
        mEngine = new AudioEngine(sampleRate, DEFAULT_FRAME_COUNT);
        //engine->SetHighQuality(false);
        mEngine->SetHeadPhoneType(eOnEar);
        if ( !useFloat ) {
            mEngine->SetOutputType(SAMPLE_TYPE_SHORT);
        }
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    releaseEngine
 * Signature: ()V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_releaseEngine(JNIEnv *env, jclass clazz)
    {
        env->DeleteGlobalRef(globalJavaAssetManager);
        OpenSLPlayer::tearDownEngine();
        delete mEngine;
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createAudioPlayer
 * Signature: (III)Z
 */
    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_createAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jint samplerate,
                                                                          jint channel) {
        //ALOGD("createAudioPlayer start");

        pthread_mutex_init(&mLock, NULL);

        /*Iitialize AudioEngine*/
        mEngine->ResetEngine();
        RinseEngine();

        mProcessor = new BoomAudioProcessor(mEngine, samplerate, channel);
        mPlayer = new gdpl::OpenSLPlayer(mProcessor);
        mPlayer->setup();
        mPlayer->play();

        return true;
    }

    /*
     * Class:     com_globaldelight_boomplayer_OpenSLPlayer
     * Method:    write
     * Signature: (BII)V
     */
    extern "C" jint Java_com_globaldelight_boomplayer_OpenSLPlayer_write(JNIEnv *env, jobject instance,
                                                          jobject buffer, jint offset,
                                                          jint size) {
        gdpl::AutoLock lock(&mLock);


        jbyte *sData = (jbyte *)env->GetDirectBufferAddress(buffer);

        int written = 0;
        //ALOGD("Enter into Write Method");
        if (mPlayer->getState() == SL_PLAYSTATE_PLAYING) {
            written = mProcessor->Write((uint8_t*)sData + offset, size - offset);
        }

        if ( !mPlayer->isReading() && mProcessor->isReady() ) {
            mPlayer->startReading();
        }

        return written;
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    setPlayingAudioPlayer
 * Signature: (Z)V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setPlayingAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jboolean play) {
        gdpl::AutoLock lock(&mLock);

        if ( play ) {
            RinseEngine();
            mPlayer->resume();
        }
        else {
            mPlayer->stopReading();
            mPlayer->pause();
            mProcessor->Flush();
        }
    }


/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    seekTo
 * Signature: (J)V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_seekTo(JNIEnv *env, jclass type,
                                                           jlong position) {
        gdpl::AutoLock lock(&mLock);

        mProcessor->Flush();
        mPlayer->stopReading();
    }

    void stopPlayer(jboolean wait) {
        while ( wait && mPlayer->isReading() ) {
            usleep(1000);
        }
        mPlayer->stop();
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    shutdown
 * Signature: (Z)Z
 */
    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_shutdown(JNIEnv *env, jclass clazz,
                                                                 jboolean wait) {

        stopPlayer(wait);
        mPlayer->tearDown();
        delete mPlayer;
        delete mProcessor;

        pthread_mutex_destroy(&mLock);
        return wait;
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    readAsset
 * Signature: (L)Z
 */

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setVolumeAudioPlayer(JNIEnv *, jclass, jint) {

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setMutAudioPlayer(JNIEnv *, jclass, jboolean) {

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableAudioEffect(JNIEnv *env, jclass clazz,
                                                                      jboolean enabled) {
        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: enableAudioEffect(%d)", enabled);
        mEngine->SetEffectsState(enabled);

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enable3DAudio(JNIEnv *env, jclass clazz,
                                                                  jboolean enabled) {
        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: enable3DAudio(%d)", enabled);
        mEngine->Set3DAudioState(enabled);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableEqualizer(JNIEnv *env, jclass clazz,
                                                                    jboolean enabled) {
        /*openSLEqualizer->Enable(enabled);*/
        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: enableEqualizer(%d)", enabled);
        mEngine->SetEffectsState(enabled);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableSuperBass(JNIEnv *env, jobject instance,
                                                                    jboolean enable) {

        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: enableSuperBass(%d)", enable);
        mEngine->SetSuperBass(enable);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableHighQuality(JNIEnv *env, jobject instance,
                                                                      jboolean enable) {
        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: enableHighQuality(%d)", enable);
        mEngine->SetHighQuality(enable);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setIntensity(JNIEnv *env, jobject instance,
                                                                 jdouble value) {
        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: setIntensity(%g)", value);
        mEngine->SetIntensity(value);

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetEqualizer(JNIEnv *env, jobject instance,
                                                                 jint id,
                                                                 jfloatArray bandGains_) {
        jfloat *bandGains = env->GetFloatArrayElements(bandGains_, NULL);

        gdpl::AutoLock lock(&mLock);
        LOGD("OpenSLPlayer: SetEqualizer(%d)", id);
        mEngine->SetEqualizer(id, (float *) bandGains);

        env->ReleaseFloatArrayElements(bandGains_, bandGains, 0);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetSpeakerState(JNIEnv *env, jobject instance,
                                                                    jint speakerId, jfloat value) {

        gdpl::AutoLock lock(&mLock);
        mEngine->SetSpeakerState(SpeakerID(speakerId), value);
    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_Get3DAudioState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&mLock);
        return mEngine->Get3DAudioState();

    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEffectsState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&mLock);
        return mEngine->GetEffectsState();

    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetIntensity(JNIEnv *env,
                                                                     jobject instance) {
        gdpl::AutoLock lock(&mLock);
        return mEngine->GetIntensity();

    }

    extern "C" jint Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEqualizerId(JNIEnv *env, jobject instance) {
        gdpl::AutoLock lock(&mLock);
        return mEngine->GetEqualizerId();

    }

    extern "C" jfloat Java_com_globaldelight_boomplayer_OpenSLPlayer_GetSpeakerState(JNIEnv *env, jobject instance,
                                                                      jint speakerId) {

        gdpl::AutoLock lock(&mLock);
        return mEngine->GetSpeakerState(SpeakerID(speakerId));
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setHeadPhoneType(JNIEnv *env, jobject instance,
                                                                         jint headphoneType)
    {
        gdpl::AutoLock lock(&mLock);
        mEngine->SetHeadPhoneType(headphoneType);
    }

    extern "C"  jint Java_com_globaldelight_boomplayer_OpenSLPlayer_getHeadPhoneType(JNIEnv *env, jobject instance)
    {
        gdpl::AutoLock lock(&mLock);
        return mEngine->GetHeadPhoneType();
    }
}
