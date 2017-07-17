package com.globaldelight.boom.business;

import android.app.ProgressDialog;
import android.content.Context;
import android.telecom.Call;

import com.globaldelight.boom.utils.Utils;
import com.google.android.exoplayer2.C;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

/**
 * Created by adarsh on 14/07/17.
 */

public class VideoAd implements RewardedVideoAdListener {

    private Context mContext;
    private RewardedVideoAd mAd;
    private Callback mCallback;
    private boolean mShow;
    private boolean mIsRewarded;

    public VideoAd(Context context, Callback callback) {
        mContext = context.getApplicationContext();
        mCallback = callback;
        MobileAds.initialize(mContext, "com.globaldelight.boom");
    }

    public void prepare() {
        mAd = MobileAds.getRewardedVideoAdInstance(mContext);
        mAd.setRewardedVideoAdListener(this);
        mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
    }


    public void show(Context context) {
        mIsRewarded = false;
        if ( mAd.isLoaded() ) {
            mAd.show();
        }
        else {
            mShow = true;
            Utils.showProgressLoader(context);
            mAd.loadAd("ca-app-pub-3940256099942544/5224354917", new AdRequest.Builder().build());
        }
    }

    @Override
    public void onRewardedVideoAdLoaded() {
        if ( mShow ) {
            mShow = false;
            Utils.dismissProgressLoader();
            if ( mAd.isLoaded() ) {
                mAd.show();
            }
        }
    }

    @Override
    public void onRewardedVideoAdOpened() {

    }

    @Override
    public void onRewardedVideoStarted() {

    }

    @Override
    public void onRewardedVideoAdClosed() {
        if ( !mIsRewarded ) {
            mCallback.onVideoAdCancelled();
        }
    }

    @Override
    public void onRewarded(RewardItem rewardItem) {
        mIsRewarded = true;
        mCallback.onVideoAdCompleted();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int i) {
        mShow = false;
        mCallback.onVideoAdCancelled();
    }

    public interface Callback {
        void onVideoAdCompleted();
        void onVideoAdCancelled();
    }
}
