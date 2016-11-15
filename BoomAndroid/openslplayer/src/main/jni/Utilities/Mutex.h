//
// Created by Adarsh on 14/11/16.
//

#ifndef BOOMANDROID_MUTEX_H
#define BOOMANDROID_MUTEX_H

#include <pthread.h>

namespace gdpl {

    class Mutex {
    public:

        Mutex(bool recursive = true);

        ~Mutex();

        void Lock();

        bool TryLock();

        void Unlock();

        pthread_mutex_t* GetNative() { return &_nativeMutex; }

    private:

        pthread_mutex_t     _nativeMutex;

    };

};



#endif //BOOMANDROID_RECURSIVEMUTEX_H
