package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;

import com.globaldelight.boom.App;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueueHandler {

    private static PlayingQueueHandler handler;
    private static UpNextList upNextList;

    private PlayingQueueHandler(Context context){
        if(upNextList == null){
            upNextList = UpNextList.getUpNextInstance(context);
        }
    }

    public static PlayingQueueHandler getHandlerInstance(Context context) {
        if(handler == null){
            handler = new PlayingQueueHandler(context);
        }
        return handler;
    }

    public UpNextList getUpNextList() {
        return upNextList;
    }


    public void Terminate() {
        handler = null;
        UpNextList.Terminate();
    }
}
