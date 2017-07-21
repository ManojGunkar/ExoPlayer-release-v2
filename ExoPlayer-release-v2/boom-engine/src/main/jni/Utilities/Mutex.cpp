//
// Created by Adarsh on 14/11/16.
//

#include "Mutex.h"

namespace gdpl {

    Mutex::Mutex(bool recursive)
    {
        pthread_mutexattr_t attr;
        pthread_mutexattr_init(&attr);
        pthread_mutexattr_settype(&attr, recursive? PTHREAD_MUTEX_RECURSIVE : PTHREAD_MUTEX_DEFAULT);

        pthread_mutex_init(&_nativeMutex, &attr);

    }

    Mutex::~Mutex()
    {
        pthread_mutex_destroy(&_nativeMutex);
    }

    void Mutex::Lock()
    {
        pthread_mutex_lock(&_nativeMutex);
    }


    bool Mutex::TryLock()
    {
        return (pthread_mutex_trylock(&_nativeMutex) == 0);
    }


    void Mutex::Unlock()
    {
        pthread_mutex_unlock(&_nativeMutex);
    }

}
