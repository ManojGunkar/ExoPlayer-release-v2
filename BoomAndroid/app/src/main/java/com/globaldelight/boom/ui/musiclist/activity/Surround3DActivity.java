package com.globaldelight.boom.ui.musiclist.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.manager.MusicReceiver;
import com.globaldelight.boom.ui.musiclist.adapter.EqualizerViewAdapter;
import com.globaldelight.boom.ui.widgets.NegativeSeekBar;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.ui.widgets.ScrollEnableLayoutManager;
import com.globaldelight.boom.ui.widgets.TooltipWindow;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class Surround3DActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MusicReceiver.updateMusic, EqualizerViewAdapter.onEqualizerUpdate {
    TooltipWindow tipWindow, tipSpeakerWidow;
    private RegularTextView mToolbarTitle, mEffectTxt, mEffectSwitchTxt, m3DTxt, m3DSwitchTxt, mSpeakerInfo, mFullbassTxt,
            mIntensityTxt, mIntensitySwitchTxt, mEqualizerTxt, mEqualizerSwitchTxt;
    private LinearLayout mIntensityIndicator, mEqualizerIndicator, mFullBassPanel;
    private /*ToggleButton*/ Switch mEffectPowerBtn;
    private ImageView m3DSwitchBtn, mIntensitySwitchBtn, mEqualizerSwitchBtn, mSpeakerSwitchBtn, mFullBassRd;
    private ImageView mSpeakerLeftFront, mSpeakerRightFront, mTweeterLeft, mTweeterRight,
            mSpeakerLeftSurround, mSpeakerRightSurround, mSpeakerSubWoofer, mCenterMan;
    private Toolbar toolbar;
    private LinearLayout mEffectSwitchPanel;
    private FrameLayout collapsablelayout;
    private NegativeSeekBar mIntensitySeek;
    private RecyclerView recyclerView;
    private ScrollEnableLayoutManager layoutManager;
    private EqualizerViewAdapter mEqualizerAdapter;
    private MusicReceiver musicReceiver;
    private AudioEffect audioEffectPreferenceHandler;
    private boolean isExpended = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surround_3d);
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_OPEN_EFFECTS);
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);
        initViews();
        setupToolbar();
        fillEqualizer();
