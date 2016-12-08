//
// Created by Adarsh on 25/10/16.
//

#ifndef BOOMANDROID_OPENSLPLAYER_H
#define BOOMANDROID_OPENSLPLAYER_H

#include <stdlib.h>
#include <pthread.h>
#include <SLES/OpenSLES.h>
#include <SLES/OpenSLES_Android.h>


namespace gdpl {


    class IDataSource {
    public:
        struct Buffer {
            void* data;
            size_t size;
        };

        virtual void getNextBuffer(Buffer* buffer) = 0;
    };

    class FIFOBuffer;

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

        static uint32_t getEngineSampleRate();

    private:

        static void BufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context);
        void enqueue();

    private:
        SLObjectItf bqPlayerObject;
        SLPlayItf bqPlayerPlay;
        SLAndroidSimpleBufferQueueItf _bufferQueue;
        SLVolumeItf bqPlayerVolume;
        SLuint32 playState;
        bool     _isReading;
        IDataSource* _dataSource;
        pthread_mutex_t _mutex;
    };
}


#endif //BOOMANDROID_OPENSLPLAYER_H
