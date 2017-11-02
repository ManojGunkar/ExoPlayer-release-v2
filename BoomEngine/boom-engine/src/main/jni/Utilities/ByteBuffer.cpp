//
// Created by Adarsh on 27/06/17.
//

#include "ByteBuffer.h"


namespace gdpl {
    bool ByteBuffer::initialized = false;
    jmethodID ByteBuffer::limit_id = nullptr;
    jmethodID ByteBuffer::limit_set_id = nullptr;
    jmethodID ByteBuffer::position_id = nullptr;
    jmethodID ByteBuffer::position_set_id = nullptr;
}
