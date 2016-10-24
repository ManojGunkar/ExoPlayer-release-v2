//
// Created by Adarsh on 24/10/16.
//

#ifndef BOOMANDROID_AUTOLOCK_H
#define BOOMANDROID_AUTOLOCK_H

#include <pthread.h>

namespace gdpl {
    class AutoLock
    {
    public:
        AutoLock(pthread_mutex_t* lock): _mutex(lock)
        {
            Lock();
        }

        ~AutoLock()
        {
            Unlock();
        }

    private:

        void Lock()
        {
            pthread_mutex_lock(_mutex);
        }

        void Unlock()
        {
            pthread_mutex_unlock(_mutex);
        }

        pthread_mutex_t* _mutex;

    };
}

#endif //BOOMANDROID_AUTOLOCK_H
