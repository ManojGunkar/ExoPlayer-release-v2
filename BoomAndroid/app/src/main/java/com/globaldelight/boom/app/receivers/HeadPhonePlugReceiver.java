package com.globaldelight.boom.app.receivers;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.util.Log;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class HeadPhonePlugReceiver extends BroadcastReceiver {
    private Callback listener=null;
    private AudioManager mAudioManager;
    private static boolean isWiredHeadsetConnected;
    private static boolean isBluetoothHeadsetConnected;


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
                listener.onHeadsetUnplugged();
                Log.d("HeadsetState:", "Disconnected");
                break;

            case 1:
                listener.onHeadsetPlugged();
                Log.d("HeadsetState:", "Connected");
                break;

            default:
                break;
        }

    }

    public HeadPhonePlugReceiver(Context context, Callback listener){
        this.listener = listener;
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        isBluetoothHeadsetConnected = mAudioManager.isBluetoothA2dpOn() || mAudioManager.isBluetoothScoOn();
        isWiredHeadsetConnected = mAudioManager.isWiredHeadsetOn();
    }

    public static boolean isHeadsetConnected() {
        return isWiredHeadsetConnected || isBluetoothHeadsetConnected;
    }

    public interface Callback {
        void onHeadsetUnplugged();
        void onHeadsetPlugged();
    }
}
