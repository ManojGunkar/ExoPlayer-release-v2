package com.globaldelight.boom.ui.musiclist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.manager.MusicReceiver;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.widgets.CircularSeekBar;
import com.globaldelight.boom.ui.widgets.CoverView.CircularCoverView;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boom.ui.widgets.TooltipWindow;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.async.Action;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import static com.globaldelight.boom.task.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener, CircularSeekBar.OnCircularSeekBarChangeListener, MusicReceiver.updateMusic {

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 25.0f;
    private static final String TAG = "BoomPlayerActivity";
    public static boolean isPlayerResume = true;
    private static boolean isUser = false;
    public ImageView mShuffleBtn, mRepeatBtn, mNextBtn, mPrevBtn, mAddToPlayList, mFavourite, mPlayerSetting;
    FrameLayout mPlayerBackground;
    LinearLayout mPlayerRootView;
    AudioEffect audioEffectPreferenceHandler;
    FrameLayout.LayoutParams param;
    MusicReceiver musicReceiver;
    private RegularTextView mTitleTxt, mSubTitleTxt, mPlayedTime, mRemainsTime;
    private CircularCoverView mAlbumArt;
    private CircularSeekBar mTrackSeek;
    private ImageView mPlayPauseBtn, mLibraryBtn, mAudioEffectBtn, mUpNextQueue;
    private TooltipWindow tipWindowLibrary, tipWindowEffect, tipWindowHold, tipWindowHeadset;
    private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaItem item;
            switch (intent.getAction()){
                case ACTION_RECEIVE_SONG :
                    item = intent.getParcelableExtra("playing_song");
                    if(item != null){
                        updateTrackToPlayer(item, intent.getBooleanExtra("playing", false), false);
                    }
//                    boolean prev_enable = intent.getBooleanExtra("is_previous", false);
//                    boolean next_enable = intent.getBooleanExtra("is_next", false);
//                    updatePreviousNext(prev_enable, next_enable);

                    break;
                case ACTION_LAST_PLAYED_SONG:
                    item = intent.getParcelableExtra("playing_song");
                    updateTrackToPlayer(item, false, intent.getBooleanExtra("last_played_song", true));
                    break;
                case ACTION_ITEM_CLICKED :
                    if(intent.getBooleanExtra("play_pause", false) == false){
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_play, null));
                    }else{
                        mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_pause, null));
                    }
                    break;
                case ACTION_TRACK_STOPPED :
                    updateTrackToPlayer(null, false, false);
                    break;
                case ACTION_UPDATE_TRACK_SEEK :
                    if(!isUser)
                        mTrackSeek.setProgress(intent.getIntExtra("percent", 0));

                    long totalMillis = intent.getLongExtra("totalms", 0);
                    long currentMillis = intent.getLongExtra("currentms", 0);
                    mPlayedTime.setText(String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(currentMillis),
                            TimeUnit.MILLISECONDS.toSeconds(currentMillis ) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentMillis))));
                    mRemainsTime.setText("-"+String.format("%02d:%02d",
                            TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis),
                            TimeUnit.MILLISECONDS.toSeconds(totalMillis - currentMillis) -
                                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis))));
                    break;
                case ACTION_UPDATE_SHUFFLE:
                    updateShuffle();
                    break;
                case ACTION_UPDATE_REPEAT :
                    updateRepeat();
                    break;
            }
        }
    };



    private void updateShuffle(){
        switch (App.getUserPreferenceHandler().getShuffle()){
            case none:
                mShuffleBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_off));
                break;
            case all:
                mShuffleBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_shuffle_on));
                break;
        }
    }

    private void updateRepeat(){
        switch (App.getUserPreferenceHandler().getRepeat()){
            case none:
                mRepeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_off));
                break;
            case one:
                mRepeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_one));
                break;
            case all:
                mRepeatBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_repeat_all));
                break;
        }
    }

    private void updatePreviousNext(boolean prev_enable, boolean next_enable){
        if(prev_enable){
            mPrevBtn.setVisibility(View.VISIBLE);
        }else{
            mPrevBtn.setVisibility(View.INVISIBLE);
        }

        if(next_enable){
            mNextBtn.setVisibility(View.VISIBLE);
        }else{
            mNextBtn.setVisibility(View.INVISIBLE);
        }
    }

    private void updateAlbumArt(final MediaItem item){
        if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
            new Action() {
                private Bitmap img;

                @NonNull
                @Override
                public String id() {
                    return TAG;
                }

                @Nullable
                @Override
                protected Object run() throws InterruptedException {
                    if (item.getItemArtUrl() != null && (new File(item.getItemArtUrl())).exists()) {
                        return null;
                    } else {
                        return img = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                R.drawable.ic_default_art_player);
                    }
                }

                @Override
                protected void done(@Nullable final Object result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (item.getItemArtUrl() != null && (new File(item.getItemArtUrl())).exists()) {
                                Logger.LOGD("ImageLoad", "Always call --");
                                try {
                                    Bitmap bitmap = BitmapFactory.decodeFile(item.getItemArtUrl());
                                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) getResources().getDimension(R.dimen.home_album_art_size),
                                            (int) getResources().getDimension(R.dimen.home_album_art_size), false);
                                    PlayerUtils.ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, bitmap);
                                    Bitmap blurredBitmap = blur(BoomPlayerActivity.this, bitmap);
                                    mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                }catch (NullPointerException e){
                                    Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                                            R.drawable.ic_default_art_player);
                                    PlayerUtils.ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, albumArt);
                                    Bitmap blurredBitmap = blur(BoomPlayerActivity.this, albumArt);
                                    mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                }
