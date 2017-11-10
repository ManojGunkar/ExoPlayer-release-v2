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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.dialogs.EqualizerDialog;
import com.globaldelight.boom.app.dialogs.SpeakerDialog;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.playbackEvent.controller.PlayerUIController;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.utils.OverFlowMenuUtils;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.view.CoachMarkerWindow;
import com.globaldelight.boom.view.NegativeSeekBar;
import com.globaldelight.boom.view.slidinguppanel.SlidingUpPanelLayout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_DISCONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_QUEUE_COMPLETED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SONG_CHANGED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_STOP_UPDATING_UPNEXT_DB;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_REPEAT;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_SHUFFLE;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_UPDATE_TRACK_POSITION;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_OPEN_EFFECT_MINI_PLAYER;
import static com.globaldelight.boom.app.sharedPreferences.Preferences.TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT;
import static com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue.REPEAT_ALL;
import static com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue.REPEAT_NONE;
import static com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue.REPEAT_ONE;
import static com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue.SHUFFLE_OFF;
import static com.globaldelight.boom.playbackEvent.handler.UpNextPlayingQueue.SHUFFLE_ON;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_BOTTOM_CENTER;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_TOP_CENTER;
import static com.globaldelight.boom.view.CoachMarkerWindow.DRAW_TOP_LEFT;

/**
 * Created by Rahul Agarwal on 16-01-17.
 */

public class MasterContentFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, Observer {
    private final String TAG = "MasterContentFragment";
    private static boolean isCloudSeek = false;
    public static boolean isUpdateUpnextDB = true;
    private static MediaItem mPlayingMediaItem;
    private static boolean mIsPlaying, mIsLastPlayed;


    private long mItemId = -1;
    private boolean isUser = false;

    private Activity mActivity;
    private ProgressBar mLoadingProgress;
    private AudioEffect audioEffects;


    /************************************************************************************/

    public View miniController, mPlayerActionPanel;

    private TextView mLargeSongTitle, mLargeSongSubTitle, mTotalSeekTime, mCurrentSeekTime;
    private View mInflater;
    private int colorTo, colorFrom, colorFromActive;
    private AppCompatSeekBar mTrackSeek;
    private ImageView mNext, mPlayPause, mPrevious, mShuffle, mRepeat, mPlayerBackBtn, mLargeAlbumArt;
    private ImageView mEffectTab, mPlayerTab;
    private ImageView mUpNextBtnPanel, mPlayerOverFlowMenuPanel;
    private LinearLayout mEffectContent, mPlayerLarge, mPlayerTitlePanel;
    private FrameLayout mPlayerContent;
    private FrameLayout mPlayerBackground;

    private LinearLayout mMiniPlayerEffectPanel, mMiniTitlePanel;
    private TextView mMiniSongTitle, mMiniSongSubTitle;
    private ImageView mMiniPlayerPlayPause, mMiniPlayerEffect;
    private AppCompatSeekBar mMiniPlayerSeek;

    private NegativeSeekBar mIntensitySeek;
    private SwitchCompat mEffectSwitch;
    private ToggleButton mFullBassCheck;
    private TextView mEffectSwitchTxt, mSelectedEqTxt;
    private ImageView mSpeakerBtn, mSelectedEqImg, mSelectedEqGoImg;
    private CheckBox m3DSurroundBtn, mIntensityBtn, mEqualizerBtn;
    private LinearLayout mEqDialogPanel;

    private List<String> eq_names;
    private TypedArray eq_active_off;

    private int ScreenWidth, ScreenHeight;
    private PlayerUIController playerUIController;
    private Handler postMessage;

    private CoachMarkerWindow coachMarkEffectPager, coachMarkEffectPlayer, coachMarkEffectSwitcher;

