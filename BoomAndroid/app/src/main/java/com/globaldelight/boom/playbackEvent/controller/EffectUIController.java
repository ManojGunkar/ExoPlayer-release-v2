package com.globaldelight.boom.playbackEvent.controller;

import android.content.Context;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IEffectUIController;
import com.globaldelight.boom.player.AudioEffect;

/**
 * Created by Rahul Agarwal on 24-01-17.
 */

public class EffectUIController implements IEffectUIController {
    private Context mContext;
    private static IEffectUIController handler;

    public EffectUIController(Context context){
        this.mContext = context;
    }

    public static void registerEffectController(IEffectUIController aaIEffectUIController){
        handler = aaIEffectUIController;
    }

    public static void unregisterEffectController(){
        handler = null;
    }

    @Override
    public void OnEffectEnable(boolean enable) {
        App.playbackManager().setEffectEnable(enable);
    }

    @Override
    public void On3DSurroundEnable(boolean enable) {
        App.playbackManager().set3DAudioEnable(enable);
    }

    @Override
    public void OnFullBassEnable(boolean enable) {
        App.playbackManager().setSuperBassEnable(enable);
    }

    @Override
    public void OnIntensityEnable(boolean enable) {
        App.playbackManager().setHighQualityEnable(enable);
    }

    @Override
    public void OnIntensityChange(int intensity) {
        App.playbackManager().setIntensityValue(intensity/(double)100);
    }

    @Override
    public void OnEqualizerEnable(boolean enable) {
        App.playbackManager().setEqualizerEnable(enable);
    }

    @Override
    public void OnEqualizerChange(int equalizer) {
        App.playbackManager().setEqualizerGain(equalizer);
    }

    @Override
    public void OnSpeakerEnable(@AudioEffect.Speaker int speakerType, boolean enable) {
        App.playbackManager().setSpeakerEnable((int)speakerType, enable);
    }
}
