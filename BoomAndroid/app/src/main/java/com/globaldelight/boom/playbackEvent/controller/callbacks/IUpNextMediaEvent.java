package com.globaldelight.boom.playbackEvent.controller.callbacks;

/**
 * Created by Rahul Agarwal on 30-09-16.
 */

public interface IUpNextMediaEvent {

    public void onPlayingItemChanged();

    public void onPlayingItemClicked();

    public void onQueueUpdated();
}