    private BroadcastReceiver mPlayerBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            IMediaItem item;
            switch (intent.getAction()) {
                case ACTION_SONG_CHANGED:
                    item = intent.getParcelableExtra("playing_song");
                    if (item != null) {
                        mPlayingMediaItem = (MediaItem) item;
                        mIsPlaying = intent.getBooleanExtra("playing", false);
                        mIsLastPlayed = false;
                        mTrackSeek.setProgress(0);
                        mMiniPlayerSeek.setProgress(0);
                        updatePlayerUI();
                    }
                    stopLoadProgress();
                    break;

                case ACTION_PLAYER_STATE_CHANGED:
                    try {
                        if (App.playbackManager().isTrackPlaying()) {
                            mMiniPlayerPlayPause.setImageResource(R.drawable.ic_miniplayer_pause);
                            mPlayPause.setImageResource(R.drawable.ic_player_pause);
                        } else {
                            mMiniPlayerPlayPause.setImageResource(R.drawable.ic_miniplayer_play);
                            mPlayPause.setImageResource(R.drawable.ic_player_play);
                        }
                    } catch (Exception e) {
                    }
                    stopLoadProgress();
                    break;
                case ACTION_QUEUE_COMPLETED:
                    mPlayingMediaItem = (MediaItem) App.playbackManager().queue().getPlayingItem();
                    mIsPlaying = false;
                    mIsLastPlayed = true;
                    mTrackSeek.setProgress(0);
                    mMiniPlayerSeek.setProgress(0);
                    updateTrackPlayTime(0, 0);
                    updatePlayerUI(false);
                    showProgressLoader();
                    break;
                case ACTION_UPDATE_TRACK_POSITION:
                    long duration = App.playbackManager().getDuration();
                    long current = App.playbackManager().getPosition();
                    if (!isUser) {
                        int percent = duration > 0 ? (int) ((current * 100) / duration) : 0;
                        mTrackSeek.setProgress(percent);
                        mMiniPlayerSeek.setProgress(percent);
                    }
                    if (isCloudSeek) {
                        stopLoadProgress();
                        isCloudSeek = false;
                    }

                    if (!isUser)
                        updateTrackPlayTime(duration, current);
                    break;
                case ACTION_UPDATE_SHUFFLE:
                    updateShuffle();
                    break;
                case ACTION_UPDATE_REPEAT:
                    updateRepeat();
                    updatePreviousNext(App.playbackManager().queue().isPrevious(), App.playbackManager().queue().isNext());
                    break;
                case ACTION_STOP_UPDATING_UPNEXT_DB:
                    isUpdateUpnextDB = false;
                    break;
                case ACTION_ON_NETWORK_DISCONNECTED:
                    stopLoadProgress();
                    break;
            }
        }
    };

    public void onBackPressed() {
        dismissTooltip();
    }

    private SlidingUpPanelLayout mSlidingPanel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mInflater = inflater.inflate(R.layout.fragment_content_master, container, false);
        if (null == mActivity)
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
        mPlayerBackground = (FrameLayout) mInflater.findViewById(R.id.player_src_background);

        initMiniPlayer();
        initLargePlayer();
        initEffectControl();

        setPlayerInfo();
    }

    @Override
    public void onResume() {
        super.onResume();
        setMiniPlayerVisible(!mSlidingPanel.isPanelExpanded());
        updateProgressLoader();
    }

    private void setPlayerEnable(boolean isEnable) {

        mPlayerTab.setSelected(isEnable);
        mEffectTab.setSelected(!isEnable);
        mEffectTab.setImageResource(audioEffects.isAudioEffectOn()? R.drawable.effect_tab_on : R.drawable.effect_tab_off);

        if (isEnable) {
            mPlayerContent.setVisibility(View.VISIBLE);
            mEffectContent.setVisibility(View.GONE);

        } else {
            mPlayerContent.setVisibility(View.GONE);
            mEffectContent.setVisibility(View.VISIBLE);
            mEffectSwitch.setChecked(audioEffects.isAudioEffectOn());
            String msg = isAllSpeakersAreOff();
            if (null != msg)
                Toast.makeText(mActivity, msg, Toast.LENGTH_SHORT).show();
        }
    }

    private void setPlayerInfo() {
        mPlayingMediaItem = (MediaItem) App.playbackManager().queue().getPlayingItem();
        mIsPlaying = App.playbackManager().isPlaying();
        mIsLastPlayed = (null != App.playbackManager().getPlayingItem() ?
                (!App.playbackManager().isPlaying() && !App.playbackManager().isPaused() ? true : false) :
                false);

        updatePlayerSeekAndTime();
        try {
            updatePlayerUI();
        } catch (Exception e) {

        }
    }

    private void updatePlayerSeekAndTime() {
        if (null != mPlayingMediaItem && !mIsPlaying && !mIsLastPlayed) {
            long currentMillis = App.playbackManager().getPosition();
            long totalMillis = App.playbackManager().getDuration();
            int progress = totalMillis > 0 ? (int) (currentMillis * 100 / totalMillis) : 0;

            if (null != mTrackSeek)
                mTrackSeek.setProgress(progress);

            if (null != mMiniPlayerSeek)
                mMiniPlayerSeek.setProgress(progress);

            updateTrackPlayTime(totalMillis, currentMillis);
        }
    }

    /* Large Player UI and Functionality*/
    private void updateActionBarButtons() {
        if (App.playbackManager().queue().getUpNextItemCount() > 0) {
            mUpNextBtnPanel.setVisibility(View.VISIBLE);
            mPlayerOverFlowMenuPanel.setVisibility(View.VISIBLE);
        } else {
            mUpNextBtnPanel.setVisibility(View.INVISIBLE);
            mPlayerOverFlowMenuPanel.setVisibility(View.INVISIBLE);
        }
    }

    private void updateShuffle() {
        switch (App.getUserPreferenceHandler().getShuffle()) {
            case SHUFFLE_OFF:
                mShuffle.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_shuffle_off, null));
                break;
            case SHUFFLE_ON:
                mShuffle.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_shuffle_on, null));
                break;
        }
    }

    private void updateRepeat() {
        switch (App.getUserPreferenceHandler().getRepeat()) {
            case REPEAT_NONE:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_off, null));
                break;
            case REPEAT_ONE:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_one, null));
                break;
            case REPEAT_ALL:
                mRepeat.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_repeat_on, null));
                break;
        }
    }

    private void updatePreviousNext(boolean prev_enable, boolean next_enable) {
        mPrevious.setEnabled(prev_enable);
        mNext.setEnabled(next_enable);
    }

    private void updateAlbumArt(final IMediaItem item) {
        new AsyncTask<Void, Void, Bitmap[]>() {

            private Context context = mActivity;

            @Override
            protected Bitmap[] doInBackground(Void... params) {
                if (context == null) {
                    return null;
                }

                Bitmap[] result = new Bitmap[2];
                boolean failed = false;
                if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeFile(item.getItemArtUrl());
                        Bitmap blurredBitmap = PlayerUtils.createBackgoundBitmap(context, bitmap, ScreenWidth / 10, ScreenHeight / 10);
                        result[0] = bitmap;
                        result[1] = blurredBitmap;
                    } catch (Exception e) {
                        failed = true;
                    }
                } else {
                    failed = true;
                }

                if (failed) {
                    Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.ic_default_art_player_header);
                    Bitmap blurredBitmap = PlayerUtils.createBackgoundBitmap(context, bitmap, ScreenWidth / 10, ScreenHeight / 10);
                    result[0] = bitmap;
                    result[1] = blurredBitmap;
                }

                return result;
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmaps) {
                if (bitmaps == null || bitmaps.length != 2) {
                    return;
                }

                final Bitmap bitmap = bitmaps[0];
                final Bitmap blurredBitmap = bitmaps[1];
                if (mItemId == -1 || mItemId != item.getItemId()) {
                    PlayerUtils.ImageViewAnimatedChange(context, mLargeAlbumArt, bitmap);
                    mItemId = item.getItemId();
                } else {
                    mLargeAlbumArt.setImageBitmap(bitmap);
                }
                mPlayerBackground.setBackground(new BitmapDrawable(context.getResources(), blurredBitmap));
            }
        }.execute();
    }

    private void changeProgress(int progress) {
        if (null != mPlayingMediaItem) {
            long totalTime = mPlayingMediaItem.getDurationLong();
            int totalProgress = 100;
            long currentTime = (totalTime / totalProgress) * progress;
            updateTrackPlayTime(totalTime, currentTime);
        }
    }

    private void updateTrackPlayTime(long totalMillis, long currentMillis) {
        if (null != mCurrentSeekTime)
            mCurrentSeekTime.setText(String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(currentMillis),
                    TimeUnit.MILLISECONDS.toSeconds(currentMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentMillis))));
        if (null != mTotalSeekTime)
            mTotalSeekTime.setText("-" + String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis),
                    TimeUnit.MILLISECONDS.toSeconds(totalMillis - currentMillis) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMillis - currentMillis))));
    }

    private void initLargePlayer() {
        mPlayerLarge = mInflater.findViewById(R.id.player_large);
        mPlayerLarge.setOnTouchListener(this);

        mPlayerActionPanel = mInflater.findViewById(R.id.player_action_bar);

        mLoadingProgress = mInflater.findViewById(R.id.load_cloud);

        mPlayerBackBtn = mInflater.findViewById(R.id.player_back_button);
        mPlayerBackBtn.setOnClickListener(this);
        mPlayerTitlePanel = mInflater.findViewById(R.id.player_title_panel);
        mPlayerTitlePanel.setOnClickListener(this);
        mLargeSongTitle = mInflater.findViewById(R.id.large_player_title);
        mLargeSongSubTitle = mInflater.findViewById(R.id.large_player_sub_title);
        mUpNextBtnPanel = mInflater.findViewById(R.id.player_upnext_button);
        mUpNextBtnPanel.setOnClickListener(this);
        mPlayerOverFlowMenuPanel = mInflater.findViewById(R.id.player_overflow_button);
        mPlayerOverFlowMenuPanel.setOnClickListener(this);

        mLargeAlbumArt = mInflater.findViewById(R.id.player_album_art);

        LinearLayout.LayoutParams artParam = new LinearLayout.LayoutParams((int) (ScreenWidth * 80) / 100, (int) (ScreenWidth * 80) / 100);
        artParam.setMargins((int) ((ScreenWidth * 10) / 100), 0, (int) ((ScreenWidth * 10) / 100), 0);
        mInflater.findViewById(R.id.player_large_header).setLayoutParams(artParam);
        mPlayerContent = mInflater.findViewById(R.id.player_content);

        mPlayerTab = mInflater.findViewById(R.id.player_tab);
        mPlayerTab.setOnClickListener(this);
        mEffectTab = mInflater.findViewById(R.id.effect_tab);
        mEffectTab.setOnClickListener(this);

        mTrackSeek = mInflater.findViewById(R.id.control_seek_bar);
        mTrackSeek.getProgressDrawable().setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
        mTrackSeek.setPadding(mTrackSeek.getPaddingLeft(), 0, mTrackSeek.getPaddingRight(), 0);

        mTrackSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
                if (fromUser) {
                    isUser = true;
                    changeProgress(progress);
                    if (App.playbackManager().getPlayingItem().getMediaType() != MediaType.DEVICE_MEDIA_LIB)
                        showProgressLoader();
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Playing_SeekBar_Used_in_Effects_screen);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (isUser)
                    postMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            playerUIController.OnPlayerSeekChange(mTrackSeek.getProgress());
                        }
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
        mCurrentSeekTime = mInflater.findViewById(R.id.played_time);
        mTotalSeekTime = mInflater.findViewById(R.id.remain_time);

        mRepeat = mInflater.findViewById(R.id.controller_repeat);
        mRepeat.setOnClickListener(this);
        mPrevious = mInflater.findViewById(R.id.controller_prev);
        mPrevious.setOnClickListener(this);
        mPlayPause = mInflater.findViewById(R.id.controller_play);
        mPlayPause.setOnClickListener(this);
        mNext = (ImageView) mInflater.findViewById(R.id.controller_next);
        mNext.setOnClickListener(this);
        mShuffle = (ImageView) mInflater.findViewById(R.id.controller_shuffle);
        mShuffle.setOnClickListener(this);
    }

    private void updatePlayerUI() {
        updatePlayerUI(true);
    }

    private void updatePlayerUI(boolean mediaChanged) {
//        if(MasterActivity.isPlayerExpended()){
        updateLargePlayerUI(mPlayingMediaItem, mIsPlaying, mIsLastPlayed);
//        }else {
        updateMiniPlayerUI(mPlayingMediaItem, mIsPlaying, mIsLastPlayed);
//        }
        if (null != mPlayingMediaItem && mediaChanged)
            updateAlbumArt(mPlayingMediaItem);
        updateAudioEffectUI();

        updateActionBarButtons();
    }

    private void updateLargePlayerUI(MediaItem item, boolean isPlaying, boolean isLastPlayedItem) {
        if (null != item) {
            switch (App.getUserPreferenceHandler().getShuffle()) {
                case SHUFFLE_OFF:
                    DrawableCompat.setTint(mShuffle.getDrawable(), colorFrom);
                    break;
                case SHUFFLE_ON:
                    DrawableCompat.setTint(mShuffle.getDrawable(), colorFromActive);
                    break;
            }
            switch (App.getUserPreferenceHandler().getRepeat()) {
                case REPEAT_NONE:
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

            if (isLastPlayedItem) {
                mTrackSeek.setProgress(0);
                mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_play, null));

                long totalMillis = item.getDurationLong();
                long currentMillis = 0;

                if (!isUser)
                    updateTrackPlayTime(totalMillis, currentMillis);

            } else {
                boolean isMediaItem = item.getMediaType() == MediaType.DEVICE_MEDIA_LIB;
                if (isPlaying) {
                    mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_pause, null));
                } else {
                    mPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_player_play, null));
                }
            }
        } else if (!isLastPlayedItem) {
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
        updatePreviousNext(App.playbackManager().queue().isPrevious(), App.playbackManager().queue().isNext());
        updateShuffle();
        updateRepeat();
    }

    /* Mini Player UI & Functionality*/
    private void initMiniPlayer() {
        miniController = mInflater.findViewById(R.id.small_panel);
        miniController.setOnTouchListener(this);

        mMiniPlayerSeek = (AppCompatSeekBar) mInflater.findViewById(R.id.mini_player_progress);
        mMiniPlayerSeek.setPadding(0, 0, 0, 0);
        mMiniPlayerSeek.setOnTouchListener(this);

        mMiniPlayerEffectPanel = (LinearLayout) mInflater.findViewById(R.id.mini_player_boom_effect);
        mMiniPlayerEffectPanel.setOnClickListener(this);
        mMiniPlayerEffect = (ImageView) mInflater.findViewById(R.id.mini_player_effect_img);
        mMiniPlayerEffect.setOnClickListener(this);

        mMiniPlayerPlayPause = (ImageView) mInflater.findViewById(R.id.mini_player_play_pause_btn);
        mMiniPlayerPlayPause.setOnClickListener(this);
        mMiniTitlePanel = (LinearLayout) mInflater.findViewById(R.id.mini_player_title_panel);
        mMiniTitlePanel.setOnClickListener(this);
        mMiniSongTitle = mInflater.findViewById(R.id.mini_player_song_title);
        mMiniSongSubTitle = mInflater.findViewById(R.id.mini_player_song_sub_title);
    }

    private void updateMiniPlayerUI(MediaItem item, boolean isPlaying, boolean isLastPlayedItem) {
        updateMiniPlayerEffectUI(audioEffects.isAudioEffectOn());
        if (null != item) {
            DrawableCompat.setTint(mMiniPlayerPlayPause.getDrawable(), colorFrom);
            mMiniSongTitle.setSelected(true);
            mMiniSongSubTitle.setSelected(true);
            mMiniSongTitle.setText(item.getItemTitle());
            mMiniSongSubTitle.setVisibility(null != item.getItemArtist() ? View.VISIBLE : View.GONE);
            mMiniSongSubTitle.setText(item.getItemArtist());
            if (isPlaying)
                mMiniPlayerPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_pause, null));
            else
                mMiniPlayerPlayPause.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_play, null));
        } else if (!isLastPlayedItem) {
            DrawableCompat.setTint(mMiniPlayerPlayPause.getDrawable(), colorTo);
        }
    }

    private void updateMiniPlayerEffectUI(boolean enable) {
        if (enable) {
            mMiniPlayerEffect.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_effects_on, null));
        } else {
            mMiniPlayerEffect.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.ic_miniplayer_effects, null));
        }
    }

    public void setMiniPlayerVisible(boolean isMiniPlayerVisible) {
        if (isMiniPlayerVisible) {
            miniController.setAlpha(1);
            mPlayerActionPanel.setAlpha(0);
            mPlayerActionPanel.setVisibility(View.INVISIBLE);
            miniController.setVisibility(View.VISIBLE);
            mLargeSongSubTitle.setEnabled(false);
            mLargeSongTitle.setEnabled(false);
        } else {
            miniController.setAlpha(0);
            mPlayerActionPanel.setAlpha(1);
            mPlayerActionPanel.setVisibility(View.VISIBLE);
            miniController.setVisibility(View.INVISIBLE);
            mLargeSongSubTitle.setEnabled(true);
            mLargeSongTitle.setEnabled(true);
        }
    }

    /* Player Slider Callbacks*/
    public void onPanelSlide(View panel, float slideOffset, boolean isEffectOpened) {
        if (slideOffset < 0.1) {
            setMiniPlayerVisible(true);
        } else {
            setMiniPlayerVisible(false);
        }
    }

    public void onPanelCollapsed(View panel) {
        setMiniPlayerVisible(true);
        updateMiniPlayerUI(mPlayingMediaItem, App.playbackManager().isTrackPlaying(), mIsLastPlayed);
        showEffectShortCut();

        if (null != coachMarkEffectSwitcher) {
            coachMarkEffectSwitcher.dismissTooltip();
        }
        if (null != coachMarkEffectPager) {
            coachMarkEffectPager.dismissTooltip();
        }
    }

    public void onPanelExpanded(View panel) {
        setMiniPlayerVisible(false);

        showEffectSwitchTip();

        if (null != coachMarkEffectPlayer) {
            coachMarkEffectPlayer.dismissTooltip();
        }
        updateProgressLoader();

        updateActionBarButtons();
    }

    private void showEffectSwitchTip() {
        if (null != mEffectContent && mEffectContent.getVisibility() == View.VISIBLE && Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true) && !App.playbackManager().isStopped() && mInflater.findViewById(R.id.effect_switch).getVisibility() == View.VISIBLE) {
            coachMarkEffectSwitcher = new CoachMarkerWindow(mActivity, DRAW_BOTTOM_CENTER, getResources().getString(R.string.effect_player_tooltip));
            coachMarkEffectSwitcher.setAutoDismissBahaviour(true);
            coachMarkEffectSwitcher.showCoachMark(mInflater.findViewById(R.id.effect_switch));
        }

        if (null != mPlayerContent && mPlayerContent.getVisibility() == View.VISIBLE && Preferences.readBoolean(mActivity, Preferences.TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER, true) && mPlayerContent.getVisibility() == View.VISIBLE &&
                !App.playbackManager().isStopped() && Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true)) {
            coachMarkEffectPager = new CoachMarkerWindow(mActivity, DRAW_TOP_CENTER, getResources().getString(R.string.switch_effect_screen_tooltip));
            coachMarkEffectPager.setAutoDismissBahaviour(true);
            coachMarkEffectPager.showCoachMark(mInflater.findViewById(R.id.effect_tab));
        }
    }

    private void showEffectShortCut() {
        if (Preferences.readBoolean(mActivity, TOOLTIP_OPEN_EFFECT_MINI_PLAYER, true) && !Preferences.readBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, true)) {
            coachMarkEffectPlayer = new CoachMarkerWindow(mActivity, DRAW_TOP_LEFT, getResources().getString(R.string.library_switch_effect_screen_tooltip));
            coachMarkEffectPlayer.setAutoDismissBahaviour(true);
            Preferences.writeBoolean(mActivity, Preferences.TOOLTIP_OPEN_EFFECT_MINI_PLAYER, false);
            coachMarkEffectPlayer.showCoachMark(mInflater.findViewById(R.id.mini_player_effect_img));
        }
    }

    public void onPanelAnchored(View panel) {

    }

    public void onPanelHidden(View panel) {

    }

    private void updateProgressLoader() {
        if (App.playbackManager().isTrackLoading())
            showProgressLoader();
        else
            stopLoadProgress();
    }

    @Override
    public void onStart() {
        setPlayerInfo();
        super.onStart();
        audioEffects.addObserver(this);
        registerPlayerReceiver(mActivity);
    }


