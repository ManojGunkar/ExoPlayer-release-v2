package com.globaldelight.boom.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.service.PlayerService;

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

    private static PlayerServiceReceiver receiverHandler;
    private static IPlayerService mPlayerService;

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
        service.registerReceiver(receiverHandler, filter);
    }

    public void unregisterPlayerServiceReceiver(PlayerService service){
        service.unregisterReceiver(receiverHandler);
        this.mPlayerService = null;
    }

    private void handleBroadcastReceived(Context context, final Intent intent) {
        Bundle bundle=intent.getExtras();
        int requestCode=0;
        if(bundle!=null){
          requestCode=bundle.getInt("requestCode");}
        switch (intent.getAction()){
            case ACTION_NOTI_CLICK :
                if(null != mPlayerService)
                    mPlayerService.onNotificationClick();
                break;
            case ACTION_NOTI_REMOVE :
                if(null != mPlayerService)
                    mPlayerService.onNotificationRemove();
                break;
            case ACTION_REPEAT_SONG :
                if(null != mPlayerService)
                    mPlayerService.onRepeatSongList();
                break;
            case ACTION_SHUFFLE_SONG :
                if(null != mPlayerService)
                    mPlayerService.onShuffleSongList();
                break;
            case ACTION_GET_SONG :
                if(null != mPlayerService)
                    mPlayerService.onSongReceived();
                break;
            case ACTION_CHANGE_SONG :
                if(null != mPlayerService)
                    mPlayerService.onSongChanged();
                break;
            case ACTION_SEEK_SONG :
                if(null != mPlayerService)
                    mPlayerService.onSeekSongTrack(intent);
                break;
            case ACTION_NEXT_SONG :
                if(requestCode==10102){
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.Next_Button_Tapped_from_Notification_bar);
                    FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Next_Button_Tapped_from_Notification_bar);

                    Log.d("notifi","1");
                }
                if(null != mPlayerService)
                    mPlayerService.onNextTrack();
                break;
            case ACTION_PREV_SONG :
                if(requestCode==10103){
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.previous_Button_Tapped_from_Notification_bar);
                    FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.previous_Button_Tapped_from_Notification_bar);

                    Log.d("notifi","2");
                }

                if(null != mPlayerService)
                    mPlayerService.onPreviousTrack();
                break;
            case ACTION_PLAY_PAUSE_SONG :
                if(requestCode==10101){
//                    FlurryAnalyticHelper.logEvent(UtilAnalytics.play_pause_Button_Tapped_from_Notification_bar);
                    FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.play_pause_Button_Tapped_from_Notification_bar);

                    Log.d("notifi","3");
                }

                if(null != mPlayerService)
                    mPlayerService.onPlayPauseTrack();
                break;
            case ACTION_ADD_QUEUE :
                if(null != mPlayerService)
                    mPlayerService.onAddToUpNext();
                break;
            case ACTION_LAST_PLAYED_SONG :
                if( null != mPlayerService)
                    mPlayerService.onLastPlayedTrack();
                break;
            case ACTION_PLAY_STOP :
                if (null != mPlayerService)
                    mPlayerService.onStopPlaying();
                break;
            case ACTION_TRACK_POSITION_UPDATE :
                if(null != mPlayerService)
                    mPlayerService.onTrackPositionUpdate(intent);
                break;
            case ACTION_UPNEXT_UPDATE :
                if (null != mPlayerService)
                    mPlayerService.onUpNextListUpdate();
                break;
            case ACTION_PLAYING_ITEM_CLICKED :
                if(null != mPlayerService)
                    mPlayerService.onPlayingItemClicked(intent);
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
    }
}
