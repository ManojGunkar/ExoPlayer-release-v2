#include "jni.h"

#ifndef OPENSLEQUALIZER_H_
#define OPENSLEQUALIZER_H_

#ifdef __cplusplus
extern "C" {
#endif

namespace android {
    class OpenSLEqualizer {
    public:
        OpenSLEqualizer();

        ~OpenSLEqualizer();

        void Enable(bool enable);

        int getNumBand(void);

        int getBandMinFrz(int band);

        int getBandMaxFrz(int band);

        int getBandMidFrz(int band);

        int getBandLevel(int band);

        bool setBandLevel(int band, int position);

        jbyte *processEqualizer(jbyte *dataPtr, int numBytes);

    private:
        int _band;
        int _minFrz;
        int _maxFrz;
        int _midFrz;
        int _bandFrz;
    };
};
#ifdef __cplusplus
}
#endif
#endif