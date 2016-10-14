#include "OpenSLEqualizer.h"

namespace android {
    OpenSLEqualizer::OpenSLEqualizer() {

    }

    OpenSLEqualizer::~OpenSLEqualizer() {

    }

    void OpenSLEqualizer::Enable(bool enable) {

    }

    int OpenSLEqualizer::getNumBand() {
        return _band;
    }

    int OpenSLEqualizer::getBandMinFrz(int band) {
        return _minFrz;
    }

    int OpenSLEqualizer::getBandMaxFrz(int band) {
        return _maxFrz;
    }

    int OpenSLEqualizer::getBandMidFrz(int band) {
        return _midFrz;
    }

    int OpenSLEqualizer::getBandLevel(int band) {
        return _bandFrz;
    }

    bool OpenSLEqualizer::setBandLevel(int band, int position) {

    }

    jbyte *OpenSLEqualizer::processEqualizer(jbyte *dataPtr, int numBytes) {
//        if(true){
//            dataPtr++;
//        }


        return dataPtr;
    }
}