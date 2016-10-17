package com.player.boom.ui.musiclist.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import com.example.openslplayer.AudioEffect;
import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.handler.PlayingQueue.PlayerEventHandler;
import com.player.boom.manager.MusicReceiver;
import com.player.boom.ui.musiclist.adapter.EqualizerViewAdapter;
import com.player.boom.ui.widgets.NegativeSeekBar;
import com.player.boom.ui.widgets.ScrollEnableLayoutManager;
import com.player.boom.ui.widgets.RegularTextView;
import com.player.boom.utils.decorations.SimpleDividerItemDecoration;

import java.util.Arrays;
import java.util.List;

import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_POWER;
import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.example.openslplayer.AudioEffect.EQUALIZER_POSITION;
import static com.example.openslplayer.AudioEffect.EQUALIZER_POWER;
import static com.example.openslplayer.AudioEffect.FULL_BASS;
import static com.example.openslplayer.AudioEffect.HEADSET_PLUGGED_INFO;
import static com.example.openslplayer.AudioEffect.INTENSITY_POSITION;
import static com.example.openslplayer.AudioEffect.INTENSITY_POWER;
import static com.example.openslplayer.AudioEffect.POWER_OFF;
import static com.example.openslplayer.AudioEffect.POWER_ON;
import static com.example.openslplayer.AudioEffect.SELECTED_EQUALIZER_POSITION;
import static com.example.openslplayer.AudioEffect.SPEAKER_LEFT_FRONT;
import static com.example.openslplayer.AudioEffect.SPEAKER_LEFT_SURROUND;
import static com.example.openslplayer.AudioEffect.SPEAKER_POWER;
import static com.example.openslplayer.AudioEffect.SPEAKER_RIGHT_FRONT;
import static com.example.openslplayer.AudioEffect.SPEAKER_RIGHT_SURROUND;
import static com.example.openslplayer.AudioEffect.SPEAKER_SUB_WOOFER;
import static com.example.openslplayer.AudioEffect.SPEAKER_TWEETER;
import static com.example.openslplayer.AudioEffect.THREE_D_SURROUND_POWER;
import static com.player.boom.manager.MusicReceiver.HEADSET_PLUGGED;
import static com.player.boom.manager.MusicReceiver.HEADSET_UNPLUGGED;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class Surround3DActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MusicReceiver.updateMusic, CompoundButton.OnCheckedChangeListener, EqualizerViewAdapter.onEqualizerUpdate {
    private RegularTextView mToolbarTitle, mEffectTxt, mEffectSwitchTxt, m3DTxt, m3DSwitchTxt, mSpeakerInfo, mFullbassTxt,
            mIntensityTxt, mIntensitySwitchTxt, mEqualizerTxt, mEqualizerSwitchTxt;
    private LinearLayout mIntensityIndicator, mEqualizerIndicator, mFullBassPanel;
    private /*ToggleButton*/ ImageView mEffectPowerBtn;
    private ImageView m3DSwitchBtn, mIntensitySwitchBtn, mEqualizerSwitchBtn, mSpeakerSwitchBtn, mFullBassRd;
    private ImageView mSpeakerLeftFront, mSpeakerRightFront, mTweeterLeft, mTweeterRight,
            mSpeakerLeftSurround, mSpeakerRightSurround, mSpeakerSubWoofer, mCenterMan;
    private Toolbar toolbar;
    private LinearLayout collapsablelayout, mEffectSwitchPanel;
    private NegativeSeekBar mIntensitySeek;
    private RecyclerView recyclerView;
    private ScrollEnableLayoutManager layoutManager;
    private EqualizerViewAdapter mEqualizerAdapter;
    private MusicReceiver musicReceiver;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private boolean isExpended = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surround_3d);

        initViews();
        setupToolbar();
        fillEqualizer();

        onPowerSwitchUpdate();
        update3DSurround();
        updateIntensity();
        updateEqualizer();
    }

    public void initViews(){

        musicReceiver = new MusicReceiver(this);

        pref = App.getApplication().getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);

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

        mEffectPowerBtn = (ImageView) findViewById(R.id.effect_power_btn);//onclick
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
        collapsablelayout = (LinearLayout) findViewById(R.id.collapsable_layout);
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
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        editor = pref.edit();
        if(isChecked){
            editor.putBoolean(AUDIO_EFFECT_POWER, POWER_ON);
        }else{
            editor.putBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        }
        editor.commit();
        onPowerSwitchUpdate();
        update3DSurround();
        updateIntensity();
        updateEqualizer();
    }

    public void onPowerSwitchUpdate(){
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        if(isPowerOn) {
            mEffectSwitchTxt.setText("on");
            /*mEffectPowerBtn.setChecked(true);*/
            mEffectPowerBtn.setImageDrawable(getResources().getDrawable(R.drawable.toggle_on, null));
            mEffectTxt.setTextColor(Color.WHITE);
            mEffectSwitchTxt.setTextColor(Color.WHITE);
        }else{
            mEffectSwitchTxt.setText("off");
            /*mEffectPowerBtn.setChecked(false);*/
            mEffectPowerBtn.setImageDrawable(getResources().getDrawable(R.drawable.toggle_off, null));
            mEffectTxt.setTextColor(Color.WHITE);
            mEffectSwitchTxt.setTextColor(Color.WHITE);
            collapse();
        }
        PlayerEventHandler.getPlayerEventInstance(this).setEffectEnable(!isPowerOn);
    }

    public void update3DSurround(){
        boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);

        if(isPowerOn && is3DOn){
            m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_surround, null));
            m3DSwitchTxt.setText("ON");
            m3DTxt.setTextColor(Color.WHITE);
            m3DSwitchTxt.setTextColor(Color.WHITE);
        }else{
            m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_surround_off, null));
            m3DSwitchTxt.setText("OFF");
            m3DTxt.setTextColor(Color.WHITE);
            m3DSwitchTxt.setTextColor(Color.WHITE);
            mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_speakers_off, null));
        }
        updateSpeakerInfo();
        updateFullBass();
        updateSpeakers();
    }

    public void updateSpeakerInfo(){
        boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);

        boolean isLeftFront = pref.getBoolean(SPEAKER_LEFT_FRONT, POWER_OFF);
        boolean isRightFront = pref.getBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF);
        boolean isLeftSurround = pref.getBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF);
        boolean isRightSurround = pref.getBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF);
        boolean isWoofer = pref.getBoolean(SPEAKER_SUB_WOOFER, POWER_OFF);
        boolean isTweeter = pref.getBoolean(SPEAKER_TWEETER, POWER_OFF);
        if(isPowerOn && is3DOn) {
            if(isLeftFront && isRightFront && isLeftSurround && isRightSurround && isWoofer && isTweeter){
                mSpeakerInfo.setVisibility(View.GONE);// All Speakers are on
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_speakers, null));
            }else if (!isLeftFront && !isRightFront && !isLeftSurround && !isRightSurround && !isWoofer && !isTweeter) {
                // All Speakers are off
                mSpeakerInfo.setText("All speakers are Off");
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// active color
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_speakers, null));
            }else {
                // Some Speakers are off
                mSpeakerInfo.setText("Some speakers are Off");
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// active color
                mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_speakers, null));
            }
        }else{
            if(isLeftFront && isRightFront && isLeftSurround && isRightSurround && isWoofer && isTweeter){
                mSpeakerInfo.setVisibility(View.GONE);// All Speakers are on
            }else if (!isLeftFront && !isRightFront && !isLeftSurround && !isRightSurround && !isWoofer && !isTweeter) {
                // All Speakers are off
                mSpeakerInfo.setText("All speakers are Off");
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// inactive color
            }else {
                // Some Speakers are off
                mSpeakerInfo.setText("Some speakers are Off");
                mSpeakerInfo.setVisibility(View.VISIBLE);
                mSpeakerInfo.setTextColor(Color.WHITE);// inactive color
            }
            mSpeakerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.three_d_speakers_off, null));
        }
    }

    public void updateFullBass(){
        boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        boolean isFullBassOn = pref.getBoolean(FULL_BASS, POWER_OFF);

        if(is3DOn && isPowerOn){
            if(isFullBassOn){
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_on, null));
            }else{
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_off, null));
            }
            mFullbassTxt.setTextColor(Color.WHITE);
        }else{
            if(isFullBassOn){
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_inactive_on, null));
            }else{
                mFullBassRd.setImageDrawable(getResources().getDrawable(R.drawable.full_bass_inactive_off, null));
            }
            mFullbassTxt.setTextColor(Color.WHITE);
        }
        PlayerEventHandler.getPlayerEventInstance(this).setSuperBassEnable(isFullBassOn);
    }

    public void updateSpeakers(){
        if(pref.getBoolean(SPEAKER_LEFT_FRONT, POWER_OFF) == POWER_OFF){
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.off_left_front, null));
        }else if(pref.getBoolean(SPEAKER_LEFT_FRONT, POWER_OFF) == POWER_ON){
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.on_left_front, null));
        }
        if(pref.getBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF) == POWER_OFF){
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.off_right_front, null));
        }else if(pref.getBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF) == POWER_ON){
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.on_right_front, null));
        }
        if(pref.getBoolean(SPEAKER_TWEETER, POWER_OFF) == POWER_OFF){
            mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.off_lefttweeter, null));
            mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.off_righttweeter, null));
        }else if(pref.getBoolean(SPEAKER_TWEETER, POWER_OFF) == POWER_ON){
            mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.on_lefttweeter, null));
            mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.on_righttweeter, null));
        }
        if(pref.getBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF) == POWER_OFF){
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_left_surround, null));
        }else if(pref.getBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF) == POWER_ON){
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_left_surround, null));
        }
        if(pref.getBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF) == POWER_OFF){
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_right_surround, null));
        }else if(pref.getBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF) == POWER_ON){
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_right_surround, null));
        }
        if(pref.getBoolean(SPEAKER_SUB_WOOFER, POWER_OFF) == POWER_OFF){
            mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.off_subwoofer, null));
        }else if(pref.getBoolean(SPEAKER_SUB_WOOFER, POWER_OFF) == POWER_ON){
            mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.on_subwoofer, null));
        }
        if(MusicReceiver.isPlugged){
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_plugged, null));
        }else{
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_normal, null));
        }
    }

    public void updateIntensity(){
        boolean isIntensityOn = pref.getBoolean(INTENSITY_POWER, POWER_OFF);
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);

        if(isPowerOn && isIntensityOn){
            mIntensitySwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.intensity, null));
            mIntensitySwitchTxt.setText("ON");
            mIntensitySwitchTxt.setTextColor(Color.WHITE);
            mIntensityTxt.setTextColor(Color.WHITE);
            EnableSeek(true);
