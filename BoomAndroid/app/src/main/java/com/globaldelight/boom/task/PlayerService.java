package com.globaldelight.boom.task;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.handler.PlayingQueue.PlayerEventHandler;
import com.globaldelight.boom.ui.musiclist.activity.BoomPlayerActivity;
import com.globaldelight.boom.ui.musiclist.activity.PlayingQueueActivity;

import java.io.IOException;


public class PlayerService extends Service {

    public static final String ACTION_NOTI_CLICK = "ACTION_NOTI_CLICK";
    public static final String ACTION_NOTI_REMOVE = "ACTION_NOTI_REMOVE";

    public static final String ACTION_REPEAT_SONG = "ACTION_REPEAT_SONG";
    public static final String ACTION_SHUFFLE_SONG = "ACTION_SHUFFLE_SONG";

    public static final String ACTION_GET_SONG = "ACTION_GET_SONG";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_SEEK_SONG = "ACTION_SEEK_SONG";
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "ACTION_PREV_SONG";
    public static final String ACTION_PLAY_PAUSE_SONG = "ACTION_PLAY_PAUSE_SONG";
    public static final String ACTION_ADD_QUEUE = "ACTION_ADD_QUEUE";
    public static final String ACTION_LAST_PLAYED_SONG = "ACTION_LAST_PLAYED_SONG";

    public static final String ACTION_PLAY_STOP = "ACTION_PLAY_STOP";
    public static final String ACTION_TRACK_POSITION_UPDATE = "ACTION_TRACK_POSITION_UPDATE";
    public static final String ACTION_UPNEXT_UPDATE = "ACTION_UPNEXT_UPDATE";
    public static final String ACTION_PLAYING_ITEM_CLICKED ="ACTION_PLAYING_ITEM_CLICKED";

    public static final String ACTION_UPDATE_BOOM_PLAYLIST ="ACTION_UPDATE_BOOM_PLAYLIST";
    public static final String ACTION_UPDATE_BOOM_PLAYLIST_LIST ="ACTION_UPDATE_BOOM_PLAYLIST_LIST";
    private static long mShiftingTime = 0;
    private PlayerEventHandler musicPlayerHandler;
    private Context context;
    private NotificationHandler notificationHandler;
    private BroadcastReceiver playerServiceBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                handleBroadcastReceived(context, intent);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(PlayerService.this, R.string.cant_play_song, Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        App.setService(this);

        App.getPlayingQueueHandler().getUpNextList().fetchUpNextItemsToDB();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (musicPlayerHandler == null)
            musicPlayerHandler = App.getPlayerEventHandler();

        App.getPlayingQueueHandler().getUpNextList().updateRepeatShuffleOnAppStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPEAT_SONG);
        filter.addAction(ACTION_SHUFFLE_SONG);
        filter.addAction(ACTION_GET_SONG);
        filter.addAction(ACTION_NEXT_SONG);
        filter.addAction(ACTION_PREV_SONG);
        filter.addAction(ACTION_PLAY_PAUSE_SONG);
        filter.addAction(ACTION_SEEK_SONG);
        filter.addAction(ACTION_CHANGE_SONG);
        filter.addAction(ACTION_NOTI_CLICK);
        filter.addAction(ACTION_NOTI_REMOVE);
        filter.addAction(ACTION_ADD_QUEUE);
        filter.addAction(ACTION_PLAY_STOP);
        filter.addAction(ACTION_TRACK_POSITION_UPDATE);
        filter.addAction(ACTION_UPNEXT_UPDATE);
        filter.addAction(ACTION_PLAYING_ITEM_CLICKED);
        filter.addAction(ACTION_LAST_PLAYED_SONG);
        registerReceiver(playerServiceBroadcastReceiver, filter);
        notificationHandler = new NotificationHandler(context, this);
        return START_NOT_STICKY;
    }

