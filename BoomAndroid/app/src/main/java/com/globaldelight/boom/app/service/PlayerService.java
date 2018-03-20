package com.globaldelight.boom.app.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.BoomSplash;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.receivers.PlayerServiceReceiver;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.player.AudioConfiguration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_DISCONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_PLAYER_STATE_CHANGED;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class PlayerService extends Service implements HeadPhonePlugReceiver.Callback, PlayerServiceReceiver.IPlayerService,
        ConnectivityReceiver.ConnectivityReceiverListener, PlaybackManager.Listener {

    private long mServiceStartTime = 0;
    private long mServiceStopTime = 0;
    private PlaybackManager mPlayback;
    private NotificationHandler notificationHandler;
    private HeadPhonePlugReceiver headPhonePlugReceiver;
    private PlayerServiceReceiver serviceReceiver;
    private ConnectivityReceiver connectivityReceiver;
    private boolean mIsTaskRunning = true;

    @Override
    public void onCreate() {
        super.onCreate();
        AudioConfiguration.getInstance(this).load();

        serviceReceiver = new PlayerServiceReceiver(this);
        serviceReceiver.registerService(this);

        notificationHandler = new NotificationHandler(this);

        mPlayback = PlaybackManager.getInstance(this);
        mPlayback.registerListener(this);

        try {
            mPlayback.queue().fetchSavedUpNextItems();
        }catch (Exception e){

        }

        headPhonePlugReceiver = new HeadPhonePlugReceiver(this, this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headPhonePlugReceiver, filter);

        try {
            mServiceStartTime = SystemClock.currentThreadTimeMillis();
        }catch (Exception e){}

        Preferences.writeBoolean(this, Preferences.SLEEP_TIMER_ENABLED, false);

        connectivityReceiver = new ConnectivityReceiver(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mIsTaskRunning = true;
        mPlayback.queue().getRepeatShuffleOnAppStart();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headPhonePlugReceiver);
        serviceReceiver.unregisterService();
        mPlayback.unregisterListener(this);
       // BusinessModelFactory.getCurrentModel().close();
        try {
            mServiceStopTime = SystemClock.currentThreadTimeMillis();
            mServiceStartTime = mServiceStopTime - mServiceStartTime;
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(mServiceStartTime),
                    TimeUnit.MILLISECONDS.toSeconds(mServiceStartTime ) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mServiceStartTime)));
            HashMap<String, String> val = new HashMap<>();
            val.put(AnalyticsHelper.EVENT_MUSIC_SESSION_DURATION, time);
            FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_MUSIC_SESSION_DURATION,val);

        }catch (Exception e){}
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        mIsTaskRunning = false;
        if ( !mPlayback.isTrackPlaying() ) {
            stopSelf();
        }
    }

    private void updatePlayPause(boolean play_pause) {
        updateNotificationPlayer(mPlayback.getPlayingItem(), play_pause, false);
    }


    private void updateNotificationPlayer(IMediaItem playingItem, boolean playing, boolean isLastPlayed) {
        if( !playing ){
            if ( !mIsTaskRunning ) {
                stopSelf();
                return; // Prevent zombie notification
            }
            else {
                stopForeground(false);
            }
        }
        notificationHandler.update(playingItem, playing, isLastPlayed);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHeadsetUnplugged() {
        if ( mPlayback.isPlaying() ) {
            onPlayPauseTrack();
        }
    }

    @Override
    public void onHeadsetPlugged() {
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_HEADSET_PLUGGED));
    }

    @Override
    public void onNotificationClick() {
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_STOP_UPDATING_UPNEXT_DB));
        final Intent i = new Intent();
        i.setClass(this, BoomSplash.class);
        if(!App.playbackManager().isLibraryResumes) {
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    public void onNotificationRemove() {
        if ( !mIsTaskRunning ) {
            stopSelf();
        }
    }

    @Override
    public void onRepeatSongList() {
        mPlayback.resetRepeat();
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_REPEAT));
        updateNotificationPlayer((IMediaItem) mPlayback.queue().getPlayingItem(), App.playbackManager().isTrackPlaying(), false);
    }

    @Override
    public void onShuffleSongList() {
        mPlayback.resetShuffle();
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_SHUFFLE));
    }

    @Override
    public void onSeekSongTrack(Intent intent) {
        mPlayback.seek(intent.getIntExtra("seek", 0));
    }

    @Override
    public void onNextTrack() {
        if ( mPlayback.queue().isNext() ) {
            mPlayback.playNextSong(true);
        }
    }

    @Override
    public void onPreviousTrack() {
        if ( mPlayback.queue().isPrevious() ) {
            mPlayback.playPrevSong();
        }
    }

    @Override
    public void onPlayPauseTrack() {
        if ( null != mPlayback.queue().getPlayingItem() ) {
            mPlayback.playPause();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected) {
            sendLocalBroadcast(new Intent(ACTION_ON_NETWORK_CONNECTED));
        }else{
            sendLocalBroadcast(new Intent(ACTION_ON_NETWORK_DISCONNECTED));
        }
    }

    @Override
    public void onMediaChanged() {
        Intent i = new Intent();
        i.setAction(PlayerEvents.ACTION_SONG_CHANGED);
        i.putExtra("playing_song", (MediaItem) mPlayback.getPlayingItem());
        i.putExtra("playing", true);
        i.putExtra("is_previous", mPlayback.isPrevious());
        i.putExtra("is_next", mPlayback.isNext());

        sendLocalBroadcast(i);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateNotificationPlayer(mPlayback.getPlayingItem(), true, false);
        mPlayback.queue().SaveUpNextItems(false);
    }

    @Override
    public void onPlayerStateChanged() {
        if ( mPlayback.isPlaying() ) {
            FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_PLAY_PLAYING);
        }
        else if (mPlayback.isPaused() ) {
            FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_PAUSE_PLAYING);
        }


        updatePlayPause(mPlayback.isTrackPlaying());
        sendLocalBroadcast(new Intent(ACTION_PLAYER_STATE_CHANGED));
    }

    @Override
    public void onPlayerError() {
        Toast.makeText(this, this.getResources().getString(R.string.error_in_playing), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpdatePlayerPosition() {
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_TRACK_POSITION));
    }

    @Override
    public void onPlaybackCompleted() {
        updateNotificationPlayer( mPlayback.getPlayingItem(), false, true);
        sendLocalBroadcast(new Intent(PlayerEvents.ACTION_QUEUE_COMPLETED));
    }

    @Override
    public void onQueueUpdated() {
        Intent i = new Intent();
        i.setAction(PlayerEvents.ACTION_QUEUE_UPDATED);
        sendLocalBroadcast(i);
    }

    private void sendLocalBroadcast(Intent intent) {
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
