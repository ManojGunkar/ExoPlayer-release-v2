package com.globaldelight.boom.playbackEvent.handler;

import android.content.Context;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueueHandler {

    private static PlayingQueueHandler handler;
    private UpNextPlayingQueue upNextList;

    private PlayingQueueHandler(Context context){
        upNextList = new UpNextPlayingQueue(context);
    }

    public static PlayingQueueHandler getInstance(Context context) {
        if(handler == null){
            handler = new PlayingQueueHandler(context.getApplicationContext());
        }
        return handler;
    }

    public UpNextPlayingQueue getUpNextList() {
        return upNextList;
    }


    public void Terminate() {
        handler = null;
    }
}
