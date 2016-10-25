/** log */
#define LOG_TAG "AudioTrackActivity"
#define DGB 1
#include <stddef.h>
#include "logger/log.h"
#include "AudioTrackActivity.h"
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"
#include "audioFx/AudioEngine.h"
#include "Utilities/AutoLock.hpp"
#include "OpenSLPlayer.hpp"



namespace gdpl {

    using namespace android;

    class PlaybackThread;

    static jobject globalJavaAssetManager;

    static PlaybackThread *mThread;
    static RingBuffer *ringBuffer;
    static pthread_mutex_t engineLock;

    gdpl::OpenSLPlayer *openSLPlayer;

//    static OpenSLEqualizer *openSLEqualizer;

    static const uint16_t UNITY_GAIN = 0x1000;
    static const int32_t FRAME_COUNT = 2048;
    static const int32_t SAMPLE_RATE = 44100;
    static const int32_t CHANNEL_COUNT = 2;


    static AudioEngine *GetEngine() {
        static AudioEngine *engine = nullptr;
        if (nullptr == engine) {
            engine = new AudioEngine(FRAME_COUNT);
        }

        return engine;
    }


    /* Checks for error. If any errors exit the application! */
    void CheckErr(SLresult res) {
        if (res != SL_RESULT_SUCCESS) {
            // Debug printing to be placed here
            exit(1);
        }
    }

    static inline int16_t clamp16(int32_t sample) {
        if ((sample >> 15) ^ (sample >> 31))
            sample = 0x7FFF ^ (sample >> 31);
        return sample;
    }

    static void ditherAndClamp(int32_t *out, int32_t const *sums, size_t c) {
        for (size_t i = 0; i < c; i++) {
            int32_t l = *sums++;
            int32_t r = *sums++;
            int32_t nl = l >> 12;
            int32_t nr = r >> 12;
            l = clamp16(nl);
            r = clamp16(nr);
            *out++ = (r << 16) | (l & 0xFFFF);
        }
    }

    void memcpy_to_float_from_i16(float *dst, const int16_t *src, size_t count)
    {
        while (count--) {
            *dst++ = *src++ / (float)32767;
        }
    }



    class PlaybackThread : public gdpl::IDataSource {
    private:

        int32_t *mResampleBuffer;
        jbyte *mBuffer;
        uint8_t *mOutputBuffer;
        pthread_mutex_t lock;
        AudioResampler *audioResampler;

    public:
        bool isPlay = false;

        PlaybackThread(int32_t sampleRate) {
            mBuffer = (jbyte *) malloc(FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t));
            memset(mBuffer, 0, FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t));

