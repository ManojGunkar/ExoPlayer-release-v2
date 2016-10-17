package com.player.boom.handler;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public interface IPlayerUIEvent {

    public void updateSeek(int percent, long currentms, long totalms);

    public void updateUI();

    public void stop();
}
