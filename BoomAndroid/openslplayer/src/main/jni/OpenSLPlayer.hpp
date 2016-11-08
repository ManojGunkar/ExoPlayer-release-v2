//
// Created by Adarsh on 25/10/16.
//

#ifndef BOOMANDROID_OPENSLPLAYER_H
#define BOOMANDROID_OPENSLPLAYER_H

#include <stdlib.h>
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

    class OpenSLPlayer {
    public:

        OpenSLPlayer(IDataSource* dataSource);

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

        static SLresult setupEngine(uint32_t sampleRate);

        static SLresult tearDownEngine();

        static uint32_t getEngineSampleRate();

    private:

        static void BufferQueueCallback(SLAndroidSimpleBufferQueueItf bq, void *context);

    private:
        SLObjectItf bqPlayerObject;
        SLPlayItf bqPlayerPlay;
        SLAndroidSimpleBufferQueueItf _bufferQueue;
        SLVolumeItf bqPlayerVolume;
        SLuint32 playState;

        IDataSource* _dataSource;
    };
}


#endif //BOOMANDROID_OPENSLPLAYER_H
