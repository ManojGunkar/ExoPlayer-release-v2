//
// Created by Adarsh on 25/10/16.
//

#include <assert.h>
#include <logger/log.h>
#include "OpenSLPlayer.hpp"

#define LOG_TAG "OpenSLPlayer"


namespace gdpl
{

    static const int BUFFER_COUNT = 4;
    static const int CHANNEL_COUNT = 2;

    static SLObjectItf engineObject = NULL;
    static SLObjectItf outputMixObject = NULL;
    static SLEngineItf engineEngine = NULL;
    static bool        sUseFloatAudio = true;



    uint32_t OpenSLPlayer::_sampleRate;
    uint32_t OpenSLPlayer::_frameCount;

    OpenSLPlayer::OpenSLPlayer(IDataSource* dataSource)
    :_dataSource(dataSource),
     _isReading(false)
    {
    }

    OpenSLPlayer::~OpenSLPlayer()
    {
    }

    SLresult OpenSLPlayer::setup()
    {
        // configure audio source
        SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE,
                                                           BUFFER_COUNT};

        SLAndroidDataFormat_PCM_EX format_pcm = {SL_ANDROID_DATAFORMAT_PCM_EX, CHANNEL_COUNT, _sampleRate * 1000,
                                                 SL_PCMSAMPLEFORMAT_FIXED_32, SL_PCMSAMPLEFORMAT_FIXED_32,
                                                 SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                                                 SL_BYTEORDER_LITTLEENDIAN,
                                                 SL_ANDROID_PCM_REPRESENTATION_FLOAT};
        if ( !sUseFloatAudio ) {
            format_pcm = {SL_ANDROID_DATAFORMAT_PCM_EX, CHANNEL_COUNT, _sampleRate * 1000,
                          SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16,
                          SL_SPEAKER_FRONT_LEFT | SL_SPEAKER_FRONT_RIGHT,
                          SL_BYTEORDER_LITTLEENDIAN,
                          SL_ANDROID_PCM_REPRESENTATION_SIGNED_INT};
        }

        SLDataSource audioSrc = {&loc_bufq, &format_pcm};

        // configure audio sink
        SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
        SLDataSink audioSnk = {&loc_outmix, NULL};

        // create audio player
        // NOTE: SL_IID_BASSBOOST is requested only to disable Fast Audio Path. It is not used anywhere
        const SLInterfaceID ids[] = { SL_IID_ANDROIDSIMPLEBUFFERQUEUE };
        const SLboolean req[] = { SL_BOOLEAN_TRUE };

        SLresult result = (*engineEngine)->CreateAudioPlayer(engineEngine, &bqPlayerObject, &audioSrc,
                                                             &audioSnk, 1, ids, req);
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
        result = (*bqPlayerObject)->GetInterface(bqPlayerObject, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                                 &_bufferQueue);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        result = (*_bufferQueue)->RegisterCallback(_bufferQueue, BufferQueueCallback, this);
        assert(SL_RESULT_SUCCESS == result);
        (void) result;

        return result;
    }


    SLresult OpenSLPlayer::tearDown()
    {
        if (bqPlayerObject != NULL) {
            (*bqPlayerObject)->Destroy(bqPlayerObject);
            bqPlayerObject = NULL;
            bqPlayerPlay = NULL;
            _bufferQueue = NULL;
        //    bqPlayerVolume = NULL;
        }

        return SL_RESULT_SUCCESS;
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
        usleep(10000);
        return result;
    }


    SLresult OpenSLPlayer::pause()
    {
        SLresult result = (*bqPlayerPlay)->SetPlayState(bqPlayerPlay, SL_PLAYSTATE_PAUSED);
        if ( SL_RESULT_SUCCESS == result ) {
            (*_bufferQueue)->Clear(_bufferQueue);
            playState = SL_PLAYSTATE_PAUSED;
        }

        return result;
    }


    SLresult OpenSLPlayer::resume()
    {
        return this->play();
    }

    void OpenSLPlayer::startReading()
    {
        _isReading = true;
        (*_bufferQueue)->Clear(_bufferQueue);
        for ( int i = 0; i < BUFFER_COUNT; i++ ) {
            enqueue();
        }
    }

    void OpenSLPlayer::stopReading()
    {
        _isReading = false;
    }


    SLresult OpenSLPlayer::setupEngine(uint32_t sampleRate, uint32_t frameCount, bool useFloat) {

        _sampleRate = sampleRate;
        _frameCount = frameCount;
        sUseFloatAudio = useFloat;

        SLresult result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;

        // realize the engine
        result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;


        // get the engine interface, which is needed in order to create other objects
        result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
        assert(SL_RESULT_SUCCESS == result);
        (void)result;


        const SLInterfaceID ids[] = {};
        const SLboolean req[] = {};
        result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 0, ids, req);
        assert(SL_RESULT_SUCCESS == result);
        if (SL_RESULT_SUCCESS == result) {
            result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
        }

        return result;
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

        return SL_RESULT_SUCCESS;
    }


    void OpenSLPlayer::BufferQueueCallback(SLAndroidSimpleBufferQueueItf bq __unused, void *context)
    {
        OpenSLPlayer* self = (OpenSLPlayer*)context;
        self->enqueue();

    }

    void OpenSLPlayer::enqueue()
    {
        const int bytesPerFrame = CHANNEL_COUNT * (sUseFloatAudio? sizeof(float) : sizeof(short));
        AudioBufferProvider::Buffer buffer;
        buffer.frameCount = _frameCount;
        _dataSource->getNextBuffer(&buffer);

        bool success = true;
        if ( buffer.frameCount > 0 && buffer.raw != nullptr ) {
            SLresult result = (*_bufferQueue)->Enqueue(_bufferQueue, buffer.raw, buffer.frameCount * bytesPerFrame);
            if ( SL_RESULT_SUCCESS != result ) {
                LOGE("OpenSLPlayer: Failed to enqueue buffer (%d)", result);
                success = false;
            }
        }
        else {
            LOGE("OpenSLPlayer: No data to enqueue!");
            success = false;
        }

        // if the enqueue is failed and there are no buffers to play restart enqueuing
        if ( !success ) {
            SLAndroidSimpleBufferQueueState state;
            (*_bufferQueue)->GetState(_bufferQueue, &state);
            LOGE("OpenSLPlayer: buffer state [count=%d, index=%d]", state.count, state.index);
            if ( state.count == 0 ) {
                LOGE("OpenSLPlayer: Run out of buffers -- restarting!");
                stopReading(); // signals the writer that callback has stopped reading
            }
        }
    }
}
