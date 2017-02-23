package com.globaldelight.boom.task;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import com.globaldelight.boom.utils.Utils;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.globaldelight.boom.App;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.client.IBusinessNetworkInit;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.ui.musiclist.activity.MainActivity;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;
import com.globaldelight.boom.manager.HeadPhonePlugReceiver;
import com.globaldelight.boom.utils.helpers.DropBoxUtills;
import com.globaldelight.boomplayer.AudioConfiguration;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.globaldelight.boom.manager.BusinessRequestReceiver.ACTION_BUSINESS_APP_EXPIRE;
import static com.globaldelight.boom.manager.BusinessRequestReceiver.ACTION_BUSINESS_CONFIGURATION;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_CONNECTED;
import static com.globaldelight.boom.task.PlayerEvents.ACTION_ON_NETWORK_DISCONNECTED;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class PlayerService extends Service implements HeadPhonePlugReceiver.IUpdateMusic, PlayerServiceReceiver.IPlayerService,
        IBusinessNetworkInit, ConnectivityReceiver.ConnectivityReceiverListener {

    private long mServiceStartTime = 0;
    private long mServiceStopTime = 0;
    private static long mShiftingTime = 0;
    private PlayerEventHandler musicPlayerHandler;
    private Context context;
    private NotificationHandler notificationHandler;
    private static boolean isPlayerScreenResume = false;
    private HeadPhonePlugReceiver headPhonePlugReceiver;
    private DropboxAPI<AndroidAuthSession> dropboxAPI;
    private PlayerServiceReceiver serviceReceiver;
    private ConnectivityReceiver connectivityReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        App.setService(this);
        AudioConfiguration.getInstance(this).load();

        serviceReceiver = new PlayerServiceReceiver();
        serviceReceiver.registerPlayerServiceReceiver(this, serviceReceiver, this);

        try {
            App.getPlayingQueueHandler().getUpNextList().fetchUpNextItemsToDB(this);
        }catch (Exception e){

        }

        if (musicPlayerHandler == null) {
            musicPlayerHandler = App.getPlayerEventHandler();
            Log.d("Service : ", "onCreate");
        }

        headPhonePlugReceiver = new HeadPhonePlugReceiver(this, this);

        IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
        registerReceiver(headPhonePlugReceiver, filter);

        try {
            mServiceStartTime = SystemClock.currentThreadTimeMillis();
        }catch (Exception e){}

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

        if (musicPlayerHandler == null)
            musicPlayerHandler = App.getPlayerEventHandler();

        App.getPlayingQueueHandler().getUpNextList().updateRepeatShuffleOnAppStart();

        notificationHandler = new NotificationHandler(context, this);
//        App.setNotifica
        return START_NOT_STICKY;
    }

    private void updatePlayerToLastPlayedSong() {
        updatePlayer(true);
    }

    private synchronized void trackSeekUpdate(boolean isUser, Intent intent){
        if(isUser){
            Intent seek = new Intent(PlayerEvents.ACTION_UPDATE_TRACK_SEEK);
            seek.putExtra("percent", intent.getIntExtra("percent", 0));
            seek.putExtra("currentms", intent.getLongExtra("currentms", 0));
            seek.putExtra("totalms", intent.getLongExtra("totalms", 0));
            sendBroadcast(seek);

            App.getUserPreferenceHandler().setPlayerSeekPosition(intent.getIntExtra("percent", 0));
            App.getUserPreferenceHandler().setPlayedTime(intent.getLongExtra("currentms", 0));
            App.getUserPreferenceHandler().setRemainsTime(intent.getLongExtra("totalms", 0));
        }else{
            musicPlayerHandler.seek(intent.getIntExtra("seek", 0));
        }
    }

    private void updatePlayPause(boolean play_pause) {
        Intent i = new Intent();
        i.setAction(PlayerEvents.ACTION_ITEM_CLICKED);
        i.putExtra("play_pause", play_pause);
        sendBroadcast(i);

        updateNotificationPlayer((IMediaItem) musicPlayerHandler.getPlayingItem(), play_pause, false);
        sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));
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
            updateNotificationPlayer(musicPlayerHandler.getPlayingItem(), true, false);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateNotificationPlayer(musicPlayerHandler.getPlayingItem(), true, false);
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
            updateNotificationPlayer((IMediaItem) musicPlayerHandler.getPlayingItem(), false, true);
        }
        sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_NOW_PLAYING_ITEM_IN_LIBRARY));
    }

    private void updateNotificationPlayer(IMediaItem playingItem, boolean playing, boolean isLastPlayed) {
        if(!playing){
            stopForeground(true);
            notificationHandler.setNotificationPlayer(true);
        }else{
            notificationHandler.setNotificationPlayer(false);
        }
        notificationHandler.changeNotificationDetails(playingItem, playing, isLastPlayed);
        if(playingItem == null && !isLastPlayed){
            updateUpNextDB();
        }
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
        i.setClass(context, MainActivity.class);
        if(!App.getPlayerEventHandler().isPlayerResume) {
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    public void onNotificationRemove() {
        notificationHandler.setNotificationActive(false);
        if(!isPlayerScreenResume) {
            updateUpNextDB();
        }
    }

    @Override
    public void onRepeatSongList() {
        musicPlayerHandler.resetRepeat();
        sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_REPEAT));
        updateNotificationPlayer((IMediaItem) App.getPlayingQueueHandler().getUpNextList().getPlayingItem(), App.getPlayerEventHandler().isPlaying(), false);
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
        trackSeekUpdate(false, intent);
    }

    @Override
    public void onNextTrack() {
        if (App.getPlayingQueueHandler().getUpNextList().isNext() && !App.getPlayerEventHandler().isTrackWaitingForPlay()) {
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
        if (App.getPlayingQueueHandler().getUpNextList().isPrevious() && !App.getPlayerEventHandler().isTrackWaitingForPlay()) {
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
        if (null != App.getPlayingQueueHandler().getUpNextList().getPlayingItem() && !App.getPlayerEventHandler().isTrackWaitingForPlay() && !musicPlayerHandler.isLoading() ) {
            if (!musicPlayerHandler.isPaused() && !musicPlayerHandler.isPlaying()  ) {
                musicPlayerHandler.onPlayingItemChanged();
            } else {
                PlayerEventHandler.PlayState state = musicPlayerHandler.PlayPause();
                if (state != PlayerEventHandler.PlayState.play && !isPlayerScreenResume) {
                    updateUpNextDB();
                } else {
                    updatePlayPause(state == PlayerEventHandler.PlayState.play ? true : false);
                }
            }
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
        updateNotificationPlayer(App.getPlayerEventHandler().getPlayingItem(), false, false);
    }

    @Override
    public void onTrackPositionUpdate(Intent intent) {
        trackSeekUpdate(true, intent);
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
    public void onCreateLibrary() {
        isPlayerScreenResume = true;
    }

    @Override
    public void onDestroyLibrary() {
        isPlayerScreenResume = false;
        if(notificationHandler.isNotificationActive() && !App.getPlayerEventHandler().isPlaying()) {
            stopForeground(true);
            notificationHandler.setNotificationPlayer(true);
            notificationHandler.changeNotificationDetails(null, false , false);
            App.getPlayingQueueHandler().getUpNextList().addUpNextItemsToDB();
        }
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
            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_MUSIC_SESSION_DURATION, val);
        }catch (Exception e){}
        super.onDestroy();
    }

    private void updateUpNextDB() {
        if(notificationHandler.isNotificationActive()) {
            notificationHandler.removeNotification();
            stopForeground(true);
        }
        App.getPlayingQueueHandler().getUpNextList().addUpNextItemsToDB();
        if (musicPlayerHandler.getPlayer() != null) {
            musicPlayerHandler.stop();
//            musicPlayerHandler.release();
        }
        App.getService().stopSelf();
    }

    private void initBusinessModel() {

        boolean isShownAdds = true;
        if(BusinessPreferences.readBoolean(this, BusinessPreferences.ACTION_APP_SHARED, false)){
            if(BusinessPreferences.readBoolean(this, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)){
                isShownAdds = false;
            }else {
                isShownAdds = Utils.isShareExpireHour(this);
            }
        }else if(BusinessPreferences.readBoolean(this, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)){
            isShownAdds = false;
        }

        if(isShownAdds) {
            App.getBusinessHandler().setBusinessNetworkListener(this);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    App.getBusinessHandler().getBoomAccessToken();
                    App.getBusinessHandler().registerAndroidDevice();
                    App.getBusinessHandler().getConfigAppWithBoomServer();
                    App.getBusinessHandler().isAppTrialVersion();
                }
            }).start();
        }
    }

    @Override
    public void onGetAccessToken(boolean success) {

    }

    @Override
    public void onRegisterDevice(boolean success) {

    }

    @Override
    public void onGetBusinessConfiguration(boolean success) {
        if (success)
            sendBroadcast(new Intent(ACTION_BUSINESS_CONFIGURATION));
    }

    @Override
    public void onAppTrailExpired(boolean expired) {
        if(expired){
//            Show dialog and get Email
            sendBroadcast(new Intent(ACTION_BUSINESS_APP_EXPIRE));
        }
    }

    @Override
    public void onEmailSubmition(boolean success) {

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