//TODO
        /**
         * Master control for managing efrfects based on purchase
         */
        //  audioEffectPreferenceHandler.setMasterEffectControl(false);

        onPowerSwitchUpdate();
        update3DSurround();
        updateIntensity();
        updateEqualizer();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            showCoachMark();

    }

    public void showCoachMark() {
        //  boolean mAppNewLaunch = Preferences.readBoolean(Surround3DActivity.this, Preferences.APP_FRESH_LAUNCH, true);
        if (Preferences.readBoolean(Surround3DActivity.this, Preferences.EFFECT_SCREEN_TAP_EFFECT_ENABLE, true) && !audioEffectPreferenceHandler.isAudioEffectOn()) {
            tipWindow = new TooltipWindow(Surround3DActivity.this, TooltipWindow.DRAW_BOTTOM, getResources().getString(R.string.tutorial_boom_effect_poweron));
            tipWindow.setAutoDismissBahaviour(true);
            tipWindow.showToolTip(findViewById(R.id.effect_power_switch), TooltipWindow.DRAW_ARROW_TOP_RIGHT);

        }
    }

    public void showSpeakerCoachMark() {
        Preferences.writeBoolean(Surround3DActivity.this, Preferences.EFFECT_SCREEN_TAP_EFFECT_ENABLE, false);

        if (Preferences.readBoolean(Surround3DActivity.this, Preferences.PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE, true)) {

            tipSpeakerWidow = new TooltipWindow(Surround3DActivity.this, TooltipWindow.DRAW_BOTTOM, getResources().getString(R.string.tutorial_select_speaker));
            tipSpeakerWidow.setAutoDismissBahaviour(true);
            tipSpeakerWidow.showToolTip(findViewById(R.id.speaker_switch_btn), TooltipWindow.DRAW_ARROW_TOP_RIGHT);

            //Preferences.writeBoolean(this,Preferences.EFFECT_SCREEN_TAP_SPEAKER_ENABLE,false);
        }
    }


    public void initViews(){
        musicReceiver = new MusicReceiver(this);
        toolbar = (Toolbar)findViewById(R.id.effect_toolbar);
        mToolbarTitle = (RegularTextView) findViewById(R.id.toolbr_title);
        mToolbarTitle.setTextColor(Color.WHITE);
        mEffectTxt = (RegularTextView) findViewById(R.id.effect_txt);
        mEffectTxt.setTextColor(Color.WHITE);
        mEffectSwitchTxt = (RegularTextView) findViewById(R.id.effect_switch_txt);
        mEffectSwitchTxt.setTextColor(Color.WHITE);
        m3DTxt = (RegularTextView) findViewById(R.id.three_d_txt);
        m3DTxt.setTextColor(Color.WHITE);
        m3DSwitchTxt = (RegularTextView) findViewById(R.id.three_d_switch_txt);
        m3DSwitchTxt.setTextColor(Color.WHITE);
        mSpeakerInfo = (RegularTextView) findViewById(R.id.speaker_info);
        mSpeakerInfo.setTextColor(Color.WHITE);
        mFullbassTxt = (RegularTextView) findViewById(R.id.fullbass_txt);
        mFullbassTxt.setTextColor(Color.WHITE);
        mIntensityTxt = (RegularTextView) findViewById(R.id.intensity_txt);
        mIntensityTxt.setTextColor(Color.WHITE);
        mIntensitySwitchTxt = (RegularTextView) findViewById(R.id.intensity_switch_txt);
        mIntensitySwitchTxt.setTextColor(Color.WHITE);
        mEqualizerTxt = (RegularTextView) findViewById(R.id.equalizer_txt);
        mEqualizerTxt.setTextColor(Color.WHITE);
        mEqualizerSwitchTxt = (RegularTextView) findViewById(R.id.equalizer_switch_txt);
        mEqualizerSwitchTxt.setTextColor(Color.WHITE);

        mEffectSwitchPanel = (LinearLayout) findViewById(R.id.effect_switch_panel);
        mFullBassPanel = (LinearLayout) findViewById(R.id.full_bass_panel);// onClick
        mIntensityIndicator = (LinearLayout)findViewById(R.id.intensity_indicator);
        mEqualizerIndicator = (LinearLayout)findViewById(R.id.equalizer_indicator);

        mEffectPowerBtn = (Switch) findViewById(R.id.effect_power_switch);//onclick
        m3DSwitchBtn = (ImageView)findViewById(R.id.three_d_switch_btn);//onclick
        mIntensitySwitchBtn = (ImageView)findViewById(R.id.intensity_switch_btn);//onclick
        mEqualizerSwitchBtn = (ImageView)findViewById(R.id.equalizer_switch_btn);//onclick
        mSpeakerSwitchBtn = (ImageView) findViewById(R.id.speaker_switch_btn);//onclick

        mSpeakerLeftFront = (ImageView)findViewById(R.id.speaker_left_front);//onclick
        mSpeakerRightFront = (ImageView)findViewById(R.id.speaker_right_front);//onclick
        mTweeterLeft = (ImageView)findViewById(R.id.speaker_left_tweeter);//onclick
        mTweeterRight = (ImageView)findViewById(R.id.speaker_right_tweeter);//onclick
        mSpeakerLeftSurround = (ImageView)findViewById(R.id.speaker_left_surround);//onclick
        mSpeakerRightSurround = (ImageView)findViewById(R.id.speaker_right_surround);//onclick
        mSpeakerSubWoofer = (ImageView)findViewById(R.id.speaker_sub_woofer);//onclick
        mCenterMan = (ImageView)findViewById(R.id.speaker_center_man);//onclick

        mFullBassRd = (ImageView) findViewById(R.id.full_bass_radio);
        collapsablelayout = (FrameLayout) findViewById(R.id.collapsable_layout);
        mIntensitySeek = (NegativeSeekBar) findViewById(R.id.intensity_seek);//onSeekChange
        recyclerView = (RecyclerView)findViewById(R.id.eq_recycler) ;

        //        Surround3DLinesView view = new Surround3DLinesView(this, Color.BLACK);
//        collapsablelayout.addView(view);
        collapsablelayout.setVisibility(View.GONE);
//        mEffectPowerBtn.setOnCheckedChangeListener(this);

        mEffectSwitchPanel.setOnClickListener(this);

        m3DSwitchBtn.setOnClickListener(this);
        mIntensitySwitchBtn.setOnClickListener(this);
        mEqualizerSwitchBtn.setOnClickListener(this);
        mSpeakerSwitchBtn.setOnClickListener(this);

        mFullBassPanel.setOnClickListener(this);

        mSpeakerLeftFront.setOnClickListener(this);
        mSpeakerRightFront.setOnClickListener(this);
        mTweeterLeft.setOnClickListener(this);
        mTweeterRight.setOnClickListener(this);
        mSpeakerLeftSurround.setOnClickListener(this);
        mSpeakerRightSurround.setOnClickListener(this);
        mSpeakerSubWoofer.setOnClickListener(this);

        mIntensitySeek.setProgress(50);
        mIntensitySeek.setOnSeekBarChangeListener(this);

        mEffectPowerBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if((!audioEffectPreferenceHandler.isAudioEffectOn() && isChecked) ||
                        (audioEffectPreferenceHandler.isAudioEffectOn() && !isChecked)){


                    showSpeakerCoachMark();
                    audioEffectPreferenceHandler.setEnableAudioEffect(isChecked);
                    if(App.getPlayerEventHandler().getPlayingItem() != null)
                        App.getPlayerEventHandler().updateEffect();
                    onPowerSwitchUpdate();
                    update3DSurround();
                    updateIntensity();
                    updateEqualizer();
                }
            }
        });
    }

    public void onPowerSwitchUpdate(){


        mEffectPowerBtn.setChecked(audioEffectPreferenceHandler.isAudioEffectOn());

        if(audioEffectPreferenceHandler.isAudioEffectOn()) {
            mEffectSwitchTxt.setText(getResources().getString(R.string.status_on));
            /*mEffectPowerBtn.setChecked(true);*/
            mEffectTxt.setTextColor(Color.WHITE);
            mEffectSwitchTxt.setTextColor(Color.WHITE);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_EFFECTS_TURNED_ON);
            Preferences.writeBoolean(this, Preferences.PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE, false);
        }else{
            mEffectSwitchTxt.setText(getResources().getString(R.string.status_off));
            /*mEffectPowerBtn.setChecked(false);*/
            mEffectTxt.setTextColor(Color.WHITE);
            mEffectSwitchTxt.setTextColor(Color.WHITE);
            collapse();
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_EFFECTS_TURNED_OFF);
        }
        FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_EFFECT_STATE_CHANGED, audioEffectPreferenceHandler.isAudioEffectOn());

    }

    public void update3DSurround(){
        if(audioEffectPreferenceHandler.isAudioEffectOn() && audioEffectPreferenceHandler.is3DSurroundOn()){
            m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_surround, null));
            m3DSwitchTxt.setText(getResources().getString(R.string.status_on_caps));
            m3DTxt.setTextColor(Color.WHITE);
            m3DSwitchTxt.setTextColor(Color.WHITE);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_3D_TURNED_ON);
        }else{
            m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_surround_off, null));
            m3DSwitchTxt.setText(getResources().getString(R.string.status_off_caps));
            m3DTxt.setTextColor(Color.WHITE);
            m3DSwitchTxt.setTextColor(Color.WHITE);
            mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speakers_inactive, null));
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_3D_TURNED_OFF);
        }
        updateSpeakerInfo();
        updateFullBass();
        updateSpeakers();
        FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_3D_STATE_CHANGED, audioEffectPreferenceHandler.is3DSurroundOn());
    }

    public void updateSpeakerInfo(){
        boolean isLeftFront = audioEffectPreferenceHandler.isLeftFrontSpeakerOn();
        boolean isRightFront = audioEffectPreferenceHandler.isRightFrontSpeakerOn();
        boolean isLeftSurround = audioEffectPreferenceHandler.isLeftSurroundSpeakerOn();
        boolean isRightSurround = audioEffectPreferenceHandler.isRightSurroundSpeakerOn();
        boolean isWoofer = audioEffectPreferenceHandler.isWooferOn();
        boolean isTweeter = audioEffectPreferenceHandler.isTweeterOn();

        if(audioEffectPreferenceHandler.isAudioEffectOn() && audioEffectPreferenceHandler.is3DSurroundOn()) {
            if(isLeftFront && isRightFront && isLeftSurround && isRightSurround){
                mSpeakerInfo.setVisibility(View.GONE);// All Speakers are on
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speakers_active_off, null));
                updateTweeterAndWoofer(true);
            }else if (!isLeftFront && !isRightFront && !isLeftSurround && !isRightSurround) {
                // All Speakers are off
                mSpeakerInfo.setText(getResources().getString(R.string.speaker_status_all_off));
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// active color
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speakers_active_off, null));
                updateTweeterAndWoofer(false);
            }else {
                // Some Speakers are off
                mSpeakerInfo.setText(getResources().getString(R.string.speaker_status_some_off));
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// active color
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speakers_active_off, null));
                updateTweeterAndWoofer(true);
            }
        }else{
            if(isLeftFront && isRightFront && isLeftSurround && isRightSurround && isWoofer && isTweeter){
                mSpeakerInfo.setVisibility(View.GONE);// All Speakers are on
                updateTweeterAndWoofer(true);
            }else if (!isLeftFront && !isRightFront && !isLeftSurround && !isRightSurround && !isWoofer && !isTweeter) {
                // All Speakers are off
                mSpeakerInfo.setText(getResources().getString(R.string.speaker_status_all_off));
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// inactive color
                updateTweeterAndWoofer(false);
            }else {
                // Some Speakers are off
                mSpeakerInfo.setText(getResources().getString(R.string.speaker_status_some_off));
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// inactive color
                updateTweeterAndWoofer(true);
            }
            mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speakers_inactive, null));
        }
    }

    private void updateTweeterAndWoofer(boolean isEnable){
        if(isEnable){
            audioEffectPreferenceHandler.setOnAllSpeaker(true);
            if(!audioEffectPreferenceHandler.isTweeterOn()){
                mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_l_inactive, null));
                mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_r_disabled, null));
            }else {
                mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_l_active, null));
                mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_r_active, null));
            }
            if(!audioEffectPreferenceHandler.isWooferOn()){
                mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.ic_woofer_inactive, null));
            }else {
                mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.ic_woofer_active, null));
            }
        }else{
            audioEffectPreferenceHandler.setOnAllSpeaker(false);
            mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_l_disabled, null));
            mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_r_disabled, null));
            mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.ic_woofer_disabled, null));
        }
    }


    public void updateFullBass(){
        if(audioEffectPreferenceHandler.is3DSurroundOn() && audioEffectPreferenceHandler.isAudioEffectOn()){
            if(audioEffectPreferenceHandler.isFullBassOn()){
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_on, null));
            }else{
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_off, null));
            }
            mFullbassTxt.setTextColor(Color.WHITE);
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FULL_BASS_ENABLED);
        }else{
            if(audioEffectPreferenceHandler.isFullBassOn()){
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_inactive_on, null));
            }else{
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_inactive_off, null));
            }
            mFullbassTxt.setTextColor(Color.WHITE);
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FULL_BASS_DISABLED);
        }
    }

    public void updateSpeakers(){
        if(!audioEffectPreferenceHandler.isLeftFrontSpeakerOn()){
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_front_inactive, null));
        }else {
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_front_active, null));
        }
        if(!audioEffectPreferenceHandler.isRightFrontSpeakerOn()){
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_front_inactive, null));
        }else {
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_front_active, null));
        }
        if(!audioEffectPreferenceHandler.isLeftSurroundSpeakerOn()){
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_surround_inactive, null));
        }else {
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_surround_active, null));
        }
        if(!audioEffectPreferenceHandler.isRightSurroundSpeakerOn()){
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_surround_inactive, null));
        }else {
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_surround_active, null));
        }
        updateTweeterAndWoofer(audioEffectPreferenceHandler.isAllSpeakerOn());
        if(MusicReceiver.isPlugged){
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.ic_man_active, null));
        }else{
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.ic_man_active, null));
        }
    }

    public void updateIntensity(){
        if(audioEffectPreferenceHandler.isAudioEffectOn() && audioEffectPreferenceHandler.isIntensityOn()){
            mIntensitySwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.intensity, null));
            mIntensitySwitchTxt.setText(getResources().getString(R.string.status_on_caps));
            mIntensitySwitchTxt.setTextColor(Color.WHITE);
            mIntensityTxt.setTextColor(Color.WHITE);
            EnableSeek(true);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_INTENSITY_TURNED_ON);
        }else{
            mIntensitySwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.intensity_off, null));
            mIntensitySwitchTxt.setText(getResources().getString(R.string.status_off_caps));
            mIntensitySwitchTxt.setTextColor(Color.WHITE);
            mIntensityTxt.setTextColor(Color.WHITE);
            EnableSeek(false);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_INTENSITY_TURNED_OFF);
        }
        mIntensitySeek.setProgress(audioEffectPreferenceHandler.getIntensity());
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_INTENSITY_STATE_CHANGED);
    }

    private void EnableSeek(boolean isEnable){
        if(isEnable){
            mIntensitySeek.setOnTouchListener(null);
        }else{
            mIntensitySeek.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
    }

    public void updateEqualizer(){
        if(audioEffectPreferenceHandler.isAudioEffectOn() && audioEffectPreferenceHandler.isEqualizerOn()){
            mEqualizerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.equalizer, null));
            mEqualizerSwitchTxt.setText(getResources().getString(R.string.status_on_caps));
            mEqualizerSwitchTxt.setTextColor(Color.WHITE);
            mEqualizerTxt.setTextColor(Color.WHITE);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_EQ_TURNED_ON);
        }else{
            mEqualizerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.equalizer_off, null));
            mEqualizerSwitchTxt.setText(getResources().getString(R.string.status_off_caps));
            mEqualizerSwitchTxt.setTextColor(Color.WHITE);
            mEqualizerTxt.setTextColor(Color.WHITE);
            MixPanelAnalyticHelper.track(this, AnalyticsHelper.EVENT_EQ_TURNED_OFF);
        }
        if(mEqualizerAdapter != null)
            mEqualizerAdapter.updateList();
        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_EQ_STATE_CHANGED);
    }

    public void switch3DSurround(boolean isPowerOn){
        if(isPowerOn) {
            if (audioEffectPreferenceHandler.is3DSurroundOn()) {
                audioEffectPreferenceHandler.setEnable3DSurround(false);
                App.getPlayerEventHandler().set3DAudioEnable(false);
                collapse();
            } else {
                //TODO
               /* int purchaseType = audioEffectPreferenceHandler.getUserPurchaseType();
                switch (AudioEffect.purchase.fromOrdinal(purchaseType)) {
                    case NORMAL_USER:
                        break;
                    case PAID_USER:
                        audioEffectPreferenceHandler.setEnable3DSurround(true);
                        App.getPlayerEventHandler().set3DAudioEnable(true);
                        break;
                    case FIVE_DAY_OFFER:
                        audioEffectPreferenceHandler.setEnable3DSurround(true);
                        App.getPlayerEventHandler().set3DAudioEnable(true);
                        break;
                }*/


            }
            update3DSurround();
            if(!audioEffectPreferenceHandler.isIntensityOn()){
                switchIntensity(isPowerOn);
            }
            if(!audioEffectPreferenceHandler.isEqualizerOn()){
                switchEqualizer(isPowerOn);
            }
        }
        //FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_3D_STATE_CHANGED, audioEffectPreferenceHandler.is3DSurroundOn());

    }

    public void switchIntensity(boolean isPowerOn){
        if(audioEffectPreferenceHandler.is3DSurroundOn() &&
                audioEffectPreferenceHandler.isIntensityOn()){
            onTextViewTurn(mIntensityIndicator);
            return;
        }
        if(isPowerOn) {
            if (audioEffectPreferenceHandler.isIntensityOn()) {
                audioEffectPreferenceHandler.setEnableIntensity(false);
                App.getPlayerEventHandler().setHighQualityEnable(false);
            } else {
                audioEffectPreferenceHandler.setEnableIntensity(true);
                App.getPlayerEventHandler().setHighQualityEnable(true);
            }
            updateIntensity();
        }
    }

    public void switchEqualizer(boolean isPowerOn){
        if(audioEffectPreferenceHandler.is3DSurroundOn() &&
                audioEffectPreferenceHandler.isEqualizerOn()){
            onTextViewTurn(mEqualizerIndicator);
            return;
        }
        if(isPowerOn) {
            if (audioEffectPreferenceHandler.isEqualizerOn()) {
                audioEffectPreferenceHandler.setEnableEqualizer(false);
                App.getPlayerEventHandler().setEqualizerEnable(false);
            } else {
                audioEffectPreferenceHandler.setEnableEqualizer(true);
                App.getPlayerEventHandler().setEqualizerEnable(true);
            }
            updateEqualizer();
        }
    }

    public void switchSuperPass(boolean isPowerOn){
        if(isPowerOn && audioEffectPreferenceHandler.is3DSurroundOn()) {
            if(audioEffectPreferenceHandler.isFullBassOn()){
                audioEffectPreferenceHandler.setEnableFullBass(false);
                App.getPlayerEventHandler().setSuperBassEnable(false);
            }else{
                audioEffectPreferenceHandler.setEnableFullBass(true);
                App.getPlayerEventHandler().setSuperBassEnable(true);
            }
            updateFullBass();
        }
    }

    @Override
    public void onClick(View v) {
        boolean isPowerOn = audioEffectPreferenceHandler.isAudioEffectOn();
        switch (v.getId()){
           /* case R.id.effect_switch_panel:
                if(isPowerOn){
                    audioEffectPreferenceHandler.setEnableAudioEffect(false);
                }else{
                    audioEffectPreferenceHandler.setEnableAudioEffect(true);
                }

                App.getPlayerEventHandler().updateEffect();
                onPowerSwitchUpdate();
                update3DSurround();
                updateIntensity();
                updateEqualizer();
                break;*/
            case R.id.three_d_switch_btn :
                switch3DSurround(isPowerOn);
                break;
            case R.id.intensity_switch_btn:
                switchIntensity(isPowerOn);
                break;
            case R.id.equalizer_switch_btn:
                switchEqualizer(isPowerOn);
                break;
            case R.id.full_bass_panel:
                switchSuperPass(isPowerOn);
                break;
            case R.id.speaker_switch_btn:
                if(isPowerOn && audioEffectPreferenceHandler.is3DSurroundOn()) {
                    if(isExpended){
                        collapse();
                        isExpended = false;
                    }else{
                        expand();
                        isExpended = true;
                    }
                }
                break;
            case R.id.speaker_left_front:
                if (audioEffectPreferenceHandler.isLeftFrontSpeakerOn()) {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_front_inactive, null));
                    audioEffectPreferenceHandler.setEnableLeftFrontSpeaker(false);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.FrontLeft, false);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FRONT_LEFT_SPEAKER_OFF);
                } else {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_front_active, null));
                    audioEffectPreferenceHandler.setEnableLeftFrontSpeaker(true);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.FrontLeft, true);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FRONT_LEFT_SPEAKER_ON);

                }
                updateSpeakerInfo();

                break;
            case R.id.speaker_right_front:
                if (audioEffectPreferenceHandler.isRightFrontSpeakerOn()) {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_front_inactive, null));
                    audioEffectPreferenceHandler.setEnableRightFrontSpeaker(false);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.FrontRight, false);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FRONT_RIGHT_SPEAKER_OFF);

                } else {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_front_active, null));
                    audioEffectPreferenceHandler.setEnableRightFrontSpeaker(true);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.FrontRight, true);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_FRONT_RIGHT_SPEAKER_ON);

                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_left_tweeter:
            case R.id.speaker_right_tweeter:
                if(audioEffectPreferenceHandler.isAllSpeakerOn()) {
                    if (audioEffectPreferenceHandler.isTweeterOn()) {
                        mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_l_disabled, null));
                        mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_r_disabled, null));
                        audioEffectPreferenceHandler.setEnableTweeter(false);
                        App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.Tweeter, false);
                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_TWEETER_OFF);

                    } else {
                        mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_l_active, null));
                        mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.ic_tweeter_r_active, null));
                        audioEffectPreferenceHandler.setEnableTweeter(true);
                        App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.Tweeter, true);
                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_TWEETER_ON);

                    }
                    updateSpeakerInfo();
                }
                break;
            case R.id.speaker_left_surround:
                if (audioEffectPreferenceHandler.isLeftSurroundSpeakerOn()) {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_surround_inactive, null));
                    audioEffectPreferenceHandler.setEnableLeftSurroundSpeaker(false);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.RearLeft, false);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REAR_LEFT_SPEAKER_OFF);

                } else {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_l_surround_active, null));
                    audioEffectPreferenceHandler.setEnableLeftSurroundSpeaker(true);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.RearLeft, true);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REAR_LEFT_SPEAKER_ON);

                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_right_surround:
                if (audioEffectPreferenceHandler.isRightSurroundSpeakerOn()) {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_surround_inactive, null));
                    audioEffectPreferenceHandler.setEnableRightSurroundSpeaker(false);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.RearRight, false);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REAR_RIGHT_SPEAKER_OFF);

                } else {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.ic_speakers_r_surround_active, null));
                    audioEffectPreferenceHandler.setEnableRightSurroundSpeaker(true);
                    App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.RearRight, true);
                    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REAR_RIGHT_SPEAKER_ON);

                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_sub_woofer:
                if(audioEffectPreferenceHandler.isAllSpeakerOn()) {
                    if (audioEffectPreferenceHandler.isWooferOn()) {
                        mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.ic_woofer_inactive, null));
                        audioEffectPreferenceHandler.setEnableWoofer(false);
                        App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.Woofer, false);
                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SUBWOOFER_OFF);

                    } else {
                        mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.ic_woofer_active, null));
                        audioEffectPreferenceHandler.setEnableWoofer(true);
                        App.getPlayerEventHandler().setSpeakerEnable(AudioEffect.Speaker.Woofer, true);
                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SUBWOOFER_ON);

                    }
                    updateSpeakerInfo();
                }
                break;
        }
    }

    public void onTextViewTurn(final LinearLayout v){
        final Animation in = new AlphaAnimation(0.0f, 1.0f);
        in.setDuration(1500);

        final Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(1000);

        v.startAnimation(in);//setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                v.startAnimation(out);
//                v.setVisibility(View.GONE);
            }
        }, 0);

    }

    private void setupToolbar() {
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.stay_out, R.anim.push_up_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.stay_out, R.anim.push_up_out);
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_EFFECTS_BACK_BUTTON_TAPPED);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void fillEqualizer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final TypedArray eq_active_on = getResources().obtainTypedArray(R.array.eq_active_on);
                final TypedArray eq_active_off = getResources().obtainTypedArray(R.array.eq_active_off);
                final TypedArray eq_inactive_on = getResources().obtainTypedArray(R.array.eq_inactive_on);
                final TypedArray eq_inactive_off = getResources().obtainTypedArray(R.array.eq_inactive_off);
                final List<String> eq_names = Arrays.asList(getResources().getStringArray(R.array.eq_names));

                layoutManager = new ScrollEnableLayoutManager(Surround3DActivity.this, LinearLayoutManager.HORIZONTAL, true);
                layoutManager.setReverseLayout(false);
                layoutManager.setStackFromEnd(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setHasFixedSize(true);
                        recyclerView.scrollToPosition(audioEffectPreferenceHandler.getSelectedEqualizerPosition());
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                            }

                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);

                                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                                    // Do something
                                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    // Do something
                                } else {
                                    // Do something
                                }
                            }
                        });
                        mEqualizerAdapter = new EqualizerViewAdapter(Surround3DActivity.this, eq_names, eq_active_on, eq_active_off, eq_inactive_on, eq_inactive_off);
                        recyclerView.setAdapter(mEqualizerAdapter);
                        if(mEqualizerAdapter != null)
                            mEqualizerAdapter.setEqualizerUpdateEvent(Surround3DActivity.this);
                    }
                });
            }
        }).start();
    }

    private void expand() {
        //when speaker panel is open
        mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_three_d_speaker_active_on, null));
        collapsablelayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        collapsablelayout.measure(widthSpec, heightSpec);
        int height = collapsablelayout.getMeasuredHeight();
        //Log.d("Height : ", ""+height);

        ValueAnimator mAnimator = slideAnimator(0, height);
        mAnimator.start();
    }

    private void collapse() {
        int finalHeight = collapsablelayout.getHeight();

        ValueAnimator mAnimator = slideAnimator(finalHeight, 0);

        mAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                //Height=0, but it set visibility to GONE
                collapsablelayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }

        });
        mAnimator.start();
    }

    private ValueAnimator slideAnimator(int start, int end) {

        ValueAnimator animator = ValueAnimator.ofInt(start, end);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //Update Height
                int value = (Integer) valueAnimator.getAnimatedValue();
                ViewGroup.LayoutParams layoutParams = collapsablelayout.getLayoutParams();
                layoutParams.height = value;
                collapsablelayout.setLayoutParams(layoutParams);
            }
        });
        return animator;
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(musicReceiver);
        if (tipWindow != null && tipWindow.isTooltipShown()) {
            tipWindow.dismissTooltip();
        }
        if (tipSpeakerWidow != null && tipSpeakerWidow.isTooltipShown()) {
            tipSpeakerWidow.dismissTooltip();
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            audioEffectPreferenceHandler.setIntensity(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        App.getPlayerEventHandler().setIntensityValue(audioEffectPreferenceHandler.getIntensity()/(double)100);
    }

    @Override
    public void onHeadsetUnplugged() {
//        mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_normal, null));
//        audioEffectPreferenceHandler.setEnableHeadsetPlugged(false);
    }

    @Override
    public void onHeadsetPlugged() {
//        mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_plugged, null));
//        audioEffectPreferenceHandler.setEnableHeadsetPlugged(true);
    }

    @Override
    public void updateEqualizerType(int position) {
        App.getPlayerEventHandler().setEqualizerGain(position);
    }
}
