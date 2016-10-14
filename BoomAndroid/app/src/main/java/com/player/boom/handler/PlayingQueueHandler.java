package com.player.boom.handler;

import android.content.Context;

import com.player.boom.App;
import com.player.boom.handler.PlayingQueue.PlayingQueue;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueueHandler {

    private static PlayingQueueHandler handler;
    private static PlayingQueue playingQueue;

    private PlayingQueueHandler(Context application){
        if(playingQueue == null){
            playingQueue = PlayingQueue.getQueueInstance(application);
        }
    }

    public static PlayingQueueHandler getHandlerInstance(App application) {
        if(handler == null){
            handler = new PlayingQueueHandler(application);
        }
        return handler;
    }

    public PlayingQueue getPlayingQueue() {
        return playingQueue;
    }


    public void Terminate() {
        handler = null;
        PlayingQueue.Terminate();
    }
}