//            mIntensitySeek.setEnabled(true);
        }else{
            mIntensitySwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.intensity_off, null));
            mIntensitySwitchTxt.setText("OFF");
            mIntensitySwitchTxt.setTextColor(Color.WHITE);
            mIntensityTxt.setTextColor(Color.WHITE);
//            mIntensitySeek.setEnabled(false);
            EnableSeek(false);
        }
        mIntensitySeek.setProgress(pref.getInt(INTENSITY_POSITION, 50));
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
        boolean isEqualizerOn = pref.getBoolean(EQUALIZER_POWER, POWER_OFF);
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);

        if(isPowerOn && isEqualizerOn){
            mEqualizerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.equalizer, null));
            mEqualizerSwitchTxt.setText("ON");
            mEqualizerSwitchTxt.setTextColor(Color.WHITE);
            mEqualizerTxt.setTextColor(Color.WHITE);
        }else{
            mEqualizerSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.equalizer_off, null));
            mEqualizerSwitchTxt.setText("OFF");
            mEqualizerSwitchTxt.setTextColor(Color.WHITE);
            mEqualizerTxt.setTextColor(Color.WHITE);
        }
        if(mEqualizerAdapter != null)
            mEqualizerAdapter.updateList();
    }

    public void switch3DSurround(boolean isPowerOn){
        if(isPowerOn) {
            boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
            editor = pref.edit();
            if (is3DOn) {
                editor.putBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
                collapse();
            } else {
                editor.putBoolean(THREE_D_SURROUND_POWER, POWER_ON);
            }
            editor.commit();
            update3DSurround();
            PlayerEventHandler.getPlayerEventInstance(this).set3DAudioEnable(!is3DOn);
            if(pref.getBoolean(INTENSITY_POWER, POWER_OFF) == POWER_OFF){
                switchIntensity(isPowerOn);
            }
            if(pref.getBoolean(EQUALIZER_POWER, POWER_OFF) == POWER_OFF){
                switchEqualizer(isPowerOn);
            }
        }
    }

    public void switchIntensity(boolean isPowerOn){
        if((pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF) == POWER_ON) &&
                pref.getBoolean(INTENSITY_POWER, POWER_OFF) == POWER_ON){
            onTextViewTurn(mIntensityIndicator);
            return;
        }
        if(isPowerOn) {
            boolean isIntensityOn = pref.getBoolean(INTENSITY_POWER, POWER_OFF);
            editor = pref.edit();
            if (isIntensityOn) {
                editor.putBoolean(INTENSITY_POWER, POWER_OFF);
            } else {
                editor.putBoolean(INTENSITY_POWER, POWER_ON);
            }
            editor.commit();
            updateIntensity();
        }
    }

    public void switchEqualizer(boolean isPowerOn){
        if((pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF) == POWER_ON) &&
                pref.getBoolean(EQUALIZER_POWER, POWER_OFF) == POWER_ON){
            onTextViewTurn(mEqualizerIndicator);
            return;
        }
        if(isPowerOn) {
            boolean isEqualizerOn = pref.getBoolean(EQUALIZER_POWER, POWER_OFF);
            editor = pref.edit();
            if (isEqualizerOn) {
                editor.putBoolean(EQUALIZER_POWER, POWER_OFF);
            } else {
                editor.putBoolean(EQUALIZER_POWER, POWER_ON);
            }
            editor.commit();
            updateEqualizer();
            PlayerEventHandler.getPlayerEventInstance(this).setEqualizerEnable(!isEqualizerOn);
        }
    }

    public void switchSuperPass(boolean isPowerOn){
        boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
        if(isPowerOn && is3DOn) {
            editor = pref.edit();
            if(pref.getBoolean(FULL_BASS, POWER_OFF) == POWER_ON){
                editor.putBoolean(FULL_BASS, POWER_OFF);
            }else{
                editor.putBoolean(FULL_BASS, POWER_ON);
            }
            editor.commit();
            updateFullBass();
        }
    }

    @Override
    public void onClick(View v) {
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        switch (v.getId()){
            case R.id.effect_switch_panel:
                editor = pref.edit();
                if(isPowerOn){
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
//                    m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.toggle_off, null));
                }else{
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_ON);
//                    m3DSwitchBtn.setImageDrawable(getResources().getDrawable(R.drawable.toggle_on, null));
                }
                editor.commit();
                onPowerSwitchUpdate();
                update3DSurround();
                updateIntensity();
                updateEqualizer();
                break;
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
                boolean isSpeakerOn = pref.getBoolean(SPEAKER_POWER, POWER_OFF);
                boolean is3DOn = pref.getBoolean(THREE_D_SURROUND_POWER, POWER_OFF);
                if(isPowerOn && is3DOn) {
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
                if (pref.getBoolean(SPEAKER_LEFT_FRONT, POWER_OFF) == POWER_ON) {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.off_left_front, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_LEFT_FRONT, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.FrontLeft, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_LEFT_FRONT, POWER_OFF) == POWER_OFF)*/ {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.on_left_front, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_LEFT_FRONT, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.FrontLeft, POWER_ON);
                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_right_front:
                if (pref.getBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF) == POWER_ON) {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.off_right_front, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.FrontRight, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_RIGHT_FRONT, POWER_OFF) == POWER_OFF)*/ {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.on_right_front, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_RIGHT_FRONT, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.FrontRight, POWER_ON);
                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_left_tweeter:
            case R.id.speaker_right_tweeter:
                if (pref.getBoolean(SPEAKER_TWEETER, POWER_OFF) == POWER_ON) {
                    mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.off_lefttweeter, null));
                    mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.off_righttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_TWEETER, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.Tweeter, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_TWEETER, POWER_OFF) == POWER_OFF)*/ {
                    mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.on_lefttweeter, null));
                    mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.on_righttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_TWEETER, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.Tweeter, POWER_ON);
                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_left_surround:
                if (pref.getBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF) == POWER_ON) {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_left_surround, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.RearLeft, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_LEFT_SURROUND, POWER_OFF) == POWER_OFF)*/ {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_left_surround, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_LEFT_SURROUND, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.RearLeft, POWER_ON);
                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_right_surround:
                if (pref.getBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF) == POWER_ON) {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_right_surround, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.RearRight, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_RIGHT_SURROUND, POWER_OFF) == POWER_OFF)*/ {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_right_surround, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_RIGHT_SURROUND, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.RearRight, POWER_ON);
                }
                updateSpeakerInfo();
                break;
            case R.id.speaker_sub_woofer:
                if (pref.getBoolean(SPEAKER_SUB_WOOFER, POWER_OFF) == POWER_ON) {
                    mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.off_subwoofer, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_SUB_WOOFER, POWER_OFF);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.Woofer, POWER_OFF);
                } else /*if (pref.getBoolean(SPEAKER_SUB_WOOFER, POWER_OFF) == POWER_OFF)*/ {
                    mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.on_subwoofer, null));
                    editor = pref.edit();
                    editor.putBoolean(SPEAKER_SUB_WOOFER, POWER_ON);
                    editor.commit();
                    PlayerEventHandler.getPlayerEventInstance(this).setSpeakerEnable(AudioEffect.Speaker.Woofer, POWER_ON);
                }
                updateSpeakerInfo();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
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
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(Surround3DActivity.this, 0));
                        recyclerView.setHasFixedSize(true);
                        int pos = pref.getInt(SELECTED_EQUALIZER_POSITION, EQUALIZER_POSITION);
                        recyclerView.scrollToPosition(pos);
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
        collapsablelayout.setVisibility(View.VISIBLE);

        final int widthSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        final int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        collapsablelayout.measure(widthSpec, heightSpec);
        int height = collapsablelayout.getMeasuredHeight();
        Log.d("Height : ", ""+height);

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
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        editor = pref.edit();
        mIntensitySeek.setProgress(pref.getInt(INTENSITY_POSITION, 50));
        editor.putInt(INTENSITY_POSITION, progress);
        editor.commit();
        PlayerEventHandler.getPlayerEventInstance(this).setIntensityValue(progress/100);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onHeadsetUnplugged() {
        mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_normal, null));
        editor = pref.edit();
        editor.putBoolean("speaker_center_man", HEADSET_UNPLUGGED);
        editor.commit();
    }

    @Override
    public void onHeadsetPlugged() {
        mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_plugged, null));
        editor = pref.edit();
        editor.putBoolean(HEADSET_PLUGGED_INFO, HEADSET_PLUGGED);
        editor.commit();
    }

    @Override
    public void updateEqualizerType(int position) {
        PlayerEventHandler.getPlayerEventInstance(this).setEqualizerGain(position);
    }
}
