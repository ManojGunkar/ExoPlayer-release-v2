package com.player.data.Handler;

import android.content.Context;

import com.player.App;
import com.player.data.PlayingQueue.PlayingQueue;

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
