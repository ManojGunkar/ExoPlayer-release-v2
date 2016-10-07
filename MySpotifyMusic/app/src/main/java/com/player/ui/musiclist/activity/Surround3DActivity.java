package com.player.ui.musiclist.activity;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.player.App;
import com.player.manager.MusicReceiver;
import com.player.myspotifymusic.R;
import com.player.ui.musiclist.adapter.EqualizerViewAdapter;
import com.player.ui.widgets.NegativeSeekBar;
import com.player.utils.decorations.SimpleDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.player.manager.MusicReceiver.HEADSET_PLUGGED;
import static com.player.manager.MusicReceiver.HEADSET_UNPLUGGED;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class Surround3DActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, MusicReceiver.updateMusic{
    ToggleButton mEffectPowerBtn;
    TextView switchTxt;
    private Toolbar toolbar;
    private FrameLayout collapsablelayout;
    private NegativeSeekBar intensity;
    private RecyclerView recyclerView;
    private EqualizerViewAdapter adapter;
    private MusicReceiver musicReceiver;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static boolean SPEAKER_ON = true;
    private static boolean SPEAKER_OFF = false;

    public static boolean EFFECT_POWER_ON = true;
    public static boolean EFFECT_POWER_OFF = false;

    public static int EQUALIZER_POSITION = 0;

    private ImageButton mSpeakerBtn;
    private ImageView mSpeakerLeftFront, mSpeakerRightFront, mTweeterLeft, mTweeterRight,
            mSpeakerLeftSurround, mSpeakerRightSurround, mSpeakerSubWoofer, mCenterMan;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_surround_3d);

        initView();
        setupToolbar();

    }

    private void setupToolbar() {
        toolbar = (Toolbar)findViewById(R.id.effect_toolbar);
        try {
            setSupportActionBar(toolbar);
        }catch (IllegalStateException e){}
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Effects");
        }
    }

    private void initView() {
        musicReceiver = new MusicReceiver(this);

        pref = App.getApplication().getSharedPreferences("AudioEffectSettings", MODE_PRIVATE);

        mEffectPowerBtn = (ToggleButton)findViewById(R.id.effect_power_btn);
        mEffectPowerBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editor = pref.edit();
                    editor.putBoolean("AUDIO_EFFECT_POWER", EFFECT_POWER_ON);
                    editor.commit();
                    switchTxt.setText("On");
                }else{
                    collapse();
                    editor = pref.edit();
                    editor.putBoolean("AUDIO_EFFECT_POWER", EFFECT_POWER_OFF);
                    editor.commit();
                    switchTxt.setText("Off");
                }
                if(adapter != null)
                    adapter.updateList();
            }
        });

        collapsablelayout = (FrameLayout) findViewById(R.id.collapsable_layout);
        collapsablelayout.setVisibility(View.GONE);
