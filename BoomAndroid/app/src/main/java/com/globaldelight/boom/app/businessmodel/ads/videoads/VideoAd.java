package com.globaldelight.boom.app.businessmodel.ads.videoads;

import android.app.ProgressDialog;
import android.content.Context;
import android.telecom.Call;
import android.widget.Toast;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.utils.Utils;
import com.google.android.exoplayer2.C;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;

import static android.widget.Toast.LENGTH_SHORT;

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
        mAd.loadAd(BuildConfig.GOOGLE_VIDEO_AD_ID, new AdRequest.Builder().build());
    }


    public void show(Context context) {
        mIsRewarded = false;
        if ( mAd.isLoaded() ) {
            mAd.show();
        }
        else {
            mShow = true;
            Utils.showProgressLoader(context);
            mAd.loadAd(BuildConfig.GOOGLE_VIDEO_AD_ID, new AdRequest.Builder().build());
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
        if ( mShow ) {
            Utils.dismissProgressLoader();
        }

        mShow = false;
        Toast.makeText(mContext, "Failed to load video ad. Error: " + i, LENGTH_SHORT).show();
        mCallback.onVideoAdCancelled();
    }

    public interface Callback {
        void onVideoAdCompleted();
        void onVideoAdCancelled();
    }
}
