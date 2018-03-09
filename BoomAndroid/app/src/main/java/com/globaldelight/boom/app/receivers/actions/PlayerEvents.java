package com.globaldelight.boom.app.receivers.actions;

/**
 * Created by Rahul Agarwal on 30-11-16.
 */

public interface PlayerEvents {
    int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 1010;

    String ACTION_SONG_CHANGED = "ACTION_SONG_CHANGED";
    String ACTION_QUEUE_COMPLETED = "ACTION_QUEUE_COMPLETED";
    String ACTION_UPDATE_TRACK_POSITION = "ACTION_UPDATE_TRACK_POSITION";
    String ACTION_PLAYER_STATE_CHANGED = "ACTION_PLAYER_STATE_CHANGED";
    String ACTION_QUEUE_UPDATED = "ACTION_QUEUE_UPDATED";


    String ACTION_UPDATE_SHUFFLE = "ACTION_UPDATE_SHUFFLE";
    String ACTION_UPDATE_REPEAT = "ACTION_UPDATE_REPEAT";


    String ACTION_STOP_UPDATING_UPNEXT_DB = "ACTION_STOP_UPDATING_UPNEXT_DB";

    String ACTION_HEADSET_UNPLUGGED = "ACTION_HEADSET_UNPLUGGED";

    String ACTION_HEADSET_PLUGGED = "ACTION_HEADSET_PLUGGED";


    String ACTION_UPDATE_PLAYLIST ="ACTION_UPDATE_PLAYLIST";
    String ACTION_UPDATE_BOOM_ITEM_LIST ="ACTION_UPDATE_BOOM_ITEM_LIST";

    String ACTION_ON_NETWORK_CONNECTED="ACTION_ON_NETWORK_CONNECTED";
    String ACTION_ON_NETWORK_DISCONNECTED="ACTION_ON_NETWORK_DISCONNECTED";

    String ACTION_CLOUD_SYNC = "ACTION_CLOUD_SYNC";

    String ACTION_SHARE_SUCCESS = "com.globaldelight.boom.SHARE_SUCCESS";
    String ACTION_SHARE_FAILED = "com.globaldelight.boom.SHARE_FAILED";
}
