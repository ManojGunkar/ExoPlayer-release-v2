package com.player.ui;

/**
 * Created by Rahul Agarwal on 03-10-16.
 */

public interface UIEvent {

    public void updateSeek(int percent, long currentms, long totalms);

    public void updateUI();

    public void stop();
}