//    Call by Player Fragment
    private void handleBroadcastReceived(Context context, final Intent intent) throws IOException {
        switch (intent.getAction()) {
            case ACTION_GET_SONG:
                try {
                    updatePlayer(false);
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                updatePlayingQueue();
                break;
            case ACTION_PLAYING_ITEM_CLICKED:
                updatePlayPause(intent.getBooleanExtra("play_pause", false));
                break;
            case ACTION_PLAY_PAUSE_SONG:
                if(!musicPlayerHandler.isPaused() && !musicPlayerHandler.isPlaying()){
                    musicPlayerHandler.onPlayingItemChanged();
                }else {
                    PlayerEventHandler.PlayState state = musicPlayerHandler.PlayPause();
                    updatePlayPause(state == PlayerEventHandler.PlayState.play ? true : false);
                }
                break;
            case ACTION_UPNEXT_UPDATE:
                updatePlayingQueue();
                break;
            case ACTION_TRACK_POSITION_UPDATE:
                trackSeekUpdate(true, intent);
                break;
            case ACTION_SEEK_SONG:
                trackSeekUpdate(false, intent);
                break;
            case ACTION_PLAY_STOP:
                sendBroadcast(new Intent(PlayerEvents.ACTION_TRACK_STOPPED));
                updateNotificationPlayer(null, false, false);
                break;
            case ACTION_SHUFFLE_SONG:
                musicPlayerHandler.resetShuffle();
                sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_SHUFFLE));
                break;
            case ACTION_REPEAT_SONG :
                musicPlayerHandler.resetRepeat();
                sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_REPEAT));
                break;
            case ACTION_NEXT_SONG :
                if(System.currentTimeMillis() - mShiftingTime > 1000) {
                    mShiftingTime = System.currentTimeMillis();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            musicPlayerHandler.playNextSong(true);
                        }
                    }, 500);
                }
                break;
            case ACTION_PREV_SONG:
                if(System.currentTimeMillis() - mShiftingTime > 1000) {
                    mShiftingTime = System.currentTimeMillis();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            musicPlayerHandler.playPrevSong();
                        }
                    }, 500);
                }
                break;
            case ACTION_LAST_PLAYED_SONG:
                updatePlayerToLastPlayedSong();
                break;
            case ACTION_NOTI_CLICK:
                final Intent i = new Intent();
                    i.setClass(context, BoomPlayerActivity.class);
                if(!App.getPlayerEventHandler().isPlayerResume) {
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                }
                break;
            case ACTION_NOTI_REMOVE:
                notificationHandler.setNotificationActive(false);
                updateUpNextDB();
                break;
            /*case ACTION_ADD_QUEUE:
                musicPlayerHandler.addSongToQueue();
                break;
                */
        }
    }

    private void updateUpNextDB() {
        App.getPlayingQueueHandler().getUpNextList().addUpNextItemsToDB();
        if (musicPlayerHandler.getPlayer() != null) {
            musicPlayerHandler.stop();
            musicPlayerHandler.release();
        }
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
        }else{
            musicPlayerHandler.seek(intent.getIntExtra("seek", 0));
        }
    }

    private void updatePlayPause(boolean play_pause) {
        Intent i = new Intent();
        i.setAction(PlayerEvents.ACTION_ITEM_CLICKED);
        i.putExtra("play_pause", play_pause);
        sendBroadcast(i);

        updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), play_pause, false);
    }

    private void updatePlayingQueue() {
        Intent i = new Intent();
        i.setAction(PlayingQueueActivity.ACTION_UPDATE_QUEUE);
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
            updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), true, false);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), true, false);
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
            updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), false, true);
        }
    }

    private void updateNotificationPlayer(MediaItem playingItem, boolean playing, boolean isLastPlayed) {
        notificationHandler.setNotificationPlayer(false);
        if(!playing){
            stopForeground(false);
            notificationHandler.setNotificationPlayer(true);
        }/*else{
            notificationHandler.setNotificationPlayer(false);
        }*/
        notificationHandler.changeNotificationDetails(playingItem, playing, isLastPlayed);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (musicPlayerHandler.getPlayer() != null) {
            musicPlayerHandler.stop();
            musicPlayerHandler.release();
        }
    }
}
