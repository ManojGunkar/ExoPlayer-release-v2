package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.ShareActivity;
import com.globaldelight.boom.app.fragments.ShareFragment;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.player.AudioEffect;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by adarsh on 13/07/17.
 */

public class BusinessStrategy implements Observer, PlaybackManager.Listener, VideoAd.Callback {

    public static final String ACTION_ADS_STATUS_CHANGED = "com.globaldelight.boom.ADS_STATUS_CHANGED";
    private static final String TAG = "Business Model";

    private Context mContext;
    private int state;
    private BusinessData data;
    private BusinessConfig config;
    private Activity mCurrentActivity;
    private Runnable mPendingAlert = null;
    private Handler  mHandler = new Handler();
    private boolean mWasEffectsOnWhenLocked = false;
    private boolean mWasPlaying = false;
    private VideoAd mVideoAd;
    private int mSongsPlayed = 0;

    private BroadcastReceiver mShareStatusReciever = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case ShareFragment.ACTION_SHARE_SUCCESS:
                    onShareSuccess();
                    break;
                case ShareFragment.ACTION_SHARE_FAILED:
                    onShareFailed();
                    break;
            }
        }
    };


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

        IntentFilter filter = new IntentFilter();
        filter.addAction(ShareFragment.ACTION_SHARE_SUCCESS);
        filter.addAction(ShareFragment.ACTION_SHARE_FAILED);
        mContext.registerReceiver(mShareStatusReciever, filter);
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

    public boolean isAdsEnabled() {
        return data.getState() != BusinessData.STATE_TRIAL && data.getState() != BusinessData.STATE_PURCHASED;
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
                    lockEffects();
                    showPopup("Sharing expired! Buy", buyResponse);
                }
                break;


            case BusinessData.STATE_VIDOE_REWARD:
                if ( isVideoRewardExpired() ) {
                    lockEffects();
                    showPopup("Video reward expired!", buyResponse);
                }
                break;

            case BusinessData.STATE_TRIAL:
                if ( isTrialExpired() ) {
                    mContext.sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
                    lockEffects();
                    if ( isSharingAllowed() ) {
                        showPopup("TODO: Trial Expired! Share", shareResponse);
                    }
                    else {
                        showPopup("TODO: Trial Expired! Buy", buyResponse);
                    }
                }
                break;

            // App is just installed or user cleared the data
            case BusinessData.STATE_UNDEFINED:
                if ( !isTrialExpired() ) {
                    data.setState(BusinessData.STATE_TRIAL);
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

    private void onPurchase() {

    }

    private void onPurchased() {
        data.setState(BusinessData.STATE_PURCHASED);
        AudioEffect.getInstance(mContext).setEnableAudioEffect(mWasEffectsOnWhenLocked);
    }

    private boolean isSharingAllowed() {
        return data.getSharedDate() == null && !isTimeExpired(data.installDate(), config.sharePeriod()*2 + config.extendedSharePeriod());
    }

    private void onShare() {
        if ( mCurrentActivity != null ) {
            mCurrentActivity.startActivity(new Intent(mCurrentActivity, ShareActivity.class));
        }
    }

    private void onShareSuccess() {
        if ( rewardUserForSharing() ) {
            data.setSharedDate(new Date());
            state = BusinessData.STATE_SHARED;
            AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
            showPopup("TODO: Shared!", null);
        }
    }

    private void onShareFailed() {
        if ( rewardUserForSharing() ) {
            // TODO: Notify user that share failed
            showPopup("TODO: Share Failed!", null);
        }
    }


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


    private void onRemindLater() {
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
            else if ( (data.getState() == BusinessData.STATE_TRIAL || data.getState() == BusinessData.STATE_UNDEFINED) && data.getStartDate() == null ) {
                data.setStartDate(new Date());
            }
        }
    }

    private void lockEffects() {
        mWasEffectsOnWhenLocked = AudioEffect.getInstance(mContext).isAudioEffectOn();
        data.setState(BusinessData.STATE_LOCKED);
        AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
    }

    @Override
    public void onMediaChanged() {
        if ( data.getState() == BusinessData.STATE_UNDEFINED && data.getStartDate() != null ) {
            if ( mSongsPlayed > 1 ) {
                mContext.sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
                lockEffects();
                if ( isSharingAllowed() ) {
                    showPopup("Share to unlock effects", shareResponse);
                }
                else {
                    showPopup("Buy to unlock effects", buyResponse);
                }
            }
            mSongsPlayed++;
        }
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
                        showPopup("Initial Popup", trialResponse);
                    }
                    else if ( data.isInitialPopupShown() && isTimeExpired(data.getLastPopupDate(), config.reminderInterval()) ) {
                        data.setLastPopupDate(new Date());
                        showPopup("Reminder", trialResponse);
                    }
                }
            }
        });
    }

    @Override
    public void onQueueUpdated() {

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
                        .content(message)
                        .titleColor(ContextCompat.getColor(mContext, R.color.dialog_title))
                        .title("TODO");
                if ( callback != null ) {
                    builder.negativeText(callback != null ? callback.cancelTitle() : "Cancel")
                            .positiveText(callback != null ? callback.okTitle() : "Accept")
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


    private interface PopupResponse {

        String okTitle();
        String cancelTitle();

        void onOk();
        void onCancel();
    }


    private PopupResponse shareResponse = new PopupResponse() {

        public String okTitle() {
            return "Share";
        }

        public String cancelTitle() {
            return "Cancel";
        }

        @Override
        public void onOk() {
            onShare();
        }

        @Override
        public void onCancel() {

        }
    };

    private PopupResponse buyResponse = new PopupResponse() {

        public String okTitle() {
            return "Buy";
        }

        public String cancelTitle() {
            return "Cancel";
        }

        @Override
        public void onOk() {
            onPurchase();
        }

        @Override
        public void onCancel() {

        }
    };

    private PopupResponse trialResponse = new PopupResponse() {
        @Override
        public String okTitle() {
            return "Share";
        }

        @Override
        public String cancelTitle() {
            return "Remind Me Later";
        }

        @Override
        public void onOk() {
            onShare();
        }

        @Override
        public void onCancel() {
            onRemindLater();
        }
    };
}
