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
#include "bufferprovider/include/FIFOBuffer.hpp"
#include "bufferprovider/include/FifoBufferProvider.h"


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
    static pthread_mutex_t engineLock;

    gdpl::OpenSLPlayer *openSLPlayer;

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

        static const int kBufferCount = 10;
        static const int kBytesPerFrame = 2048 * 2 * sizeof(float);
    public:
        bool isPlay = false;

        PlaybackThread(int32_t sampleRate, uint32_t nativeSampleRate, uint32_t frameCount, uint32_t channels) :
                kNativeSampleRate(nativeSampleRate),
                kInputSampleRate(sampleRate),
                kInputChannels(channels),
                _frameCount(frameCount)
        {

            mBuffer = (int16_t *)calloc(_frameCount * CHANNEL_COUNT, sizeof(int16_t));
            mOutputBuffer = (float*)calloc(_frameCount * CHANNEL_COUNT, sizeof(float));
            mResampleBuffer = (int32_t*)calloc(_frameCount * CHANNEL_COUNT, sizeof(int32_t));
            mTempBuffers[0] = (float*)calloc(_frameCount * CHANNEL_COUNT, sizeof(float));
            mTempBuffers[1] = (float*)calloc(_frameCount * CHANNEL_COUNT, sizeof(float));
            mIndex = 0;


            audioResampler = AudioResampler::create(16, channels, nativeSampleRate, AudioResampler::HIGH_QUALITY);
            audioResampler->setSampleRate(sampleRate);
            audioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);

            size_t inFrameCount = (_frameCount * kInputSampleRate) / kNativeSampleRate;


            mFifo = new FIFOBuffer(inFrameCount*channels*sizeof(int16_t), 6);
            mProvider = new FifoBufferProvider(mFifo, channels, sizeof(uint16_t));
            ringBuffer = new RingBuffer(kBytesPerFrame*10, channels, sizeof(uint16_t));

            pthread_mutex_init(&lock, NULL);
        }


        ~PlaybackThread() {
            isPlay = false;
            release();

            delete ringBuffer;
            delete mFifo;
            delete mProvider;
            pthread_mutex_destroy(&lock);
            //ALOGD("~PlaybackThread");
        }

        size_t Write(uint8_t* data, size_t size) {
            const size_t inFrameCount = (_frameCount * kInputSampleRate) / kNativeSampleRate;
            const size_t minBufferSize = (inFrameCount * kInputChannels * sizeof(int16_t));
            mFifo->append(data, size);
            while ( mFifo->filledSize() >= minBufferSize * 2 ) {
                memset(mResampleBuffer, 0, _frameCount * CHANNEL_COUNT * sizeof(int32_t));
                audioResampler->resample(mResampleBuffer, _frameCount, mProvider);
                ditherAndClamp((int32_t *) mBuffer, mResampleBuffer, _frameCount);
                GetEngine()->ProcessAudio(mBuffer, mOutputBuffer, _frameCount * CHANNEL_COUNT);

                int bytesToWrite = _frameCount * CHANNEL_COUNT * sizeof(float);
                int offset = 0;
                while ( bytesToWrite > offset ) {
                    offset += ringBuffer->Write((uint8_t*)mOutputBuffer, offset, bytesToWrite);
                }
            }

            return size;

        }


        void Flush() {
            mFifo->reset();
            ringBuffer->Empty();
        }

        void Unblock()
        {
            ringBuffer->UnblockWrite();
        }


        void Finish(bool wait)
        {
            ringBuffer->UnblockWrite();
            if ( wait ) {
                while (ringBuffer->GetReadAvail());
            }
        }

        bool isReady() {
            return (ringBuffer->GetReadAvail() > (kBytesPerFrame*8) );
        }


        void getNextBuffer(IDataSource::Buffer *buffer) {
            assert(buffer != nullptr);

            buffer->data = mTempBuffers[mIndex];
            mIndex = (mIndex + 1) % 2;
            if (ringBuffer->GetReadAvail() >= buffer->size ) {
                ringBuffer->Read((uint8_t*)buffer->data, buffer->size);
            } else {
                ALOGD("Enqueue : Ring Buffer Empty");
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
        float   *mTempBuffers[2];
        int mIndex;

        pthread_mutex_t lock;
        AudioResampler *audioResampler;
        uint32_t _frameCount;

        FIFOBuffer *mFifo;
        FifoBufferProvider* mProvider;
        RingBuffer *ringBuffer;

        const uint32_t kNativeSampleRate;
        const uint32_t kInputSampleRate;
        const uint32_t kInputChannels;
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

        gFrameCount = DEFAULT_FRAME_COUNT;
        uint32_t frameCount = inFrameCount;
        if ( DEFAULT_FRAME_COUNT > inFrameCount ) {
            frameCount = inFrameCount * (DEFAULT_FRAME_COUNT/inFrameCount);
        }
        OpenSLPlayer::setupEngine(sampleRate, frameCount);
        engine = new AudioEngine(sampleRate, gFrameCount);
        //engine->SetHighQuality(false);
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
            written = mThread->Write((uint8_t*)sData, frameCount);
        }

        if ( !openSLPlayer->isReading() && mThread->isReady() ) {
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
            openSLPlayer->stopReading();
            openSLPlayer->pause();
            mThread->Flush();
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
        mThread->Flush();
        openSLPlayer->stopReading();
    }

    void stopPlayer(jboolean enable) {
        mThread->Finish(enable && openSLPlayer->isReading());
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
