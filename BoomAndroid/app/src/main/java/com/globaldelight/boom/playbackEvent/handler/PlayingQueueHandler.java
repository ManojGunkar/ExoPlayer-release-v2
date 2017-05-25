package com.globaldelight.boom.playbackEvent.handler;

import android.content.Context;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueueHandler {

    private static PlayingQueueHandler handler;
    private static UpNextPlayingQueue upNextList;

    private PlayingQueueHandler(Context context){
        if(upNextList == null){
            upNextList = UpNextPlayingQueue.getUpNextInstance(context);
        }
    }

    public static PlayingQueueHandler getHandlerInstance(Context context) {
        if(handler == null){
            handler = new PlayingQueueHandler(context);
        }
        return handler;
    }

    public UpNextPlayingQueue getUpNextList() {
        return upNextList;
    }


    public void Terminate() {
        handler = null;
        getUpNextList().Terminate();
    }
}