            mOutputBuffer = (uint8_t *) malloc(FRAME_COUNT * CHANNEL_COUNT * sizeof(float));
            memset(mOutputBuffer, 0, FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t));

            mResampleBuffer = new int32_t[FRAME_COUNT * CHANNEL_COUNT];

            audioResampler = AudioResampler::create(16, CHANNEL_COUNT, SAMPLE_RATE,
                                                    AudioResampler::HIGH_QUALITY);
            audioResampler->setSampleRate(sampleRate);
            audioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);


            pthread_mutex_init(&lock, NULL);
        }


        // release file buffer
        void release() {
            gdpl::AutoLock guard(&lock);

            if (mBuffer != NULL) {
                free(mBuffer);
                mBuffer = NULL;
            }

            if (mOutputBuffer != NULL) {
                free(mOutputBuffer);
                mOutputBuffer = NULL;
            }

            delete[] mResampleBuffer;
            delete audioResampler;
        }

        ~PlaybackThread() {
            isPlay = false;
            release();
            pthread_mutex_destroy(&lock);
            //ALOGD("~PlaybackThread");
        }

        void getNextBuffer(gdpl::IDataSource::Buffer *buffer) {
            assert(buffer != nullptr);
            gdpl::AutoLock guard(&lock);
            gdpl::AutoLock guard2(&engineLock);

            if (isPlay) {
                //ALOGD("Enqueue : RingBufferSize : %d", ringBuffer->GetReadAvail());
                if (ringBuffer->GetReadAvail() > 0) {
                    memset(mResampleBuffer, 0, FRAME_COUNT * CHANNEL_COUNT * sizeof(int32_t));
                    audioResampler->resample(mResampleBuffer, FRAME_COUNT, ringBuffer);
                    ditherAndClamp((int32_t *) mBuffer, mResampleBuffer, FRAME_COUNT);
                    GetEngine()->ProcessAudio((short *) mBuffer, mOutputBuffer,
                                              FRAME_COUNT * CHANNEL_COUNT);

                    int bufferSize = FRAME_COUNT * CHANNEL_COUNT * sizeof(float);

                    // enqueue another buffer
                    buffer->data = mOutputBuffer;
                    buffer->size = FRAME_COUNT * CHANNEL_COUNT * sizeof(float);
                } else {
                    //ALOGD("Enqueue : Ring Buffer Empty");
                    isPlay = false;
                }
            }
        }
    };

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    createEngine
 * Signature: ()V
 */
    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_createEngine(JNIEnv *env, jclass clazz,
                                                                 jobject assetManager) {
        globalJavaAssetManager = env->NewGlobalRef(assetManager);
        InitAssetManager(AAssetManager_fromJava(env, globalJavaAssetManager));

        OpenSLPlayer::setupEngine();
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    createAudioPlayer
 * Signature: (III)Z
 */
    extern "C" jboolean Java_com_example_openslplayer_OpenSLPlayer_createAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jint bufferSize,
                                                                          jint samplerate,
                                                                          jint channel) {
        //ALOGD("createAudioPlayer start");

        pthread_mutex_init(&engineLock, NULL);

        /*Iitialize AudioEngine*/
        GetEngine()->ResetEngine();

        ringBuffer = new RingBuffer(bufferSize);
        mThread = new PlaybackThread(samplerate);

        openSLPlayer = new gdpl::OpenSLPlayer(mThread);
        openSLPlayer->setup();
        openSLPlayer->play();

        return true;
    }

    /*
     * Class:     com_example_openslplayer_OpenSLPlayer
     * Method:    write
     * Signature: (BII)V
     */
    extern "C" jint Java_com_example_openslplayer_OpenSLPlayer_write(JNIEnv *env, jobject instance,
                                                          jbyteArray sData_, jint offset,
                                                          jint frameCount) {
        jbyte *sData = env->GetByteArrayElements(sData_, NULL);

        int written = 0;
        //ALOGD("Enter into Write Method");
        if (openSLPlayer->getState() == SL_PLAYSTATE_PLAYING) {
            written = ringBuffer->Write(sData, offset, frameCount);
        }

        if (!mThread->isPlay && ringBuffer->GetWriteAvail() <= frameCount) {
            mThread->isPlay = true;
            openSLPlayer->startReading();
        }


        env->ReleaseByteArrayElements(sData_, sData, 0);
        //ALOGD("Exit from Write Method");
        return written;
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    setPlayingAudioPlayer
 * Signature: (Z)V
 */
    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_setPlayingAudioPlayer(JNIEnv *env, jclass clazz,
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
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    seekTo
 * Signature: (J)V
 */
    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_seekTo(JNIEnv *env, jclass type,
                                                           jlong position) {
        // TODO
        if (ringBuffer != NULL) {
            ringBuffer->Empty();
        }
    }

    void stopPlayer(jboolean enable) {
        if (ringBuffer != nullptr) {
            ringBuffer->UnblockWrite();
        }

        while (ringBuffer->GetReadAvail()) {
            //ALOGE("Not Stopped...!");
            if (!enable)
                break;

        }
        openSLPlayer->stop();
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    shutdown
 * Signature: (Z)Z
 */
    extern "C" jboolean Java_com_example_openslplayer_OpenSLPlayer_shutdown(JNIEnv *env, jclass clazz,
                                                                 jboolean enable) {
        env->DeleteGlobalRef(globalJavaAssetManager);

        stopPlayer(enable);
        openSLPlayer->tearDown();
        delete openSLPlayer;

        OpenSLPlayer::tearDownEngine();

//        destroy buffers
        delete mThread;
        mThread = NULL;
        delete ringBuffer;
        ringBuffer = NULL;

        return enable;
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    readAsset
 * Signature: (L)Z
 */

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_setVolumeAudioPlayer(JNIEnv *, jclass, jint) {

    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_setMutAudioPlayer(JNIEnv *, jclass, jboolean) {

    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_enableAudioEffect(JNIEnv *env, jclass clazz,
                                                                      jboolean enabled) {
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetEffectsState(enabled);

    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_enable3DAudio(JNIEnv *env, jclass clazz,
                                                                  jboolean enabled) {
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->Set3DAudioState(enabled);
    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_enableEqualizer(JNIEnv *env, jclass clazz,
                                                                    jboolean enabled) {
        /*openSLEqualizer->Enable(enabled);*/
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetEffectsState(enabled);
    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_enableSuperBass(JNIEnv *env, jobject instance,
                                                                    jboolean enable) {

        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetSuperBass(enable);
    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_enableHighQuality(JNIEnv *env, jobject instance,
                                                                      jboolean enable) {
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetHighQuality(enable);
    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_setIntensity(JNIEnv *env, jobject instance,
                                                                 jdouble value) {
        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetIntensity(value);

    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_SetEqualizer(JNIEnv *env, jobject instance,
                                                                 jint id,
                                                                 jfloatArray bandGains_) {
        jfloat *bandGains = env->GetFloatArrayElements(bandGains_, NULL);

        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetEqualizer(id, (float *) bandGains);

        env->ReleaseFloatArrayElements(bandGains_, bandGains, 0);
    }

    extern "C" void Java_com_example_openslplayer_OpenSLPlayer_SetSpeakerState(JNIEnv *env, jobject instance,
                                                                    jint speakerId, jfloat value) {

        gdpl::AutoLock lock(&engineLock);
        GetEngine()->SetSpeakerState(SpeakerID(speakerId), value);
    }

    extern "C" jboolean Java_com_example_openslplayer_OpenSLPlayer_Get3DAudioState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->Get3DAudioState();

    }

    extern "C" jboolean Java_com_example_openslplayer_OpenSLPlayer_GetEffectsState(JNIEnv *env,
                                                                        jobject instance) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetEffectsState();

    }

    extern "C" jboolean Java_com_example_openslplayer_OpenSLPlayer_GetIntensity(JNIEnv *env,
                                                                     jobject instance) {
        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetIntensity();

    }

    extern "C" jint Java_com_example_openslplayer_OpenSLPlayer_GetEqualizerId(JNIEnv *env, jobject instance) {
        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetEqualizerId();

    }

    extern "C" jfloat Java_com_example_openslplayer_OpenSLPlayer_GetSpeakerState(JNIEnv *env, jobject instance,
                                                                      jint speakerId) {

        gdpl::AutoLock lock(&engineLock);
        return GetEngine()->GetSpeakerState(SpeakerID(speakerId));
    }
}
