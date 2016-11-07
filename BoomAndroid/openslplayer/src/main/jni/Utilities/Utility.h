//
// Created by Adarsh on 07/11/16.
//

#ifndef BOOMANDROID_UTILITY_H
#define BOOMANDROID_UTILITY_H

#include <stdlib.h>

void ditherAndClamp(int32_t *out, int32_t const *sums, size_t c);

void memcpy_to_i16_from_float(int16_t *dst, const float *src, size_t count);

void memcpy_to_float_from_i16(float *dst, const int16_t *src, size_t count);


#endif //BOOMANDROID_UTILITY_H
