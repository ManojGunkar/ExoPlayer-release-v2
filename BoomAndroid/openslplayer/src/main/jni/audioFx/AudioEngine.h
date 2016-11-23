//
// Created by Ram Acharya on 07/10/16.
// Copyright © 2016 GlobalDelight. All rights reserved.
//

#ifndef BOOM_AUDIOENGINE_H
#define BOOM_AUDIOENGINE_H

//#include "SoundLayout.hpp"




#define NUM_BANDS 16
#define NUM_SPEAKERS 6
#define SAMPLE_TYPE_FLOAT 1
#define SAMPLE_TYPE_SHORT 2



static float Music[] = {4.2, 4.6, 4.5, 2.6, 2.0, 2.31, 2.0, 1.9, 2.21, 2.0, 2.0, 2.4, 2.021, 2.0, 3.93, 4.6};
static float BassBoost[] = {4.5, 5.5, 4.5, 3.0, 2.2, 1.5, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
static float Accoustic[] = {4.5, 4.5, 4.5, 3.7, 3.0, 2.0, 1.0, 1.0, 1.25, 2.0, 2.5, 1.75, 1.75, 3.25, 4.0, 3.7};
static float Sixties[] = {2.94, 3.1, 3.0, 4.0, 4.5, 4.0, 1.5, -1.73, 2.0, 2.0, 0.0, -2.2, 0.0, 0.5, 0.25, 0.0};
static float Classic[] = {4.0, 4.1, 3.7, 3.7, 3.3, 2.6, 1.0, 0.0, -0.75, -1.25, -0.75, 0.0, 2.0, 3.0, 3.5, 4.0};
static float DubSteb[] = {5.0, 5.0, 4.5, 4.5, 3.0, 2.4, 2.0, 1.25, 0.0, -2.0, -0.5, 1.0, 2.5, 3.0, 4.33, 4.0};
static float Electronic[] = {3.7, 4.2, 3.2, 2.0, 1.5, 0.5, 0.0, -1.25, -1.0, 3.0, 1.0, 2.7, 3.5, 3.7, 4.1, 4.1};
static float Flat[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
static float Hihop[] = {4.2, 4.3, 4.1, 1.0, -0.5, 4.0, 3.0, -1.2, -1.5, -1.5, 0.0, 3.0, -1.75, -0.5, 3.0, 3.7};
static float House[] = {-1.0, 1.0, 2.0, 4.0, 4.5, 4.75, 3.0, -2.0, -1.75, 0.0, -1.0, -2.0, 2.21, 3.3, 0.5, 0.0};
static float Jazz[] = {4.2, 4.0, 3.3, -0.5, 2.8, 3.7, 2.0, -1.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 3.5, 3.7};
static float Loud[] = {4.0, 4.5, 4.0, 2.5, 3.3, 4.0, 2.0, -0.5, -1.0, 1.0, 4.0, 1.0, -1.0, 3.2, 3.7, 1.5};
static float Party[] = {4.2, 4.6, 4.0, 3.3, 2.2, 1.75, 1.0, -1.25, 0.0, 1.25, 1.0, 1.5, 2.5, 3.5, 4.33, 4.0};
static float Regge[] = {1.0, 3.5, 4.6, 4.2, 4.2, 2.75, 1.0, -0.75, -1.2, -1.0, 0.0, 2.0, 3.0, 4.0, 2.5, 1.0};
static float Soft[] = {0.25, 0.25, 0.5, 0.5, 1.22, 1.22, 1.0, -1.0, -1.0, 0.0, -1.0, -1.2, -1.5, -1.5, -1.9, -2.21};
static float TrebleBoost[] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.2, 1.5, 3.0, 5.0, 4.5, 4.0};
static float Vocals[] = {-1.5, -1.5, -1.22, -0.5, 0.5, 1.0, 2.0, 3.0, 3.7, 4.0, 2.21, 1.0, 0.0, -1.0, -2.0, -3.0};
static float RnB[] = {3.0, 4.0, 4.2, 2.9, 2.71, 1.5, 0.0, -0.5, -1.0, 0.5, 1.0, 2.8, 3.2, 4.0, 3.5, 4.0};
static float Metal[] = {4.2, 4.65, 4.51, 3.3, 0.5, 3.3, 2.0, 0.0, 0.5, 0.5, 2.5, 3.2, 0.0, 2.8, 4.0, 4.33};
static float Pop[] = {-1.0, -0.5, -0.5, 0.0, 1.72, 2.7, 3.52, 4.1, 3.7, 3.7, 2.2, 1.0, 0.0, -1.0, -1.5, -2.0};
static float Rock[] = {4.0, 4.5, 4.2, 3.7, 3.2, 2.3, 1.0, 0.0, -1.0, -1.0, -1.0, 1.0, 2.4, 3.7, 4.0, 4.2};



//static float Accoustic[NUM_BANDS] = {4.5, 5.5, 4.5, 4.0, 3.0, 0.0, 0.0, 1.0, 1.0, 2.0, 2.5, 1.75, 1.5, 3.25, 3.25, 4.5};
//static float BassBoost[NUM_BANDS] = {4.5, 5.5, 4.5, 2.0, 1.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
//static float Sixties[NUM_BANDS] = {3.0, 3.1, 3.0, 4.0, 4.5, 4.0, 1.5, -1.5, 2.0, 2.0, 0.0, -2.0, 0.0, 0.0, 1.0, 0.0};
//static float Classic[NUM_BANDS] = {4.0, 4.1, 3.7, 3.7, 3.3, 2.6, 1.0, 0.0, -0.5, -1.0, -0.5, 0.0, 2.0, 3.0, 3.5, 4.0};
//static float DubSteb[NUM_BANDS] = {5.0, 5.0, 4.5, 4.5, 3.0, 2.4, 2.0, 1.0, 0.0, -2.0, -0.5, 1.0, 2.5, 3.0, 4.5, 4.0};
//static float Electronic[NUM_BANDS] = {3.6, 4.1, 3.2, 2.0, 1.0, 0.0, 0.0, -1.0, -1.0, 3.0, 1.0, 3.0, 3.5, 4.0, 4.5, 5.0};
//static float Hihop[NUM_BANDS] = {4.0, 4.5, 4.1, 1.0, -1.0, 4.0, 3.0, -1.0, -1.5, -1.5, 0.0, 3.0, -1.0, -1.0, 3.0, 4.0};
//static float House[NUM_BANDS] = {-1.0, 1.0, 2.0, 4.0, 4.0, 4.0, 3.0, -2.0, -2.0, 0.0, -1.0, -2.0, 1.0, 3.0, 1.0, 0.0};
//static float Jazz[NUM_BANDS] = {4.0, 4.0, 3.0, -1.0, 2.0, 3.0, 2.0, -1.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0, 4.0, 4.0};
//static float Loud[NUM_BANDS] = {4.0, 4.5, 4.0, 2.0, 3.0, 4.0, 2.0, 0.0, -1.0, 1.0, 4.0, 1.0, -1.0, 4.0, 4.0, 3.0};
//static float Metal[NUM_BANDS] = {5.0, 5.0, 4.5, 2.0, 0.0, 3.0, 2.0, 0.0, 0.0, 0.0, 2.0, 3.0, 0.0, 3.0, 4.0, 5.0};
//
////static float Music[NUM_BANDS] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 1.0, 2.0, 3.0, 1.0, 4.0, 5.0, 4.0};
//static float Music[NUM_BANDS] = {4.0, 5.0, 4.5, 2.0, 1.0, 1.0, 1.0, 0.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 3.0, 3.0};
//static float Pop[NUM_BANDS] = {1.0, 1.5, 2.0, 2.0, 2.5, 2.5, 3.0, 4.0, 4.0, 3.0, 2.0, 1.0, 0.0, 0.0, 0.0, 0.0};//done
//
//static float Party[NUM_BANDS] = {4.5, 5.0, 4.0, 2.0, 1.0, 0.5, 0.0, -1.0, 2.5, 1.0, 1.0, 1.0, 1.5, 3.5, 5.0, 3.0};
//static float Regge[NUM_BANDS] = {1.0, 3.5, 5.0, 4.5, 4.5, 2.5, 1.0, 0.0, -1.0, -1.0, 0.0, 2.0, 3.0, 4.0, 2.5, 1.0};
//static float Rock[NUM_BANDS] = {5.0, 4.5, 4.0, 3.0, 2.0, 1.0, 0.0, -1.0, -1.0, -1.0, 0.0, 0.0, 3.0, 4.0, 4.5, 4.5};
//static float Soft[NUM_BANDS] = {-1.5, -1.0, -0.5, 0.0, 0.0, 0.0, 0.0, -1.0, -1.0, 0.0, -1.0, 0.0, -1.0, -1.5, -1.5, -1.5};// done
//static float TrebleBoost[NUM_BANDS] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,1.0, 3.0, 5.0, 4.0, 2.0};
//static float Vocals[NUM_BANDS] = {-1.5, -1.5, -1.0, -0.5, 0.5, 0.5, 1.0, 2.0, 3.0, 2.0, 1.5, 1.0, 0.0, -1.0, -2.0, -3.0};// done
//static float RnB[NUM_BANDS] = {3.0, 5.0, 4.0, 2.0, 1.0, 0.0, -1.0, -1.0, -1.0, 1.0, 2.0, 3.5, 4.0, 4.5, 4.0, 4.0};
//static float Flat[NUM_BANDS] = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};

#define eOverEar 0
#define eOnEar 1
#define eInEar 2
#define eInCanal 3

#include <android/asset_manager.h>

class SoundLayout;

typedef enum { eFrontLeft=0, eFrontRight, eRearLeft, eRearRight, eWoofer, eTweeter} SpeakerID;

void InitAssetManager(AAssetManager *inAssetManager);

class AudioEngine {
public:
    AudioEngine(int sampleRate, int sampleSize);
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

    void SetHighQuality(bool state);

    void SetIntensity(float value); // value from -1.0 to +1.0
    float GetIntensity();
    void SetHeadPhoneType(int headpHoneType);
    int GetHeadPhoneType();

    // should be a 16-band equalier with pre-defined mid-frequencies
//  1 -  36.0
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
//  16 - 12k

    void SetEqualizer(int id, float *bandGains);  // id is the identifier of the eq set should be a postive number and, 0 is the auto mode
    int GetEqualizerId();  // returns the equalizer id

    void SetSpeakerState(SpeakerID id, float value);
    float GetSpeakerState(SpeakerID id);

    void ProcessAudio(short *buffer, void *outBuffer, int size);

    void WidenStereo(bool state);

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
    bool mHasNeon;

    bool mWiden;
    int mHeadPhoneType;
};



#endif //BOOM_AUDIOENGINE_H
