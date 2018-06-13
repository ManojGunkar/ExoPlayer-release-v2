package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.globaldelight.boom.Constants;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.activities.BoomSplash;
import com.globaldelight.boom.app.activities.StoreActivity;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.login.BoomLoginActivity;
import com.globaldelight.boom.business.ads.Advertiser;
import com.globaldelight.boom.business.ads.InlineAds;
import com.globaldelight.boom.business.inapp.InAppPurchase;
import com.globaldelight.boom.app.share.ShareDialog;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.utils.Utils;

import java.util.Date;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by adarsh on 13/07/17.
 */

@Keep
public class GooglePlayStoreModel implements BusinessModel, Observer, PlaybackManager.Listener, VideoAd.Callback {

    private static final String TAG = "Business Model";

    public static final String ACTION_STATE_CHANGED = "com.globaldelight.boom.business_state_changed";



    public static final int STATE_UNDEFINED = -1;
    public static final int STATE_TRIAL = 1;
    public static final int STATE_LOCKED = 2;
    public static final int STATE_EXTENDED_TRIAL = 3;
    public static final int STATE_PURCHASED = 4;


    static class AdvertiserImpl implements Advertiser {

        @Override
        public InlineAds createInlineAds(Activity activity, RecyclerView recyclerView, RecyclerView.Adapter baseAdapter) {
            return new AdsController(activity, recyclerView, baseAdapter);
        }

    }


    @IntDef({PRICE_FULL, PRICE_DISCOUNT, PRICE_DISCOUNT_2})
    public @interface Price{}
    public static final int PRICE_FULL = 0;
    public static final int PRICE_DISCOUNT = 1;
    public static final int PRICE_DISCOUNT_2 = 2;

    private Context mContext;
    private BusinessData data;
    private BusinessConfig config;
    private Activity mCurrentActivity;
    private BusinessPopup mPopup = new BusinessPopup();
    private Handler  mHandler = new Handler();
    private boolean mWasEffectsOnWhenLocked = false;
    private boolean mWasPlaying = false;
    private VideoAd mVideoAd;
    private Advertiser mAdsImpl;


    private Application.ActivityLifecycleCallbacks mLifecycleCallbacks = new DefaultActivityLifecycleCallbacks() {
        @Override
        public void onActivityStarted(Activity activity) {
            if ( !(activity instanceof BoomSplash) ) {
                setCurrentActivity(activity);
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (  !(activity instanceof BoomSplash) && getCurrentActivity() != activity ) {
                setCurrentActivity(activity);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            if ( getCurrentActivity() == activity ) {
                setCurrentActivity(null);
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
                    onPurchaseFailed();
                    break;
            }
        }
    };

    public GooglePlayStoreModel(Context context) {
        mContext = context;
        data = new BusinessData(mContext);
        config = new BusinessConfig();
        mAdsImpl = new AdvertiserImpl();

        FacebookSdk.sdkInitialize(mContext);

        App.getApplication().registerActivityLifecycleCallbacks(mLifecycleCallbacks);

        AudioEffect.getInstance(mContext).addObserver(this);
        App.playbackManager().registerListener(this);

        IntentFilter iapFilter = new IntentFilter();
        iapFilter.addAction(InAppPurchase.ACTION_IAP_RESTORED);
        iapFilter.addAction(InAppPurchase.ACTION_IAP_SUCCESS);
        iapFilter.addAction(InAppPurchase.ACTION_IAP_FAILED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mIAPReceiver, iapFilter);
        InAppPurchase.getInstance(mContext).initInAppPurchase();
    }


    @Override
    public boolean isAdsEnabled() {
        int state = data.getState();
        return state != STATE_PURCHASED && (state == STATE_LOCKED && isTimeExpired(data.installDate(), config.adsFreeTrialPeriod()));
    }


    @Override
    public Advertiser getAdFactory() {
        return mAdsImpl;
    }

    @Override
    public void addItemsToDrawer(Menu menu, int groupId) {
        menu.add(1,R.id.nav_store, Menu.NONE, R.string.store_title).setIcon(R.drawable.ic_store);
        menu.add(1,R.id.nav_share, Menu.NONE, R.string.title_share).setIcon(R.drawable.ic_share);
    }

    @Override
    public void onDrawerItemClicked(MenuItem item, Context context) {
        switch ( item.getItemId() ) {

            case R.id.boom_login:
                mCurrentActivity.startActivity(new Intent(mCurrentActivity,BoomLoginActivity.class));
                break;

            case R.id.nav_store:
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        context.startActivity(new Intent(context, StoreActivity.class));
                    }
                }, 300);
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Store_Page_Opened_from_Drawer);
                break;

