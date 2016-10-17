#include <android/log.h>
#include "../include/RingBuffer.h"
#include "../../logger/log.h"

namespace android {

    static const int32_t kTempBufferSize = 1024*1024;

    RingBuffer::RingBuffer(int sizeBytes) {
        _data = new jbyte[sizeBytes];
        memset(_data, 0, sizeBytes);
        _size = sizeBytes;
        _readPtr = 0;
        _writePtr = 0;
        _writeBytesAvail = sizeBytes;

        _tempBuffer = new uint16_t[kTempBufferSize];


        pthread_mutex_init(&mutex, NULL);
        //pthread_cond_init(&_cond, NULL);
    }

    RingBuffer::~RingBuffer() {
        delete[] _data;
        delete[] _tempBuffer;
    }

// Set all data to 0 and flag buffer as empty.
    bool RingBuffer::Empty(void) {
        memset(_data, 0, _size);
        _readPtr = 0;
        _writePtr = 0;
        _writeBytesAvail = _size;
        return true;
    }

    int RingBuffer::Read(jbyte *dataPtr, int numBytes) {
        pthread_mutex_lock(&mutex);
        // If there's nothing to read or no data available, then we can't read anything.
        if (dataPtr == 0 || numBytes <= 0 || _writeBytesAvail == _size) {
            pthread_mutex_unlock(&mutex);
            return 0;
        }

        int readBytesAvail = _size - _writeBytesAvail;

        // Cap our read at the number of bytes available to be read.
        if (numBytes > readBytesAvail) {
            numBytes = readBytesAvail;
        }

        // Simultaneously keep track of how many bytes we've read and our position in the outgoing buffer
        if (numBytes > _size - _readPtr) {
            int len = _size - _readPtr;
            memcpy(dataPtr, _data + _readPtr, len);
            memcpy(dataPtr + len, _data, numBytes - len);
        }
        else {
            memcpy(dataPtr, _data + _readPtr, numBytes);
        }

        _readPtr = (_readPtr + numBytes) % _size;
        _writeBytesAvail += numBytes;

        pthread_mutex_unlock(&mutex);
        return numBytes;
    }

// Write to the ring buffer.  Do not overwrite data that has not yet
// been read.
    int RingBuffer::Write(jbyte *dataPtr, jint offset, jint numBytes) {
        pthread_mutex_lock(&mutex);


        // If there's nothing to write or no room available, we can't write anything.
        if (dataPtr == 0 || (numBytes - offset) <= 0 || _writeBytesAvail == 0) {
            pthread_mutex_unlock(&mutex);
            return 0;
        }

        // Cap our write at the number of bytes available to be written.
        if ((numBytes - offset) > _writeBytesAvail) {
            numBytes = _writeBytesAvail;
        } else {
            numBytes = numBytes - offset;
        }

        // Simultaneously keep track of how many bytes we've written and our position in the incoming buffer
        if (numBytes > _size - _writePtr) {
            int len = _size - _writePtr;
            memcpy(_data + _writePtr, dataPtr + offset, len);
            memcpy(_data, dataPtr + offset + len, numBytes - len);
        } else {
            memcpy(_data + _writePtr, dataPtr + offset, numBytes);
        }

        _writePtr = (_writePtr + numBytes) % _size;
        _writeBytesAvail -= numBytes;

        //pthread_cond_signal(&_cond);
        pthread_mutex_unlock(&mutex);
        return numBytes;
    }

    int RingBuffer::GetSize(void) {
        return _size;
    }

    int RingBuffer::GetWriteAvail(void) {
        return _writeBytesAvail;
    }

    int RingBuffer::GetReadAvail(void) {
        return _size - _writeBytesAvail;
    }


    status_t RingBuffer::getNextBuffer(Buffer* buffer) {

        //ALOGD("getNextBuffer", "Requested Frames = %d", buffer->frameCount);

        int bytesPerFrame =  sizeof(int16_t) * 2; // assume 2 channels - stereo
        int requestedBytes = buffer->frameCount * bytesPerFrame;

//  TODO: In some extreme cases we may have to wait for data
//        pthread_mutex_lock(&mutex);
//        if ( this->GetReadAvail() < requestedBytes  ) {
//            pthread_cond_wait(&_cond, &mutex);
//        }
//        pthread_mutex_unlock(&mutex);

        if ( this->GetReadAvail() <  requestedBytes ) {
            requestedBytes = this->GetReadAvail();
            buffer->frameCount = this->GetReadAvail() / bytesPerFrame;
        }

        this->Read((jbyte*)_tempBuffer, requestedBytes);
        buffer->raw = _tempBuffer;

        //ALOGD("getNextBuffer", "Available Frames = %d", buffer->frameCount);

        return  NO_ERROR;
    }

    void RingBuffer::releaseBuffer(Buffer* buffer)
    {
        buffer->frameCount = 0;
        if(buffer->raw != NULL)
            buffer->raw = NULL;
    };

}