package com.globaldelight.boom.business;

import android.content.Context;
import android.os.Handler;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.ads.Ad;
import com.facebook.ads.AdChoicesView;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdSettings;
import com.facebook.ads.InterstitialAdListener;
import com.facebook.ads.NativeAd;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.NativeExpressAdView;

import static com.facebook.FacebookSdk.getApplicationContext;
import com.globaldelight.boom.R;

import java.util.ArrayList;
import java.util.List;

import static com.globaldelight.boom.business.Utills.nativePlacementId;

/**
 * Created by Rahul Agarwal on 01-02-17.
 */

public class BusinessHandler {

    private NativeExpressAdView mAdView2;
    private LinearLayout adView;
    private com.facebook.ads.InterstitialAd interstitialAd;
    private InterstitialAd mInterstitialAd;
    private NativeAd nativeAd;
    private IFBAddsUpdater ifbAddsUpdater;
    private IGoogleAddsUpdater iGoogleAddsUpdater;
    private Handler postAdds;
    private Context mContext;
    private static BusinessHandler businessHandler;
    private NetworkCalls networkCallHandler;

    private BusinessHandler(Context context){
        mContext = context;
        networkCallHandler = NetworkCalls.getNetworkCallsInstance(context);
        postAdds = new Handler();
    }

    public static BusinessHandler getBusinessHandlerInstance(Context context){
        if(null == businessHandler)
            businessHandler = new BusinessHandler(context);
        return businessHandler;
    }

    public void getBoomAccessToken(){
        networkCallHandler.getAccessToken();
    }

    public void registerAndroidDevice(){
        networkCallHandler.registerDevice();
    }

    public void configAppWithBoomServer(){
        networkCallHandler.configApp();
    }

    public void saveEmailAddress(Utills.EmailSource emailSource, String emailid, boolean newsletteroptin){
        networkCallHandler.saveEmailAddress(emailSource, emailid, newsletteroptin);
    }

    public boolean isAppTrialVersion(){
        return networkCallHandler.isAppTrailVersion();
    }

    public void setFBNativeAddListener(IFBAddsUpdater ifbAddsUpdater){
        this.ifbAddsUpdater = ifbAddsUpdater;
    }

    public void setGoogleNativeAddListener(IGoogleAddsUpdater iGoogleAddsUpdater){
        this.iGoogleAddsUpdater = iGoogleAddsUpdater;
    }

    public void setBusinessNetworkListener(IBusinessNetworkInit iBusinessNetworkInit){
        networkCallHandler.setBusinessNetworkListener(iBusinessNetworkInit);
    }

    public void loadFbNativeAdds() {
        nativeAd = new NativeAd(mContext,nativePlacementId);
        nativeAd.setAdListener(new AdListener() {

            @Override
            public void onError(Ad ad, AdError error) {
                // Ad error callback
            }

            @Override
            public void onAdLoaded(Ad ad) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                adView = (LinearLayout) inflater.inflate(R.layout.fb_native_add_layout, null);

                // Create native UI using the ad metadata.
                ImageView nativeAdIcon = (ImageView) adView.findViewById(R.id.native_ad_icon);
                TextView nativeAdTitle = (TextView) adView.findViewById(R.id.native_ad_title);
//                MediaView nativeAdMedia = (MediaView) adView.findViewById(R.id.native_ad_media);
                TextView nativeAdSocialContext = (TextView) adView.findViewById(R.id.native_ad_social_context);
                TextView nativeAdBody = (TextView) adView.findViewById(R.id.native_ad_body);
                Button nativeAdCallToAction = (Button) adView.findViewById(R.id.native_ad_call_to_action);

                // Set the Text.
                nativeAdTitle.setText(nativeAd.getAdTitle());
                nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
                nativeAdBody.setText(nativeAd.getAdBody());
                nativeAdCallToAction.setText(nativeAd.getAdCallToAction());

                // Download and display the ad icon.
                NativeAd.Image adIcon = nativeAd.getAdIcon();
                NativeAd.downloadAndDisplayImage(adIcon, nativeAdIcon);

                // Download and display the cover image.
//                nativeAdMedia.setNativeAd(nativeAd);

                // Add the AdChoices icon
                LinearLayout adChoicesContainer = (LinearLayout) adView.findViewById(R.id.ad_choices_container);
                AdChoicesView adChoicesView = new AdChoicesView(mContext, nativeAd, true);
                adChoicesContainer.addView(adChoicesView);

                // Register the Title and CTA button to listen for clicks.
                List<View> clickableViews = new ArrayList<>();
                clickableViews.add(nativeAdTitle);
                clickableViews.add(nativeAdCallToAction);
                nativeAd.registerViewForInteraction(adView, clickableViews);

                postAdds.post(new Runnable() {
                    @Override
                    public void run() {
                        ifbAddsUpdater.onLoadFBNativeAdds(adView);
                    }
                });
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }
        });

        // Request an ad
        AdSettings.addTestDevice(Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID));
        AdSettings.addTestDevice("e15f1d6b5aa766e730784fd824668412");
        nativeAd.loadAd();
    }

    public void loadGoogleNativeAdd() {
        mAdView2 = new NativeExpressAdView(mContext);
        mAdView2.setAdSize(new AdSize(AdSize.FULL_WIDTH, 80));
        mAdView2.setAdUnitId(mContext.getResources().getString(R.string.native_adds));

        mAdView2.loadAd(new AdRequest.Builder().addTestDevice(mContext.getResources().getString(R.string.test_device)).build());
        postAdds.post(new Runnable() {
            @Override
            public void run() {
                iGoogleAddsUpdater.onLoadGoogleNativeAdds(mAdView2);
            }
        });
    }

    public void loadFullScreenFbAdds() {
        interstitialAd = new com.facebook.ads.InterstitialAd(mContext, mContext.getResources().getString(R.string.fb_full_screen_add));
        AdSettings.addTestDevice(mContext.getResources().getString(R.string.test_device));

        interstitialAd.setAdListener(new InterstitialAdListener() {
            @Override
            public void onInterstitialDisplayed(Ad ad) {
                // Interstitial displayed callback
            }

            @Override
            public void onInterstitialDismissed(Ad ad) {
                // Interstitial dismissed callback
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Ad error callback
                Toast.makeText(mContext, "Error: " + adError.getErrorMessage(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Show the ad when it's done loading.
                interstitialAd.show();
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

        });
        interstitialAd.loadAd();
    }

    public void loadGoogleFullScreenAdds() {
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(mContext.getResources().getString(R.string.adMob_full_screen_add));
        requestNewInterstitial();

        mInterstitialAd.setAdListener(new com.google.android.gms.ads.AdListener() {

            @Override
            public void onAdLoaded() {
                showInterstitial();
            }

            @Override
            public void onAdClosed() {
                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdOpened() {
                Toast.makeText(getApplicationContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice(mContext.getResources().getString(R.string.test_device))
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

    private void showInterstitial() {
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (mInterstitialAd != null && mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        } else {
            Toast.makeText(mContext, "Ad did not load", Toast.LENGTH_SHORT).show();
        }
    }
}
