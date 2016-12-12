package com.globaldelight.boom.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.App;
import com.globaldelight.boom.task.PlayerService;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class MusicReceiver extends BroadcastReceiver {
    public static boolean isPlugged=false;
    private Handler handler;
    private updateMusic listener=null;
    public static boolean HEADSET_PLUGGED = true;
    public static boolean HEADSET_UNPLUGGED = false;
    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
            int state = intent.getIntExtra("state", -1);
            switch (state) {
                case 0:
                    if(App.getPlayerEventHandler().isPlaying() && isPlugged)
                        context.sendBroadcast(new Intent(PlayerService.ACTION_PLAY_PAUSE_SONG));
                    isPlugged = false;

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onHeadsetUnplugged();
                        }
                    });
//                    Toast.makeText(context, "Headset is unplugged", Toast.LENGTH_LONG).show();
                    break;
                case 1:
                    isPlugged = true;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            listener.onHeadsetPlugged();
                        }
                    });
//                    Toast.makeText(context, "Headset is plugged", Toast.LENGTH_LONG).show();
                    break;
                default:
//                    Toast.makeText(context, "No Idea", Toast.LENGTH_LONG).show();
            }
        }
    }

    public MusicReceiver(updateMusic listener){
        handler = new Handler();
        this.listener = listener;
    }

    public interface updateMusic{
        public void onHeadsetUnplugged();
        public void onHeadsetPlugged();
    }
}
