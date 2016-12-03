/** log */
#define LOG_TAG "AudioTrackActivity"
#define DGB 1
#include <stddef.h>
#include <cmath>
#include "logger/log.h"
#include "AudioTrackActivity.h"
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "audioFx/AudioEngine.h"
#include "Utilities/AutoLock.hpp"
#include "Utilities/Utility.h"

#include "OpenSLPlayer.hpp"



namespace gdpl {

    using namespace android;

    class PlaybackThread;

    static const uint16_t UNITY_GAIN = 0x1000;
    static const int32_t DEFAULT_FRAME_COUNT = 2048;
    static const int32_t DEFAULT_SAMPLE_RATE = 44100;
    static const int32_t CHANNEL_COUNT = 2;


    static jobject globalJavaAssetManager;

    static int gFrameCount = 0;
    static PlaybackThread *mThread;
    static RingBuffer *ringBuffer;
    static pthread_mutex_t engineLock;

    gdpl::OpenSLPlayer *openSLPlayer;

//    static OpenSLEqualizer *openSLEqualizer;
    static AudioEngine *engine = nullptr;
    inline static AudioEngine *GetEngine() {
        return engine;
    }


    static void RinseEngine() {
        int16_t* inbuffer = (int16_t*)calloc(gFrameCount*CHANNEL_COUNT, sizeof(int16_t));
        float* outbuffer = (float*)calloc(gFrameCount*CHANNEL_COUNT, sizeof(float));
        for ( int i = 0; i < 4; i++ ) {
            GetEngine()->ProcessAudio(inbuffer, outbuffer, gFrameCount*CHANNEL_COUNT);
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



    class PlaybackThread : public gdpl::IDataSource {
    public:
        bool isPlay = false;

        PlaybackThread(int32_t sampleRate, uint32_t nativeSampleRate, uint32_t frameCount, uint32_t channels) {
            _frameCount = frameCount;
            mBuffer = (int16_t *)calloc(_frameCount * CHANNEL_COUNT, sizeof(int16_t));
            mOutputBuffer = (float*)calloc(_frameCount * CHANNEL_COUNT, sizeof(float));
            mResampleBuffer = (int32_t*)calloc(_frameCount * CHANNEL_COUNT, sizeof(int32_t));

            audioResampler = AudioResampler::create(16, channels, nativeSampleRate, AudioResampler::HIGH_QUALITY);
            audioResampler->setSampleRate(sampleRate);
            audioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);


            pthread_mutex_init(&lock, NULL);
        }


        ~PlaybackThread() {
            isPlay = false;
            release();
            pthread_mutex_destroy(&lock);
            //ALOGD("~PlaybackThread");
        }

        void getNextBuffer(IDataSource::Buffer *buffer) {
            assert(buffer != nullptr);

            AutoLock guard(&lock);
            AutoLock guard2(&engineLock);

            if (ringBuffer->GetReadAvail() > 0) {
                memset(mResampleBuffer, 0, _frameCount * CHANNEL_COUNT * sizeof(int32_t));
                audioResampler->resample(mResampleBuffer, _frameCount, ringBuffer);
                ditherAndClamp((int32_t *) mBuffer, mResampleBuffer, _frameCount);
                GetEngine()->ProcessAudio(mBuffer, mOutputBuffer, _frameCount * CHANNEL_COUNT);
                //memcpy_to_float_from_i16(mOutputBuffer, mBuffer, _frameCount * CHANNEL_COUNT);

                // enqueue another buffer
                buffer->data = mOutputBuffer;
                buffer->size = _frameCount * CHANNEL_COUNT * sizeof(float);
            } else {
                //ALOGD("Enqueue : Ring Buffer Empty");
                buffer->data = nullptr;
                buffer->size = 0;
            }
        }

    private:
        void release() {
            gdpl::AutoLock guard(&lock);
            free(mBuffer);
            free(mOutputBuffer);
            free(mResampleBuffer);
            delete audioResampler;
        }

    private:
        int32_t *mResampleBuffer;
        int16_t *mBuffer;
        float   *mOutputBuffer;
        pthread_mutex_t lock;
        AudioResampler *audioResampler;
        uint32_t _frameCount;

    };

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createEngine
 * Signature: ()V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_createEngine(JNIEnv *env, jclass clazz,
                                                                 jobject assetManager, jint sampleRate, jint inFrameCount) {
        globalJavaAssetManager = env->NewGlobalRef(assetManager);
        InitAssetManager(AAssetManager_fromJava(env, globalJavaAssetManager));

//        gFrameCount = pow(2, round(log2((double)inFrameCount))); // round off the frame count to nearest power of 2
//        LOGD("Computed frame count is %d", gFrameCount);
//        OpenSLPlayer::setupEngine(sampleRate);
        gFrameCount = DEFAULT_FRAME_COUNT;
        uint32_t frameCount = inFrameCount;
        if ( DEFAULT_FRAME_COUNT > inFrameCount ) {
            frameCount = inFrameCount * (DEFAULT_FRAME_COUNT/inFrameCount);
        }
        OpenSLPlayer::setupEngine(sampleRate, frameCount);
        engine = new AudioEngine(sampleRate, gFrameCount);
        engine->SetHeadPhoneType(eOnEar);
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
        delete engine;
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    createAudioPlayer
 * Signature: (III)Z
 */
    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_createAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jint bufferSize,
                                                                          jint samplerate,
                                                                          jint channel) {
        //ALOGD("createAudioPlayer start");

        pthread_mutex_init(&engineLock, NULL);

        /*Iitialize AudioEngine*/
        GetEngine()->ResetEngine();
        RinseEngine();


        ringBuffer = new RingBuffer(bufferSize, channel, sizeof(uint16_t));
        mThread = new PlaybackThread(samplerate, OpenSLPlayer::getEngineSampleRate(), gFrameCount, channel);

        openSLPlayer = new gdpl::OpenSLPlayer(mThread);
        openSLPlayer->setup();
        openSLPlayer->play();

        return true;
    }

    /*
     * Class:     com_globaldelight_boomplayer_OpenSLPlayer
     * Method:    write
     * Signature: (BII)V
     */
    extern "C" jint Java_com_globaldelight_boomplayer_OpenSLPlayer_write(JNIEnv *env, jobject instance,
                                                          jobject buffer, jint offset,
                                                          jint frameCount) {
        //jbyte *sData = env->GetByteArrayElements(sData_, NULL);


        jbyte *sData = (jbyte *)env->GetDirectBufferAddress(buffer);

        int written = 0;
        //ALOGD("Enter into Write Method");
        if (openSLPlayer->getState() == SL_PLAYSTATE_PLAYING) {
            written = ringBuffer->Write((uint8_t*)sData, offset, frameCount);
        }

        if (!openSLPlayer->isReading() && ringBuffer->GetWriteAvail() <= frameCount) {
            openSLPlayer->startReading();
        }

        //env->ReleaseByteArrayElements(sData_, sData, 0);
        //ALOGD("Exit from Write Method");
        return written;
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    setPlayingAudioPlayer
 * Signature: (Z)V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setPlayingAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jboolean enable) {
        SLresult result;
        // set the player's state to playing
        if (!enable) {
            openSLPlayer->pause();

            if (ringBuffer != nullptr) {
                ringBuffer->UnblockWrite();
            }
        } else {
            openSLPlayer->resume();
        }
    }


/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    seekTo
 * Signature: (J)V
 */
    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_seekTo(JNIEnv *env, jclass type,
                                                           jlong position) {
        // TODO
        if (ringBuffer != NULL) {
            ringBuffer->Empty();
        }

        openSLPlayer->stopReading();
    }

    void stopPlayer(jboolean enable) {
        if (ringBuffer != nullptr) {
            ringBuffer->UnblockWrite();
        }

        while ( ringBuffer->GetReadAvail() && openSLPlayer->isReading() ) {
            //ALOGE("Not Stopped...!");
            if (!enable)
                break;

        }
        openSLPlayer->stop();
    }

/*
 * Class:     com_globaldelight_boomplayer_OpenSLPlayer
 * Method:    shutdown
 * Signature: (Z)Z
 */
    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_shutdown(JNIEnv *env, jclass clazz,
                                                                 jboolean enable) {
        stopPlayer(enable);
        openSLPlayer->tearDown();
        delete openSLPlayer;

//        destroy buffers
        delete mThread;
        mThread = NULL;
        delete ringBuffer;
        ringBuffer = NULL;

        return enable;
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
        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: enableAudioEffect(%d)", enabled);
        GetEngine()->SetEffectsState(enabled);

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enable3DAudio(JNIEnv *env, jclass clazz,
                                                                  jboolean enabled) {
        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: enable3DAudio(%d)", enabled);
        GetEngine()->Set3DAudioState(enabled);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableEqualizer(JNIEnv *env, jclass clazz,
                                                                    jboolean enabled) {
        /*openSLEqualizer->Enable(enabled);*/
        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: enableEqualizer(%d)", enabled);
        GetEngine()->SetEffectsState(enabled);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableSuperBass(JNIEnv *env, jobject instance,
                                                                    jboolean enable) {

        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: enableSuperBass(%d)", enable);
        GetEngine()->SetSuperBass(enable);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_enableHighQuality(JNIEnv *env, jobject instance,
                                                                      jboolean enable) {
        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: enableHighQuality(%d)", enable);
        GetEngine()->SetHighQuality(enable);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setIntensity(JNIEnv *env, jobject instance,
                                                                 jdouble value) {
        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: setIntensity(%g)", value);
        GetEngine()->SetIntensity(value);

    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetEqualizer(JNIEnv *env, jobject instance,
                                                                 jint id,
                                                                 jfloatArray bandGains_) {
        jfloat *bandGains = env->GetFloatArrayElements(bandGains_, NULL);

        gdpl::AutoLock lock(&engineLock);
        LOGD("OpenSLPlayer: SetEqualizer(%d)", id);
        GetEngine()->SetEqualizer(id, (float *) bandGains);

        env->ReleaseFloatArrayElements(bandGains_, bandGains, 0);
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_SetSpeakerState(JNIEnv *env, jobject instance,
                                                                    jint speakerId, jfloat value) {

        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetSpeakerState(SpeakerID(speakerId), value);
    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_Get3DAudioState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->Get3DAudioState();

    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEffectsState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetEffectsState();

    }

    extern "C" jboolean Java_com_globaldelight_boomplayer_OpenSLPlayer_GetIntensity(JNIEnv *env,
                                                                     jobject instance) {
        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetIntensity();

    }

    extern "C" jint Java_com_globaldelight_boomplayer_OpenSLPlayer_GetEqualizerId(JNIEnv *env, jobject instance) {
        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetEqualizerId();

    }

    extern "C" jfloat Java_com_globaldelight_boomplayer_OpenSLPlayer_GetSpeakerState(JNIEnv *env, jobject instance,
                                                                      jint speakerId) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetSpeakerState(SpeakerID(speakerId));
    }

    extern "C" void Java_com_globaldelight_boomplayer_OpenSLPlayer_setHeadPhoneType(JNIEnv *env, jobject instance,
                                                                         jint headphoneType)
    {
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetHeadPhoneType(headphoneType);
    }

    extern "C"  jint Java_com_globaldelight_boomplayer_OpenSLPlayer_getHeadPhoneType(JNIEnv *env, jobject instance)
    {
        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetHeadPhoneType();
    }
}
