//
// Created by Ram Acharya on 07/10/16.
// Copyright © 2016 GlobalDelight. All rights reserved.
//

#ifndef BOOM_AUDIOENGINE_HEADERS_H
#define BOOM_AUDIOENGINE_HEADERS_H


#define NUM_BANDS 16
#define NUM_SPEAKERS 6
#define SAMPLE_TYPE_FLOAT 1
#define SAMPLE_TYPE_SHORT 2

#define eOverEar 0
#define eOnEar 1
#define eInEar 2
#define eInCanal 3

typedef enum {
    eQualityLow = 1,
    eQualityMid,
    eQualityHigh
} eQualityLevel;


#endif //BOOM_AUDIOENGINE_HEADERS_H
