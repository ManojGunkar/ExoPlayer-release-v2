//
// Created by Adarsh on 25/10/16.
//

#include <assert.h>
#include "OpenSLPlayer.hpp"


namespace gdpl
{

    static SLObjectItf engineObject = NULL;
    static SLObjectItf outputMixObject = NULL;
    static SLEngineItf engineEngine = NULL;




    OpenSLPlayer::OpenSLPlayer(IDataSource* dataSource)
    :_dataSource(dataSource)
    {

    }


    SLresult OpenSLPlayer::setup()
    {
        // configure audio source
        SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                           4};

#if 1
        SLAndroidDataFormat_PCM_EX format_pcm = {SL_ANDROID_DATAFORMAT_PCM_EX, 2, SL_SAMPLINGRATE_44_1,
                                                 SL_PCMSAMPLEFORMAT_FIXED_32, SL_PCMSAMPLEFORMAT_FIXED_32,
                                                 SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                                 SL_BYTEORDER_LITTLEENDIAN,
                                                 SL_ANDROID_PCM_REPRESENTATION_FLOAT};
#else
        SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 2, SL_SAMPLINGRATE_44_1,
                                                 SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                                                 SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                                 SL_BYTEORDER_LITTLEENDIAN,
                                                 };
#endif
        SLDataSource audioSrc = {&loc_bufq, &format_pcm};

        // configure audio sink
        SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
        SLDataSink audioSnk = {&loc_outmix, NULL};

        // create audio player
        const SLInterfaceID ids[3] = { SL_IID_BUFFERQUEUE, SL_IID_VOLUME };
        const SLboolean req[3] = { SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE };

        SLresult result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc,
                                                             &audioSnk, 2, ids, req);
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

        result = (*bqPlayerBufferQueue)->RegisterCallback(bqPlayerBufferQueue,
                                                          BufferQueueCallback, _dataSource);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        // get the volume interface
//        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_VOLUME, &bqPlayerVolume);
//        assert(SL_RESULT_SUCCESS == result);
//        (void) result;
//        /* Before we start set volume to -3dB (-300mB) and enable equalizer */
//        result = (*bqPlayerVolume)->SetVolumeLevel(bqPlayerVolume, -300);
//        assert(SL_RESULT_SUCCESS == result);
//        (void) result;

        return result;
    }


    SLresult OpenSLPlayer::tearDown()
    {
        if (bqPlayerObject != NULL) {
            (*bqPlayerObject)->Destroy(bqPlayerObject);
            bqPlayerObject = NULL;
            bqPlayerPlay = NULL;
            bqPlayerBufferQueue = NULL;
        //    bqPlayerVolume = NULL;
        }
    }


    SLresult OpenSLPlayer::play()
    {
        // set the player's state to playing
        SLresult result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PLAYING);
        if ( SL_RESULT_SUCCESS == result ) {
            playState = SL_PLAYSTATE_PLAYING;
        }
        return result;
    }


    SLresult OpenSLPlayer::stop()
    {
        SLresult result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_STOPPED);
        if ( SL_RESULT_SUCCESS == result ) {
            playState = SL_PLAYSTATE_STOPPED;
        }

        return result;
    }


    SLresult OpenSLPlayer::pause()
    {
        SLresult result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
        if ( SL_RESULT_SUCCESS == result ) {
            playState = SL_PLAYSTATE_PAUSED;
        }
    }


    SLresult OpenSLPlayer::resume()
    {
        return this->play();
    }

    void OpenSLPlayer::startReading()
    {
        BufferQueueCallback(bqPlayerBufferQueue, _dataSource);
    }

    SLresult OpenSLPlayer::setupEngine() {
        SLresult result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
        assert(SL_RESULT_SUCCESS == result);

        // realize the engine
        result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);

        // get the engine interface, which is needed in order to create other objects
        result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
        assert(SL_RESULT_SUCCESS == result);

        const SLInterfaceID ids[] = {};
        const SLboolean req[] = {};
        result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, ids, req);
        assert(SL_RESULT_SUCCESS == result);
        if (SL_RESULT_SUCCESS == result) {
            result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
        }
    }


    SLresult OpenSLPlayer::tearDownEngine()
    {
        // destroy output mix object, and invalidate all associated interfaces
        if (outputMixObject != NULL) {
            (*outputMixObject)->Destroy(outputMixObject);
            outputMixObject = NULL;
        }

        // destroy engine object, and invalidate all associated interfaces
        if (engineObject != NULL) {
            (*engineObject)->Destroy(engineObject);
            engineObject = NULL;
            engineEngine = NULL;
        }
    }


    void OpenSLPlayer::BufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context)
    {
        IDataSource* dataSource = (IDataSource*)context;
        IDataSource::Buffer buffer;
        dataSource->getNextBuffer(&buffer);
        SLresult result = (*bq)->Enqueue(bq, buffer.data, buffer.size);
        if ( SL_RESULT_SUCCESS != result ) {

        }
    }
}
