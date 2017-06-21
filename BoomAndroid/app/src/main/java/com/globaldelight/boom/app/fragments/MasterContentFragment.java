package com.globaldelight.boom.app.fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.app.analytics.UtilAnalytics;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.adapters.utils.EqualizerDialogAdapter;
import com.globaldelight.boom.playbackEvent.controller.EffectUIController;
import com.globaldelight.boom.playbackEvent.controller.PlayerUIController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.view.CoachMarkerWindow;
import com.globaldelight.boom.view.NegativeSeekBar;
import com.globaldelight.boom.view.RegularTextView;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_HOME_SCREEN_BACK_PRESSED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ITEM_CLICKED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_LAST_PLAYED_SONG;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_DISCONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_SWITCH_OFF_AUDIO_EFFECT;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_SCREEN_RESUME;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_RECEIVE_SONG;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_STOP_UPDATING_UPNEXT_DB;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_TRACK_STOPPED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_TRACK_SEEK;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_BOTTOM_CENTER;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_TOP_CENTER;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_TOP_LEFT;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_OPEN_EFFECT_MINI_PLAYER;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT;

/**
 * Created by Rahul Agarwal on 16-01-17.
 */

public class MasterContentFragment extends Fragment implements MasterActivity.IPlayerSliderControl, View.OnClickListener, View.OnTouchListener, EqualizerDialogAdapter.IEqualizerSelect {
    private final String TAG = "PlayerFragment-TAG";

    private long mItemId=-1;
    private boolean isUser = false;
    
    Activity mActivity;
    private static ProgressBar mLoadingProgress;
    private static boolean isCloudSeek = false;
    private AudioEffect audioEffects;

    public static boolean isUpdateUpnextDB = true;

    private static MediaItem mPlayingMediaItem;
    private static boolean mIsPlaying, mIsLastPlayed;

    /************************************************************************************/

    public View miniController, mPlayerActionPanel;

    private RegularTextView mLargeSongTitle, mLargeSongSubTitle, mTotalSeekTime, mCurrentSeekTime;
    private View mInflater;
    private int colorTo , colorFrom, colorFromActive;
    private AppCompatSeekBar mTrackSeek;
    private ImageView mNext, mPlayPause, mPrevious, mShuffle, mRepeat, mPlayerBackBtn, mLargeAlbumArt;
    private ImageView mEffectTab, mPlayerTab;
    private ImageView mUpNextBtnPanel, mPlayerOverFlowMenuPanel;
    private LinearLayout mEffectContent, mPlayerLarge, mPlayerTitlePanel;
    private FrameLayout mPlayerContent;
    private FrameLayout mPlayerBackground;

    private LinearLayout mMiniPlayerEffectPanel, mMiniTitlePanel;
    private RegularTextView mMiniSongTitle, mMiniSongSubTitle;
    private ImageView mMiniPlayerPlayPause, mMiniPlayerEffect;
    private AppCompatSeekBar mMiniPlayerSeek;

    private RegularTextView mDisableIntensity;
    private NegativeSeekBar mIntensitySeek;
    private SwitchCompat mEffectSwitch;
    private AppCompatCheckBox mFullBassCheck;
    private RegularTextView mEffectSwitchTxt, m3DSurroundTxt, mIntensityTxt, mEqualizerTxt, mSelectedEqTxt;
    private ImageView m3DSurroundBtn, mIntensityBtn, mEqualizerBtn, mSpeakerBtn, mSelectedEqImg, mSelectedEqGoImg;
    private LinearLayout mEqDialogPanel, mSpeakerDialogPanel;
    private double mOldIntensity;

    private List<String> eq_names;
    private TypedArray eq_active_off;

    private int ScreenWidth, ScreenHeight;
    private boolean isEffectOn = false;
    private EffectUIController aaEffectUIController;
    private PlayerUIController playerUIController;
    private Handler postMessage;

    private CoachMarkerWindow coachMarkEffectPager, coachMarkEffectPlayer, coachMarkEffectSwitcher;

