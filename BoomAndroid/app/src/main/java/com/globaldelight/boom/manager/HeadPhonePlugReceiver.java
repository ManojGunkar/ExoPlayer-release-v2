package com.globaldelight.boom.manager;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.globaldelight.boom.App;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class HeadPhonePlugReceiver extends BroadcastReceiver {
    public static boolean isPlugged = false;
    private Handler handler;
    private IUpdateMusic listener=null;
    private AudioManager mAudioManager;
    public static boolean HEADSET_PLUGGED = true;
    public static boolean HEADSET_UNPLUGGED = false;
    private boolean isWiredHeadsetConnected;
    private boolean isBluetoothHeadsetConnected;


    @Override public void onReceive(final Context context, Intent intent) {

        int state = -1;
        switch (intent.getAction()) {
            case BluetoothAdapter.ACTION_STATE_CHANGED: {
                int btState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                if ( isBluetoothHeadsetConnected && btState == BluetoothAdapter.STATE_OFF  ) {
                    state = 0;
                    isBluetoothHeadsetConnected = false;
                }
                break;
            }

            case BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED: {
                int headsetState = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, -1);
                if ( headsetState == BluetoothHeadset.STATE_CONNECTED && !isBluetoothHeadsetConnected ) {
                    state = 1;
                    isBluetoothHeadsetConnected = true;
                }
                else  if ( headsetState == BluetoothHeadset.STATE_DISCONNECTED && isBluetoothHeadsetConnected) {
                    state = 0;
                    isBluetoothHeadsetConnected = false;

                }

                break;
            }

            case Intent.ACTION_HEADSET_PLUG: {
                int headsetState = intent.getIntExtra("state", -1);
                if ( headsetState == 0 && isWiredHeadsetConnected ) {
                    state = 0;
                    isWiredHeadsetConnected = false;
                }
                else if ( headsetState == 1 && !isWiredHeadsetConnected ) {
                    state = 1;
                    isWiredHeadsetConnected = true;
                }
                break;
            }
        }

        switch (state) {
            case 0:
                if( App.getPlayerEventHandler().isPlaying() )
                    context.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG));

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onHeadsetUnplugged();
                        Log.d("HeadsetState:", "Disconnected");
                    }
                });
                break;

            case 1:
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        listener.onHeadsetPlugged();
                        Log.d("HeadsetState:", "Connected");
                    }
                });
                break;
            default:
//                    Toast.makeText(context, "No Idea", Toast.LENGTH_LONG).show();
        }

        isPlugged = isHeadsetConnected();
    }

    public HeadPhonePlugReceiver(Context context, IUpdateMusic listener){
        handler = new Handler();
        this.listener = listener;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isBluetoothHeadsetConnected = mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn();
        isWiredHeadsetConnected = mAudioManager.isWiredHeadsetOn();
        isPlugged = isHeadsetConnected();
    }

    public boolean isHeadsetConnected() {
        return isWiredHeadsetConnected || isBluetoothHeadsetConnected;
    }

    public interface IUpdateMusic {
        public void onHeadsetUnplugged();
        public void onHeadsetPlugged();
    }
}