/*Player Screen Utils*/

    private void registerPlayerReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_SONG_CHANGED);
        intentFilter.addAction(ACTION_PLAYER_STATE_CHANGED);
        intentFilter.addAction(ACTION_QUEUE_COMPLETED);
        intentFilter.addAction(ACTION_UPDATE_TRACK_POSITION);
        intentFilter.addAction(ACTION_UPDATE_SHUFFLE);
        intentFilter.addAction(ACTION_UPDATE_REPEAT);
        intentFilter.addAction(ACTION_STOP_UPDATING_UPNEXT_DB);
        intentFilter.addAction(ACTION_ON_NETWORK_DISCONNECTED);
        LocalBroadcastManager.getInstance(context).registerReceiver(mPlayerBroadcastReceiver, intentFilter);

        setPlayerInfo();
    }

    private void unregisterPlayerReceiver(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(mPlayerBroadcastReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.player_back_button:
            case R.id.mini_player_effect_img:
            case R.id.mini_player_boom_effect:
                if (!MasterActivity.isPlayerExpended()) {
                    setPlayerEnable(false);
                }

                toggleSlidingPanel();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Effects_Screen_Opened_from_Mini_Player);

                break;
            case R.id.mini_player_title_panel:
            case R.id.player_title_panel:
                setPlayerEnable(true);
                toggleSlidingPanel();
                break;
            case R.id.player_upnext_button:
                startUpNextActivity();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.UpNext_Button_Tapped);
                break;

            case R.id.player_overflow_button:
                overFlowMenu(mActivity, view);
                break;

            case R.id.mini_player_play_pause_btn:
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Play_Pause_Button_tapped_Mini_Player);
            case R.id.controller_play:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnPlayPause();
                    }
                });
                showProgressLoader();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Songs_Pause_Or_Play_From_Effects_Screen);
                break;
            case R.id.controller_prev:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnPreviousTrackClick();
                    }
                });
                showProgressLoader();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Previous_button_tapped_From_effect_screen);
                break;
            case R.id.controller_next:
                postMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        playerUIController.OnNextTrackClick();
                    }
                });
                showProgressLoader();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Next_button_tapped_From_effect_screen);
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
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Player_Tab_PLayer_screen);
                break;
            case R.id.effect_tab:
                setPlayerEnable(false);
                showEffectSwitchTip();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Effects_Opened_From_Player_Screen);
                break;
            case R.id.three_surround_btn:
                switch3DSurround();
                break;
            case R.id.intensity_btn:
                switchIntensity();
                break;
            case R.id.equalizer_btn:
                switchEqualizer();
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Equalizer_status, audioEffects.isEqualizerOn());
                FlurryAnalytics.getInstance(getActivity()).setEvent(audioEffects.isEqualizerOn() ? FlurryEvents.EVENT_EQ_TURNED_ON : FlurryEvents.EVENT_EQ_TURNED_OFF);
                break;
            case R.id.eq_dialog_panel:
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Open_Equalizer_Dailog_from_DropDown);
                if (audioEffects.isAudioEffectOn() && audioEffects.isEqualizerOn())
                    onEqDialogOpen();
                break;
            case R.id.speaker_btn:
                FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Speaker_Dialog_Opened_From_Arrow);
                openSpeakerDialog();
                break;
        }
    }



    private void overFlowMenu(Context context, View view) {
        OverFlowMenuUtils.showMediaItemMenu(mActivity, view, R.menu.player_popup, App.playbackManager().getPlayingItem());
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        switch (view.getId()) {
            case R.id.small_panel:
                return true;
            case R.id.player_large:
                return true;
            case R.id.mini_player_progress:
                return true;
        }
        return false;
    }

    /*Audio Effect UI & Functionality*/

    private void initEffectControl() {
        postMessage = new Handler();

//        FrameLayout.LayoutParams effectParam = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (ScreenWidth*85)/100);
        mEffectContent = (LinearLayout) mInflater.findViewById(R.id.effect_content);
//        mEffectContent.setLayoutParams(effectParam);

        mEffectSwitchTxt = mInflater.findViewById(R.id.effect_switch_txt);
        mEffectSwitch = mInflater.findViewById(R.id.effect_switch);
        mEffectSwitch.setChecked(audioEffects.isAudioEffectOn());

        m3DSurroundBtn = mInflater.findViewById(R.id.three_surround_btn);
        m3DSurroundBtn.setOnClickListener(this);

        mSpeakerBtn = mInflater.findViewById(R.id.speaker_btn);
        mSpeakerBtn.setOnClickListener(this);

        mFullBassCheck = mInflater.findViewById(R.id.fullbass_chk);
        mFullBassCheck.setOnCheckedChangeListener(new ToggleButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked != audioEffects.isFullBassOn()) {
                    audioEffects.setEnableFullBass(isChecked);
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_FULL_BASS, audioEffects.isFullBassOn());
                }
            }
        });


        mIntensityBtn = mInflater.findViewById(R.id.intensity_btn);
        mIntensityBtn.setOnClickListener(this);
        mIntensitySeek = mInflater.findViewById(R.id.intensity_seek);
        mIntensitySeek.setProgress((int) (audioEffects.getIntensity() * 50 + 50));
        mIntensitySeek.setOnClickListener(this);

        mEqualizerBtn = mInflater.findViewById(R.id.equalizer_btn);
        mEqualizerBtn.setOnClickListener(this);
        mEqDialogPanel = mInflater.findViewById(R.id.eq_dialog_panel);
        mEqDialogPanel.setOnClickListener(this);

        mSelectedEqImg = mInflater.findViewById(R.id.selected_eq_img);
        mSelectedEqTxt = mInflater.findViewById(R.id.selected_eq_txt);
        mSelectedEqGoImg = mInflater.findViewById(R.id.selected_eq_go_img);

        eq_names = Arrays.asList(mActivity.getResources().getStringArray(R.array.eq_names));
        eq_active_off = mActivity.getResources().obtainTypedArray(R.array.eq_active_off);

        mSelectedEqImg.setImageDrawable(eq_active_off.getDrawable(audioEffects.getSelectedEqualizerPosition()));
        mSelectedEqTxt.setText(eq_names.get(audioEffects.getSelectedEqualizerPosition()));


        setEffectIntensity();

        switchAudioEffect();

        updateAudioEffectUI();
    }

    private void switchAudioEffect() {

        mEffectSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean enable) {
                if (audioEffects.isAudioEffectOn() != enable) {
                    audioEffects.setEnableAudioEffect(!audioEffects.isAudioEffectOn());
                    MixPanelAnalyticHelper.track(mActivity, enable ? AnalyticsHelper.EVENT_EFFECTS_TURNED_ON : AnalyticsHelper.EVENT_EFFECTS_TURNED_OFF);
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_EFFECT_STATE_CHANGED, audioEffects.isAudioEffectOn());
                    FlurryAnalytics.getInstance(getActivity()).setEvent(enable ? FlurryEvents.EVENT_EFFECTS_TURNED_ON : FlurryEvents.EVENT_EFFECTS_TURNED_OFF);
                }
                Preferences.writeBoolean(mActivity, Preferences.TOOLTIP_SWITCH_EFFECT_LARGE_PLAYER, false);
                Preferences.writeBoolean(mActivity, TOOLTIP_SWITCH_EFFECT_SCREEN_EFFECT, false);
            }
        });
    }

    private void updateAudioEffectUI() {
        boolean isEffectOn = audioEffects.isAudioEffectOn();
        if (mEffectSwitch.isChecked() != isEffectOn) {
            mEffectSwitch.setChecked(isEffectOn);
        }

        mEffectSwitchTxt.setText(isEffectOn ? R.string.on : R.string.off);
        mEffectTab.setImageResource(isEffectOn? R.drawable.effect_tab_on : R.drawable.effect_tab_off);

        boolean isSurroundOn = audioEffects.is3DSurroundOn();
        m3DSurroundBtn.setChecked(isSurroundOn);
        m3DSurroundBtn.setEnabled(isEffectOn);
        mFullBassCheck.setChecked(audioEffects.isFullBassOn());
        mFullBassCheck.setEnabled(isEffectOn && isSurroundOn);
        mSpeakerBtn.setEnabled(isEffectOn && isSurroundOn);

        boolean isIntensityOn = audioEffects.isIntensityOn();
        mIntensityBtn.setChecked(isIntensityOn);
        mIntensityBtn.setEnabled(isEffectOn);
        mIntensitySeek.setDisable(!(isIntensityOn && isEffectOn));


        boolean isEqualizerOn = audioEffects.isEqualizerOn();
        mEqualizerBtn.setChecked(isEqualizerOn);
        mEqualizerBtn.setEnabled(isEffectOn);
        mSelectedEqGoImg.setEnabled(isEffectOn);
        mEqDialogPanel.setEnabled(isEffectOn);
        mSelectedEqTxt.setEnabled(isEffectOn);
        mSelectedEqImg.setEnabled(isEffectOn);
        if (isEffectOn) {
            mSelectedEqTxt.setSelected(isEqualizerOn);
            mSelectedEqImg.setSelected(isEqualizerOn);
            mSelectedEqGoImg.setSelected(isEqualizerOn);
            mEqDialogPanel.setSelected(isEqualizerOn);
        }

        int eqPosition = audioEffects.getSelectedEqualizerPosition();
        mSelectedEqImg.setImageDrawable(eq_active_off.getDrawable(eqPosition));
        mSelectedEqTxt.setText(eq_names.get(eqPosition));
    }

    private String isAllSpeakersAreOff() {
        if (audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn()) {
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


    private void onEqDialogOpen() {
        new EqualizerDialog(mActivity).show();
    }

    private void openSpeakerDialog() {
        new SpeakerDialog(mActivity).show();
    }

    private void switch3DSurround() {
        if (audioEffects.isAudioEffectOn()) {
            audioEffects.setEnable3DSurround(!audioEffects.is3DSurroundOn());
            if (audioEffects.is3DSurroundOn()) {
                audioEffects.setEnableIntensity(true);
                audioEffects.setEnableEqualizer(true);
            }
        }
    }

    private void switchIntensity() {
        if (audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && audioEffects.isIntensityOn()) {
            mIntensityBtn.setChecked(true);
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.req_intensity), Toast.LENGTH_LONG).show();
        } else {
            audioEffects.setEnableIntensity(!audioEffects.isIntensityOn());
        }
    }

    private void switchEqualizer() {
        if (audioEffects.isAudioEffectOn() && audioEffects.is3DSurroundOn() && audioEffects.isEqualizerOn()) {
            mEqualizerBtn.setChecked(true);
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.req_equlaizer), Toast.LENGTH_LONG).show();
        } else {
            audioEffects.setEnableEqualizer(!audioEffects.isEqualizerOn());
        }
    }

    private void setEffectIntensity() {
        mIntensitySeek.setOnSeekBarChangeListener(new NegativeSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, final int progress, boolean isUser) {
                audioEffects.setIntensity((progress - 50) / 50.0f);

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
        audioEffects.deleteObserver(this);
        unregisterPlayerReceiver(mActivity);
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

    private void showProgressLoader() {
        if (null != mPlayingMediaItem && mPlayingMediaItem.getMediaType() != MediaType.DEVICE_MEDIA_LIB &&
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

    private void stopLoadProgress() {
        if (View.VISIBLE == mLoadingProgress.getVisibility())
            mLoadingProgress.setVisibility(View.GONE);
    }

    private void startUpNextActivity() {
        Intent queueIntent = new Intent(getActivity(), ActivityContainer.class);
        queueIntent.putExtra("container", R.string.up_next);
        getActivity().startActivity(queueIntent);
    }


    @Override
    public void update(Observable o, Object arg) {
        if (o instanceof AudioEffect) {
            String property = (String) arg;
            updateMiniPlayerEffectUI(audioEffects.isAudioEffectOn());
            updateAudioEffectUI();
            switch (property) {
                case AudioEffect.INTENSITY_PROPERTY:
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_INTENSITY_STATE_CHANGED);
                    break;
                case AudioEffect.SURROUND_SOUND_PROPERTY:
                    MixPanelAnalyticHelper.track(mActivity, audioEffects.is3DSurroundOn() ? AnalyticsHelper.EVENT_3D_TURNED_ON : AnalyticsHelper.EVENT_3D_TURNED_OFF);
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_3D_STATE_CHANGED, audioEffects.is3DSurroundOn());
                    FlurryAnalytics.getInstance(getActivity()).setEvent(audioEffects.is3DSurroundOn() ? FlurryEvents.EVENT_3D_TURNED_ON : FlurryEvents.EVENT_3D_TURNED_OFF);
                    break;
                case AudioEffect.AUTO_EQUALIZER:
                    break;
                case AudioEffect.EQUALIZER_STATE_PROPERTY:
                    FlurryAnalytics.getInstance(mActivity).setEvent(FlurryEvents.EVENT_EQ_STATE_CHANGED);
                    MixPanelAnalyticHelper.track(mActivity, audioEffects.isEqualizerOn() ? AnalyticsHelper.EVENT_EQ_TURNED_ON : AnalyticsHelper.EVENT_EQ_TURNED_OFF);
                    break;
                case AudioEffect.FULL_BASS_PROPERTY:
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.EVENT_FULL_BASS);
                    break;
                case AudioEffect.EQUALIZER_PROPERTY:
                    HashMap<String, String> articleParams = new HashMap<>();
                    articleParams.put(FlurryEvents.PARAM_SELECTED_EQUALIZER, eq_names.get(audioEffects.getSelectedEqualizerPosition()));
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Type_of_Equalizer_selected, articleParams);
                    FlurryAnalytics.getInstance(getActivity()).setEvent(FlurryEvents.Equalizer_selected, articleParams);
                    break;
            }
        }
    }


    public void setSlidingPanel(SlidingUpPanelLayout slidingPanel) {
        mSlidingPanel = slidingPanel;
    }

    public void toggleSlidingPanel() {
        if (mSlidingPanel.isPanelExpanded()) {
            mSlidingPanel.collapsePanel();
        } else {
            mSlidingPanel.expandPanel();
        }
    }
}