//                                Picasso.with(BoomPlayerActivity.this).load(file).resize((int) getResources().getDimension(R.dimen.home_album_art_size), (int) getResources().getDimension(R.dimen.home_album_art_size))
//                                        .centerCrop().priority(Picasso.Priority.HIGH).memoryPolicy(MemoryPolicy.NO_CACHE).into(new Target() {
//                                    @Override
//                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//                                        Log.d("ImageLoad", "Always");
//                                        ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, bitmap);
//                                        Bitmap blurredBitmap = blur(BoomPlayerActivity.this, bitmap);
//                                        mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
//                                    }
//
//                                    @Override
//                                    public void onBitmapFailed(Drawable errorDrawable) {
//                                        ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, (Bitmap) result);
//                                        Bitmap blurredBitmap = blur(BoomPlayerActivity.this, (Bitmap) result);
//                                        mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
//                                    }
//
//                                    @Override
//                                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                                    }
//                                });
                            }
                        }
                    });
                }
            }.execute();
        } else {
            if(item != null) {
                Bitmap albumArt = BitmapFactory.decodeResource(getResources(),
                        R.drawable.ic_default_art_player);
                PlayerUtils.ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, albumArt);
                Bitmap blurredBitmap = blur(BoomPlayerActivity.this, albumArt);
                mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
            }
        }
    }

    private void updateTrackToPlayer(final MediaItem item, boolean playing, boolean isLastPlayedSong) {
        if(item != null && !isLastPlayedSong){
            mRepeatBtn.setVisibility(View.VISIBLE);
            mShuffleBtn.setVisibility(View.VISIBLE);
            mTitleTxt.setVisibility(View.VISIBLE);
            mSubTitleTxt.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);
            mRemainsTime.setVisibility(View.VISIBLE);
            mPlayedTime.setVisibility(View.VISIBLE);
            mNextBtn.setVisibility(View.VISIBLE);
            mPrevBtn.setVisibility(View.VISIBLE);
            mTitleTxt.setSelected(true);
            mSubTitleTxt.setSelected(true);
            mFavourite.setVisibility(View.VISIBLE);
            mAddToPlayList.setVisibility(View.VISIBLE);

            updateAlbumArt(item);

            updateFavoriteTrack(false);

            mTitleTxt.setText(item.getItemTitle());
            mSubTitleTxt.setText(item.getItemArtist());
            if (playing) {
                mPlayPauseBtn.setVisibility(View.VISIBLE);
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_pause, null));
            } else {
                mPlayPauseBtn.setVisibility(View.VISIBLE);
                mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_play, null));
            }
        }else if(!isLastPlayedSong){
            param = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            param.gravity = Gravity.CENTER;
            mAlbumArt.setLayoutParams(param);
            mRepeatBtn.setVisibility(View.INVISIBLE);
            mShuffleBtn.setVisibility(View.INVISIBLE);
            mTitleTxt.setVisibility(View.GONE);
            mSubTitleTxt.setVisibility(View.GONE);
            mTrackSeek.setVisibility(View.INVISIBLE);
            mPlayPauseBtn.setVisibility(View.INVISIBLE);
            mAlbumArt.setImageDrawable(getBaseContext().getResources().getDrawable(R.drawable.no_song_selected));
//            ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, BitmapFactory.decodeResource(getBaseContext().getResources(),
//                    R.drawable.no_song_selected));
            mRemainsTime.setVisibility(View.INVISIBLE);
            mPlayedTime.setVisibility(View.INVISIBLE);
            mFavourite.setVisibility(View.INVISIBLE);
            mAddToPlayList.setVisibility(View.INVISIBLE);
        }else if(isLastPlayedSong){
            mTrackSeek.setProgress(0);
            mPlayPauseBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_player_play, null));
            mRemainsTime.setText("-"+mPlayedTime.getText());
            mPlayedTime.setText("00:00");
        }
        updatePreviousNext(App.getPlayingQueueHandler().getUpNextList().isPrevious(), App.getPlayingQueueHandler().getUpNextList().isNext());
        updateShuffle();
        updateRepeat();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        super.onCreate(savedInstanceState);
