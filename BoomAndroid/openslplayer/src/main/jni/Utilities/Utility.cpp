//
// Created by Adarsh on 07/11/16.
//

#include "Utility.h"

static inline int16_t clamp16(int32_t sample) {
    if ((sample >> 15) ^ (sample >> 31))
        sample = 0x7FFF ^ (sample >> 31);
    return sample;
}

void ditherAndClamp(int32_t *out, int32_t const *sums, size_t c) {
    for (size_t i = 0; i < c; i++) {
        int32_t l = *sums++;
        int32_t r = *sums++;
        int32_t nl = l >> 12;
        int32_t nr = r >> 12;
        l = clamp16(nl);
        r = clamp16(nr);
        *out++ = (r << 16) | (l & 0xFFFF);
    }
}


void memcpy_to_i16_from_float(int16_t *dst, const float *src, size_t count)
{
    while (count--) {
        float f = *src++;
        int16_t i;
        if (f > 1.0) {
            i = 32767;
        } else if (f < -1.0) {
            i = -32768;
        } else {
            // does not specifically handle NaN
            i = f * 32767.0;
        }
        *dst++ = i;
    }
}


void memcpy_to_float_from_i16(float *dst, const int16_t *src, size_t count)
{
    while (count--) {
        *dst++ = *src++ / (float)32767;
    }
}