    private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMediaItem item;
            switch (intent.getAction()){
                case ACTION_RECEIVE_SONG :
                    item = intent.getParcelableExtra("playing_song");
                    if(item != null){
                        mPlayingMediaItem = (MediaItem) item;
                        mIsPlaying = intent.getBooleanExtra("playing", false);
                        mIsLastPlayed = false;
                        mTrackSeek.setProgress(0);
                        mMiniPlayerSeek.setProgress(0);
                        updatePlayerUI();
                    }
                    stopLoadProgress();
                    break;
                case ACTION_LAST_PLAYED_SONG:
                    item = intent.getParcelableExtra("playing_song");
                    mPlayingMediaItem = (MediaItem) item;
                    mIsPlaying = false;
                    mIsLastPlayed = intent.getBooleanExtra("last_played_song", true);
                    updatePlayerUI();
                    break;
                case ACTION_ITEM_CLICKED :
                    try {
                        if (intent.getBooleanExtra("play_pause", false) == false) {
                            mMiniPlayerPlayPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_miniplayer_play, null));
                            mPlayPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_player_play, null));
                        } else {
                            mMiniPlayerPlayPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_miniplayer_pause, null));
                            mPlayPause.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_player_pause, null));
                        }
                    }catch (Exception e){}
                    stopLoadProgress();
                    break;
                case ACTION_TRACK_STOPPED :
                    mPlayingMediaItem = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
                    mIsPlaying = false;
                    mIsLastPlayed = false;
                    updatePlayerUI(false);
                    showProgressLoader();
                    break;
                case ACTION_UPDATE_TRACK_SEEK :
                    if(!isUser) {
                        mTrackSeek.setProgress(intent.getIntExtra("percent", 0));
                        mMiniPlayerSeek.setProgress(intent.getIntExtra("percent", 0));
                    }
                    if(isCloudSeek){
                        stopLoadProgress();
                        isCloudSeek = false;
                    }

                    long totalMillis = intent.getLongExtra("totalms", 0);
                    long currentMillis = intent.getLongExtra("currentms", 0);
                    if(!isUser)
                        updateTrackPlayTime(totalMillis, currentMillis);
                    break;
                case ACTION_UPDATE_SHUFFLE:
                    updateShuffle();
                    break;
                case ACTION_UPDATE_REPEAT :
                    updateRepeat();
                    updatePreviousNext(App.getPlayingQueueHandler().getUpNextList().isPrevious(), App.getPlayingQueueHandler().getUpNextList().isNext());
                    break;
                case ACTION_STOP_UPDATING_UPNEXT_DB:
                    isUpdateUpnextDB = false;
                    break;
                case ACTION_HOME_SCREEN_BACK_PRESSED:
                    dismissTooltip();
                    break;
                case ACTION_PLAYER_SCREEN_RESUME:

                    break;
                case ACTION_ON_NETWORK_DISCONNECTED:
                    stopLoadProgress();
                    break;
                case ACTION_ON_SWITCH_OFF_AUDIO_EFFECT:
                    // update UI of effect (mini player effect button also)
                    mEffectSwitch.setChecked(false);
                    updateMiniPlayerEffectUI(false);
                    setEnableEffects();
                    break;
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity){
            mActivity = (Activity) context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater.inflate(R.layout.fragment_content_master, container, false);
        if(null == mActivity)
            mActivity = getActivity();
        return mInflater;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        colorTo = ContextCompat.getColor(mActivity, R.color.effect_inactive);
        colorFrom = ContextCompat.getColor(mActivity, R.color.effect_active);
        colorFromActive = ContextCompat.getColor(mActivity, R.color.colorAccent);

        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        ScreenWidth = point.x;
        ScreenHeight = point.y;

        audioEffects = AudioEffect.getInstance(mActivity);

        playerUIController = new PlayerUIController(mActivity);
        PlayerUIController.registerPlayerUIController(playerUIController);

        mPlayerBackground = (FrameLayout) mInflater.findViewById(R.id.player_src_background);

        initMiniPlayer();
        initLargePlayer();
        initEffectControl();

        setPlayerInfo();
        FlurryAnalyticHelper.init(mActivity);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateProgressLoader();
        Utils.InternetPopup(mActivity);
    }

    private void setPlayerEnable(boolean isEnable){
        mPlayerTab.setSelected(isEnable);
        mEffectTab.setSelected(!isEnable);

        if(isEnable){
            mPlayerContent.setVisibility(View.VISIBLE);
            mEffectContent.setVisibility(View.GONE);
        }else{
            mPlayerContent.setVisibility(View.GONE);
            mEffectContent.setVisibility(View.VISIBLE);
            mEffectSwitch.setChecked(audioEffects.isAudioEffectOn());
            String msg = isAllSpeakersAreOff();
            if(null != msg)
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void setPlayerInfo(){
        mPlayingMediaItem = (MediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem();
        mIsPlaying = App.getPlayerEventHandler().isPlaying();
        mIsLastPlayed = (null != App.getPlayerEventHandler().getPlayingItem() ?
                (!App.getPlayerEventHandler().isPlaying() && !App.getPlayerEventHandler().isPaused() ? true : false) :
                false);

        updatePlayerSeekAndTime();
        try {
            updatePlayerUI();
        }catch (Exception e){

        }
    }

    private void updatePlayerSeekAndTime() {
        if(null != mPlayingMediaItem && !mIsPlaying && !mIsLastPlayed){
            int progress = App.getUserPreferenceHandler().getPlayerSeekPosition();
            long currentMillis= App.getUserPreferenceHandler().getPlayedTime();
            long totalMillis = App.getUserPreferenceHandler().getRemainsTime(mPlayingMediaItem.getDurationLong());

            if(null != mTrackSeek)
                mTrackSeek.setProgress(progress);

            if(null != mMiniPlayerSeek)
                mMiniPlayerSeek.setProgress(progress);

            updateTrackPlayTime(totalMillis, currentMillis);
        }
    }

    /* Large Player UI and Functionality*/
    private void updateActionBarButtons() {
        if(App.getPlayingQueueHandler().getUpNextList().getUpNextItemCount() > 0 ){
            mUpNextBtnPanel.setVisibility(View.VISIBLE);
            mPlayerOverFlowMenuPanel.setVisibility(View.VISIBLE);
        }else{
            mUpNextBtnPanel.setVisibility(View.INVISIBLE);
            mPlayerOverFlowMenuPanel.setVisibility(View.INVISIBLE);
        }
    }

    private void updateShuffle(){
        switch (App.getUserPreferenceHandler().getShuffle()){
            case none:
                mShuffle.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_shuffle_off, null));
                break;
            case all:
                mShuffle.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_shuffle_on, null));
                break;
        }
    }

    private void updateRepeat(){
        switch (App.getUserPreferenceHandler().getRepeat()){
            case none:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_off, null));
                break;
            case one:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_one, null));
                break;
            case all:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_on, null));
                break;
        }
    }

    private void updatePreviousNext(boolean prev_enable, boolean next_enable){
        mPrevious.setEnabled(prev_enable);
        mNext.setEnabled(next_enable);
    }

    private void updateAlbumArt(final IMediaItem item){
        new AsyncTask<Void, Void, Bitmap []>() {
            @Override
            protected Bitmap[] doInBackground(Void... params) {
                Bitmap[] result = new Bitmap[2];
                boolean failed = false;
                if ( PlayerUtils.isPathValid(item.getItemArtUrl()) ) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(item.getItemArtUrl());
                        Bitmap blurredBitmap = PlayerUtils.createBackgoundBitmap(mActivity, bitmap, ScreenWidth/10, ScreenHeight/10);
                        result[0] = bitmap;
                        result[1] = blurredBitmap;
                    }catch (Exception e){
                        failed = true;
                    }
                }
                else {
                    failed = true;
                }

                if ( failed ) {
                    Bitmap bitmap = BitmapFactory.decodeResource(mActivity.getResources(),
                            R.drawable.ic_default_art_player_header);
                    Bitmap blurredBitmap = PlayerUtils.createBackgoundBitmap(mActivity, bitmap, ScreenWidth/10, ScreenHeight/10);
                    result[0] = bitmap;
                    result[1] = blurredBitmap;
                }

                return result;
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if ( bitmaps.length != 2 ) {
                    return;
                }

                final Bitmap bitmap = bitmaps[0];
                final Bitmap blurredBitmap = bitmaps[1];
                if ( mItemId == -1 || mItemId != item.getItemId() ) {
                    PlayerUtils.ImageViewAnimatedChange(mActivity, mLargeAlbumArt, bitmap);
                    mItemId = item.getItemId();
                }else{
                    mLargeAlbumArt.setImageBitmap(bitmap);
                }
                mPlayerBackground.setBackground(new BitmapDrawable(mActivity.getResources(), blurredBitmap));
            }
        }.execute();
    }

    private void changeProgress(int progress){
        if(null != mPlayingMediaItem) {
            long totalTime = mPlayingMediaItem.getDurationLong();
            int totalProgress = 100;
            long currentTime = (totalTime / totalProgress) * progress;
            updateTrackPlayTime(totalTime, currentTime);
        }
    }

    private void updateTrackPlayTime(long totalMillis, long currentMillis) {
        if(null != mCurrentSeekTime)
            mCurrentSeekTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentMillis),
                    TimeUnit.MILLISECONDS.toSeconds(currentMillis ) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentMillis))));
        if(null != mTotalSeekTime)
            mTotalSeekTime.setText("-"+String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis),
                    TimeUnit.MILLISECONDS.toSeconds(totalMillis - currentMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis))));
    }

    private void initLargePlayer() {
        mPlayerLarge = (LinearLayout) mInflater.findViewById(R.id.player_large);
        mPlayerLarge.setOnTouchListener(this);

        mPlayerActionPanel = mInflater.findViewById(R.id.player_action_bar);

        mLoadingProgress = (ProgressBar) mInflater. findViewById(R.id.load_cloud);

        mPlayerBackBtn = (ImageView) mInflater.findViewById(R.id.player_back_button);
        mPlayerBackBtn.setOnClickListener(this);
        mPlayerTitlePanel = (LinearLayout) mInflater.findViewById(R.id.player_title_panel);
        mPlayerTitlePanel.setOnClickListener(this);
        mLargeSongTitle = (RegularTextView) mInflater.findViewById(R.id.large_player_title);
        mLargeSongSubTitle = (RegularTextView) mInflater.findViewById(R.id.large_player_sub_title);
        mUpNextBtnPanel = (ImageView) mInflater.findViewById(R.id.player_upnext_button);
        mUpNextBtnPanel.setOnClickListener(this);
        mPlayerOverFlowMenuPanel = (ImageView) mInflater.findViewById(R.id.player_overflow_button);
        mPlayerOverFlowMenuPanel.setOnClickListener(this);

        mLargeAlbumArt = (ImageView) mInflater.findViewById(R.id.player_album_art);

        LinearLayout.LayoutParams artParam = new LinearLayout.LayoutParams((int)(ScreenWidth * 80) / 100, (int)(ScreenWidth * 80) / 100);
        artParam.setMargins((int) ((ScreenWidth * 10) /100), 0, (int) ((ScreenWidth * 10)/100), 0);
        mInflater.findViewById(R.id.player_large_header).setLayoutParams(artParam);
        mPlayerContent = (FrameLayout) mInflater.findViewById(R.id.player_content);

        mPlayerTab = (ImageView) mInflater.findViewById(R.id.player_tab);
        mPlayerTab.setOnClickListener(this);
        mEffectTab = (ImageView) mInflater.findViewById(R.id.effect_tab);
        mEffectTab.setOnClickListener(this);

        mTrackSeek = (AppCompatSeekBar) mInflater.findViewById(R.id.control_seek_bar);
        mTrackSeek.getProgressDrawable().setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
        mTrackSeek.setPadding(mTrackSeek.getPaddingLeft(), 0, mTrackSeek.getPaddingRight(), 0);

        mTrackSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if(fromUser) {
                    isUser = true;

                    mTrackSeek.setProgress(progress);

                    changeProgress(progress);


                    if(App.getPlayerEventHandler().getPlayingItem().getMediaType() != MediaType.DEVICE_MEDIA_LIB){
                        showProgressLoader();
                    }

                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Playing_SeekBar_Used_in_Effects_screen);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(isUser)
                    postMessage.post(new Runnable() {
                        @Override
                        public void run() { playerUIController.OnPlayerSeekChange(mTrackSeek.getProgress()); }
                    });
                postMessage.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        isUser = false;
                        isCloudSeek = true;
                    }
                }, 300);
            }
        });
        mCurrentSeekTime = (RegularTextView) mInflater.findViewById(R.id.played_time);
        mTotalSeekTime = (RegularTextView) mInflater.findViewById(R.id.remain_time);

        mRepeat = (ImageView) mInflater.findViewById(R.id.controller_repeat);
        mRepeat.setOnClickListener(this);
        mPrevious = (ImageView) mInflater.findViewById(R.id.controller_prev);
        mPrevious.setOnClickListener(this);
        mPlayPause = (ImageView) mInflater.findViewById(R.id.controller_play);
        mPlayPause.setOnClickListener(this);
        mNext = (ImageView) mInflater.findViewById(R.id.controller_next);
        mNext.setOnClickListener(this);
        mShuffle = (ImageView) mInflater.findViewById(R.id.controller_shuffle);
        mShuffle.setOnClickListener(this);
    }

    private void updatePlayerUI(){
        updatePlayerUI(true);
    }

    private void updatePlayerUI(boolean mediaChanged) {
//        if(MasterActivity.isPlayerExpended()){
        updateLargePlayerUI(mPlayingMediaItem, mIsPlaying, mIsLastPlayed);
//        }else {
        updateMiniPlayerUI(mPlayingMediaItem, mIsPlaying, mIsLastPlayed);
//        }
        if(null != mPlayingMediaItem && mediaChanged)
            updateAlbumArt(mPlayingMediaItem);
        setEnableEffects();

        updateActionBarButtons();
    }

    private void updateLargePlayerUI(MediaItem item, boolean isPlaying, boolean isLastPlayedItem) {
        if(null != item){
            switch (App.getUserPreferenceHandler().getShuffle()){
                case none:
                    DrawableCompat.setTint(mShuffle.getDrawable(), colorFrom);
                    break;
                case all:
                    DrawableCompat.setTint(mShuffle.getDrawable(), colorFromActive);
                    break;
            }
            switch (App.getUserPreferenceHandler().getRepeat()){
                case none:
                    DrawableCompat.setTint(mRepeat.getDrawable(), colorFrom);
                    break;
                default:
                    DrawableCompat.setTint(mRepeat.getDrawable(), colorFromActive);
                    break;
            }
            mLargeSongTitle.setVisibility(View.VISIBLE);
            mLargeSongSubTitle.setVisibility(View.VISIBLE);
            mTrackSeek.setVisibility(View.VISIBLE);
            mTotalSeekTime.setVisibility(View.VISIBLE);
            mCurrentSeekTime.setVisibility(View.VISIBLE);
            mPrevious.setVisibility(View.VISIBLE);
            mLargeSongTitle.setSelected(true);
            mLargeSongSubTitle.setSelected(true);

            DrawableCompat.setTint(mPlayPause.getDrawable(), colorFrom);
            mLargeSongTitle.setText(item.getItemTitle());
            mLargeSongSubTitle.setVisibility(null != item.getItemArtist() ? View.VISIBLE : View.GONE);
            mLargeSongSubTitle.setText(item.getItemArtist());

            if(isLastPlayedItem){
                mTrackSeek.setProgress(0);
                mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_play, null));

                long totalMillis = item.getDurationLong();
                long currentMillis = 0;

                if(!isUser)
                    updateTrackPlayTime(totalMillis, currentMillis);

            }else {
                boolean isMediaItem = item.getMediaType() == MediaType.DEVICE_MEDIA_LIB;
                if (isPlaying) {
                    mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                } else {
                    mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }
        }else if(!isLastPlayedItem){
            DrawableCompat.setTint(mRepeat.getDrawable(), colorTo);
            DrawableCompat.setTint(mShuffle.getDrawable(), colorTo);
            DrawableCompat.setTint(mPlayPause.getDrawable(), colorTo);
            mLargeSongTitle.setVisibility(View.INVISIBLE);
            mLargeSongSubTitle.setVisibility(View.INVISIBLE);
            mTrackSeek.setVisibility(View.INVISIBLE);
            mLargeAlbumArt.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_no_music_selected, null));
            mTotalSeekTime.setVisibility(View.INVISIBLE);
            mCurrentSeekTime.setVisibility(View.INVISIBLE);
        }
        updatePreviousNext(App.getPlayingQueueHandler().getUpNextList().isPrevious(), App.getPlayingQueueHandler().getUpNextList().isNext());
        updateShuffle();
        updateRepeat();
    }

    /* Mini Player UI & Functionality*/
    private void initMiniPlayer() {
        miniController = mInflater.findViewById(R.id.small_panel);
        miniController.setOnTouchListener(this);

        mMiniPlayerSeek = (AppCompatSeekBar) mInflater.findViewById(R.id.mini_player_progress);
        mMiniPlayerSeek.setPadding(0,0,0,0);
        mMiniPlayerSeek.setOnTouchListener(this);

        mMiniPlayerEffectPanel = (LinearLayout) mInflater.findViewById(R.id.mini_player_boom_effect);
        mMiniPlayerEffectPanel.setOnClickListener(this);
        mMiniPlayerEffect = (ImageView) mInflater.findViewById(R.id.mini_player_effect_img);
        mMiniPlayerEffect.setOnClickListener(this);

        mMiniPlayerPlayPause = (ImageView) mInflater.findViewById(R.id.mini_player_play_pause_btn);
        mMiniPlayerPlayPause.setOnClickListener(this);
        mMiniTitlePanel = (LinearLayout) mInflater.findViewById(R.id.mini_player_title_panel);
        mMiniTitlePanel.setOnClickListener(this);
        mMiniSongTitle = (RegularTextView) mInflater.findViewById(R.id.mini_player_song_title);
        mMiniSongSubTitle = (RegularTextView) mInflater.findViewById(R.id.mini_player_song_sub_title);
    }

    private void updateMiniPlayerUI(MediaItem item, boolean isPlaying, boolean isLastPlayedItem) {
        updateMiniPlayerEffectUI(audioEffects.isAudioEffectOn());
        if(null != item){
            DrawableCompat.setTint(mMiniPlayerPlayPause.getDrawable(), colorFrom);
            mMiniSongTitle.setSelected(true);
            mMiniSongSubTitle.setSelected(true);
            mMiniSongTitle.setText(item.getItemTitle());
            mMiniSongSubTitle.setVisibility(null != item.getItemArtist() ? View.VISIBLE : View.GONE);
            mMiniSongSubTitle.setText(item.getItemArtist());
            if(isPlaying)
                mMiniPlayerPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_pause, null));
            else
                mMiniPlayerPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_play, null));
        }else if(!isLastPlayedItem){
            DrawableCompat.setTint(mMiniPlayerPlayPause.getDrawable(), colorTo);
        }
    }

    private void updateMiniPlayerEffectUI(boolean enable){
        if(enable) {
            mMiniPlayerEffect.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_effects_on, null));
        }else{
            mMiniPlayerEffect.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_effects, null));
        }
    }

    public void setMiniPlayerAlpha(float alpha) {
        miniController.setAlpha(alpha);
    }

    public void setMiniPlayerVisible(boolean isMiniPlayerVisible) {
        if(isMiniPlayerVisible){
            miniController.setAlpha(1);
            mPlayerActionPanel.setAlpha(0);
            mPlayerActionPanel.setVisibility(View.INVISIBLE);
            miniController.setVisibility(View.VISIBLE);
        }else{
            miniController.setAlpha(0);
            mPlayerActionPanel.setAlpha(1);
            mPlayerActionPanel.setVisibility(View.VISIBLE);
            miniController.setVisibility(View.INVISIBLE);
        }
    }

    /* Player Slider Callbacks*/

    @Override
    public void onPanelSlide(View panel, float slideOffset, boolean isEffectOpened) {
        if(slideOffset < 0.1){
            setMiniPlayerVisible(true);
        }else {
            setMiniPlayerVisible(false);
        }
    }

    @Override
    public void onPanelCollapsed(View panel) {
        setMiniPlayerVisible(true);
        updateMiniPlayerUI(mPlayingMediaItem, App.getPlayerEventHandler().isPlaying(), mIsLastPlayed);
        showEffectShortCut();

        if(null != coachMarkEffectSwitcher){
            coachMarkEffectSwitcher.dismissTooltip();
        }
        if(null!=coachMarkEffectPager){
            coachMarkEffectPager.dismissTooltip();
        }
    }

    @Override
    public void onPanelExpanded(View panel) {
        setMiniPlayerVisible(false);

        showEffectSwitchTip();

        if(null != coachMarkEffectPlayer){
            coachMarkEffectPlayer.dismissTooltip();
        }
        updateProgressLoader();

        updateActionBarButtons();
    }

    private void showEffectSwitchTip(){
        if (null != mEffectContent && mEffectContent.getVisibility() == View.VISIBLE && Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true) && !App.getPlayerEventHandler().isStopped() && mInflater.findViewById(R.id.effect_switch).getVisibility() == View.VISIBLE) {
            coachMarkEffectSwitcher = new CoachMarkerWindow(mActivity, DRAW_BOTTOM_CENTER, getResources().getString(R.string.effect_player_tooltip));
            coachMarkEffectSwitcher.setAutoDismissBahaviour(true);
            coachMarkEffectSwitcher.showCoachMark(mInflater.findViewById(R.id.effect_switch));
        }

        if (null != mPlayerContent && mPlayerContent.getVisibility() == View.VISIBLE && Preferences.readBoolean(mActivity, Preferences.TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER, true) && mPlayerContent.getVisibility() == View.VISIBLE &&
                !App.getPlayerEventHandler().isStopped() && Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true)) {
            coachMarkEffectPager = new CoachMarkerWindow(mActivity, DRAW_TOP_CENTER, getResources().getString(R.string.switch_effect_screen_tooltip));
            coachMarkEffectPager.setAutoDismissBahaviour(true);
            coachMarkEffectPager.showCoachMark(mInflater.findViewById(R.id.effect_tab));
        }
    }

    private void showEffectShortCut(){
        if(Preferences.readBoolean(mActivity, TOOLTIP_OPEN_EFFECT_MINI_PLAYER, true) && !Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true)) {
            coachMarkEffectPlayer = new CoachMarkerWindow(mActivity, DRAW_TOP_LEFT, getResources().getString(R.string.library_switch_effect_screen_tooltip));
            coachMarkEffectPlayer.setAutoDismissBahaviour(true);
            Preferences.writeBoolean(mActivity, Preferences.TOOLTIP_OPEN_EFFECT_MINI_PLAYER, false);
            coachMarkEffectPlayer.showCoachMark(mInflater.findViewById(R.id.mini_player_effect_img));
        }
    }

    @Override
    public void onPanelAnchored(View panel) {

    }

    @Override
    public void onPanelHidden(View panel) {

    }

    @Override
    public void onResumeFragment(int alfa) {
        mPlayerActionPanel.setAlpha(alfa);
        updateProgressLoader();
    }

    private void updateProgressLoader(){
        if(App.getPlayerEventHandler().isTrackWaitingForPlay())
            showProgressLoader();
        else
            stopLoadProgress();
    }

    @Override
    public void onStart() {
        setPlayerInfo();
        super.onStart();
        FlurryAnalyticHelper.flurryStartSession(mActivity);
    }

    @Override
    public void onVolumeUp() {
    }

    @Override
    public void onVolumeDown() {
    }


