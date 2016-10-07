package com.player.player;

import android.graphics.drawable.Drawable;
import android.media.AudioTrack;

/**
 * Created by Rahul Kumar Agrawal on 6/7/2016.
 */
public interface IActivityCallback {

    public void enableVisualizer(boolean enable);
    public void setSeekProgress(int progress);
    public void setProgressPercent(int progress);
    public int getProgressPercent();
    public void updatePlayPause(Drawable d);
    public void setAlbumArtImage();
    public void addVisualizer(AudioTrack player);
}
