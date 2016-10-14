#include <pthread.h>
#include "jni.h"
#include "AudioBufferProvider.h"

#ifndef RINGBUFFER_H_
#define RINGBUFFER_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace android {
    class RingBuffer : public AudioBufferProvider {
    public:
        RingBuffer(int sizeBytes);

        ~RingBuffer();

        int Read(jbyte *dataPtr, int numBytes);

        int Write(jbyte *dataPtr, jint numBytes, jint i);

        bool Empty(void);

        int GetSize();

        int GetWriteAvail();

        int GetReadAvail();

        virtual status_t getNextBuffer(Buffer* buffer);

        virtual void releaseBuffer(Buffer* buffer);

    private:
        pthread_mutex_t mutex;
        //pthread_cond_t _cond;
        jbyte *_data;
        int _size;
        int _readPtr;
        int _writePtr;
        int _writeBytesAvail;


        uint16_t* _tempBuffer;
    };
};
#ifdef __cplusplus
}
#endif
#endif