/*Player Screen Utils*/

    public void registerPlayerReceiver(Context context){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_SONG);
        intentFilter.addAction(ACTION_LAST_PLAYED_SONG);
        intentFilter.addAction(ACTION_ITEM_CLICKED);
        intentFilter.addAction(ACTION_TRACK_STOPPED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_SEEK);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        intentFilter.addAction(ACTION_STOP_UPDATING_UPNEXT_DB);
        intentFilter.addAction(ACTION_HOME_SCREEN_BACK_PRESSED);
        intentFilter.addAction(ACTION_PLAYER_SCREEN_RESUME);
        intentFilter.addAction(ACTION_ON_NETWORK_DISCONNECTED);
        intentFilter.addAction(ACTION_ON_SWITCH_OFF_AUDIO_EFFECT);
        context.registerReceiver(mPlayerBroadcastReceiver, intentFilter);

        setPlayerInfo();
    }

    public void unregisterPlayerReceiver(Context context){
        context.unregisterReceiver(mPlayerBroadcastReceiver);
    }

    public MasterActivity.IPlayerSliderControl getPlayerSliderControl() {
        return this;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.player_back_button:
            case R.id.mini_player_effect_img:
            case R.id.mini_player_boom_effect:
                if(!MasterActivity.isPlayerExpended()){
                    setPlayerEnable(false);
                }
                mActivity.sendBroadcast(new Intent(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE));
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Effects_Screen_Opened_from_Mini_Player);
                break;
            case R.id.mini_player_title_panel:
            case R.id.player_title_panel:
               /* if(MasterActivity.isPlayerExpended()) {
                    postMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            playerUIController.OnPlayerTitleClick((MasterActivity) mActivity);
                        }
                    });
                }else{*/
                    setPlayerEnable(true);
                mActivity.sendBroadcast(new Intent(PlayerEvents.ACTION_TOGGLE_PLAYER_SLIDE));

                /*}*/
                break;
            case R.id.player_upnext_button:
                playerUIController.OnUpNextClick(mActivity);
                FlurryAnalyticHelper.logEvent(UtilAnalytics.UpNext_Button_Tapped);
                break;

            case R.id.player_overflow_button:
                overFlowMenu(mActivity, view);
                break;

            case R.id.mini_player_play_pause_btn:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Play_Pause_Button_tapped_Mini_Player);
            case R.id.controller_play:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnPlayPause();
                    }
                });
                showProgressLoader();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Songs_Pause_Or_Play_From_Effects_Screen);
                break;
            case R.id.controller_prev:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnPreviousTrackClick();
                    }
                });
                showProgressLoader();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Previous_button_tapped_From_effect_screen);
                break;
            case R.id.controller_next:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnNextTrackClick();
                    }
                });
                showProgressLoader();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Next_button_tapped_From_effect_screen);
                break;
            case R.id.controller_repeat:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnRepeatClick();
                    }
                });
                break;
            case R.id.controller_shuffle:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnShuffleClick();
                    }
                });
                break;
            case R.id.player_tab:
                setPlayerEnable(true);
                showEffectSwitchTip();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Player_Tab_PLayer_screen);
                break;
            case R.id.effect_tab:
                setPlayerEnable(false);
                showEffectSwitchTip();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Effects_Opened_From_Player_Screen);
                break;
            case R.id.three_surround_btn:
                switch3DSurround();
                break;
            case R.id.intensity_btn:
                switchIntensity();
                break;
            case R.id.equalizer_btn:
                switchEqualizer();
                break;
            case R.id.eq_dialog_panel:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Open_Equalizer_Dailog_from_DropDown);
                if(isEffectOn && audioEffects.isEqualizerOn())
                    onEqDialogOpen();
                break;
            case R.id.equalizer_txt:
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Open_Equalizer_Dailog_from_text);
                if(isEffectOn && audioEffects.isEqualizerOn())
                    onEqDialogOpen();
                break;
            case R.id.speaker_btn :
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Speaker_Dialog_Opened_From_Arrow);
            case R.id.three_surround_txt :
                if(isEffectOn && audioEffects.is3DSurroundOn())
                    openSpeakerDialog();
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Speaker_Dialog_Opened_From_Text);
                break;
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

    /*private void startCloudItemProgress() {
        if(!App.getPlayerEventHandler().isPaused() && App.getPlayerEventHandler().isTrackWaitingForPlay()) {
            mInflater.findViewById(R.id.load_cloud).setVisibility(View.VISIBLE);
        }else{
            mInflater.findViewById(R.id.load_cloud).setVisibility(View.GONE);
        }
    }

    private void stopCloudItemProgress() {
        mInflater.findViewById(R.id.load_cloud).setVisibility(View.GONE);
    }*/

    private void overFlowMenu(Context context, View view) {
        PopupMenu pm = new PopupMenu(context, view);
        boolean isCurrentTrackFav= false;
        if(App.getPlayerEventHandler().getPlayingItem() != null) {
            isCurrentTrackFav = MediaController.getInstance(mActivity).isFavoriteItem(App.getPlayerEventHandler().getPlayingItem().getItemId());
        }
        final boolean isFav = isCurrentTrackFav;
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_song_add_fav:
                            if(isFav){
                                MediaController.getInstance(mActivity).removeItemToFavoriteList(App.getPlayerEventHandler().getPlayingItem().getItemId());
                                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                            }else{
                                MediaController.getInstance(mActivity).addItemToFavoriteList(App.getPlayerEventHandler().getPlayingItem());
                                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.popup_song_add_playlist:
                            ArrayList list = new ArrayList();
                            list.add(App.getPlayerEventHandler().getPlayingItem());
                            Utils.addToPlaylist(mActivity, list, null);
                            break;
                    }
                }catch (Exception e){
                }
                return false;
            }
        });
        if(isCurrentTrackFav){
            pm.inflate(R.menu.player_remove_menu);
        }else{
            pm.inflate(R.menu.player_add_menu);
        }
        pm.show();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()){
            case R.id.small_panel:
                return true;
            case R.id.player_large:
                return true;
            case R.id.intensity_disable_img:
                if(isEffectOn && audioEffects.isIntensityOn())
                    return false;
                else
                    return true;
            case R.id.mini_player_progress :
                return true;
        }
        return false;
    }

    /*Audio Effect UI & Functionality*/

    private void initEffectControl() {
        postMessage = new Handler();

        aaEffectUIController = new EffectUIController(mActivity);
        EffectUIController.registerEffectController(aaEffectUIController);

//        FrameLayout.LayoutParams effectParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ScreenWidth*85)/100);
        mEffectContent = (LinearLayout) mInflater.findViewById(R.id.effect_content);
//        mEffectContent.setLayoutParams(effectParam);

        mEffectSwitchTxt = (RegularTextView) mInflater.findViewById(R.id.effect_switch_txt);
        mEffectSwitch = (SwitchCompat) mInflater.findViewById(R.id.effect_switch);
        mEffectSwitch.setChecked(audioEffects.isAudioEffectOn());

        m3DSurroundBtn = (ImageView) mInflater.findViewById(R.id.three_surround_btn);
        m3DSurroundBtn.setOnClickListener(this);
        m3DSurroundTxt = (RegularTextView) mInflater.findViewById(R.id.three_surround_txt);
        mSpeakerBtn = (ImageView) mInflater.findViewById(R.id.speaker_btn) ;
        mSpeakerBtn.setOnClickListener(this);
        m3DSurroundTxt.setOnClickListener(this);

        mFullBassCheck = (AppCompatCheckBox) mInflater.findViewById(R.id.fullbass_chk);
        mFullBassCheck.setChecked(audioEffects.isFullBassOn());

        mFullBassCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
                if(audioEffects.isAudioEffectOn() &&
                        audioEffects.is3DSurroundOn()){

                    audioEffects.setEnableFullBass(!audioEffects.isFullBassOn());
                    postMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            aaEffectUIController.OnFullBassEnable(audioEffects.isFullBassOn());
                        }
                    });

                    FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_FULL_BASS, audioEffects.isFullBassOn());
                }
            }
        });

        mIntensityBtn = (ImageView) mInflater.findViewById(R.id.intensity_btn);
        mIntensityBtn.setOnClickListener(this);
        mIntensityTxt = (RegularTextView) mInflater.findViewById(R.id.intensity_txt);
        mIntensitySeek = (NegativeSeekBar) mInflater.findViewById(R.id.intensity_seek);
        mIntensitySeek.setProgress(audioEffects.getIntensity());
        mIntensitySeek.setOnClickListener(this);

        mEqualizerBtn = (ImageView) mInflater.findViewById(R.id.equalizer_btn);
        mEqualizerBtn.setOnClickListener(this);
        mEqualizerTxt = (RegularTextView) mInflater.findViewById(R.id.equalizer_txt);
        mEqDialogPanel = (LinearLayout) mInflater.findViewById(R.id.eq_dialog_panel);
        mEqDialogPanel.setOnClickListener(this);
        mEqualizerTxt.setOnClickListener(this);

        mSelectedEqImg = (ImageView) mInflater.findViewById(R.id.selected_eq_img);
        mSelectedEqTxt = (RegularTextView) mInflater.findViewById(R.id.selected_eq_txt);
        mSelectedEqGoImg = (ImageView) mInflater.findViewById(R.id.selected_eq_go_img);

        mDisableIntensity = (RegularTextView) mInflater.findViewById(R.id.intensity_disable_img);
        mDisableIntensity.setOnTouchListener(this);
        eq_names = Arrays.asList(mActivity.getResources().getStringArray(R.array.eq_names));
        eq_active_off = mActivity.getResources().obtainTypedArray(R.array.eq_active_off);

        mSelectedEqImg.setImageDrawable(eq_active_off.getDrawable(audioEffects.getSelectedEqualizerPosition()));
        mSelectedEqTxt.setText(eq_names.get(audioEffects.getSelectedEqualizerPosition()));


        setEffectIntensity();

        switchAudioEffect();

        setEnableEffects();
    }

    private void switchAudioEffect(){

        mEffectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
                if(audioEffects.isAudioEffectOn() != enable) {
                    audioEffects.setEnableAudioEffect(!audioEffects.isAudioEffectOn());
                    setEnableEffects();
                    postMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            aaEffectUIController.OnEffectEnable(isEffectOn);
                        }
                    });
                    MixPanelAnalyticHelper.track(mActivity, enable ? AnalyticsHelper.EVENT_EFFECTS_TURNED_ON : AnalyticsHelper.EVENT_EFFECTS_TURNED_OFF);
                    FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_EFFECT_STATE_CHANGED, audioEffects.isAudioEffectOn());
                    FlurryAnalyticHelper.logEvent(enable ? AnalyticsHelper.EVENT_EFFECTS_TURNED_ON : AnalyticsHelper.EVENT_EFFECTS_TURNED_OFF);
                }
                Preferences.writeBoolean(mActivity, Preferences.TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER, false);
                Preferences.writeBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, false);
            }
        });
    }

    private void setEnableEffects(){
        isEffectOn = audioEffects.isAudioEffectOn();
        mOldIntensity = audioEffects.getIntensity()/(double)100;
        if(isEffectOn){
            mEffectSwitchTxt.setText(mActivity.getString(R.string.on));

            setEnable3DEffect(audioEffects.is3DSurroundOn());

            setEnableIntensity(audioEffects.isIntensityOn());

            setEnableEqualizer(audioEffects.isEqualizerOn());

        }else{
            mEffectSwitchTxt.setText(mActivity.getString(R.string.off));

            setEnable3DEffect(audioEffects.is3DSurroundOn());

            setEnableIntensity(audioEffects.isIntensityOn());

            setEnableEqualizer(audioEffects.isEqualizerOn());
        }
    }

    private void setEnable3DEffect(boolean enable){
        boolean effectsOn = audioEffects.isAudioEffectOn();
        m3DSurroundBtn.setEnabled(effectsOn);
        m3DSurroundTxt.setEnabled(effectsOn);
        mSpeakerBtn.setEnabled(effectsOn);

        if ( effectsOn ) {
            m3DSurroundBtn.setSelected(enable);
            m3DSurroundTxt.setSelected(enable);
            mSpeakerBtn.setSelected(enable);
        }

        setEnableFullBass(audioEffects.isFullBassOn());
    }

    private void setEnableFullBass(boolean enable){
        mFullBassCheck.setEnabled(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn());
    }

    private void setEnableIntensity(boolean enable) {
        boolean effectsOn = audioEffects.isAudioEffectOn();
        mIntensityBtn.setEnabled(effectsOn);
        mIntensityTxt.setEnabled(effectsOn);
        mIntensitySeek.setDisable(!(enable && effectsOn));

        if ( effectsOn ) {
            mIntensityBtn.setSelected(enable);
            mIntensityTxt.setSelected(enable);
        }

        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_INTENSITY_STATE_CHANGED);
    }

    private void setEnableEqualizer(boolean active) {
        setChangeEqualizerValue(audioEffects.getSelectedEqualizerPosition());
        boolean enable = audioEffects.isAudioEffectOn();
        mEqualizerBtn.setEnabled(enable);
        mEqualizerTxt.setEnabled(enable);
        mSelectedEqGoImg.setEnabled(enable);
        mEqDialogPanel.setEnabled(enable);
        mSelectedEqTxt.setEnabled(enable);
        mSelectedEqImg.setEnabled(enable);

        if ( enable ) {
            mEqualizerBtn.setSelected(active);
            mEqualizerTxt.setSelected(active);
            mSelectedEqTxt.setSelected(active);
            mSelectedEqImg.setSelected(active);
            mSelectedEqGoImg.setSelected(active);
            mEqDialogPanel.setSelected(active);
        }
    }

    private String isAllSpeakersAreOff(){
        if(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn()) {
            if (!audioEffects.isLeftFrontSpeakerOn() && !audioEffects.isRightFrontSpeakerOn()
                    && !audioEffects.isLeftSurroundSpeakerOn() && !audioEffects.isRightSurroundSpeakerOn()) {
                return getResources().getString(R.string.all_speakers_off);
            } else if (!audioEffects.isLeftFrontSpeakerOn() || !audioEffects.isRightFrontSpeakerOn()
                    || !audioEffects.isLeftSurroundSpeakerOn() || !audioEffects.isRightSurroundSpeakerOn()) {
                return getResources().getString(R.string.some_speakers_off);
            }
        }
        return null;
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

    private void onEqDialogOpen(){
        final EqualizerDialogAdapter adapter = new EqualizerDialogAdapter(mActivity, audioEffects.getSelectedEqualizerPosition(), eq_names, eq_active_off, this);
        RecyclerView recyclerView = (RecyclerView)mActivity.getLayoutInflater()
                .inflate(R.layout.recycler_view_layout, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.scrollToPosition(audioEffects.getSelectedEqualizerPosition());
        recyclerView.setAdapter(adapter);

        MaterialDialog dialog = new MaterialDialog.Builder(mActivity)
                .title(R.string.eq_dialog_title)
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(mActivity, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(mActivity, R.color.dialog_submit_positive))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .customView(recyclerView, false)
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
        dialog.getWindow().setLayout((ScreenWidth *80)/100, (ScreenHeight *70)/100);
    }

    private void openSpeakerDialog() {
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

    private void switch3DSurround(){
        if(audioEffects.isAudioEffectOn()) {
            audioEffects.setEnable3DSurround(!audioEffects.is3DSurroundOn());
            postMessage.post(new Runnable() {
                @Override
                public void run() {
                    aaEffectUIController.On3DSurroundEnable(audioEffects.is3DSurroundOn());
                }
            });
            setEnable3DEffect(audioEffects.is3DSurroundOn());

            if(audioEffects.is3DSurroundOn()) {
                if (!audioEffects.isIntensityOn()) {
                    switchIntensity();
                }
                if (!audioEffects.isEqualizerOn()) {
                    switchEqualizer();
                }
            }
            MixPanelAnalyticHelper.track(mActivity, audioEffects.is3DSurroundOn() ? AnalyticsHelper.EVENT_3D_TURNED_ON : AnalyticsHelper.EVENT_3D_TURNED_OFF);
            FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_3D_STATE_CHANGED, audioEffects.is3DSurroundOn());
        }
    }

    private void switchIntensity(){
        if(audioEffects.isAudioEffectOn() && !audioEffects.is3DSurroundOn()){
            setEnableIntensity(!audioEffects.isIntensityOn());
            audioEffects.setEnableIntensity(!audioEffects.isIntensityOn());
        }else if(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && !audioEffects.isIntensityOn()){
            setEnableIntensity(!audioEffects.isIntensityOn());
            audioEffects.setEnableIntensity(!audioEffects.isIntensityOn());
        }else if(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && audioEffects.isIntensityOn()){
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.req_intensity), Toast.LENGTH_LONG).show();
        }
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                aaEffectUIController.OnIntensityEnable(audioEffects.isIntensityOn());
            }
        });
    }

    private void switchEqualizer(){
        if(audioEffects.isAudioEffectOn()  && !audioEffects.is3DSurroundOn()){
            setEnableEqualizer(!audioEffects.isEqualizerOn());
            audioEffects.setEnableEqualizer(!audioEffects.isEqualizerOn());
        }else if(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && !audioEffects.isEqualizerOn()){
            setEnableEqualizer(!audioEffects.isEqualizerOn());
            audioEffects.setEnableEqualizer(!audioEffects.isEqualizerOn());
        } else if(audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && audioEffects.isEqualizerOn()){
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.req_equlaizer), Toast.LENGTH_LONG).show();
        }
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                aaEffectUIController.OnEqualizerEnable(audioEffects.isEqualizerOn());
            }
        });
        MixPanelAnalyticHelper.track(mActivity, audioEffects.isEqualizerOn() ? AnalyticsHelper.EVENT_EQ_TURNED_ON : AnalyticsHelper.EVENT_EQ_TURNED_OFF);
        FlurryAnalyticHelper.logEventWithStatus(UtilAnalytics.Equalizer_status, audioEffects.isEqualizerOn());
    }

    private void setEffectIntensity() {
        mIntensitySeek.setOnSeekBarChangeListener(new NegativeSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean isUser) {
                if((progress/(double)100 - mOldIntensity >= .1 || mOldIntensity - progress/(double)100 >= .1) || progress == 100  || progress == 0) {
                    mOldIntensity = progress / (double) 100;
                    audioEffects.setIntensity(progress);

                    if (isUser)
                        postMessage.post(new Runnable() {
                            @Override
                            public void run() {
                                aaEffectUIController.OnIntensityChange(progress);
                            }
                        });
                    try {
                        FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_INTENSITY_STATE_CHANGED);
                    }catch (Exception e){}
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        FlurryAnalyticHelper.flurryStopSession(mActivity);
    }
    @Override
    public void onChangeEqualizerValue(final int position) {
        setChangeEqualizerValue(position);
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                aaEffectUIController.OnEqualizerChange(position);
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_EQ_STATE_CHANGED);
            }
        });
    }

    public void setChangeEqualizerValue(final int position) {
        postMessage.post(new Runnable() {
            @Override
            public void run() {
                mSelectedEqImg.setImageDrawable(eq_active_off.getDrawable(position));
                mSelectedEqTxt.setText(eq_names.get(position));
                HashMap<String, String> articleParams = new HashMap<>();
                articleParams.put(UtilAnalytics.PARAM_SELECTED_EQUALIZER, eq_names.get(position));
                FlurryAnalyticHelper.logEvent(UtilAnalytics.Type_of_Equalizer_selected, articleParams);
                audioEffects.setSelectedEqualizerPosition(position);
            }
        });
        HashMap<String, String> articleParams = new HashMap<>();
        articleParams.put(UtilAnalytics.Type_Equalizer_selected, eq_names.get(position));
        FlurryAnalyticHelper.logEvent(UtilAnalytics.Equalizer_selected, articleParams);
    }

    private void updateSpeakers(@AudioEffect.Speaker final int speakerType){
        boolean enable = false;
        switch (speakerType) {
            case AudioEffect.SPEAKER_FRONT_LEFT:
                enable = !audioEffects.isLeftFrontSpeakerOn();
                audioEffects.setEnableLeftFrontSpeaker(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_FRONT_LEFT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_FRONT_RIGHT:
                enable = !audioEffects.isRightFrontSpeakerOn();
                audioEffects.setEnableRightFrontSpeaker(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_FRONT_RIGHT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_SURROUND_LEFT:
                enable = !audioEffects.isLeftSurroundSpeakerOn();
                audioEffects.setEnableLeftSurroundSpeaker(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_REAR_LEFT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_SURROUND_RIGHT:
                enable = !audioEffects.isRightSurroundSpeakerOn();
                audioEffects.setEnableRightSurroundSpeaker(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_REAR_RIGHT_SPEAKER, enable);
                break;

            case AudioEffect.SPEAKER_TWEETER:
                enable = !audioEffects.isTweeterOn();
                audioEffects.setEnableTweeter(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_TWEETER, enable);
                break;

            case AudioEffect.SPEAKER_WOOFER:
                enable = !audioEffects.isWooferOn();
                audioEffects.setEnableWoofer(enable);
                FlurryAnalyticHelper.logEventWithStatus(AnalyticsHelper.EVENT_SUBWOOFER, enable);
                break;
        }

        updateSpeakers(mSpeakerDialogPanel);
        aaEffectUIController.OnSpeakerEnable(speakerType, enable);
    }

    private void dismissTooltip() {
        if (null != coachMarkEffectPager) {
            coachMarkEffectPager.dismissTooltip();
        }
        if (null != coachMarkEffectPlayer) {
            coachMarkEffectPlayer.dismissTooltip();
        }
        if (null != coachMarkEffectSwitcher) {
            coachMarkEffectSwitcher.dismissTooltip();
        }
    }

    private void showProgressLoader(){
        if(null != mPlayingMediaItem && mPlayingMediaItem.getMediaType() != MediaType.DEVICE_MEDIA_LIB &&
                View.GONE == mLoadingProgress.getVisibility() && ConnectivityReceiver.isNetworkAvailable(mActivity, true))
            mLoadingProgress.post(new Runnable() {
                @Override
                public void run() {
                    mLoadingProgress.setVisibility(View.VISIBLE);
                }
            });
        else
            stopLoadProgress();
    }

    private void stopLoadProgress(){
        if(View.VISIBLE == mLoadingProgress.getVisibility())
            mLoadingProgress.setVisibility(View.GONE);
    }
}
