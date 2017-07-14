package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.player.AudioEffect;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by adarsh on 13/07/17.
 */

public class BusinessStrategy implements Observer, PlaybackManager.Listener, VideoAd.Callback {
    private static final String TAG = "Business Model";

    private Context mContext;
    private int state;
    private BusinessData data;
    private BusinessConfig config;
    private Activity mCurrentActivity;
    private Runnable mPendingAlert = null;
    private Handler  mHandler = new Handler();
    private int mAllowedSongs = 0;
    private VideoAd mVideoAd;


    private static BusinessStrategy instance;
    public static BusinessStrategy getInstance(Context context) {
        if ( instance == null ) {
            instance = new BusinessStrategy(context.getApplicationContext());
        }
        return instance;
    }

    private BusinessStrategy(Context context) {
        mContext = context;
        data = new BusinessData(mContext);
        config = new BusinessConfig();
        AudioEffect.getInstance(mContext).addObserver(this);
        App.playbackManager().registerListener(this);
    }


    public void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;

        // update the states
        update();

        // Show any pending alerts
        if ( mCurrentActivity != null && mPendingAlert != null ) {
            mHandler.post(mPendingAlert);
        }
    }

    public Activity getCurrentActivity() {
        return mCurrentActivity;
    }


    private void update() {

        if ( mCurrentActivity != null && mVideoAd == null && data.getState() == BusinessData.STATE_LOCKED ) {
            mVideoAd = new VideoAd(mCurrentActivity, this);
            mVideoAd.prepare();
        }

        switch ( data.getState() ) {
            case BusinessData.STATE_PURCHASED:
                break;

            case BusinessData.STATE_LOCKED:
                break;

            case BusinessData.STATE_SHARED:
                if ( isShareExpired() && isExtendedShareExpired() ) {
                    data.setState(BusinessData.STATE_LOCKED);
                    AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
                    showPopup("TODO: Sharing expired! Buy", buyResponse);
                }
                break;


            case BusinessData.STATE_VIDOE_REWARD:
                if ( isVideoRewardExpired() ) {
                    data.setState(BusinessData.STATE_LOCKED);
                    AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
                    showPopup("TODO: Video reward expired!", buyResponse);
                }
                break;

            case BusinessData.STATE_TRIAL:
                if ( isTrialExpired() ) {
                    showPopup("TODO: Trial Expired! Share", shareResponse);
                    data.setState(BusinessData.STATE_LOCKED);
                    AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
                }
                break;

            // App is just installed or user cleared the data
            case BusinessData.STATE_UNDEFINED:
                if ( !isTrialExpired() ) {
                    data.setState(BusinessData.STATE_TRIAL);
                }
                else {
                    mAllowedSongs = 1;
                }
                break;
        }
    }

    private static boolean isTimeExpired(Date startDate, long period) {
        return (startDate == null) || ( System.currentTimeMillis() > startDate.getTime() + period );
    }

    private boolean isTrialExpired() {
        return isTimeExpired(data.installDate(), config.trialPeriod());
    }

    private boolean isShareExpired() {
        return isTimeExpired(data.getSharedDate(), config.sharePeriod());
    }

    private boolean isExtendedShareExpired() {
        return isTimeExpired(data.getSharedDate(), config.sharePeriod() + config.extendedSharePeriod());
    }

    private boolean isVideoRewardExpired() {
        return isTimeExpired(data.getVideoRewardDate(), config.videoRewardPeriod());
    }

    private boolean rewardUserForSharing() {
        return ( data.getState() != BusinessData.STATE_PURCHASED && data.getState() != BusinessData.STATE_SHARED && data.getSharedDate() == null );
    }

    private void onPurchased() {
        data.setState(BusinessData.STATE_PURCHASED);
    }


    private void onShareSuccess() {
        if ( rewardUserForSharing() ) {
            data.setSharedDate(new Date());
            state = BusinessData.STATE_SHARED;
            showPopup("TODO: Shared!", null);
        }
    }

    private void onShareFailed() {
        if ( rewardUserForSharing() ) {
            // TODO: Notify user that share failed
            showPopup("TODO: Share Failed!", null);
        }
    }


    private boolean mWasPlaying;

    @Override
    public void onVideoAdCompleted() {
        // Start the playback if previously playing
        data.setState(BusinessData.STATE_VIDOE_REWARD);
        data.setVideoRewardDate(new Date());
        if ( mWasPlaying ) {
            App.playbackManager().playPause();
        }
    }

    @Override
    public void onVideoAdCancelled() {
        AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
        if ( mWasPlaying ) {
            App.playbackManager().playPause();
        }
        // TODO: Should we show any popup?
    }

    private void showPopup(final String message, final PopupResponse callback) {
        mPendingAlert = new Runnable() {
            @Override
            public void run() {
                if ( mCurrentActivity == null ) {
                    return;
                }

                MaterialDialog.Builder builder = new MaterialDialog.Builder(mCurrentActivity);
                builder.backgroundColor(ContextCompat.getColor(mContext, R.color.dialog_background))
                        .positiveColor(ContextCompat.getColor(mContext, R.color.dialog_submit_positive))
                        .negativeColor(ContextCompat.getColor(mContext, R.color.dialog_submit_negative))
                        .widgetColor(ContextCompat.getColor(mContext, R.color.dialog_widget))
                        .contentColor(ContextCompat.getColor(mContext, R.color.dialog_content))
                        .content(message);
                if ( callback != null ) {
                    builder.negativeText("Cancel")
                            .positiveText("Accept")
                            .onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onCancel();

                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onOk();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onCancel();
                                }
                            });
                }
                builder.show();
                mPendingAlert = null;
            }
        };

        if ( mCurrentActivity != null ) {
            mHandler.post(mPendingAlert);
            mPendingAlert = null;
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        String property = (String)arg;
        if ( property.equals(AudioEffect.AUDIO_EFFECT_PROPERTY) ) {
            update();
            if ( data.getState() == BusinessData.STATE_LOCKED && AudioEffect.getInstance(mContext).isAudioEffectOn() ) {
                if ( App.playbackManager().isTrackPlaying() ) {
                    mWasPlaying = true;
                    App.playbackManager().playPause();
                }
                mVideoAd.show(mCurrentActivity);
            }
            else if ( data.getState() == BusinessData.STATE_TRIAL && data.getStartDate() == null ) {
                data.setStartDate(new Date());
            }
        }
    }

    private void postUpdate() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                update();
            }
        });
    }

    @Override
    public void onMediaChanged() {
        postUpdate();
    }

    @Override
    public void onPlaybackCompleted() {
        postUpdate();
    }

    @Override
    public void onPlayerStateChanged() {
        postUpdate();
    }

    @Override
    public void onPlayerError() {

    }

    @Override
    public void onUpdatePlayerPosition() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if ( data.getState() == BusinessData.STATE_TRIAL ) {
                    if ( !data.isInitialPopupShown() && data.getStartDate() != null && isTimeExpired(data.getStartDate(), config.initialPopupDelay())) {
                        data.setInitialPopupShown();
                        data.setLastPopupDate(new Date());
                        showPopup("TODO: Initial Popup", buyResponse);
                    }
                    else if ( data.isInitialPopupShown() && isTimeExpired(data.getLastPopupDate(), config.reminderInterval()) ) {
                        data.setLastPopupDate(new Date());
                        showPopup("TODO: Reminder", buyResponse);
                    }
                }
            }
        });
    }

    @Override
    public void onQueueUpdated() {

    }


    private interface PopupResponse {
        void onOk();
        void onCancel();
    }

    private PopupResponse videoResponse = new PopupResponse() {
        @Override
        public void onOk() {
            onVideoAdCompleted();
        }

        @Override
        public void onCancel() {
            onVideoAdCancelled();
        }
    };


    private PopupResponse shareResponse = new PopupResponse() {
        @Override
        public void onOk() {
            onShareSuccess();
        }

        @Override
        public void onCancel() {
            onShareFailed();
        }
    };

    private PopupResponse buyResponse = new PopupResponse() {
        @Override
        public void onOk() {
            onPurchased();
        }

        @Override
        public void onCancel() {

        }
    };
}
