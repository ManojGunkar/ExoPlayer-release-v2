package com.globaldelight.boom.business;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.ShareActivity;
import com.globaldelight.boom.app.businessmodel.inapp.InAppPurchase;
import com.globaldelight.boom.app.businessmodel.inapp.activity.InAppPurchaseActivity;
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

    private static final int SHARE_REMINDER_PERIOD = 5*60*1000;
    private static final int PURCHASE_REMINDER_PERIOD = 15*60*1000;


    @IntDef({PRICE_FULL, PRICE_DISCOUNT, PRICE_MIN})
    public @interface Price{};
    public static final int PRICE_FULL = 0;
    public static final int PRICE_DISCOUNT = 1;
    public static final int PRICE_MIN = 2;

    private Context mContext;
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


    private BroadcastReceiver mIAPReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case InAppPurchase.ACTION_IAP_RESTORED:
                case InAppPurchase.ACTION_IAP_SUCCESS:
                    onPurchaseSuccess();
                    break;

                case InAppPurchase.ACTION_IAP_FAILED:
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
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mShareStatusReciever, filter);

        IntentFilter iapFilter = new IntentFilter();
        iapFilter.addAction(InAppPurchase.ACTION_IAP_RESTORED);
        iapFilter.addAction(InAppPurchase.ACTION_IAP_SUCCESS);
        iapFilter.addAction(InAppPurchase.ACTION_IAP_FAILED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mIAPReceiver, iapFilter);

        if ( !isPurchased() ) {
            InAppPurchase.getInstance(mContext).initInAppPurchase();
        }
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
                if ( shouldRemindSharing() ) {
                    showShareDialog();
                }
                else if ( shouldRemindPurchase() ) {
                    showPurchaseDialog(false);
                }
                break;

            case BusinessData.STATE_SHARED:
                if ( isShareExpired() && isExtendedShareExpired() ) {
                    lockEffects();
                    showPurchaseDialog(false);
                }
                break;


            case BusinessData.STATE_VIDOE_REWARD:
                if ( isVideoRewardExpired() ) {
                    lockEffects();
                }
                break;

            case BusinessData.STATE_TRIAL:
                if ( isTrialExpired() ) {
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
                    lockEffects();
                    if ( shouldRemindSharing() ) {
                        showShareDialog();
                    }
                    else if ( shouldRemindPurchase() ) {
                        showPurchaseDialog(false);
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

    private boolean shouldRemindPurchase() {
        return isTimeExpired(data.getLastPurchaseReminder(), PURCHASE_REMINDER_PERIOD);
    }

    private void showPurchaseDialog(boolean sharingAllowed) {
        String message = "Buy @ ";
        String price = "";
        switch ( getPurchaseLevel() ) {
            case PRICE_FULL:
                price = "$2.99";
                break;
            case PRICE_DISCOUNT:
                price = "$1.99";
                break;
            case PRICE_MIN:
                price = "$0.99";
                break;
        }

        data.setLastPurchaseReminder(new Date());

        if ( sharingAllowed ) {
            showPopup(message+price + "\n or \n Share", "Buy", "Share", shareResponse );
        }
        else {
            showPopup(message+price, "Buy", "Remind Me Later", buyResponse );
        }
    };

    public @Price int getPurchaseLevel() {
        Date startDate = data.getStartDate();
        if ( startDate == null || !isTimeExpired(startDate, config.fullPricePeriod() ) ) {
            return PRICE_FULL;
        }
        else if ( !isTimeExpired(startDate, config.discountPeriod()) ) {
            return PRICE_DISCOUNT;
        }

        return PRICE_MIN;
    }



    private void onPurchase() {
        mContext.startActivity(new Intent(mContext, InAppPurchaseActivity.class));
    }

    public boolean isPurchased() {
        return data.getState() == BusinessData.STATE_PURCHASED;
    }

    public void onPurchaseSuccess() {
        data.setState(BusinessData.STATE_PURCHASED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
        AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
    }

    public void onPurchaseFailed() {

    }


    private boolean shouldRemindSharing() {
        Date sharedDate = data.getSharedDate();
        if (  isSharingAllowed() && sharedDate == null && isTimeExpired(data.getLastShareReminder(), SHARE_REMINDER_PERIOD) ) {
            return true;
        }

        return false;
    }


    private void showShareDialog() {
        data.setLastShareReminder(new Date());
        showPurchaseDialog(true);
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
            data.setState(BusinessData.STATE_SHARED);
            AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
            showPopup("Enjoy!", "Ok", null, null);
        }
    }

    private void onShareFailed() {
        if ( rewardUserForSharing() ) {
            // TODO: Notify user that share failed
            showPopup("Failed!", "Ok", null, null);
        }
    }


    private void showVideoAd() {
        mVideoAd.show(mCurrentActivity);
    }


    @Override
    public void onVideoAdCompleted() {
        // Start the playback if previously playing
        data.setState(BusinessData.STATE_VIDOE_REWARD);
        data.setVideoRewardDate(new Date());
        if ( mWasPlaying ) {
            App.playbackManager().playPause();
            mWasPlaying = false;
        }
    }

    @Override
    public void onVideoAdCancelled() {
        AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
        if ( mWasPlaying ) {
            App.playbackManager().playPause();
            mWasPlaying = false;
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
                showPopup("Watch Video Ad to unlock effects", "Watch", "Cancel", videoResponse);
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
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
                lockEffects();
                if ( isSharingAllowed() ) {
                    showShareDialog();
                }
                else {
                    showPurchaseDialog(false);
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
                        showPurchaseDialog(false);
                    }
                    else if ( data.isInitialPopupShown() && isTimeExpired(data.getLastPopupDate(), config.reminderInterval()) ) {
                        data.setLastPopupDate(new Date());
                        showPurchaseDialog(true);
                    }
                }
            }
        });
    }

    @Override
    public void onQueueUpdated() {

    }


    private void showPopup(final String message, final String primaryTitle, final String secondaryTitle, final PopupResponse callback) {
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

                if ( primaryTitle != null ) {
                    builder.positiveText(primaryTitle);
                }
                if ( secondaryTitle != null ) {
                    builder.negativeText(secondaryTitle);
                }

                if ( callback != null ) {
                    builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onSecondaryAction();

                                }
                            })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onPrimaryAction();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    callback.onCancel();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
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
        void onPrimaryAction();
        void onSecondaryAction();
        void onCancel();
    }

    private abstract class DefaultResponse implements PopupResponse {
        @Override
        public void onSecondaryAction() {

        }

        @Override
        public void onCancel() {

        }
    }


    private PopupResponse shareResponse = new DefaultResponse() {

        @Override
        public void onPrimaryAction() {
            onPurchase();
        }

        @Override
        public void onSecondaryAction() {
            onShare();
        }
    };

    private PopupResponse buyResponse = new DefaultResponse() {
        @Override
        public void onPrimaryAction() {
            onPurchase();
        }
    };


    private PopupResponse videoResponse = new DefaultResponse() {

        @Override
        public void onPrimaryAction() {
            showVideoAd();
        }

        public void onCancel() {
            onVideoAdCancelled();
        }
    };

}
