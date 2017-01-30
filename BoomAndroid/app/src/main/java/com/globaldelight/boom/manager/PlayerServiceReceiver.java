package com.globaldelight.boom.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.task.PlayerService;

/**
 * Created by Rahul Agarwal on 28-01-17.
 */

public class PlayerServiceReceiver extends BroadcastReceiver {
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

    public static final String ACTION_CREATE_PLAYER_SCREEN = "ACTION_CREATE_PLAYER_SCREEN";
    public static final String ACTION_DESTROY_PLAYER_SCREEN ="ACTION_DESTROY_PLAYER_SCREEN";

    private static PlayerServiceReceiver receiverHandler;
    private static IPlayerService mPlayerService;

    private static Handler mPostMessage;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            handleBroadcastReceived(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_play_song, Toast.LENGTH_SHORT).show();
        }
    }

    public void registerPlayerServiceReceiver(PlayerService service, PlayerServiceReceiver receiver, IPlayerService mPlayerService){
        this.receiverHandler = receiver;
        mPostMessage = new Handler();
        this.mPlayerService = mPlayerService;

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
        filter.addAction(ACTION_CREATE_PLAYER_SCREEN);
        filter.addAction(ACTION_DESTROY_PLAYER_SCREEN);
        service.registerReceiver(receiverHandler, filter);
    }

    public void unregisterPlayerServiceReceiver(PlayerService service){
        service.unregisterReceiver(receiverHandler);
        this.mPlayerService = null;
    }

    private void handleBroadcastReceived(Context context, final Intent intent) {
        switch (intent.getAction()){
            case ACTION_NOTI_CLICK :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onNotificationClick();
                    }
                });
                break;
            case ACTION_NOTI_REMOVE :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onNotificationRemove();
                    }
                });
                break;
            case ACTION_REPEAT_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onRepeatSongList();
                    }
                });
                break;
            case ACTION_SHUFFLE_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onShuffleSongList();
                    }
                });
                break;
            case ACTION_GET_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onSongReceived();
                    }
                });
                break;
            case ACTION_CHANGE_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onSongChanged();
                    }
                });
                break;
            case ACTION_SEEK_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onSeekSongTrack(intent);
                    }
                });
                break;
            case ACTION_NEXT_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onNextTrack();
                    }
                });
                break;
            case ACTION_PREV_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onPreviousTrack();
                    }
                });
                break;
            case ACTION_PLAY_PAUSE_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onPlayPauseTrack();
                    }
                });
                break;
            case ACTION_ADD_QUEUE :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onAddToUpNext();
                    }
                });
                break;
            case ACTION_LAST_PLAYED_SONG :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onLastPlayedTrack();
                    }
                });
                break;
            case ACTION_PLAY_STOP :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onStopPlaying();
                    }
                });
                break;
            case ACTION_TRACK_POSITION_UPDATE :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onTrackPositionUpdate(intent);
                    }
                });
                break;
            case ACTION_UPNEXT_UPDATE :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onUpNextListUpdate();
                    }
                });
                break;
            case ACTION_PLAYING_ITEM_CLICKED :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onPlayingItemClicked(intent);
                    }
                });
                break;
            case ACTION_CREATE_PLAYER_SCREEN :
                mPostMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        mPlayerService.onCreateLibrary();
                    }
                });
                break;
            case ACTION_DESTROY_PLAYER_SCREEN :
                mPlayerService.onDestroyLibrary();
                break;
        }
    }

    public interface IPlayerService{
        void onNotificationClick();
        void onNotificationRemove();
        void onRepeatSongList();
        void onShuffleSongList();

        void onSongReceived();
        void onSongChanged();
        void onSeekSongTrack(Intent intent);
        void onNextTrack();
        void onPreviousTrack();
        void onPlayPauseTrack();
        void onAddToUpNext();
        void onLastPlayedTrack();

        void onStopPlaying();
        void onTrackPositionUpdate(Intent intent);
        void onUpNextListUpdate();
        void onPlayingItemClicked(Intent intent);

        void onCreateLibrary();
        void onDestroyLibrary();
    }
}
