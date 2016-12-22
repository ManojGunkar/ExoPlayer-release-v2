//
// Created by Adarsh on 25/10/16.
//

#ifndef BOOMANDROID_OPENSLPLAYER_H
#define BOOMANDROID_OPENSLPLAYER_H

#include <stdlib.h>
#include <pthread.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>
#include <atomic>
#include "bufferprovider/include/AudioBufferProvider.h"

using namespace android;
namespace gdpl {


    class IDataSource {
    public:
        virtual void getNextBuffer(AudioBufferProvider::Buffer* buffer) = 0;
    };

    class OpenSLPlayer {
    public:

        OpenSLPlayer(IDataSource* dataSource);

        ~OpenSLPlayer();

        SLresult setup();

        SLresult tearDown();

        SLresult play();

        SLresult stop();

        SLresult pause();

        SLresult resume();

        SLuint32 getState() {
            return playState;
        }

        void startReading();

        void stopReading();

        bool isReading() const {
            return _isReading;
        }

        static SLresult setupEngine(uint32_t sampleRate, uint32_t frameCount, bool useFolat = true);

        static SLresult tearDownEngine();

        static uint32_t getSampleRate() {
            return _sampleRate;
        }

        static uint32_t getFrameCount()  {
            return _frameCount;
        }

    private:

        static void BufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
        void enqueue();

    private:
        SLObjectItf bqPlayerObject;
        SLPlayItf bqPlayerPlay;
        SLAndroidSimpleBufferQueueItf _bufferQueue;
        SLuint32 playState;
        std::atomic<bool>   _isReading;
        IDataSource*        _dataSource;

        static uint32_t    _sampleRate;
        static uint32_t    _frameCount;
    };
}


#endif //BOOMANDROID_OPENSLPLAYER_H
