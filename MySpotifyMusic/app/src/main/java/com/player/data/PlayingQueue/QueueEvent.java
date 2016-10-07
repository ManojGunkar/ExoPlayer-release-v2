package com.player.data.PlayingQueue;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public interface QueueEvent {

    public void onPlayingItemChanged();

    public void onQueueUpdated();

}

