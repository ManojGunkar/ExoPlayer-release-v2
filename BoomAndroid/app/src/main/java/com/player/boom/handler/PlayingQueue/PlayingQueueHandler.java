package com.player.boom.handler.PlayingQueue;

import android.content.Context;

import com.player.boom.App;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueueHandler {

    private static PlayingQueueHandler handler;
    private static UpNextList upNextList;

    private PlayingQueueHandler(Context application){
        if(upNextList == null){
            upNextList = UpNextList.getUpNextInstance(application);
        }
    }

    public static PlayingQueueHandler getHandlerInstance(App application) {
        if(handler == null){
            handler = new PlayingQueueHandler(application);
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