            case R.id.nav_share:
                new ShareDialog((Activity)context).show();
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Share_Opened_from_Boom);
                break;
        }
    }

    public int getCurrentState() {
        return data.getState();
    }



    private void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;

        // update the states
        update();

        // Show any pending alerts
        mPopup.showPending(mCurrentActivity);
    }

    @Override
    public EffectsScreenPolicy createEffectsScreenPolicy() {
        return new EffectsScreenPolicyForStore();
    }

    private Activity getCurrentActivity() {
        return mCurrentActivity;
    }


    public void showPurchaseDialog() {
        if ( isLoggedIn() ) {
            mPopup.show(mCurrentActivity,
                    "Extend Trial",
                    "Sign-up and extend trial for 5 days",
                    "Sign up",
                    "Skip",
                    new DefaultResponse() {
                        @Override
                        public void onPrimaryAction() {
                            mHandler.post(GooglePlayStoreModel.this::onLoginSuccess);
                        }
                    }
            );

        }
        else {
            mPopup.show(mCurrentActivity,
                    "Subscribe",
                    "Subscribe and use all features",
                    "Subscribe",
                    "Skip",
                    new DefaultResponse() {
                        @Override
                        public void onPrimaryAction() {
                            onPurchaseSuccess();
                            setState(STATE_PURCHASED);
                        }
                    }
            );
        }
    }


    private void update() {
        if ( mCurrentActivity != null && mVideoAd == null && data.getState() == STATE_LOCKED ) {
            mVideoAd = new VideoAd(mCurrentActivity, this);
            mVideoAd.prepare();
        }

        switch ( data.getState() ) {
            default:
                setState(STATE_TRIAL);
                break;

            case STATE_LOCKED:
                break;

            case STATE_PURCHASED:
                checkSubscription();
                break;

            case STATE_EXTENDED_TRIAL:
                checkExtendedTrial();
                break;

            case STATE_TRIAL:
                checkTrial();
                break;
        }
    }


    private void setState(int newState) {
        int oldState = data.getState();
        data.setState(newState);
        switch (newState) {
            case STATE_LOCKED:
                lockEffects();
                break;

            case STATE_EXTENDED_TRIAL:
                break;

            case STATE_PURCHASED:
                break;
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }

    private void lockEffects() {
        AudioEffect.getInstance(mContext).setEnable3DSurround(true);
        AudioEffect.getInstance(mContext).setEnableLeftFrontSpeaker(true);
        AudioEffect.getInstance(mContext).setEnableRightFrontSpeaker(true);
        AudioEffect.getInstance(mContext).setEnableLeftSurroundSpeaker(true);
        AudioEffect.getInstance(mContext).setEnableRightSurroundSpeaker(true);
        AudioEffect.getInstance(mContext).setEnableTweeter(true);
        AudioEffect.getInstance(mContext).setEnableWoofer(true);

        AudioEffect.getInstance(mContext).setEnableFullBass(false);
        AudioEffect.getInstance(mContext).setEnableIntensity(true);
        AudioEffect.getInstance(mContext).setIntensity(0.0f);

        AudioEffect.getInstance(mContext).setEnableEqualizer(true);
        AudioEffect.getInstance(mContext).setSelectedEqualizerPosition(Constants.EQ.AUTO);
    }


    private boolean mIsLoggedIn = false;

    private boolean isLoggedIn() {
        return mIsLoggedIn;
    }

    private void onLoginSuccess() {
        mIsLoggedIn = true;
        if ( hasValidPurchase() ) {
            setState(STATE_PURCHASED);
        }
        else if ( !hasAvailedExtendedTrial() ) {
            setState(STATE_EXTENDED_TRIAL);
        }
        else {
            setState(STATE_LOCKED);
        }
    }

    private boolean hasValidPurchase() {
        return true;
    }

    private boolean hasAvailedExtendedTrial() {
        return false;
    }


    private void checkTrial() {
        if ( isTimeExpired(data.installDate(),config.trialPeriod()) ) {
            setState(STATE_LOCKED);
            mPopup.show(mCurrentActivity,
                    "Trial Expired!",
                    "Sign-up to continue",
                    "Sign up",
                    "Continue with Ads",
                    new DefaultResponse() {
                        @Override
                        public void onPrimaryAction() {
                            setState(STATE_EXTENDED_TRIAL);
                        }
                    }
            );
        }
    }


    private void checkExtendedTrial() {
        checkAds();
        if ( isTimeExpired(data.installDate(),config.trialPeriod()) ) {
            mPopup.show(mCurrentActivity,
                    "Trial Expired!",
                    "Subscribe to continue",
                    "Subscribe",
                    "Continue with Ads",
                    new BusinessPopup.Actions() {
                        @Override
                        public void onPrimaryAction() {
                            setState(STATE_PURCHASED);
                        }

                        @Override
                        public void onSecondaryAction() {
                            setState(STATE_LOCKED);
                        }

                        @Override
                        public void onCancel() {
                            setState(STATE_LOCKED);
                        }
                    }
            );
        }
    }


    private void checkAds() {
        if ( isTimeExpired(data.installDate(),config.adsFreeTrialPeriod()) ) {
            // enable ads
            mPopup.show( mCurrentActivity,
                    "Ad free usage expired!",
                    "Ads will start",
                    "OK",
                    null,
                    new DefaultResponse() {
                        @Override
                        public void onPrimaryAction() {

                        }
                    });
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
        }
    }

    private void checkSubscription() {

    }



    private static boolean isTimeExpired(Date startDate, long period) {
        return (startDate == null) || ( System.currentTimeMillis() > startDate.getTime() + period );
    }




    public @Price int getPurchaseLevel() {
        if ( !isTimeExpired(data.installDate(), config.fullPricePeriod() ) ) {
            return PRICE_FULL;
        }

        if ( !isTimeExpired(data.installDate(), config.discountPeriod()) ) {
            return PRICE_DISCOUNT;
        }

        return PRICE_DISCOUNT_2;
    }


    private void onPurchase() {
        if ( mCurrentActivity != null ) {
            Intent intent = new Intent(mContext, ActivityContainer.class);
            intent.putExtra("container",R.string.store_title);
            mCurrentActivity.startActivity(intent);
            FlurryAnalytics.getInstance(mContext).setEvent(FlurryEvents.EVENT_ON_PURCHASE);
        }
    }

    public boolean isPurchased() {
        return data.getState() == STATE_PURCHASED;
    }

    public void onPurchaseSuccess() {
        setState(STATE_PURCHASED);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_ADS_STATUS_CHANGED));
        AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
    }

    public void onPurchaseFailed() {
        // Probably an attempt to hack the app.
        if ( data.getState() == STATE_PURCHASED ) {
            data.setState(STATE_LOCKED);
        }
    }


    private void showVideoAd() {
        if ( App.playbackManager().isTrackPlaying() ) {
            mWasPlaying = true;
            App.playbackManager().playPause();
        }
        mVideoAd.show(mCurrentActivity);
    }


    @Override
    public void onVideoAdCompleted() {
        FlurryAnalytics.getInstance(mContext).setEvent(FlurryEvents.EVENT_WATCHED_VIDEO);

    //    data.setState(BusinessData.STATE_VIDOE_REWARD);
        data.setVideoRewardDate(new Date());
        mPopup.show(mCurrentActivity,
                mContext.getString(R.string.share_success_title),
                mContext.getString(R.string.video_ad_success_message, BusinessConfig.toHours(config.videoRewardPeriod())),
                mContext.getString(R.string.ok),
                null,
                null);

    }

    @Override
    public void onVideoAdCancelled() {
    }


    private void onRemindLater() {
    }

    private void postUpdate() {
        mHandler.post(this::update);
    }


    private void onEffectsON() {
    }


    @Override
    public void update(Observable o, Object arg) {
        String property = (String)arg;
        if ( property.equals(AudioEffect.AUDIO_EFFECT_PROPERTY) ) {
            update();
            if ( AudioEffect.getInstance(mContext).isAudioEffectOn() ) {
                onEffectsON();
            }
        }
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


    private boolean mAlertIsVisible = false;

 //   private void showPopup(final String message, final String primaryTitle, final String secondaryTitle, final PopupResponse callback) {


    private abstract class DefaultResponse implements BusinessPopup.Actions {
        @Override
        public void onSecondaryAction() {

        }

        @Override
        public void onCancel() {

        }
    }
}
