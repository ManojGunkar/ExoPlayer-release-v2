package com.globaldelight.boom.business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.SwitchCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.view.NegativeSeekBar;

import static com.globaldelight.boom.business.GooglePlayStoreModel.ACTION_STATE_CHANGED;
import static com.globaldelight.boom.business.GooglePlayStoreModel.STATE_LOCKED;

/**
 * Created by adarsh on 04/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class EffectsScreenPolicyForStore extends BroadcastReceiver implements EffectsScreenPolicy {

    private SwitchCompat mEffectSwitch;
    private NegativeSeekBar mIntensitySeek;
    private ToggleButton mFullBassCheck;
    private TextView mSelectedEqTxt;
    private ImageView mSpeakerBtn, mSelectedEqImg, mSelectedEqGoImg;
    private CheckBox m3DSurroundBtn, mIntensityBtn, mEqualizerBtn;
    private View mEqDialogPanel;

    private Activity mActivity;
    private ViewGroup mRootView;
    private GooglePlayStoreModel mBusinessModel;


    @Override
    public void init(Activity activity, ViewGroup root) {
        mActivity = activity;
        mRootView = root;

        mBusinessModel = (GooglePlayStoreModel)BusinessModelFactory.getCurrentModel();

        mEffectSwitch = mRootView.findViewById(R.id.effect_switch);
        m3DSurroundBtn = mRootView.findViewById(R.id.three_surround_btn);
        mSpeakerBtn = mRootView.findViewById(R.id.speaker_btn);
        mFullBassCheck = mRootView.findViewById(R.id.fullbass_chk);
        mIntensityBtn = mRootView.findViewById(R.id.intensity_btn);
        mIntensitySeek = mRootView.findViewById(R.id.intensity_seek);
        mEqualizerBtn = mRootView.findViewById(R.id.equalizer_btn);
        mEqDialogPanel = mRootView.findViewById(R.id.eq_dialog_panel);

        m3DSurroundBtn.setOnTouchListener(this::onTouch);
        mSpeakerBtn.setOnTouchListener(this::onTouch);
        mFullBassCheck.setOnTouchListener(this::onTouch);
        mIntensityBtn.setOnTouchListener(this::onTouch);
        mIntensitySeek.setOnTouchListener(this::onTouch);
        mEqualizerBtn.setOnTouchListener(this::onTouch);
        mEqDialogPanel.setOnTouchListener(this::onTouch);

        IntentFilter filter = new IntentFilter(ACTION_STATE_CHANGED);
        LocalBroadcastManager.getInstance(mActivity).registerReceiver(this, filter);

        update();
    }

    @Override
    public void finish() {
        LocalBroadcastManager.getInstance(mActivity).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( intent.getAction().equals(ACTION_STATE_CHANGED) ) {
            update();
        }
    }


    private boolean onTouch(View view, MotionEvent motionEvent) {
        if ( mBusinessModel.getCurrentState() == STATE_LOCKED ) {
            if ( motionEvent.getAction() == MotionEvent.ACTION_DOWN ) {
                mBusinessModel.showPurchaseDialog();
            }
            return true;
        }

        return false;
    }


    private void update() {
        final float LOCKED = 0.5f;
        final float UNLOCKED = 1.0f;
        if ( mBusinessModel.getCurrentState() == STATE_LOCKED ) {
            m3DSurroundBtn.setAlpha(LOCKED);
            mSpeakerBtn.setAlpha(LOCKED);
            mFullBassCheck.setAlpha(LOCKED);
            mIntensityBtn.setAlpha(LOCKED);
            mIntensitySeek.setAlpha(LOCKED);
            mEqualizerBtn.setAlpha(LOCKED);
            mEqDialogPanel.setAlpha(LOCKED);
        }
        else {
            m3DSurroundBtn.setAlpha(UNLOCKED);
            mSpeakerBtn.setAlpha(UNLOCKED);
            mFullBassCheck.setAlpha(UNLOCKED);
            mIntensityBtn.setAlpha(UNLOCKED);
            mIntensitySeek.setAlpha(UNLOCKED);
            mEqualizerBtn.setAlpha(UNLOCKED);
            mEqDialogPanel.setAlpha(UNLOCKED);
        }
    }

}
