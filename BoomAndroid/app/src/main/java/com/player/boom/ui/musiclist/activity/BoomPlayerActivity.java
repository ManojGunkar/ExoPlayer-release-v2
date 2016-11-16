package com.player.boom.ui.musiclist.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.openslplayer.AudioEffect;
import com.player.boom.App;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.R;
import com.player.boom.task.PlayerService;
import com.player.boom.ui.widgets.CircularSeekBar;
import com.player.boom.ui.widgets.CoverView.CircularCoverView;
import com.player.boom.ui.widgets.RegularTextView;
import com.player.boom.utils.async.Action;
import com.player.boom.utils.handlers.UserPreferenceHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public class BoomPlayerActivity extends AppCompatActivity implements View.OnClickListener, CircularSeekBar.OnCircularSeekBarChangeListener{

    private static final float BITMAP_SCALE = 0.4f;
    private static final float BLUR_RADIUS = 25.0f;
    private static final String TAG = "BoomPlayerActivity";
    private RegularTextView mTitleTxt, mSubTitleTxt;
    private CircularCoverView mAlbumArt;
    private CircularSeekBar mTrackSeek;
    private ImageView mPlayPauseBtn, mLibraryBtn, mAudioEffectBtn, mUpNextQueue;
    FrameLayout mPlayerBackground;
    LinearLayout mPlayerRootView;
    AudioEffect audioEffectPreferenceHandler;
    private static boolean isUser = false;
    public static boolean isPlayerResume = true;
    public static String mCurrentArtUrl = "albumart";
    public static final String ACTION_RECEIVE_SONG = "ACTION_RECEIVE_SONG";
    public static final String ACTION_ITEM_CLICKED = "ACTION_ITEM_CLICKED";
    public static final String ACTION_TRACK_STOPPED = "ACTION_TRACK_STOPPED";
    public static final String ACTION_UPDATE_TRACK_SEEK = "ACTION_UPDATE_TRACK_SEEK";
    public static final String ACTION_UPDATE_SHUFFLE = "ACTION_UPDATE_SHUFFLE";
    public static final String ACTION_UPDATE_REPEAT = "ACTION_UPDATE_REPEAT";


    public ImageView mShuffleBtn, mRepeatBtn, mNextBtn, mPrevBtn;

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
                    if(!isUser)
                        mTrackSeek.setProgress(intent.getIntExtra("percent", 0));
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
//                mShuffleBtn.setImageDrawable();
                break;
            case all:
//                mShuffleBtn.setImageDrawable();
                break;
        }
    }

    private void updateRepeat(){
        switch (App.getUserPreferenceHandler().getRepeat()){
            case none:
//mRepeatBtn.setImageDrawable();
                break;
            case one:
//mRepeatBtn.setImageDrawable();
                break;
            case all:
//mRepeatBtn.setImageDrawable();
                break;
        }
    }

    private void updateTrackToPlayer(final MediaItem item, boolean playing) {
        if(item != null){
            mTitleTxt.setVisibility(View.VISIBLE);
            mSubTitleTxt.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);
            mSubTitleTxt.setSelected(true);
            if(!mCurrentArtUrl.equals(item.getItemArtUrl())) {
                mCurrentArtUrl = item.getItemArtUrl();
                if (isPathValid(item.getItemArtUrl())) {
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
                            if (mCurrentArtUrl != null && (new File(mCurrentArtUrl)).exists()) {
                                return null;
                            } else {
                                img = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                        R.drawable.default_album_art_home);
                            }
                            return null;
                        }

                        @Override
                        protected void done(@Nullable Object result) {
                            if (mCurrentArtUrl != null && (new File(mCurrentArtUrl)).exists()) {
                                Picasso.with(BoomPlayerActivity.this).load(new File(mCurrentArtUrl)).resize((int) getResources().getDimension(R.dimen.home_album_art_size), (int) getResources().getDimension(R.dimen.home_album_art_size))
                                        .centerCrop().noFade().into(new Target() {
                                    @Override
                                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                                        ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, bitmap);
                                        Bitmap blurredBitmap = blur(BoomPlayerActivity.this, bitmap);
                                        mPlayerBackground.setBackground(new BitmapDrawable(getResources(), blurredBitmap));
                                    }

                                    @Override
                                    public void onBitmapFailed(Drawable errorDrawable) {
                                        img = BitmapFactory.decodeResource(getBaseContext().getResources(),
                                                R.drawable.default_album_art_home);
                                        ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, img);
                                    }

                                    @Override
                                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                                    }
                                });
                                return;
                            }
                            ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, img);
                            mPlayerBackground.setBackground(new BitmapDrawable(getResources(), img));
                        }
                    }.execute();
                } else {
                    ImageViewAnimatedChange(BoomPlayerActivity.this, mAlbumArt, BitmapFactory.decodeResource(getBaseContext().getResources(),
                            R.drawable.default_album_art_home));
                }
            }
            mTitleTxt.setText(item.getItemTitle());
            mSubTitleTxt.setText(item.getItemArtist());
            if (playing) {
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
            mTitleTxt.setVisibility(View.GONE);
            mSubTitleTxt.setVisibility(View.GONE);
            mTrackSeek.setVisibility(View.INVISIBLE);
            mPlayPauseBtn.setVisibility(View.INVISIBLE);
            mAlbumArt.setImageDrawable(getResources().getDrawable(R.drawable.no_song_selected));
        }
    }

    public static void ImageViewAnimatedChange(Context context, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(context, android.R.anim.fade_out);
        final Animation anim_in  = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        anim_out.setAnimationListener(new Animation.AnimationListener()
        {
            @Override public void onAnimationStart(Animation animation) {}
            @Override public void onAnimationRepeat(Animation animation) {}
            @Override public void onAnimationEnd(Animation animation)
            {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override public void onAnimationStart(Animation animation) {}
                    @Override public void onAnimationRepeat(Animation animation) {}
                    @Override public void onAnimationEnd(Animation animation) {}
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

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

        mPlayerBackground = (FrameLayout)findViewById(R.id.player_background);

        mPlayerRootView.setPadding(0, getStatusBarHeight(), 0, 0);

        mPlayPauseBtn.setOnClickListener(this);
        mLibraryBtn.setOnClickListener(this);
        mAudioEffectBtn.setOnClickListener(this);
        mUpNextQueue.setOnClickListener(this);
        mShuffleBtn.setOnClickListener(this);
        mRepeatBtn.setOnClickListener(this);

        mAudioEffectBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                audioEffectPreferenceHandler.setEnableAudioEffect(!audioEffectPreferenceHandler.isAudioEffectOn());
                updateEffectIcon();
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
            case R.id.player_shuffle_btn:
                App.getUserPreferenceHandler().resetShuffle();
                sendBroadcast(new Intent(PlayerService.ACTION_SHUFFLE_SONG));
                break;
            case R.id.player_repeat_btn:
                App.getUserPreferenceHandler().resetRepeat();
                sendBroadcast(new Intent(PlayerService.ACTION_REPEAT_SONG));
                break;
            case R.id.player_next_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_NEXT_SONG));
                break;
            case R.id.player_prev_btn:
                sendBroadcast(new Intent(PlayerService.ACTION_PREV_SONG));
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
        updateTrackToPlayer((MediaItem) App.getPlayingQueueHandler().getPlayingQueue().getPlayingItem(), App.getPlayerEventHandler().isPlaying());
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
        if(audioEffectPreferenceHandler.isAudioEffectOn()) {
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_on, null));
        }else{
            mAudioEffectBtn.setImageDrawable(getResources().getDrawable(R.drawable.boom_effect_off, null));
        }
    }

    @Override
    public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
        if(fromUser && App.getPlayerEventHandler().isPlaying()) {
            isUser = true;
            mTrackSeek.setProgress(progress);
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
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    isUser = false;
                }
            }, 300);
        }
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
}
