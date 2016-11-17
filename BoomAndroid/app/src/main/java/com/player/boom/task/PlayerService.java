package com.player.boom.task;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;
import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.handler.PlayingQueue.PlayerEventHandler;
import com.player.boom.ui.musiclist.activity.BoomPlayerActivity;
import com.player.boom.ui.musiclist.activity.PlayingQueueActivity;

import java.io.IOException;

/**
 * Created by architjn on 11/12/15.
 */
public class PlayerService extends Service {

//    public static final String ACTION_PLAY_SINGLE = "ACTION_PLAY_SINGLE";
    public static final String ACTION_REPEAT_SONG = "ACTION_REPEAT_SONG";
    public static final String ACTION_SHUFFLE_SONG = "ACTION_SHUFFLE_SONG";

    public static final String ACTION_GET_SONG = "ACTION_GET_SONG";
    public static final String ACTION_NOTI_CLICK = "ACTION_NOTI_CLICK";
    public static final String ACTION_NOTI_REMOVE = "ACTION_NOTI_REMOVE";
    public static final String ACTION_CHANGE_SONG = "ACTION_CHANGE_SONG";
    public static final String ACTION_SEEK_SONG = "ACTION_SEEK_SONG";
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "ACTION_PREV_SONG";
    public static final String ACTION_PLAY_PAUSE_SONG = "ACTION_PLAY_PAUSE_SONG";
    public static final String ACTION_ADD_QUEUE = "ACTION_ADD_QUEUE";


    public static final String ACTION_PLAY_STOP = "ACTION_PLAY_STOP";
    public static final String ACTION_TRACK_POSITION_UPDATE = "ACTION_TRACK_POSITION_UPDATE";
    public static final String ACTION_UPNEXT_UPDATE = "ACTION_UPNEXT_UPDATE";
    public static final String ACTION_PLAYING_ITEM_CLICKED ="ACTION_PLAYING_ITEM_CLICKED";
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = this;
        App.setService(this);
        if (musicPlayerHandler == null)
            musicPlayerHandler = App.getPlayerEventHandler();
        IntentFilter filter = new IntentFilter();
//        filter.addAction(ACTION_PLAY_SINGLE);
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
        registerReceiver(playerServiceBroadcastReceiver, filter);
        notificationHandler = new NotificationHandler(context, this);
        return START_NOT_STICKY;
    }

//    Call by Player Fragment
    private void handleBroadcastReceived(Context context, final Intent intent) throws IOException {
        switch (intent.getAction()) {
            case ACTION_GET_SONG:
                try {
                    updatePlayer();
                } catch (ArrayIndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
                updatePlayingQueue();
                break;
            case ACTION_PLAYING_ITEM_CLICKED:
                updatePlayPause(intent.getBooleanExtra("play_pause", false));
                break;
            case ACTION_PLAY_PAUSE_SONG:
                PlayerEventHandler.PlayState state = musicPlayerHandler.PlayPause();
                updatePlayPause(state == PlayerEventHandler.PlayState.play ? true : false);
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
                sendBroadcast(new Intent(BoomPlayerActivity.ACTION_TRACK_STOPPED));
                updateNotificationPlayer(null, false);
                break;
            case ACTION_SHUFFLE_SONG:
                sendBroadcast(new Intent(BoomPlayerActivity.ACTION_UPDATE_SHUFFLE));
                musicPlayerHandler.resetShuffle();
                break;
            case ACTION_REPEAT_SONG :
                sendBroadcast(new Intent(BoomPlayerActivity.ACTION_UPDATE_REPEAT));
                musicPlayerHandler.resetRepeat();
                break;
            case ACTION_NEXT_SONG :
                musicPlayerHandler.playNextSong(true);
                break;
            case ACTION_PREV_SONG:
                musicPlayerHandler.playPrevSong();
                break;
            /*case ACTION_PLAY_SINGLE:
                musicPlayerHandler.playSingleSong(intent.getLongExtra("songId", 0));
                updatePlayer();
                break;
            case ACTION_REPEAT_SINGLE:
                musicPlayerHandler.setRepeat(PlayingQueue.REPEAT.one);
                break;
            case ACTION_REPEAT_ALL_SONGS:
                musicPlayerHandler.setRepeat(PlayingQueue.REPEAT.all);
                break;
            case ACTION_REPEAT_NONE:
                musicPlayerHandler.setRepeat(PlayingQueue.REPEAT.none);
                break;
            case ACTION_SHUFFLE_SONG:
                musicPlayerHandler.setShuffle(PlayingQueue.SHUFFLE.all);
                break;
            case ACTION_SHUFFLE_NONE:
                musicPlayerHandler.setShuffle(PlayingQueue.SHUFFLE.none);
                break;

            case ACTION_NEXT_SONG:
                musicPlayerHandler.playNextSong();
                break;
            case ACTION_PREV_SONG:
                musicPlayerHandler.playPrevSong();
                updatePlayer();
                break;
            case ACTION_CHANGE_SONG:
                musicPlayerHandler.playNextSong();
                break;*/
            case ACTION_NOTI_CLICK:
                final Intent i = new Intent();
                    i.setClass(context, BoomPlayerActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                break;/*
            case ACTION_NOTI_REMOVE:
                notificationHandler.setNotificationActive(false);
                musicPlayerHandler.stopPlayer();
                break;
            case ACTION_ADD_QUEUE:
                musicPlayerHandler.addSongToQueue();
                break;
                */
        }
    }

    private synchronized void trackSeekUpdate(boolean isUser, Intent intent){
        if(isUser){
            Intent seek = new Intent(BoomPlayerActivity.ACTION_UPDATE_TRACK_SEEK);
            seek.putExtra("percent", intent.getIntExtra("percent", 0));
            sendBroadcast(seek);
        }else{
            musicPlayerHandler.seek(intent.getIntExtra("seek", 0));
        }
    }

    private void updatePlayPause(boolean play_pause) {
        Intent i = new Intent();
        i.setAction(BoomPlayerActivity.ACTION_ITEM_CLICKED);
        i.putExtra("play_pause", play_pause);
        sendBroadcast(i);

        updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), play_pause);
    }

    private void updatePlayingQueue() {
        Intent i = new Intent();
        i.setAction(PlayingQueueActivity.ACTION_UPDATE_QUEUE);
        sendBroadcast(i);
    }

    public void updatePlayer() {
        Intent i = new Intent();
        i.setAction(BoomPlayerActivity.ACTION_RECEIVE_SONG);
        i.putExtra("playing_song", (MediaItem)musicPlayerHandler.getPlayingItem());
        i.putExtra("playing", true);
        sendBroadcast(i);
        updateNotificationPlayer((MediaItem)musicPlayerHandler.getPlayingItem(), true);
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        updateNotificationPlayer((MediaItem) musicPlayerHandler.getPlayingItem(), true);
    }

    private void updateNotificationPlayer(MediaItem playingItem, boolean playing) {
        if(!playing){
            stopForeground(false);
            notificationHandler.setNotificationPlayer(true);
        }else{
            notificationHandler.setNotificationPlayer(false);
        }
        notificationHandler.changeNotificationDetails(playingItem, playing);
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
