package com.globaldelight.boom.handler.controller;

import com.globaldelight.boomplayer.AudioEffect;

/**
 * Created by Rahul Agarwal on 24-01-17.
 */

public interface IEffectUIController {
    void OnEffectEnable(boolean enable);

    void On3DSurroundEnable(boolean enable);

    void OnFullBassEnable(boolean enable);

    void OnIntensityEnable(boolean enable);

    void OnIntensityChange(int intensity);

    void OnEqualizerEnable(boolean enable);

    void OnEqualizerChange(int equalizer);

    void OnSpeakerEnable(AudioEffect.Speaker speakerType, boolean enable);
}
