/** log */
#define LOG_TAG "AudioTrackActivity"
#define DGB 1

#include "logger/log.h"
#include "AudioTrackActivity.h"
#include "audioresampler/include/AudioResampler.h"
#include "bufferprovider/include/RingBuffer.h"

namespace android {
    class PlaybackThread;

// engine interfaces
    static SLObjectItf engineObject = NULL;
    static SLEngineItf engineEngine;

// output mix interfaces
    static SLObjectItf outputMixObject = NULL;
    static SLEnvironmentalReverbItf outputMixEnvironmentalReverb = NULL;
    static SLEqualizerItf outputMixEqualizer = NULL;
// aux effect on the output mix, used by the buffer queue player
    static SLEnvironmentalReverbSettings reverbSettings =
            SL_I3DL2_ENVIRONMENT_PRESET_STONECORRIDOR;
// buffer queue player interfaces
    static SLObjectItf bqPlayerObject = NULL;
    static SLPlayItf bqPlayerPlay;
    static SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
    static SLEffectSendItf bqPlayerEffectSend;
    static SLVolumeItf bqPlayerVolume;
    static PlaybackThread *mThread;
    static RingBuffer *ringBuffer;
    static SLuint32 playState;
    static OpenSLEqualizer *openSLEqualizer;
    static AudioResampler *audioResampler;

    static const uint16_t UNITY_GAIN = 0x1000;
    static const int32_t FRAME_COUNT = 1024;
    static const int32_t SAMPLE_RATE = 44100;
    static const int32_t CHANNEL_COUNT = 2;



    static inline int16_t clamp16(int32_t sample)
    {
        if ((sample>>15) ^ (sample>>31))
            sample = 0x7FFF ^ (sample>>31);
        return sample;
    }

    static void ditherAndClamp(int32_t* out, int32_t const *sums, size_t c)
    {
        for (size_t i=0 ; i<c ; i++) {
            int32_t l = *sums++;
            int32_t r = *sums++;
            int32_t nl = l >> 12;
            int32_t nr = r >> 12;
            l = clamp16(nl);
            r = clamp16(nr);
            *out++ = (r<<16) | (l & 0xFFFF);
        }
    }



    class PlaybackThread {
    private:
        int32_t* mResampleBuffer;
        jbyte *mBuffer;
        size_t mSize;
        pthread_mutex_t lock;

    public:
        bool isPlay = false;
        PlaybackThread() : mBuffer(NULL), mSize(0) {
            mBuffer = (jbyte *) malloc(FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t));
            mResampleBuffer = new int32_t[FRAME_COUNT * CHANNEL_COUNT];
            pthread_mutex_init(&lock, NULL);
        }

        void start() {
            if (ringBuffer->GetSize() == 0) {
                return;
            }
            enqueueBuffer();
        }

        // release file buffer
       void release() {
            pthread_mutex_lock(&lock);
            if (mBuffer != NULL) {
                free(mBuffer);
                mBuffer == NULL;
            }
            if (mResampleBuffer != NULL) {
                delete[] mResampleBuffer;
            }
            pthread_mutex_unlock(&lock);
        }

        ~PlaybackThread() {
            isPlay = false;
            release();
            pthread_mutex_destroy(&lock);
            //ALOGD("~PlaybackThread");
        }

        void processEqualizer(jbyte *buffer, size_t size);

        void enqueueBuffer() {
            pthread_mutex_lock(&lock);
            if (bqPlayerBufferQueue == NULL) {
                pthread_mutex_unlock(&lock);
                return;
            }
            while (isPlay) {
                //ALOGD("Enqueue : RingBufferSize : %d", ringBuffer->GetReadAvail());
                if ( ringBuffer->GetReadAvail()>0 ) {

                    //mSize = ringBuffer->Read(mBuffer, bufferSize);
//                    int32_t frameCount = FRAME_COUNT;
                    memset(mResampleBuffer, 0, FRAME_COUNT * CHANNEL_COUNT * sizeof(int32_t));
                    memset(mBuffer, 0, FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t));
                    audioResampler->resample(mResampleBuffer, FRAME_COUNT, ringBuffer);
                    ditherAndClamp((int32_t*)mBuffer, mResampleBuffer, FRAME_COUNT);

                    // enqueue another buffer
                    SLresult result;

                    mSize = FRAME_COUNT * CHANNEL_COUNT * sizeof(int16_t);

                    result = (*bqPlayerBufferQueue)->Enqueue(bqPlayerBufferQueue, openSLEqualizer->processEqualizer(mBuffer, mSize), mSize);

                    if (result == SL_RESULT_BUFFER_INSUFFICIENT) {
                                //ALOGD("Enqueue : false");
                        isPlay = false;
                        return;
                    } else {
                                //ALOGD("Enqueue : true");
                    }
                    break;
                } else {
                            //ALOGD("Enqueue : Ring Buffer Empty");
                    isPlay = false;
                    pthread_mutex_unlock(&lock);
                    return;
                }
            }
            pthread_mutex_unlock(&lock);
        }

        // this callback handler is called every time a buffer finishes playing
        static void playerCallback(SLAndroidSimpleBufferQueueItf bq, void *context) {
            assert(NULL != context);

            PlaybackThread *thread = (PlaybackThread *) context;
            if (thread != NULL) {
                //ALOGD("Enqueue : call");
                thread->enqueueBuffer();
//            thread->start();
            }
        }
    };

