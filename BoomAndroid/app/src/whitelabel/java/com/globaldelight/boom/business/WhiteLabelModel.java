package com.globaldelight.boom.business;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.Keep;
import android.support.v4.content.LocalBroadcastManager;
import android.view.Menu;
import android.view.MenuItem;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.activities.ConnectionActivity;
import com.globaldelight.boom.app.activities.MasterActivity;
import com.globaldelight.boom.bluetooth.BluetoothConnection;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.player.AudioEffect;
import com.globaldelight.boom.utils.DefaultActivityLifecycleCallbacks;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.webapiconnector.Headset;

import java.io.IOException;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Manoj Kumar on 24-01-2018.
 */

@Keep
public class WhiteLabelModel implements BusinessModel, BluetoothConnection.Callback, PlaybackManager.Listener {

    private static final String TAG = "WhiteLabelModel";

    private Context mContext;

    private Activity mCurrentActivity;
    private boolean mWasPlaying = false;


    private BroadcastReceiver mHeadsetStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case HeadsetMonitor.ACTION_HEADSET_CONNECTED:
                    onHeadsetConnected();
                    break;
                case HeadsetMonitor.ACTION_HEADSET_DISCONNECTED:
                    onHeadsetDisconnected();
                    break;
            }
        }
    };


    private Application.ActivityLifecycleCallbacks mlifecycle = new DefaultActivityLifecycleCallbacks() {
        @Override
        public void onActivityStarted(Activity activity) {
            mCurrentActivity = activity;
            if ( mCurrentActivity instanceof MasterActivity ) {
                verify();
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {
            super.onActivityResumed(activity);
            mCurrentActivity = activity;
        }

        @Override
        public void onActivityStopped(Activity activity) {
            super.onActivityStopped(activity);
            if (mCurrentActivity == activity) {
                mCurrentActivity = null;
            }
        }
    };


    public WhiteLabelModel(Context context) {
        mContext = context;
        App.getApplication().registerActivityLifecycleCallbacks(mlifecycle);
        HeadsetMonitor.getInstance(mContext).start();

        IntentFilter filter = new IntentFilter();
        filter.addAction(HeadsetMonitor.ACTION_HEADSET_CONNECTED);
        filter.addAction(HeadsetMonitor.ACTION_HEADSET_DISCONNECTED);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(mHeadsetStateReceiver,filter);

        PlaybackManager.getInstance(mContext).registerListener(this);
    }

    @Override
    public boolean isAdsEnabled() {
        return false;
    }

    @Override
    public void addItemsToDrawer(Menu menu, int groupId) {

    }

    @Override
    public void onDrawerItemClicked(MenuItem item, Context context) {

    }


    private void verify() {
        if ( isHeadsetConnected() ) {
            updateHeadphoneType();
        }
        else {
            showPrompt();
        }
    }

    private void updateHeadphoneType() {
        try {
            Headset headset = HeadsetMonitor.getInstance(mContext).getConnectedHeadset();
            if ( headset != null && AudioEffect.getInstance(mContext).getHeadPhoneType() != headset.getType()) {
                AudioEffect.getInstance(mContext).setHeadPhoneType(headset.getType());
            }
        }
        catch (IOException e) {
        }
    }


    private boolean isHeadsetConnected() {
        try {
            return HeadsetMonitor.getInstance(mContext).getConnectedHeadset() != null;
        }
        catch (IOException e) {
            return false;
        }
    }

    @Override
    public void onHeadsetConnected() {
        Log.d(TAG, "Headset is Connected" );
        updateHeadphoneType();
    }

    @Override
    public void onHeadsetDisconnected() {
        Log.d(TAG, "Headset is Disconnected" );
        if ( PlaybackManager.getInstance(mContext).isTrackPlaying() ) {
            PlaybackManager.getInstance(mContext).playPause();
        }

        showPrompt();
    }

    public void showPrompt() {
        try {
            if ( mCurrentActivity != null ) {
                Intent intent = new Intent(mContext, ConnectionActivity.class);
                mCurrentActivity.startActivity(intent);
            }
        }
        catch (Exception e) {
        }
    }

    @Override
    public void onMediaChanged() {

    }

    @Override
    public void onPlaybackCompleted() {

    }

    @Override
    public void onPlayerStateChanged() {
        if ( !isHeadsetConnected() && PlaybackManager.getInstance(mContext).isTrackPlaying() ) {
            PlaybackManager.getInstance(mContext).playPause();
        }
    }

    @Override
    public void onPlayerError() {

    }

    @Override
    public void onUpdatePlayerPosition() {

    }

    @Override
    public void onQueueUpdated() {

    }
}
