package com.player.ui.musiclist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.player.App;
import com.player.data.PlayingQueue.PlayerEventHandler;
import com.player.myspotifymusic.R;
import com.player.ui.UIEvent;
import com.player.ui.widgets.CircleImageView;
import com.player.ui.widgets.CircularSeekBar;
import com.player.ui.widgets.RegularTextView;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.player.manager.AudioEffect.AUDIO_EFFECT_POWER;
import static com.player.manager.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.player.manager.AudioEffect.POWER_OFF;
import static com.player.manager.AudioEffect.POWER_ON;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CircularSeekBar.OnCircularSeekBarChangeListener{

    private RegularTextView mSongName;
    private CircleImageView mAlbumArt;
    private CircularSeekBar mTrackSeek;
    private ImageView mPlayPauseBtn, mLibraryBtn, mAudioEffectBtn, mUpNextQueue;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_boom_player);

        initViews();
    }

    public void initViews(){
        pref = App.getApplication().getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);
        PlayerEventHandler.getQueueEventInstance(this).setUIEvent(event);

        mSongName = (RegularTextView) findViewById(R.id.media_item_name_txt);
        mTrackSeek = (CircularSeekBar)findViewById(R.id.track_seek);
        mAlbumArt = (CircleImageView)findViewById(R.id.album_art);
        mPlayPauseBtn = (ImageView)findViewById(R.id.play_pause_btn);
        mLibraryBtn = (ImageView)findViewById(R.id.library_btn);
        mAudioEffectBtn = (ImageView)findViewById(R.id.audio_effect_btn);
        mUpNextQueue = (ImageView)findViewById(R.id.up_next_queue_btn);


        mPlayPauseBtn.setOnClickListener(this);
        mLibraryBtn.setOnClickListener(this);
        mAudioEffectBtn.setOnClickListener(this);
        mUpNextQueue.setOnClickListener(this);

        mAudioEffectBtn.setOnLongClickListener(this);

        mTrackSeek.setOnCircularSeekBarChangeListener(this);

        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        if(isPowerOn) {
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
        }else{
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
        }
    }

    UIEvent event = new UIEvent() {
        @Override
        public void updateSeek(int percent, long currentms, long totalms) {
            mTrackSeek.setProgress(percent);
        }

        @Override
        public void updateUI() {
            if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying()){
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
            }else{
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            }
            updateAlbumArt();
        }

        @Override
        public void stop() {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            mTrackSeek. setProgress(0);
        }
    };

    public void updateAlbumArt(){
        FrameLayout.LayoutParams param;
        if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).getPlayingItem() != null){
            mSongName.setVisibility(View.VISIBLE);
            param = new FrameLayout.LayoutParams(mTrackSeek.getWidth(), mTrackSeek.getHeight());
            param.gravity = Gravity.CENTER;
            mAlbumArt.setPadding(40,40,40,40);
            mAlbumArt.setLayoutParams(param);
            Picasso.with(this).load(new File(PlayerEventHandler.getQueueEventInstance(this).getPlayingItem().getItemArtUrl()))
                    .memoryPolicy(MemoryPolicy.NO_CACHE).error(getResources().getDrawable(R.drawable.default_album_art_home)).noFade().into(mAlbumArt);
            mSongName.setText(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).getPlayingItem().getItemTitle());
        }else{
            param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            param.gravity = Gravity.CENTER;
            mAlbumArt.setLayoutParams(param);
            mSongName.setVisibility(View.GONE);
            mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art_home));
        }
    }
    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.audio_effect_btn:
                Intent queueIntent = new Intent(BoomPlayerActivity.this, Surround3DActivity.class);
                startActivity(queueIntent);
                break;
        }
        return false;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                int play = PlayerEventHandler.getQueueEventInstance(this).Play();
                if(play == 0){
                    mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
                }else if(play == 1){
                    mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
                }
                break;
            case R.id.up_next_queue_btn:
                Intent queueIntent = new Intent(BoomPlayerActivity.this, PlayingQueueActivity.class);
                startActivity(queueIntent);
                break;
            case R.id.library_btn:
                Intent listIntent = new Intent(BoomPlayerActivity.this, DeviceMusicActivity.class);
                startActivity(listIntent);
                break;
            case R.id.audio_effect_btn:
                boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
                editor = pref.edit();
                if(isPowerOn) {
                    mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
                }else{
                    mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_ON);
                }
                editor.commit();
                break;
            /*case R.id.nextTrack:
                PlayerEventHandler.getQueueEventInstance(this).next();
                break;
            case R.id.stopTrack:
                PlayerEventHandler.getQueueEventInstance(this).stop();
                break;
            case R.id.prevTrack:
                PlayerEventHandler.getQueueEventInstance(this).previous();
                break;*/
            default:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(PlayerEventHandler.getQueueEventInstance(this).isPlaying()){
            mPlayPauseBtn.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
        }else if(PlayerEventHandler.getQueueEventInstance(this).isPaused()){
            mPlayPauseBtn.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
        }else if(PlayerEventHandler.getQueueEventInstance(this).isStopped()){
            mPlayPauseBtn.setVisibility(View.INVISIBLE);
            mTrackSeek.setVisibility(View.INVISIBLE);
        }
        updateAlbumArt();
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if(fromUser && PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying()) {
            circularSeekBar.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar circularSeekBar) {

    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar circularSeekBar) {
        if(PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).isPlaying())
            PlayerEventHandler.getQueueEventInstance(BoomPlayerActivity.this).seek(circularSeekBar.getProgress());
    }
}