//        Surround3DLinesView view = new Surround3DLinesView(this, Color.BLACK);
//        collapsablelayout.addView(view);

        mSpeakerBtn = (ImageButton) findViewById(R.id.speaker_btn);
        mSpeakerBtn.setOnClickListener(this);

        mSpeakerLeftFront = (ImageView)findViewById(R.id.speaker_left_front);
        mSpeakerRightFront = (ImageView)findViewById(R.id.speaker_right_front);
        mTweeterLeft = (ImageView)findViewById(R.id.speaker_left_tweeter);
        mTweeterRight = (ImageView)findViewById(R.id.speaker_right_tweeter);
        mSpeakerLeftSurround = (ImageView)findViewById(R.id.speaker_left_surround);
        mSpeakerRightSurround = (ImageView)findViewById(R.id.speaker_right_surround);
        mSpeakerSubWoofer = (ImageView)findViewById(R.id.speaker_sub_woofer);
        mCenterMan = (ImageView)findViewById(R.id.speaker_center_man);

        mSpeakerLeftFront.setOnClickListener(this);
        mSpeakerRightFront.setOnClickListener(this);
        mTweeterLeft.setOnClickListener(this);
        mTweeterRight.setOnClickListener(this);
        mSpeakerLeftSurround.setOnClickListener(this);
        mSpeakerRightSurround.setOnClickListener(this);
        mSpeakerSubWoofer.setOnClickListener(this);

        intensity = (NegativeSeekBar) findViewById(R.id.intensity_seek);
        intensity.setProgress(50);
        intensity.setOnSeekBarChangeListener(this);

        switchTxt = (TextView) findViewById(R.id.switch_effect);
        recyclerView = (RecyclerView)findViewById(R.id.eq_recycler) ;
        fillEqualizer();
        updateSpeakers();
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

                final LinearLayoutManager llm = new LinearLayoutManager(Surround3DActivity.this, LinearLayoutManager.HORIZONTAL, true);
                llm.setReverseLayout(false);
                llm.setStackFromEnd(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(llm);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(Surround3DActivity.this, 0));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.scrollToPosition(0);
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
                        adapter = new EqualizerViewAdapter(Surround3DActivity.this, eq_names, eq_active_on, eq_active_off, eq_inactive_on, eq_inactive_off);
                        recyclerView.setAdapter(adapter);
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

        ValueAnimator mAnimator = slideAnimator(0, 800);
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
    public void onClick(View v) {
        boolean state;
        switch (v.getId()){
            case R.id.speaker_left_front:
                state = pref.getBoolean("speaker_left_front", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.off_left_front, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_front", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.on_left_front, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_front", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_right_front:
                state = pref.getBoolean("speaker_right_front", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.off_right_front, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_front", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.on_right_front, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_front", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_left_tweeter:
                state = pref.getBoolean("speaker_left_tweeter", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.off_lefttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_tweeter", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.on_lefttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_tweeter", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_right_tweeter:
                state = pref.getBoolean("speaker_right_tweeter", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.off_righttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_tweeter", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.on_righttweeter, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_tweeter", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_left_surround:
                state = pref.getBoolean("speaker_left_surround", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_left_surround, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_surround", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_left_surround, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_left_surround", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_right_surround:
                state = pref.getBoolean("speaker_right_surround", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_right_surround, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_surround", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_right_surround, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_right_surround", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_sub_woofer:
                state = pref.getBoolean("speaker_sub_woofer", SPEAKER_OFF);
                if (state == SPEAKER_ON) {
                    mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.off_subwoofer, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_sub_woofer", SPEAKER_OFF);
                    editor.commit();
                } else if (state == SPEAKER_OFF) {
                    mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.on_subwoofer, null));
                    editor = pref.edit();
                    editor.putBoolean("speaker_sub_woofer", SPEAKER_ON);
                    editor.commit();
                }
                break;
            case R.id.speaker_btn:
                if(collapsablelayout.getVisibility()==View.GONE) {
                    if (pref.getBoolean("AUDIO_EFFECT_POWER", EFFECT_POWER_OFF) == EFFECT_POWER_ON)
                        expand();
                }else {
                    collapse();
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        Log.e("Seek", "Working : "+progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void updateSpeakers(){
        boolean enable = pref.getBoolean("AUDIO_EFFECT_POWER", EFFECT_POWER_OFF);
        if(enable){
            switchTxt.setText("On");
            mEffectPowerBtn.setChecked(true);
        }else{
            switchTxt.setText("Off");
            mEffectPowerBtn.setChecked(false);
        }
        if(pref.getBoolean("speaker_left_front", SPEAKER_OFF) == SPEAKER_OFF){
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.off_left_front, null));
        }else if(pref.getBoolean("speaker_left_front", SPEAKER_OFF) == SPEAKER_ON){
            mSpeakerLeftFront.setImageDrawable(getResources().getDrawable(R.drawable.on_left_front, null));
        }
        if(pref.getBoolean("speaker_right_front", SPEAKER_OFF) == SPEAKER_OFF){
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.off_right_front, null));
        }else if(pref.getBoolean("speaker_right_front", SPEAKER_OFF) == SPEAKER_ON){
            mSpeakerRightFront.setImageDrawable(getResources().getDrawable(R.drawable.on_right_front, null));
        }
        if(pref.getBoolean("speaker_left_tweeter", SPEAKER_OFF) == SPEAKER_OFF){
            mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.off_lefttweeter, null));
        }else if(pref.getBoolean("speaker_left_tweeter", SPEAKER_OFF) == SPEAKER_ON){
            mTweeterLeft.setImageDrawable(getResources().getDrawable(R.drawable.on_lefttweeter, null));
        }
        if(pref.getBoolean("speaker_right_tweeter", SPEAKER_OFF) == SPEAKER_OFF){
            mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.off_righttweeter, null));
        }else if(pref.getBoolean("speaker_right_tweeter", SPEAKER_OFF) == SPEAKER_ON){
            mTweeterRight.setImageDrawable(getResources().getDrawable(R.drawable.on_righttweeter, null));
        }
        if(pref.getBoolean("speaker_left_surround", SPEAKER_OFF) == SPEAKER_OFF){
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_left_surround, null));
        }else if(pref.getBoolean("speaker_left_surround", SPEAKER_OFF) == SPEAKER_ON){
            mSpeakerLeftSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_left_surround, null));
        }
        if(pref.getBoolean("speaker_right_surround", SPEAKER_OFF) == SPEAKER_OFF){
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.off_right_surround, null));
        }else if(pref.getBoolean("speaker_right_surround", SPEAKER_OFF) == SPEAKER_ON){
            mSpeakerRightSurround.setImageDrawable(getResources().getDrawable(R.drawable.on_right_surround, null));
        }
        if(pref.getBoolean("speaker_sub_woofer", SPEAKER_OFF) == SPEAKER_OFF){
            mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.off_subwoofer, null));
        }else if(pref.getBoolean("speaker_sub_woofer", SPEAKER_OFF) == SPEAKER_ON){
            mSpeakerSubWoofer.setImageDrawable(getResources().getDrawable(R.drawable.on_subwoofer, null));
        }
        if(MusicReceiver.isPlugged){
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_plugged, null));
        }else{
            mCenterMan.setImageDrawable(getResources().getDrawable(R.drawable.man_normal, null));
        }
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
        editor.putBoolean("speaker_center_man", HEADSET_PLUGGED);
        editor.commit();
    }

    public enum equalizer{
        on,
        off,
    }

}
