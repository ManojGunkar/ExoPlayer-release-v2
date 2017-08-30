package com.globaldelight.boom.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
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

    public static final String ACTION_SEEK_SONG = "ACTION_SEEK_SONG";
    public static final String ACTION_NEXT_SONG = "ACTION_NEXT_SONG";
    public static final String ACTION_PREV_SONG = "ACTION_PREV_SONG";
    public static final String ACTION_PLAY_PAUSE_SONG = "ACTION_PLAY_PAUSE_SONG";

    private IPlayerService mPlayerService;
    private Context mContext;

    public PlayerServiceReceiver(Context context) {
        mContext = context;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            handleBroadcastReceived(context, intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.cant_play_song, Toast.LENGTH_SHORT).show();
        }
    }

    public void registerService(IPlayerService mPlayerService){
        this.mPlayerService = mPlayerService;

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_REPEAT_SONG);
        filter.addAction(ACTION_SHUFFLE_SONG);
        filter.addAction(ACTION_NEXT_SONG);
        filter.addAction(ACTION_PREV_SONG);
        filter.addAction(ACTION_PLAY_PAUSE_SONG);
        filter.addAction(ACTION_SEEK_SONG);
        filter.addAction(ACTION_NOTI_CLICK);
        filter.addAction(ACTION_NOTI_REMOVE);
        mContext.registerReceiver(this, filter);
        LocalBroadcastManager.getInstance(mContext).registerReceiver(this, filter);
    }

    public void unregisterService(){
        mContext.unregisterReceiver(this);
        LocalBroadcastManager.getInstance(mContext).unregisterReceiver(this);
        this.mPlayerService = null;
    }

    private void handleBroadcastReceived(Context context, final Intent intent) {
        Bundle bundle=intent.getExtras();
        int requestCode=0;
        if(bundle!=null){
          requestCode=bundle.getInt("requestCode");
        }
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
        }
    }

    public interface IPlayerService{
        void onNotificationClick();
        void onNotificationRemove();
        void onRepeatSongList();
        void onShuffleSongList();

        void onSeekSongTrack(Intent intent);
        void onNextTrack();
        void onPreviousTrack();
        void onPlayPauseTrack();
    }
}