/*
* Called when the display is repainted.
*/
    void drawEQDisplay();

    void testBandLevel();

    static void *thread_proc(void *x) {
        PlaybackThread *playbackThread = (PlaybackThread *) x;
        playbackThread->start();
        pthread_exit(NULL);
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    createEngine
 * Signature: ()V
 */
    void Java_com_example_openslplayer_OpenSLPlayer_createEngine(JNIEnv *, jclass) {
        SLresult result;

        // create engine
        result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // realize the engine
        result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the engine interface, which is needed in order to create other objects
        result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;



        // create output mix, with environmental reverb specified as a non-required interface
        const SLInterfaceID ids[2] = {SL_IID_ENVIRONMENTALREVERB, SL_IID_EQUALIZER};
        const SLboolean req[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
        result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 2, ids, req);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // realize the output mix
        result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the environmental reverb interface
        // this could fail if the environmental reverb effect is not available,
        // either because the feature is not present, excessive CPU load, or
        // the required MODIFY_AUDIO_SETTINGS permission was not requested and granted
        result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_ENVIRONMENTALREVERB,
                                                  &outputMixEnvironmentalReverb);
        if (SL_RESULT_SUCCESS == result) {
            reverbSettings = SL_I3DL2_ENVIRONMENT_PRESET_LARGEHALL;
            result = (*outputMixEnvironmentalReverb)->SetEnvironmentalReverbProperties(
                    outputMixEnvironmentalReverb, &reverbSettings);
            (void) result;
        }

        result = (*outputMixObject)->GetInterface(outputMixObject, SL_IID_EQUALIZER,
                                                  &outputMixEqualizer);

        if (SL_RESULT_SUCCESS == result) {
            (void) result;
        }
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    createAudioPlayer
 * Signature: (III)Z
 */
    jboolean Java_com_example_openslplayer_OpenSLPlayer_createAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jint bufferSize, jint samplerate, jint channel) {
        //ALOGD("createAudioPlayer start");

        SLresult result;
        // configure audio source
        SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                           4};

        /*// Set-up sound audio source.
        SLDataLocator_AndroidSimpleBufferQueue lDataLocatorIn;
        lDataLocatorIn.locatorType =            SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE;
        lDataLocatorIn.numBuffers = 1;// At most one buffer in the queue.

        SLDataFormat_PCM lDataFormat;
        lDataFormat.formatType = SL_DATAFORMAT_PCM;
        lDataFormat.numChannels = 1; // Mono sound.
        lDataFormat.samplesPerSec = SL_SAMPLINGRATE_44_1;
        lDataFormat.bitsPerSample = SL_PCMSAMPLEFORMAT_FIXED_16;
        lDataFormat.containerSize = SL_PCMSAMPLEFORMAT_FIXED_16;
        lDataFormat.channelMask = SL_SPEAKER_FRONT_CENTER;
        lDataFormat.endianness = SL_BYTEORDER_LITTLEENDIAN;

        SLDataSource lDataSource;
        lDataSource.pLocator = &lDataLocatorIn;
        lDataSource.pFormat = &lDataFormat;

        SLDataLocator_OutputMix lDataLocatorOut;
        lDataLocatorOut.locatorType = SL_DATALOCATOR_OUTPUTMIX;
        lDataLocatorOut.outputMix = outputMixObject;

        SLDataSink lDataSink;
        lDataSink.pLocator = &lDataLocatorOut;
        lDataSink.pFormat = NULL;*/


        SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
                                       SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                       SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                       SL_BYTEORDER_LITTLEENDIAN};
        SLDataSource audioSrc = {&loc_bufq, &format_pcm};

        // configure audio sink
        SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
        SLDataSink audioSnk = {&loc_outmix, NULL};

        // create audio player
        const SLInterfaceID ids[3] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND, SL_IID_VOLUME};
        const SLboolean req[3] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};

        result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc,
                                                    &audioSnk, 3, ids, req);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // realize the player
        result = (*bqPlayerObject)->Realize(bqPlayerObject, SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;


        // get the play interface
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_PLAY, &bqPlayerPlay);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the buffer queue interface
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_BUFFERQUEUE,
                                                 &bqPlayerBufferQueue);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        ringBuffer = new RingBuffer(bufferSize);
        mThread = new PlaybackThread();

        audioResampler =  AudioResampler::create(16, CHANNEL_COUNT, SAMPLE_RATE, AudioResampler::HIGH_QUALITY);
        audioResampler->setSampleRate(samplerate);
        audioResampler->setVolume(UNITY_GAIN, UNITY_GAIN);

        // register callback on the buffer queue
        result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue,
                                                          PlaybackThread::playerCallback, mThread);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the effect send interface
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_EFFECTSEND,
                                                 &bqPlayerEffectSend);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the volume interface
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        /* Before we start set volume to -3dB (-300mB) and enable equalizer */
        result = (*bqPlayerVolume)->SetVolumeLevel(bqPlayerVolume, -300);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        result = (*outputMixEqualizer)->SetEnabled(outputMixEqualizer, SL_BOOLEAN_TRUE);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // set the player's state to playing
        result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;
        playState = SL_PLAYSTATE_PLAYING;

        /*Iitialize Equalizer*/

        openSLEqualizer = new OpenSLEqualizer();
        /* Draw the graphical EQ */
        drawEQDisplay();

        //ALOGD("createAudioPlayer finish");
        return true;
    }

    /*
     * Class:     com_example_openslplayer_OpenSLPlayer
     * Method:    write
     * Signature: (BII)V
     */
    jint Java_com_example_openslplayer_OpenSLPlayer_write(JNIEnv *env, jobject instance,
                                                          jbyteArray sData_, jint offset,
                                                          jint frameCount) {
        jbyte *sData = env->GetByteArrayElements(sData_, NULL);

        int written = 0;
        //ALOGD("Enter into Write Method");
        if (ringBuffer->GetWriteAvail() > 0) {

            written = ringBuffer->Write(sData, offset, frameCount);
            //ALOGD("Write data on Ring Buffer : %d", written);
        }

        if (!mThread->isPlay) {
            mThread->isPlay = true;
            pthread_t thread;
            pthread_create(&thread, NULL, thread_proc, mThread);
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
    void Java_com_example_openslplayer_OpenSLPlayer_setPlayingAudioPlayer(JNIEnv *env, jclass clazz,
                                                                          jboolean enable) {
        SLresult result;
        // set the player's state to playing
        if (playState == SL_PLAYSTATE_PLAYING && !enable) {
            result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
            assert(SL_RESULT_SUCCESS == result);
            (void) result;
            playState = SL_PLAYSTATE_PAUSED;
            //ALOGD("playState = SL_PLAYSTATE_PAUSED");
        } else if (playState == SL_PLAYSTATE_PAUSED && enable) {
            result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
            assert(SL_RESULT_SUCCESS == result);
            (void) result;
            playState = SL_PLAYSTATE_PLAYING;
            //ALOGD("playState = SL_PLAYSTATE_PLAYING");
        }
    }


/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    seekTo
 * Signature: (J)V
 */
    void Java_com_example_openslplayer_OpenSLPlayer_seekTo(JNIEnv *env, jclass type,
                                                           jlong position) {
        // TODO
        if ( ringBuffer != NULL ) {
            ringBuffer->Empty();
        }
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    setVolumeAudioPlayer
 * Signature: (I)V
 */
    void Java_com_example_openslplayer_OpenSLPlayer_setVolumeAudioPlayer(JNIEnv *, jclass, jint) {

    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    setMutAudioPlayer
 * Signature: (Z)V
 */
    void Java_com_example_openslplayer_OpenSLPlayer_setMutAudioPlayer(JNIEnv *, jclass, jboolean) {

    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    enableReverb
 * Signature: (Z)V
 */
    jboolean Java_com_example_openslplayer_OpenSLPlayer_enableReverb(JNIEnv *env, jclass clazz,
                                                                     jboolean enabled) {
        SLresult result;
        // we might not have been able to add environmental reverb to the output mix
        if (NULL == outputMixEnvironmentalReverb) {
            //ALOGD("Reverb Disable");
            return JNI_FALSE;
        }

        if (enabled) {
            result = (*bqPlayerEffectSend)->EnableEffectSend(bqPlayerEffectSend,
                                                             outputMixEnvironmentalReverb,
                                                             SL_BOOLEAN_TRUE, -300);
            //ALOGD("Reverb Enable");
        } else {
            result = (*bqPlayerEffectSend)->EnableEffectSend(bqPlayerEffectSend,
                                                             outputMixEnvironmentalReverb,
                                                             SL_BOOLEAN_FALSE, (SLmillibel) 0);
            //ALOGD("Reverb Disable");
        }
        // and even if environmental reverb was present, it might no longer be available
        if (SL_RESULT_SUCCESS != result) {
            //ALOGD("Reverb false");
            return JNI_FALSE;
        }
        return JNI_TRUE;
    }


/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    enableEqualizer
 * Signature: (Z)V
 */
    jboolean Java_com_example_openslplayer_OpenSLPlayer_enableEqualizer(JNIEnv *env, jclass clazz,
                                                                        jboolean enabled) {
        openSLEqualizer->Enable(enabled);
        SLresult result;
        //ALOGD("Equalizer Disabled");
        // we might not have been able to add environmental reverb to the output mix
        if (NULL == outputMixEqualizer) {
            //ALOGD("Equalizer Disabled");
            return JNI_FALSE;
        }
        if (enabled) {
            result = (*outputMixEqualizer)->SetEnabled(outputMixEqualizer, SL_BOOLEAN_TRUE);
            if (SL_RESULT_SUCCESS == result) {
                //ALOGD("Equalizer Enable");
                testBandLevel();
            }
        } else {
            result = (*outputMixEqualizer)->SetEnabled(outputMixEqualizer, SL_BOOLEAN_FALSE);
            if (SL_RESULT_SUCCESS == result) {
                //ALOGD("Equalizer Disable");
            }
        }
        // and even if environmental reverb was present, it might no longer be available
        if (SL_RESULT_SUCCESS != result) {
            //ALOGD("Equalizer false");
            return JNI_FALSE;
        }

        return JNI_TRUE;
    }

/*
* Draws single EQ band to the screen. Called by drawEQDisplay
*/
    void drawEQBand(int minFreq, int maxFreq, int level) {
        /* insert drawing routines here for single EQ band
        (use GetBandLevelRange and screen height to map the level to screen y-coordinate) */
    }

/* Checks for error. If any errors exit the application! */
    void CheckErr(SLresult res) {
        if (res != SL_RESULT_SUCCESS) {
            // Debug printing to be placed here
            exit(1);
        }
    }

/*
* Called when the display is repainted.
*/
    void drawEQDisplay() {
        SLuint16 numBands;
        SLmillibel bandLevel, minLevel, maxLevel;
        SLmilliHertz minFreq, maxFreq;
        int band;
        SLresult res;
        res = (*outputMixEqualizer)->GetNumberOfBands(outputMixEqualizer, &numBands);
        CheckErr(res);
        //ALOGD("Equalizer Band : %d", numBands);
        res = (*outputMixEqualizer)->GetBandLevelRange(outputMixEqualizer, &minLevel, &maxLevel);
        CheckErr(res);
        for (band = 0; band < numBands; band++) {
            res = (*outputMixEqualizer)->GetBandFreqRange(outputMixEqualizer, (SLint16) band,
                                                          &minFreq, &maxFreq);
            CheckErr(res);
            res = (*outputMixEqualizer)->GetBandLevel(outputMixEqualizer, (SLint16) band,
                                                      &bandLevel);
            CheckErr(res);
            drawEQBand(minFreq, maxFreq, bandLevel);
        }
    }


    void testBandLevel() {
        SLuint16 numBands;
        SLmillibel bandLevel, minLevel, maxLevel;
        SLresult res;
        res = (*outputMixEqualizer)->GetNumberOfBands(outputMixEqualizer, &numBands);
        CheckErr(res);
        res = (*outputMixEqualizer)->GetBandLevelRange(outputMixEqualizer, &minLevel, &maxLevel);
        CheckErr(res);

        res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, 0, 1000);
        CheckErr(res);
        res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, 1, 0);
        CheckErr(res);
        res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, 2, 0);
        CheckErr(res);
        res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, 3, 0);
        CheckErr(res);
        res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, 4, 0);
        CheckErr(res);
    }


