package com.globaldelight.boom.business;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.globaldelight.boom.view.NegativeSeekBar;

/**
 * Created by adarsh on 04/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class StoreUIHandler implements UIHandler {

    private NegativeSeekBar mIntensitySeek;
    private SwitchCompat mEffectSwitch;
    private ToggleButton mFullBassCheck;
    private TextView mSelectedEqTxt;
    private ImageView mSpeakerBtn, mSelectedEqImg, mSelectedEqGoImg;
    private CheckBox m3DSurroundBtn, mIntensityBtn, mEqualizerBtn;
    private View mEqDialogPanel;



    @Override
    public void handleEffectsScreen(ViewGroup root) {


    }

    @Override
    public RecyclerView.Adapter handleEqualizerPresets(RecyclerView.Adapter adapter) {
        return null;
    }
}
