package com.globaldelight.boom.handler.PlayingQueue;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public interface QueueEvent {

    public void onPlayingItemChanged();

    public void onPlayingItemClicked();

    public void onQueueUpdated();
}

