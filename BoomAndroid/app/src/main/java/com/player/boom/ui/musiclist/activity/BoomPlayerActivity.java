package com.player.boom.ui.musiclist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.handler.PlayingQueue.PlayerEventHandler;
import com.player.boom.R;
import com.player.boom.handler.IPlayerUIEvent;
import com.player.boom.task.PlayerService;
import com.player.boom.ui.widgets.CircularSeekBar;
import com.player.boom.ui.widgets.CoverView.CircularCoverView;
import com.player.boom.ui.widgets.RegularTextView;
import com.player.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;

import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_POWER;
import static com.example.openslplayer.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.example.openslplayer.AudioEffect.EFFECT_DEFAULT_POWER;
import static com.player.boom.handler.PlayingQueue.PlayerEventHandler.PlayState.play;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener, CircularSeekBar.OnCircularSeekBarChangeListener{

    private RegularTextView mSongName;
    private CircularCoverView mAlbumArt;
    private CircularSeekBar mTrackSeek;
    private ImageView mPlayPauseBtn, mLibraryBtn, mAudioEffectBtn, mUpNextQueue;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    private static boolean isUser = false;
    public static boolean isPlayerResume = true;
    public static final String ACTION_RECEIVE_SONG = "ACTION_RECEIVE_SONG";
    public static final String ACTION_ITEM_CLICKED = "ACTION_ITEM_CLICKED";
    public static final String ACTION_TRACK_STOPPED = "ACTION_TRACK_STOPPED";
    public static final String ACTION_UPDATE_TRACK_SEEK = "ACTION_UPDATE_TRACK_SEEK";


    FrameLayout.LayoutParams param;
    private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()){
                case ACTION_RECEIVE_SONG :
                    MediaItem item = intent.getParcelableExtra("playing_song");
                    if(item != null){
                        updateTrackToPlayer(item, intent.getBooleanExtra("playing", false));
                    }
                    break;
                case ACTION_ITEM_CLICKED :
                    if(intent.getBooleanExtra("play_pause", false) == false){
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
                    }else{
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
                    }
                    break;
                case ACTION_TRACK_STOPPED :
                    updateTrackToPlayer(null, false);
                    break;
                case ACTION_UPDATE_TRACK_SEEK :
                    mTrackSeek.setProgress(intent.getIntExtra("percent", 0));
                    break;
            }
        }
    };

    private void updateTrackToPlayer(MediaItem item, boolean playing) {
        if(item != null){
            mSongName.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);

            if (isPathValid(item.getItemArtUrl())) {
                int size = Utils.dpToPx(this, 45);
                Picasso.with(this).load(new File(item.getItemArtUrl())).resize(mTrackSeek.getWidth() - size, mTrackSeek.getHeight() - size).centerCrop()
                        .error(getResources().getDrawable(R.drawable.default_album_art_home)).into(mAlbumArt);
            } else {
                mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.default_album_art_home));
            }
            mSongName.setText(item.getItemTitle());
            if (App.getPlayerEventHandler().isPlaying()) {
                mPlayPauseBtn.setVisibility(View.VISIBLE);
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause, null));
            } else {
                mPlayPauseBtn.setVisibility(View.VISIBLE);
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_play, null));
            }
        }else{
            param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            param.gravity = Gravity.CENTER;
            mAlbumArt.setLayoutParams(param);
            mSongName.setVisibility(View.GONE);
            mTrackSeek.setVisibility(View.INVISIBLE);
            mPlayPauseBtn.setVisibility(View.INVISIBLE);
            mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.no_song_selected));
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_boom_player);

        Log.d("Screen Density : ", ""+ getResources().getDisplayMetrics().density);
        initViews();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        registerReceiver(mPlayerBroadcastReceiver, intentFilter);
    }

    public void initViews(){
        pref = App.getApplication().getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);

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

        mAudioEffectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return false;
            }
        });

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

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    private void startEffectActivity(){
        Intent queueIntent = new Intent(BoomPlayerActivity.this, Surround3DActivity.class);
        startActivity(queueIntent);
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
    }

    private void startUpNextActivity() {
        Intent queueIntent = new Intent(BoomPlayerActivity.this, PlayingQueueActivity.class);
        startActivity(queueIntent);
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
    }

    private void startLibraryActivity() {
        Intent listIntent = new Intent(BoomPlayerActivity.this, DeviceMusicActivity.class);
        startActivity(listIntent);
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE_SONG));
                break;
            case R.id.up_next_queue_btn:
                startUpNextActivity();
                break;
            case R.id.library_btn:
                startLibraryActivity();
                break;
            case R.id.audio_effect_btn:
                startEffectActivity();
                break;
            default:

                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerResume = true;
        updateEffectIcon();
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
        unregisterReceiver(mPlayerBroadcastReceiver);
    }

    private void updateEffectIcon() {
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, EFFECT_DEFAULT_POWER);
        if(isPowerOn) {
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
        }else{
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
        }
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if(fromUser && App.getPlayerEventHandler().isPlaying()) {
            circularSeekBar.setProgress(progress);
            isUser = true;
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar circularSeekBar) {

    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar circularSeekBar) {
        if(App.getPlayerEventHandler().isPlaying()) {
            Intent intent = new Intent(PlayerService.ACTION_SEEK_SONG);
            intent.putExtra("seek", circularSeekBar.getProgress());
            sendBroadcast(intent);
            isUser = false;
        }
    }
}
