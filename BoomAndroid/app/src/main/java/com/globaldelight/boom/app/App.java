package com.globaldelight.boom.app;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.BoomSplash;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.business.BusinessStrategy;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.app.service.PlayerService;
import com.globaldelight.boom.app.database.CloudMediaItemDBHelper;
import com.globaldelight.boom.app.database.FavoriteDBHelper;
import com.globaldelight.boom.app.database.PlaylistDBHelper;
import com.globaldelight.boom.app.database.UpNextDBHelper;
import com.globaldelight.boom.app.sharedPreferences.UserPreferenceHandler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.fabric.sdk.android.DefaultLogger;
import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.Kit;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class App extends Application implements Application.ActivityLifecycleCallbacks {

//    public static final String TWEET_CONSUMER ="K8KDQHnW8Zi0ZLZklAcKGqI92";
//    public static final String TWEET_SECRET ="OLhb1Lp9BaZf26qOQE6EOHBP66sImaZ635ls23TJqrDcmwhpuy";


    private static App application;
    private static PlaylistDBHelper boomPlayListhelper;
    private static FavoriteDBHelper favoriteDBHelper;
    private static UpNextDBHelper upNextDBHelper;
    private static CloudMediaItemDBHelper cloudMediaItemDBHelper;
    private static UserPreferenceHandler userPreferenceHandler;

    public static App getApplication() {
        return application;
    }

    @Override
    public void onCreate() {
        application = this;
        super.onCreate();
        registerActivityLifecycleCallbacks(this);

        FacebookSdk.sdkInitialize(this);


        if ( BuildConfig.FLAVOR.equals("production") ) {
            final Fabric fabric = new Fabric.Builder(this)
                    .kits(new Crashlytics())
                    .logger(new DefaultLogger(Log.DEBUG))
                    .debuggable(true)
                    .build();
            Fabric.with(fabric);
        }
//        else {
//            TwitterAuthConfig authConfig =  new TwitterAuthConfig(TWEET_CONSUMER, TWEET_SECRET);
//            final Fabric fabric = new Fabric.Builder(this)
//                    .kits(new TwitterCore(authConfig), new TweetComposer())
//                    .logger(new DefaultLogger(Log.DEBUG))
//                    .debuggable(true)
//                    .build();
//
//            Fabric.with(fabric);
//        }

        MixPanelAnalyticHelper.initPushNotification(this);


    }

    public static PlaybackManager playbackManager() {
        return PlaybackManager.getInstance(application);
    }

    public static UserPreferenceHandler getUserPreferenceHandler() {
        if ( userPreferenceHandler == null ) {
            userPreferenceHandler = new UserPreferenceHandler(application);
        }
        return userPreferenceHandler;
    }

    public static UpNextDBHelper getUPNEXTDBHelper() {
        if ( upNextDBHelper == null ) {
            upNextDBHelper = new UpNextDBHelper(application);
        }
        return upNextDBHelper;
    }

    public static CloudMediaItemDBHelper getCloudMediaItemDBHelper() {
        if ( cloudMediaItemDBHelper == null ) {
            cloudMediaItemDBHelper = new CloudMediaItemDBHelper(application);
        }
        return cloudMediaItemDBHelper;
    }

    public static PlaylistDBHelper getBoomPlayListHelper() {
        if ( boomPlayListhelper == null ) {
            boomPlayListhelper = new PlaylistDBHelper(application);
        }
        return boomPlayListhelper;
    }

    public static FavoriteDBHelper getFavoriteDBHelper() {
        if ( favoriteDBHelper == null ) {
            favoriteDBHelper = new FavoriteDBHelper(application);
        }
        return favoriteDBHelper;
    }

    public static void startPlayerService(){
        application.startService(new Intent(application, PlayerService.class));
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        application = null;
        MixPanelAnalyticHelper.getInstance(this).flush();
    }

    public void onActivityCreated(Activity var1, Bundle var2) {

    }

    public void onActivityStarted(Activity activity) {
        if ( !(activity instanceof BoomSplash) ) {
            BusinessStrategy.getInstance(this).setCurrentActivity(activity);
        }

       /* Boolean terminate = false;
        int terminateReason = R.string.app_expire;
        if( isExpired() ) {
            terminateReason = R.string.app_expire;
            terminate = true;
        }
        else if ( !isSupportedDevice() ) {
            terminateReason = R.string.app_unsupported;
            terminate = true;
        }

        if ( terminate ) {
            Toast.makeText(this, activity.getResources().getString(terminateReason), Toast.LENGTH_LONG).show();
            activity.finishAndRemoveTask();

            // Terminate the App process after 2 seconds
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.exit(0);
                }
            }, 2000);
        }*/
    }

    public void onActivityResumed(Activity activity) {
        if (  !(activity instanceof BoomSplash) && BusinessStrategy.getInstance(this).getCurrentActivity() != activity ) {
            BusinessStrategy.getInstance(this).setCurrentActivity(activity);
        }
    }

    public void onActivityPaused(Activity var1) {
    }

    public void onActivityStopped(Activity activity) {
        if ( BusinessStrategy.getInstance(this).getCurrentActivity() == activity ) {
            BusinessStrategy.getInstance(this).setCurrentActivity(null);
            Glide.get(this).clearMemory();
        }
    }

    public void onActivitySaveInstanceState(Activity var1, Bundle var2) {
    }

    public void onActivityDestroyed(Activity var1) {

    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    private boolean isExpired(){
        if( BuildConfig.EXPIRY_DATE != null ) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
                Date expiryDate = sdf.parse(BuildConfig.EXPIRY_DATE);
                Date today = new Date();
                return today.after(expiryDate);
            } catch (ParseException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    private boolean isSupportedDevice(){
        return true; //Build.MANUFACTURER.equalsIgnoreCase("Celkon");
    }
}
