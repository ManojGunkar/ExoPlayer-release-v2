//
// Created by Ram Acharya on 07/10/16.
// Copyright © 2016 GlobalDelight. All rights reserved.
//

#ifndef BOOM_AUDIOENGINE_H
#define BOOM_AUDIOENGINE_H

#include "SoundLayout.hpp"

#define NUM_BANDS 16
#define NUM_SPEAKERS 6
#define SAMPLE_TYPE_FLOAT 1
#define SAMPLE_TYPE_SHORT 2

class SoundLayout;

typedef enum { eFrontLeft=0, eFrontRight, eRearLeft, eRearRight, eWoofer, eTweeter} SpeakerID;

class AudioEngine {
public:
    AudioEngine(int SampleSize);
    ~AudioEngine();

    int GetSampleSize();

    void ResetEngine();

    void SetOutputType(int type);
    int GetOutputType();

    void Set3DAudioState(bool state);
    bool Get3DAudioState();

    void SetEffectsState(bool state);
    bool GetEffectsState();

    void SetSuperBass(bool state);
// bool getSuperBass() require

    void SetHighQuality(bool state);
    // bool getHighQuality() require

    void SetIntensity(float value); // value from -1.0 to +1.0
    float GetIntensity();

    // should be a 16-band equaliser with pre-defined mid-frequencies
/*//  1 -  36.0
//  2 -  50.0
//  3 -  75.0
//  4 -  100.0
//  5 -  150.0
//  6 -  250.0
//  7 -  350.0
//  8 -  500.0
//  9 -  750.0
//  10 - 1.1k
//  11 - 1.6k
//  12 - 2.5k
//  13 - 3.6k
//  14 - 5k
//  15 - 8k
//  16 - 12k*/

    void SetEqualizer(int id, float *bandGains);  // id is the identifier of the eq set should be a postive number and, 0 is the auto mode
    int GetEqualizerId();  // returns the equalizer id

    void SetSpeakerState(SpeakerID id, float value);
    float GetSpeakerState(SpeakerID id);

    void ProcessAudio(short *buffer, void *outBuffer, int size);

private:
    int mSampleSize;
    int mOutputSampleType;
    int mEQId;
    bool m3DAudioState;
    bool mEffectsState;
    int mEqualizerId;
    float mIntensity;
    float mSpeakerState[NUM_SPEAKERS];
    SoundLayout *mLayout;
};



#endif //BOOM_AUDIOENGINE_H
