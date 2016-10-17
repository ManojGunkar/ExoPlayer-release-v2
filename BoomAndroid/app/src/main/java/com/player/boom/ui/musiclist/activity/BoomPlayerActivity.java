package com.player.boom.ui.musiclist.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.player.boom.App;
import com.player.boom.handler.PlayingQueue.PlayerEventHandler;
import com.player.boom.R;
import com.player.boom.task.IPlayerUIEvent;
import com.player.boom.ui.widgets.CircleImageView;
import com.player.boom.ui.widgets.CircularSeekBar;
import com.player.boom.ui.widgets.CoverView.CircularCoverView;
import com.player.boom.ui.widgets.RegularTextView;
import com.player.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_POWER;
import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.example.openslplayer.AudioEffect.POWER_OFF;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener, CircularSeekBar.OnCircularSeekBarChangeListener{

    private RegularTextView mSongName;
    private CircularCoverView mAlbumArt;
    private CircularSeekBar mTrackSeek;
    private ImageView mPlayPauseBtn, mLibraryBtn, mAudioEffectBtn, mUpNextQueue;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static boolean isUser = false;
    public static boolean isPlayerResume = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_boom_player);

        Log.d("Screen Density : ", ""+ getResources().getDisplayMetrics().density);
        initViews();
    }

    public void initViews(){
        pref = App.getApplication().getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);
        PlayerEventHandler.getPlayerEventInstance(this).setPlayerUIEvent(event);

        mSongName = (RegularTextView) findViewById(R.id.media_item_name_txt);
        mTrackSeek = (CircularSeekBar)findViewById(R.id.track_seek);
        mAlbumArt = (CircularCoverView)findViewById(R.id.album_art);
        mPlayPauseBtn = (ImageView)findViewById(R.id.play_pause_btn);
        mLibraryBtn = (ImageView)findViewById(R.id.library_btn);
        mAudioEffectBtn = (ImageView)findViewById(R.id.audio_effect_btn);
        mUpNextQueue = (ImageView)findViewById(R.id.up_next_queue_btn);


        mPlayPauseBtn.setOnClickListener(this);
        mLibraryBtn.setOnClickListener(this);
        mAudioEffectBtn.setOnClickListener(this);
        mUpNextQueue.setOnClickListener(this);

//        mAudioEffectBtn.setOnLongClickListener(this);

        mTrackSeek.setOnCircularSeekBarChangeListener(this);
        mTrackSeek.setTouchInSide(false);

        mAlbumArt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.e("Player", "Touch");
                return true;
            }
        });

        updateEffectIcon();
    }

    IPlayerUIEvent event = new IPlayerUIEvent() {
        @Override
        public void updateSeek(int percent, long currentms, long totalms) {
            if(!isUser)
                mTrackSeek.setProgress(percent);
        }

        @Override
        public void updateUI() {
            updateTrackToPlayer();
        }

        @Override
        public void stop() {
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            mTrackSeek. setProgress(0);
            updateTrackToPlayer();
        }
    };

    public void updateTrackToPlayer(){
        FrameLayout.LayoutParams param;
        try {
            if(isPlayerResume) {
                if (PlayerEventHandler.getPlayerEventInstance(BoomPlayerActivity.this).getPlayingItem() != null &&
                        (PlayerEventHandler.getPlayerEventInstance(this).isPlaying() || PlayerEventHandler.getPlayerEventInstance(this).isPaused())) {
                    mSongName.setVisibility(View.VISIBLE);
                    mTrackSeek.setVisibility(View.VISIBLE);

                    if (isPathValid(PlayerEventHandler.getPlayerEventInstance(this).getPlayingItem().getItemArtUrl())) {
                        int size = Utils.dpToPx(this, 45);
                        Picasso.with(this).load(new File(PlayerEventHandler.getPlayerEventInstance(this).getPlayingItem().getItemArtUrl())).resize(mTrackSeek.getWidth() - size, mTrackSeek.getHeight() - size).centerCrop()
                                .error(getResources().getDrawable(R.drawable.default_album_art_home)).into(mAlbumArt);
                    } else {
                        mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art_home));
                    }
                    mSongName.setText(PlayerEventHandler.getPlayerEventInstance(BoomPlayerActivity.this).getPlayingItem().getItemTitle());
                    if (PlayerEventHandler.getPlayerEventInstance(this).isPlaying()) {
                        mPlayPauseBtn.setVisibility(View.VISIBLE);
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
                    } else {
                        mPlayPauseBtn.setVisibility(View.VISIBLE);
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
                    }
                } else {
                    param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    param.gravity = Gravity.CENTER;
                    mAlbumArt.setLayoutParams(param);
                    mSongName.setVisibility(View.GONE);
                    mTrackSeek.setVisibility(View.INVISIBLE);
                    mPlayPauseBtn.setVisibility(View.INVISIBLE);
                    mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.no_song_selected));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.audio_effect_btn:
                startEffectActivity();
                break;
        }
        return false;
    }

    private void startEffectActivity(){
        Intent queueIntent = new Intent(BoomPlayerActivity.this, Surround3DActivity.class);
        startActivity(queueIntent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                int play = PlayerEventHandler.getPlayerEventInstance(this).PlayPause();
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
                startEffectActivity();
                /*boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
                editor = pref.edit();
                if(isPowerOn) {
                    mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
                }else{
                    mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
                    editor.putBoolean(AUDIO_EFFECT_POWER, POWER_ON);
                }
                editor.commit();*/
                break;
            /*case R.id.nextTrack:
                PlayerEventHandler.getPlayerEventInstance(this).next();
                break;
            case R.id.stopTrack:
                PlayerEventHandler.getPlayerEventInstance(this).stop();
                break;
            case R.id.prevTrack:
                PlayerEventHandler.getPlayerEventInstance(this).previous();
                break;*/
            default:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerResume = true;
        updateEffectIcon();
        updateTrackToPlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isPlayerResume = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.getPlayingQueueHandler().getPlayingQueue().finishTrack(false);
    }

    private void updateEffectIcon() {
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        if(isPowerOn) {
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
        }else{
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
        }
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if(fromUser && PlayerEventHandler.getPlayerEventInstance(BoomPlayerActivity.this).isPlaying()) {
            circularSeekBar.setProgress(progress);
            isUser = true;
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar circularSeekBar) {

    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar circularSeekBar) {
        if(PlayerEventHandler.getPlayerEventInstance(BoomPlayerActivity.this).isPlaying()) {
            PlayerEventHandler.getPlayerEventInstance(BoomPlayerActivity.this).seek(circularSeekBar.getProgress());
            isUser = false;
        }
    }
}
