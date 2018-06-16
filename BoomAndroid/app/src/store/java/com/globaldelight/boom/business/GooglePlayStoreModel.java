package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.Keep;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.FacebookSdk;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
    public static final int STATE_LIMITED = 5;


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
    private VideoAd mVideoAd;
    private Advertiser mAdsImpl;
    private int mSongsRemaining = 0;


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


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
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

                case BoomLoginActivity.ACTION_LOGIN_SUCCESS:
                    onLoginSuccess();
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

        IntentFilter filter = new IntentFilter();
        filter.addAction(InAppPurchase.ACTION_IAP_RESTORED);
        filter.addAction(InAppPurchase.ACTION_IAP_SUCCESS);
        filter.addAction(InAppPurchase.ACTION_IAP_FAILED);
        filter.addAction(BoomLoginActivity.ACTION_LOGIN_SUCCESS);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mReceiver, filter);
        InAppPurchase.getInstance(mContext).initInAppPurchase();
    }


    @Override
    public boolean isAdsEnabled() {
        int state = data.getState();
        return state != STATE_PURCHASED && data.shouldShowAds();
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
        if ( !isLoggedIn() ) {
            showExtendTrialDialog();
        }
        else {
            showSubscribeDialog();
        }
    }


    // Handle states
    private void update() {
        if ( mCurrentActivity != null && mVideoAd == null && data.getState() == STATE_LOCKED ) {
            mVideoAd = new VideoAd(mCurrentActivity, this);
            mVideoAd.prepare();
        }

        switch ( data.getState() ) {
            default:
            case STATE_UNDEFINED:
                setState(STATE_TRIAL);
                break;

            case STATE_LOCKED:
                checkLocked();
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

            case STATE_LIMITED:
                checkLimited();
                break;
        }
    }

    private void checkTrial() {
        checkAds();
        if ( isTimeExpired(data.installDate(),config.trialPeriod()) ) {
            setState(STATE_LOCKED);
            showExtendTrialDialog();
        }
    }

    private void checkExtendedTrial() {
        checkAds();
        if ( isTimeExpired(data.getSignupDate(),config.extendTrialPeriod()) ) {
            setState(STATE_LIMITED);
            showLimitedDialog();
        }
    }

    private void checkLocked() {
        if ( !isLoggedIn() ) {
            return;
        }

        // TODO: Compare the day instead of 24hrs
        Date lastDate = data.getLastLimitedDate();
        if ( lastDate != null && isTimeExpired(lastDate, 24 * 60 * 60 * 1000L) ) {
            setState(STATE_LIMITED);
        }
    }

    private void checkLimited() {
        if ( mSongsRemaining < 0 ) {
            setState(STATE_LOCKED);
        }
    }

    private void setState(int newState) {
        data.setState(newState);
        switch (newState) {
            case STATE_LOCKED:
                lockEffects();
                break;

            case STATE_EXTENDED_TRIAL:
                AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
                data.setSignupDate(new Date());
                break;

            case STATE_PURCHASED:
                AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
                break;

            case STATE_LIMITED:
                AudioEffect.getInstance(mContext).setEnableAudioEffect(true);
                mSongsRemaining = config.freeSongsLimit();
                data.setLastLimitedDate(new Date());
                break;
        }
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(new Intent(ACTION_STATE_CHANGED));
    }


    private void lockEffects() {
        AudioEffect.getInstance(mContext).setEnableAudioEffect(false);
    }


    private void showLoginPage() {
        mCurrentActivity.startActivity(new Intent(mCurrentActivity, BoomLoginActivity.class));
    }

    private void onLoginSuccess() {
        data.setLoginState(true);
        if ( getCurrentState() != STATE_PURCHASED ) {
            if ( !hasAvailedExtendedTrial() ) {
                setState(STATE_EXTENDED_TRIAL);
            }
            else {
                setState(STATE_LIMITED);
            }
        }
    }

    private boolean isLoggedIn() {
        return data.isLoggedIn();
    }

    private boolean hasAvailedExtendedTrial() {
        return false;
    }

    private void checkAds() {
        if ( !data.shouldShowAds() && isTimeExpired(data.installDate(),config.adsFreeTrialPeriod()) ) {
            data.setShowAds(true);
            showAdsDialog();
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
        if ( getCurrentState() == STATE_LIMITED ) {
            mSongsRemaining--;
            Toast.makeText(mCurrentActivity,
                    String.format("%d songs reamining", mSongsRemaining),
                    Toast.LENGTH_LONG).show();
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


    private abstract class DefaultResponse implements BusinessPopup.Actions {
        @Override
        public void onSecondaryAction() {

        }

        @Override
        public void onCancel() {

        }
    }


    private void showExtendTrialDialog() {
        mPopup.show(mCurrentActivity,
                "Trial Expired!",
                "Sign-up and extend trial for 5 days",
                "Sign up",
                "Cancel",
                new GooglePlayStoreModel.DefaultResponse() {
                    @Override
                    public void onPrimaryAction() {
                        showLoginPage();
                    }
                });
    }


    private void showAdsDialog() {
        mPopup.show( mCurrentActivity,
                "Ad free usage expired!",
                "Ads will start. Subscribe to remove Ads and enjoy all features!",
                "Subscribe",
                "Cancel",
                new DefaultResponse() {
                    @Override
                    public void onPrimaryAction() {
                        // show store
                    }

                    @Override
                    public void onSecondaryAction() {

                    }
        });
    }


    private void showLimitedDialog() {
        mPopup.show(mCurrentActivity,
                "Trial Expired!",
                "Subscribe to unlock full features.",
                "Subscribe",
                "Cancel",
                new DefaultResponse() {
                    @Override
                    public void onPrimaryAction() {
                        setState(STATE_PURCHASED);
                    }
                }
        );
    }


    private void showSubscribeDialog() {
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
