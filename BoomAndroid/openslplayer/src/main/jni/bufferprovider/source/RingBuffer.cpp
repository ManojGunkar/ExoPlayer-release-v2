#include <android/log.h>
#include <Utilities/AutoLock.hpp>
#include "../include/RingBuffer.h"
#include "../../logger/log.h"
#include "Utilities/AutoLock.hpp"

namespace gdpl {


    RingBuffer::RingBuffer(int sizeBytes, uint32_t channels, uint32_t bytesPerChannel)
            : _size(sizeBytes), kChannelCount(channels), kBytesPerChannel(bytesPerChannel)
    {
        _data = (uint8_t*)calloc(_size, sizeof(uint8_t));
        _readPtr = 0;
        _writePtr = 0;
        _writeBytesAvail = sizeBytes;

        _tempBuffer = (uint8_t*)calloc(_size, sizeof(uint8_t));

        pthread_mutexattr_t attr;
        pthread_mutexattr_init(&attr);
        pthread_mutexattr_settype(&attr, PTHREAD_MUTEX_RECURSIVE);
        pthread_mutex_init(&_mutex, &attr);
        pthread_cond_init(&_writeCond, NULL);
    }

    RingBuffer::~RingBuffer() {
        free(_data);
        free(_tempBuffer);
        pthread_cond_destroy(&_writeCond);
    }

// Set all data to 0 and flag buffer as empty.
    bool RingBuffer::Empty(void) {
        AutoLock lock(&_mutex);
        memset(_data, 0, _size);
        _readPtr = 0;
        _writePtr = 0;
        _writeBytesAvail = _size;
        pthread_cond_signal(&_writeCond);
        return true;
    }


    void RingBuffer::UnblockWrite()
    {
        pthread_mutex_lock(&_mutex);
        pthread_cond_signal(&_writeCond);
        pthread_mutex_unlock(&_mutex);
    }

    int RingBuffer::Read(uint8_t *dataPtr, int numBytes) {
        AutoLock lock(&_mutex);
        // If there's nothing to read or no data available, then we can't read anything.
        if (dataPtr == 0 || numBytes <= 0 || _writeBytesAvail == _size) {
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

        pthread_cond_signal(&_writeCond);
        return numBytes;
    }

// Write to the ring buffer.  Do not overwrite data that has not yet
// been read.
    int RingBuffer::Write(uint8_t *dataPtr, int offset, int numBytes) {
        gdpl::AutoLock lock(&_mutex);

        if (dataPtr == 0 || (numBytes - offset) <= 0 || _writeBytesAvail < numBytes) {
            pthread_cond_wait(&_writeCond, &_mutex);
        }

        // If there's nothing to write or no room available, we can't write anything.
        if (dataPtr == 0 || (numBytes - offset) <= 0 || _writeBytesAvail == 0) {
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
        return numBytes;
    }

    int RingBuffer::GetSize(void) {
        return _size;
    }

    int RingBuffer::GetWriteAvail(void) {
        AutoLock lock(&_mutex);
        return _writeBytesAvail;
    }

    int RingBuffer::GetReadAvail(void) {
        AutoLock lock(&_mutex);
        return _size - _writeBytesAvail;
    }


    status_t RingBuffer::getNextBuffer(Buffer* buffer) {

        const uint32_t kBytesPerFrame =  kBytesPerChannel * kChannelCount;

        //ALOGD("getNextBuffer", "Requested Frames = %d", buffer->frameCount);
        AutoLock lock(&_mutex);

        int requestedBytes = buffer->frameCount * kBytesPerFrame;
        if ( this->GetReadAvail() <  requestedBytes ) {
            LOGD("RingBuffer::getNextBuffer not enough data");
            requestedBytes = this->GetReadAvail();
            buffer->frameCount = this->GetReadAvail() / kBytesPerFrame;
        }

        this->Read(_tempBuffer, requestedBytes);
        buffer->raw = (buffer->frameCount != 0)? _tempBuffer : NULL;

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