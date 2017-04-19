package com.globaldelight.boom.handler.controller;

import android.content.Context;

import com.globaldelight.boom.App;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.manager.BoomPlayTimeReceiver;
import com.globaldelight.boomplayer.AudioEffect;

import static com.globaldelight.boom.manager.BoomPlayTimeReceiver.EFFECT_ON_AFTER_SECONDARY_POPUP;

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
        App.getPlayerEventHandler().setEffectEnable(enable);

        if(enable && BoomPlayTimeReceiver.isSecondaryPopupShown())
            BusinessPreferences.writeBoolean(mContext, EFFECT_ON_AFTER_SECONDARY_POPUP, true);
        if(enable)
            BoomPlayTimeReceiver.setEffectOffIn5Minutes();
    }

    @Override
    public void On3DSurroundEnable(boolean enable) {
        App.getPlayerEventHandler().set3DAudioEnable(enable);
    }

    @Override
    public void OnFullBassEnable(boolean enable) {
        App.getPlayerEventHandler().setSuperBassEnable(enable);
    }

    @Override
    public void OnIntensityEnable(boolean enable) {
        App.getPlayerEventHandler().setHighQualityEnable(enable);
    }

    @Override
    public void OnIntensityChange(int intensity) {
        App.getPlayerEventHandler().setIntensityValue(intensity/(double)100);
    }

    @Override
    public void OnEqualizerEnable(boolean enable) {
        App.getPlayerEventHandler().setEqualizerEnable(enable);
    }

    @Override
    public void OnEqualizerChange(int equalizer) {
        App.getPlayerEventHandler().setEqualizerGain(equalizer);
    }

    @Override
    public void OnSpeakerEnable(AudioEffect.Speaker speakerType, boolean enable) {
        App.getPlayerEventHandler().setSpeakerEnable(speakerType, enable);
    }
}
