package com.globaldelight.boom.app.dialogs;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.player.AudioEffect;

/**
 * Created by adarsh on 11/07/17.
 */

public class SpeakerDialog implements View.OnClickListener {

    private Activity mActivity;
    private AudioEffect audioEffects;
    private LinearLayout mSpeakerDialogPanel;


    public SpeakerDialog(Activity activity) {
        mActivity = activity;
        audioEffects = AudioEffect.getInstance(activity);
    }

    public Activity getActivity() {
        return mActivity;
    }

    public void show() {
        mSpeakerDialogPanel = (LinearLayout) mActivity.getLayoutInflater()
                .inflate(R.layout.speaker_panel, null);

        updateSpeakers(mSpeakerDialogPanel);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.speaker_dialog_title)
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialog_submit_positive))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .customView(mSpeakerDialogPanel, false)
                .positiveText(R.string.done)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void updateSpeakers(LinearLayout speakerPanel){
        ImageView mFrontLeftSpeaker, mFrontRightSpeaker, mSurroundLeftSpeaker, mSurroundRightSpeaker;

        mFrontLeftSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_left_front);
        mFrontRightSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_right_front);
        mSurroundLeftSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_left_surround);
        mSurroundRightSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_right_surround);

        mFrontLeftSpeaker.setOnClickListener(this);
        mFrontRightSpeaker.setOnClickListener(this);
        mSurroundLeftSpeaker.setOnClickListener(this);
        mSurroundRightSpeaker.setOnClickListener(this);

        mFrontLeftSpeaker.setSelected(audioEffects.isLeftFrontSpeakerOn());
        mFrontRightSpeaker.setSelected(audioEffects.isRightFrontSpeakerOn());
        mSurroundLeftSpeaker.setSelected(audioEffects.isLeftSurroundSpeakerOn());
        mSurroundRightSpeaker.setSelected(audioEffects.isRightSurroundSpeakerOn());

        updateTweeterAndWoofer(speakerPanel, audioEffects.isAllSpeakerOn());
    }

    private void updateTweeterAndWoofer(LinearLayout speakerPanel, boolean enable){
        ImageView mTweeterLeftSpeaker, mTweeterRightSpeaker, mWoofer;
        mTweeterLeftSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_left_tweeter);
        mTweeterRightSpeaker = (ImageView) speakerPanel.findViewById(R.id.speaker_right_tweeter);
        mWoofer = (ImageView) speakerPanel.findViewById(R.id.speaker_sub_woofer);

        mTweeterLeftSpeaker.setOnClickListener(this);
        mTweeterRightSpeaker.setOnClickListener(this);
        mWoofer.setOnClickListener(this);

        audioEffects.setOnAllSpeaker(enable);

        mTweeterLeftSpeaker.setEnabled(enable);
        mTweeterRightSpeaker.setEnabled(enable);
        mWoofer.setEnabled(enable);

        if ( enable ) {
            mTweeterLeftSpeaker.setSelected(audioEffects.isTweeterOn());
            mTweeterRightSpeaker.setSelected(audioEffects.isTweeterOn());
            mWoofer.setSelected(audioEffects.isWooferOn());
        }
    }

    private void updateSpeakers(@AudioEffect.Speaker final int speakerType){
        boolean enable = false;
        switch (speakerType) {
            case AudioEffect.SPEAKER_FRONT_LEFT:
                enable = !audioEffects.isLeftFrontSpeakerOn();
                audioEffects.setEnableLeftFrontSpeaker(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_FRONT_LEFT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_FRONT_RIGHT:
                enable = !audioEffects.isRightFrontSpeakerOn();
                audioEffects.setEnableRightFrontSpeaker(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_FRONT_RIGHT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_SURROUND_LEFT:
                enable = !audioEffects.isLeftSurroundSpeakerOn();
                audioEffects.setEnableLeftSurroundSpeaker(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_REAR_LEFT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_SURROUND_RIGHT:
                enable = !audioEffects.isRightSurroundSpeakerOn();
                audioEffects.setEnableRightSurroundSpeaker(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_REAR_RIGHT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_TWEETER:
                enable = !audioEffects.isTweeterOn();
                audioEffects.setEnableTweeter(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_TWEETER, enable);
                break;

            case AudioEffect.SPEAKER_WOOFER:
                enable = !audioEffects.isWooferOn();
                audioEffects.setEnableWoofer(enable);
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_SUBWOOFER, enable);
                break;
        }
        updateSpeakers(mSpeakerDialogPanel);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.speaker_left_front:
                updateSpeakers(AudioEffect.SPEAKER_FRONT_LEFT);
                break;
            case R.id.speaker_right_front:
                updateSpeakers(AudioEffect.SPEAKER_FRONT_RIGHT);
                break;
            case R.id.speaker_left_surround:
                updateSpeakers(AudioEffect.SPEAKER_SURROUND_LEFT);
                break;
            case R.id.speaker_right_surround:
                updateSpeakers(AudioEffect.SPEAKER_SURROUND_RIGHT);
                break;
            case R.id.speaker_left_tweeter:
                updateSpeakers(AudioEffect.SPEAKER_TWEETER);
                break;
            case R.id.speaker_right_tweeter:
                updateSpeakers(AudioEffect.SPEAKER_TWEETER);
                break;
            case R.id.speaker_sub_woofer:
                updateSpeakers(AudioEffect.SPEAKER_WOOFER);
                break;
        }
    }
}