/*
* Called by UI when user increases or decreases a band level.
*/
    void setBandLevel(SLint16 band, SLboolean increase) {
        SLuint16 numBands;
        SLmillibel bandLevel, minLevel, maxLevel;
        SLresult res;
        res = (*outputMixEqualizer)->GetNumberOfBands(outputMixEqualizer, &numBands);
        CheckErr(res);
        res = (*outputMixEqualizer)->GetBandLevelRange(outputMixEqualizer, &minLevel, &maxLevel);
        CheckErr(res);
        if (band >= numBands) {
            /* Error. Insert debug print here. */
            exit(0);
        }
        res = (*outputMixEqualizer)->GetBandLevel(outputMixEqualizer, band, &bandLevel);
        CheckErr(res);
        if (increase == SL_BOOLEAN_TRUE) {
            /* increase the level by 1 dB (100mB) if the max supported level is not exceeded */
            bandLevel = bandLevel + 100;
            if (bandLevel < maxLevel) {
                res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, band, bandLevel);
                CheckErr(res);
                drawEQDisplay();
            }
        } else /* increase==false */
        {
            /* decrease the level by 1 dB (100mB) if the min supported level is not crossed */
            bandLevel = bandLevel - 100;
            if (bandLevel > minLevel) {
                res = (*outputMixEqualizer)->SetBandLevel(outputMixEqualizer, band, bandLevel);
                CheckErr(res);
                drawEQDisplay();
            }
        }
    }


    void stopPlayer(jboolean enable){
        while (ringBuffer->GetReadAvail()) {
                    //ALOGE("Not Stopped...!");
            if(!enable)
                break;

        }
        SLresult res;
        /* Stop the music */
        res = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
        CheckErr(res);
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    shutdown
 * Signature: ()V
 */
    void Java_com_example_openslplayer_OpenSLPlayer_shutdown(JNIEnv *env, jclass clazz,
                                                             jboolean enable) {

        stopPlayer(enable);
        // destroy buffer queue audio player object, and invalidate all associated interfaces
        if (bqPlayerObject != NULL) {
            (*bqPlayerObject)->Destroy(bqPlayerObject);
            bqPlayerObject = NULL;
            bqPlayerPlay = NULL;
            bqPlayerBufferQueue = NULL;
            bqPlayerEffectSend = NULL;
            bqPlayerVolume = NULL;
        }

//        destroy buffers
        delete mThread;
        mThread = NULL;
        delete ringBuffer;
        ringBuffer = NULL;
        delete audioResampler;
        audioResampler = NULL;

        // destroy output mix object, and invalidate all associated interfaces
        if (outputMixObject != NULL) {
            (*outputMixObject)->Destroy(outputMixObject);
            outputMixObject = NULL;
            outputMixEnvironmentalReverb = NULL;
        }

        // destroy engine object, and invalidate all associated interfaces
        if (engineObject != NULL) {
            (*engineObject)->Destroy(engineObject);
            engineObject = NULL;
            engineEngine = NULL;
            outputMixEqualizer = NULL;
        }
    }

/*
 * Class:     com_example_openslplayer_OpenSLPlayer
 * Method:    readAsset
 * Signature: (L)Z
 */
    jboolean Java_com_example_openslplayer_OpenSLPlayer_readAsset(JNIEnv *env, jclass type,
                                                                  jobject manager) {

        AAssetManager *mgr = AAssetManager_fromJava(env, manager);
        const char imageFilename[] = "README.txt";
        AAsset *asset = AAssetManager_open(mgr, imageFilename,
                                           AASSET_MODE_UNKNOWN);
        if (NULL == asset) {
                    //ALOGE("ASSERT_MANAGER", "_ASSET_NOT_FOUND_");
            return JNI_FALSE;
        }
        long size = AAsset_getLength(asset);
        char *buffer = (char *) malloc(sizeof(char) * size);
        AAsset_read(asset, buffer, size);
                //ALOGD("ASSERT_MANAGER", buffer);
        //delete[] buffer;
        AAsset_close(asset);
        return JNI_TRUE;
    }

}