//        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        setContentView(R.layout.activity_boom_player);

        initViews();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_LAST_PLAYED_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        registerReceiver(mPlayerBroadcastReceiver, intentFilter);
        // new BoomServerRequest().getAccessToken(this);
        showPurchaseOption();
        musicReceiver = new MusicReceiver(this);
    }

    public void showCoachMark() {
       /* if(App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null){
            Preferences.writeBoolean(this,Preferences.PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE,false);
        }*/

        if (App.getPlayingQueueHandler().getUpNextList().getPlayingItem() == null && Preferences.readBoolean(this, Preferences.PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE, true)) {
            tipWindowLibrary = new TooltipWindow(BoomPlayerActivity.this, TooltipWindow.DRAW_TOP_RIGHT, getResources().getString(R.string.tutorial_select_song));
            tipWindowLibrary.showToolTip(findViewById(R.id.library_btn), TooltipWindow.DRAW_ARROW_BOTTOM_LEFT);
            //Preferences.writeBoolean(this,Preferences.PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE,false);
            tipWindowLibrary.setAutoDismissBahaviour(false);

        } else {
            Preferences.writeBoolean(this, Preferences.PLAYER_SCREEN_LIBRARY_COACHMARK_ENABLE, false);
        }
        if (App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null && !audioEffectPreferenceHandler.isAudioEffectOn() && Preferences.readBoolean(this,Preferences.PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE,true)) {
            tipWindowEffect = new TooltipWindow(BoomPlayerActivity.this, TooltipWindow.DRAW_TOP_CENTER, getResources().getString(R.string.tutorial_boom_effect));
            tipWindowEffect.setAutoDismissBahaviour(true);

            tipWindowEffect.showToolTip(findViewById(R.id.audio_effect_btn), TooltipWindow.DRAW_ARROW_DEFAULT_CENTER);
        }
        if (App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null && !Preferences.readBoolean(this, Preferences.PLAYER_SCREEN_EFFECT_COACHMARK_ENABLE, true) && Preferences.readBoolean(this, Preferences.PLAYER_SCREEN_EFFECT_TAPANDHOLD_COACHMARK_ENABLE, true)) {
            tipWindowHold = new TooltipWindow(BoomPlayerActivity.this, TooltipWindow.DRAW_TOP_CENTER, getResources().getString(R.string.tutorial_effect_tap_hold));
            tipWindowHold.showToolTip(findViewById(R.id.audio_effect_btn), TooltipWindow.DRAW_ARROW_DEFAULT_CENTER);
            Preferences.writeBoolean(this, Preferences.PLAYER_SCREEN_EFFECT_TAPANDHOLD_COACHMARK_ENABLE, false);
            tipWindowHold.setAutoDismissBahaviour(true);

        }
        //AudioManager am1 = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (Preferences.readBoolean(this, Preferences.APP_FRESH_LAUNCH, true) && Preferences.readBoolean(this, Preferences.PLAYER_SCREEN_HEADSET_ENABLE, true) && !MusicReceiver.isPlugged) {
            tipWindowHeadset = new TooltipWindow(BoomPlayerActivity.this, TooltipWindow.DRAW_ABOVE_WITH_CLOSE, getResources().getString(R.string.tutorial_use_headphones));
            tipWindowHeadset.showToolTip(findViewById(R.id.player_title_txt), 0);
            tipWindowHeadset.setAutoDismissBahaviour(false);
        }
    }

    public void showPurchaseOption() {
        //  PurchaseUtil.checkUserPurchase(this);


    }
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        moveTaskToBack(true);
        Preferences.writeBoolean(BoomPlayerActivity.this, Preferences.APP_FRESH_LAUNCH, false);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            showCoachMark();
    }
    private int getStatusBarHeight(){
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public void initViews(){
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(this);

        mPlayerRootView = (LinearLayout)findViewById(R.id.player_root_view);

        mTitleTxt = (RegularTextView) findViewById(R.id.player_title_txt);
        mSubTitleTxt = (RegularTextView) findViewById(R.id.player_subtitle_txt);
        mTrackSeek = (CircularSeekBar)findViewById(R.id.track_seek);
        mAlbumArt = (CircularCoverView)findViewById(R.id.album_art);
        mPlayPauseBtn = (ImageView)findViewById(R.id.play_pause_btn);
        mLibraryBtn = (ImageView)findViewById(R.id.library_btn);

        mAudioEffectBtn = (ImageView)findViewById(R.id.audio_effect_btn);
        mUpNextQueue = (ImageView)findViewById(R.id.up_next_queue_btn);

        mNextBtn = (ImageView)findViewById(R.id.player_next_btn);
        mPrevBtn = (ImageView)findViewById(R.id.player_prev_btn);

        mShuffleBtn = (ImageView) findViewById(R.id.player_shuffle_btn);
        mRepeatBtn = (ImageView) findViewById(R.id.player_repeat_btn);

        mAddToPlayList = (ImageView) findViewById(R.id.player_add_to_playlist);
        mFavourite = (ImageView)findViewById(R.id.player_fav_btn);
        mPlayerSetting = (ImageView)findViewById(R.id.player_setting_btn);

        mPlayedTime = (RegularTextView) findViewById(R.id.played_time);
        mRemainsTime = (RegularTextView) findViewById(R.id.remains_time);


        mPlayerBackground = (FrameLayout)findViewById(R.id.player_background);

        mPlayerRootView.setPadding(0, getStatusBarHeight(), 0, 0);

        mPlayPauseBtn.setOnClickListener(this);
        mLibraryBtn.setOnClickListener(this);
        mAudioEffectBtn.setOnClickListener(this);
        mUpNextQueue.setOnClickListener(this);
        mShuffleBtn.setOnClickListener(this);
        mRepeatBtn.setOnClickListener(this);

        mNextBtn.setOnClickListener(this);
        mPrevBtn.setOnClickListener(this);

        mAddToPlayList.setOnClickListener(this);
        mFavourite.setOnClickListener(this);
        mPlayerSetting.setOnClickListener(this);

        mAudioEffectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                audioEffectPreferenceHandler.setEnableAudioEffect(!audioEffectPreferenceHandler.isAudioEffectOn());
                if (App.getPlayerEventHandler().getPlayingItem() != null && (App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused()))
                    App.getPlayerEventHandler().updateEffect();

                updateEffectIcon();
                if (tipWindowHold != null) {
                    tipWindowHold.dismissTooltip();
                }

                return true;
            }
        });

        mTrackSeek.setOnCircularSeekBarChangeListener(this);

        mAlbumArt.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Logger.LOGE("Player", "Touch");
                return true;
            }
        });

        updateEffectIcon();
    }

    private void startEffectActivity(){
        Intent queueIntent = new Intent(BoomPlayerActivity.this, Surround3DActivity.class);
        startActivity(queueIntent);
    }

    private void startUpNextActivity() {
        Intent queueIntent = new Intent(BoomPlayerActivity.this, PlayingQueueActivity.class);
        startActivity(queueIntent);
    }

    private void startLibraryActivity() {
        Intent listIntent = new Intent(BoomPlayerActivity.this, DeviceMusicActivity.class);
        listIntent.setAction("visible");
        startActivity(listIntent);
        overridePendingTransition(R.anim.slide_in_left, R.anim.stay_out);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.play_pause_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE_SONG));
                break;
            case R.id.up_next_queue_btn:
                startUpNextActivity();
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_QUEUE_BUTTON_FROM_PLAYER_SCREEN);
                break;
            case R.id.library_btn:
                startLibraryActivity();
                break;
            case R.id.audio_effect_btn:
                startEffectActivity();
                break;
            case R.id.player_shuffle_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_SHUFFLE_SONG));
                break;
            case R.id.player_repeat_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_REPEAT_SONG));
                break;
            case R.id.player_next_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_NEXT_SONG));
                break;
            case R.id.player_prev_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_PREV_SONG));
                break;
            case R.id.player_add_to_playlist:
                addToPlayList();
                break;
            case R.id.player_fav_btn:
                updateFavoriteTrack(true);
                break;
            case R.id.player_setting_btn:
                startSettingActivity();
            default:

                break;
        }
    }

    private void addToPlayList() {
        Utils util = new Utils(this);
        ArrayList list = new ArrayList<IMediaItemBase>();
        list.add(App.getPlayerEventHandler().getPlayingItem());
        util.addToPlaylist(BoomPlayerActivity.this, list, null);
    }

    private void updateFavoriteTrack(boolean isUser) {
        if(App.getPlayerEventHandler().getPlayingItem() != null) {
            boolean isCurrentTrackFav = MediaController.getInstance(this).isFavouriteItems(App.getPlayerEventHandler().getPlayingItem().getItemId());
            if (isCurrentTrackFav) {
                if(isUser){
                    MediaController.getInstance(this).removeItemToFavoriteList(App.getPlayerEventHandler().getPlayingItem().getItemId());
                    Toast.makeText(this, getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favourites_normal));
                }else {
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favourites_selected));
                }
            } else {
                if(isUser){
                    MediaController.getInstance(this).addSongsToFavoriteList(App.getPlayerEventHandler().getPlayingItem());
                    Toast.makeText(this, getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favourites_selected));
                }else{
                    mFavourite.setImageDrawable(getResources().getDrawable(R.drawable.ic_favourites_normal));
                }
            }
        }else{
            Toast.makeText(this, getResources().getString(R.string.no_song), Toast.LENGTH_SHORT).show();
        }
    }

    private void startSettingActivity() {
        Intent i = new Intent(BoomPlayerActivity.this, SettingsActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.push_up_in, R.anim.stay_out);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPlayerResume = true;
        updateEffectIcon();
        updateTrackToPlayer(App.getPlayingQueueHandler().getUpNextList().getPlayingItem() != null ?
                (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem() :
                null, App.getPlayerEventHandler().isPlaying(), false);

        updateUpNextButton();
        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(musicReceiver, filter);
    }

    private void updateUpNextButton() {
        if(App.getPlayingQueueHandler().getUpNextList().getAutoUpNextList().size() > 0 ||
                App.getPlayingQueueHandler().getUpNextList().getManualUpNextList().size() > 0 ||
                null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem() ||
                App.getPlayingQueueHandler().getUpNextList().getHistoryList().size() > 0){
            mUpNextQueue.setVisibility(View.VISIBLE);
        }else{
            mUpNextQueue.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        isPlayerResume = false;
        if (tipWindowLibrary != null) {
            tipWindowLibrary.dismissTooltip();
        }
        if (tipWindowEffect != null) {
            tipWindowEffect.dismissTooltip();
        }
        if (tipWindowHold != null) {
            tipWindowHold.dismissTooltip();
        }
        if (tipWindowHeadset != null) {
            tipWindowHeadset.dismissTooltip();
        }
        unregisterReceiver(musicReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mPlayerBroadcastReceiver);
    }

    private void updateEffectIcon() {
        if(audioEffectPreferenceHandler.isAudioEffectOn()) {
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_boom_effect_on, null));
        }else{
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.ic_boom_effect_off, null));
        }
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if(fromUser/* && App.getPlayerEventHandler().isPlaying()*/) {
            isUser = true;
            mTrackSeek.setProgress(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(CircularSeekBar circularSeekBar) {

    }

    @Override
    public void onStopTrackingTouch(CircularSeekBar circularSeekBar) {
//        if(App.getPlayerEventHandler().isPlaying()){
        Intent intent = new Intent(PlayerService.ACTION_SEEK_SONG);
        intent.putExtra("seek", circularSeekBar.getProgress());
        sendBroadcast(intent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isUser = false;
            }
        }, 300);
//        }
    }

    public Bitmap blur(Context context, Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(context);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    @Override
    public void onHeadsetUnplugged() {

    }

    @Override
    public void onHeadsetPlugged() {

        if (tipWindowHeadset != null) {
            tipWindowHeadset.dismissTooltip();
            Preferences.writeBoolean(this, Preferences.PLAYER_SCREEN_HEADSET_ENABLE, false);

        }

    }

    class TrackTimerTask extends TimerTask {
        Long itemDuration = null;
        public TrackTimerTask(Long time){
            itemDuration = time;
        }
        @Override
        public void run() {
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat simpleDateFormat =
                    new SimpleDateFormat("HH:mm:ss a");
            final String remainsTime = simpleDateFormat.format(itemDuration--);
            final String playedTime = simpleDateFormat.format(0);

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    mRemainsTime.setText("-"+remainsTime);
                    mPlayedTime.setText(playedTime);
                }
            });
        }
    }
}
