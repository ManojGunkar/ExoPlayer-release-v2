package com.globaldelight.boom.app.service;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;

import com.globaldelight.boom.app.activities.BoomSplash;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.notification.NotificationHandler;
import com.globaldelight.boom.app.receivers.actions.PlayerEvents;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.receivers.PlayerServiceReceiver;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.local.callback.IMediaItem;
import com.globaldelight.boom.playbackEvent.handler.PlaybackManager;
import com.globaldelight.boom.app.receivers.HeadPhonePlugReceiver;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boom.player.AudioConfiguration;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_ON_NETWORK_DISCONNECTED;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class PlayerService extends Service implements HeadPhonePlugReceiver.IUpdateMusic, PlayerServiceReceiver.IPlayerService,
        ConnectivityReceiver.ConnectivityReceiverListener {

    private long mServiceStartTime = 0;
    private long mServiceStopTime = 0;
    private static long mShiftingTime = 0;
    private PlaybackManager musicPlayerHandler;
    private NotificationHandler notificationHandler;
    private HeadPhonePlugReceiver headPhonePlugReceiver;
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private PlayerServiceReceiver serviceReceiver;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        AudioConfiguration.getInstance(this).load();

        serviceReceiver = new PlayerServiceReceiver();
        serviceReceiver.registerPlayerServiceReceiver(this, serviceReceiver, this);

        try {
            App.getPlayingQueueHandler().getUpNextList().fetchSavedUpNextItems();
        }catch (Exception e){

        }
        musicPlayerHandler = App.playbackManager();
        headPhonePlugReceiver = new HeadPhonePlugReceiver(this, this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headPhonePlugReceiver, filter);

        try {
            mServiceStartTime = SystemClock.currentThreadTimeMillis();
        }catch (Exception e){}

        Preferences.writeBoolean(this, Preferences.SLEEP_TIMER_ENABLED, false);

        connectivityReceiver = new ConnectivityReceiver(this);

        if(connectivityReceiver.isNetworkAvailable(this, false)){
            LoadNetworkCalls();
        }
    }

    private void LoadNetworkCalls() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AndroidAuthSession session = DropBoxUtills.buildSession(App.getApplication());
                dropboxAPI = new DropboxAPI<AndroidAuthSession>(session);
                App.setDropboxAPI(dropboxAPI);
                DropBoxUtills.checkAppKeySetup(App.getApplication());
            }
        }).start();

        initBusinessModel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        musicPlayerHandler = App.playbackManager();

        App.getPlayingQueueHandler().getUpNextList().getRepeatShuffleOnAppStart();

        notificationHandler = new NotificationHandler(this, this);
        return START_NOT_STICKY;
    }

    private void updatePlayerToLastPlayedSong() {
        updatePlayer(true);
    }

    private void updatePlayPause(boolean play_pause) {
        updateNotificationPlayer(musicPlayerHandler.getPlayingItem(), play_pause, false);
    }

    private void updatePlayingQueue() {
        Intent i = new Intent();
        i.setAction(PlayerEvents.ACTION_UPDATE_QUEUE);
        sendBroadcast(i);
    }

    public void updatePlayer(boolean isLastPlayedSong) {
        if(!isLastPlayedSong) {
            Intent i = new Intent();
            i.setAction(PlayerEvents.ACTION_RECEIVE_SONG);
            i.putExtra("playing_song", (MediaItem) musicPlayerHandler.getPlayingItem());
            i.putExtra("playing", true);
            i.putExtra("is_previous", musicPlayerHandler.isPrevious());
            i.putExtra("is_next", musicPlayerHandler.isNext());

            sendBroadcast(i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateNotificationPlayer(musicPlayerHandler.getPlayingItem(), true, false);
            updateUpNextDB();
        }else{
            Intent i = new Intent();
            i.setAction(PlayerEvents.ACTION_LAST_PLAYED_SONG);
            i.putExtra("playing_song", (MediaItem) musicPlayerHandler.getPlayingItem());
            i.putExtra("last_played_song", true);
            i.putExtra("is_previous", musicPlayerHandler.isPrevious());
            i.putExtra("is_next", musicPlayerHandler.isNext());
            sendBroadcast(i);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateNotificationPlayer( musicPlayerHandler.getPlayingItem(), false, true);
        }
        sendBroadcast(new Intent(PlayerEvents.ACTION_PLAYER_STATE_CHANGED));
    }

    private void updateNotificationPlayer(IMediaItem playingItem, boolean playing, boolean isLastPlayed) {
        if(!playing){
            stopForeground(false);
            notificationHandler.setNotificationPlayer(true);
        }else{
            notificationHandler.setNotificationPlayer(false);
        }
        notificationHandler.changeNotificationDetails(playingItem, playing, isLastPlayed);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onHeadsetUnplugged() {

    }

    @Override
    public void onHeadsetPlugged() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_HEADSET_PLUGGED));
    }

    @Override
    public void onNotificationClick() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_STOP_UPDATING_UPNEXT_DB));
        final Intent i = new Intent();
        i.setClass(this, BoomSplash.class);
        if(!App.playbackManager().isLibraryResumes) {
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    public void onNotificationRemove() {
        notificationHandler.setNotificationActive(false);
    }

    @Override
    public void onRepeatSongList() {
        musicPlayerHandler.resetRepeat();
        sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_REPEAT));
        updateNotificationPlayer((IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem(), App.playbackManager().isTrackPlaying(), false);
    }

    @Override
    public void onShuffleSongList() {
        musicPlayerHandler.resetShuffle();
        sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_SHUFFLE));
    }

    @Override
    public void onSongReceived() {
        try {
            updatePlayer(false);
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        updatePlayingQueue();
    }

    @Override
    public void onSongChanged() {

    }

    @Override
    public void onSeekSongTrack(Intent intent) {
        musicPlayerHandler.seek(intent.getIntExtra("seek", 0));
    }

    @Override
    public void onNextTrack() {
        if (App.getPlayingQueueHandler().getUpNextList().isNext() && !App.playbackManager().isTrackWaitingForPlay()) {
            if (System.currentTimeMillis() - mShiftingTime > 1000) {
                mShiftingTime = System.currentTimeMillis();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        musicPlayerHandler.playNextSong(true);
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onPreviousTrack() {
        if (App.getPlayingQueueHandler().getUpNextList().isPrevious() && !App.playbackManager().isTrackWaitingForPlay()) {
            if (System.currentTimeMillis() - mShiftingTime > 1000) {
                mShiftingTime = System.currentTimeMillis();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        musicPlayerHandler.playPrevSong();
                    }
                }, 500);
            }
        }
    }

    @Override
    public void onPlayPauseTrack() {
        if (null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem() && !musicPlayerHandler.isTrackWaitingForPlay() ) {
            updatePlayPause(musicPlayerHandler.playPause());
        }
    }

    @Override
    public void onAddToUpNext() {
//        musicPlayerHandler.addSongToQueue();
    }

    @Override
    public void onLastPlayedTrack() {
        updatePlayerToLastPlayedSong();
    }

    @Override
    public void onStopPlaying() {
        sendBroadcast(new Intent(PlayerEvents.ACTION_TRACK_STOPPED));
        notificationHandler.setNotificationPlayer(false);
        notificationHandler.changeNotificationDetails(App.playbackManager().getPlayingItem(), false, false);
    }


    @Override
    public void onUpNextListUpdate() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayingItemClicked(Intent intent) {
        updatePlayPause(intent.getBooleanExtra("play_pause", false));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(headPhonePlugReceiver);
        serviceReceiver.unregisterPlayerServiceReceiver(this);
        try {
            mServiceStopTime = SystemClock.currentThreadTimeMillis();
            mServiceStartTime = mServiceStopTime - mServiceStartTime;
            String time = String.format("%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(mServiceStartTime),
                    TimeUnit.MILLISECONDS.toSeconds(mServiceStartTime ) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mServiceStartTime)));
            HashMap<String, String> val = new HashMap<>();
            val.put(AnalyticsHelper.EVENT_MUSIC_SESSION_DURATION, time);
         //   FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_SESSION_DURATION, val);
            FlurryAnalytics.getInstance(this).setEvent(FlurryEvents.EVENT_MUSIC_SESSION_DURATION,val);

        }catch (Exception e){}
        super.onDestroy();
    }

    private void updateUpNextDB() {
        App.getPlayingQueueHandler().getUpNextList().SaveUpNextItems();
    }

    private void initBusinessModel() {
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        if(isConnected) {
            LoadNetworkCalls();
            sendBroadcast(new Intent(ACTION_ON_NETWORK_CONNECTED));
        }else{
            sendBroadcast(new Intent(ACTION_ON_NETWORK_DISCONNECTED));
        }
    }
}
