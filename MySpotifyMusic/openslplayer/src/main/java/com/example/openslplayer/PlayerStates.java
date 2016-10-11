package com.example.openslplayer;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public class PlayerStates {
    /**
     * Playing state which can either be stopped, playing, or reading the header before playing
     */

    public static final int PLAYING = 1;
    public static final int PAUSED = 2;
    public static final int STOPPED = 3;
    public int playerState = STOPPED;

    public int get() {
        return playerState;
    }

    public void set(int state) {
        playerState = state;
    }


    /**
     * Checks whether the player is ready to play, this is the state used also for Pause (phase 2)
     *
     * @return <code>true</code> if ready, <code>false</code> otherwise
     */
    public synchronized boolean isReadyToPlay() {
        return playerState == PlayerStates.PAUSED;
    }


    /**
     * Checks whether the player is currently playing (phase 3)
     *
     * @return <code>true</code> if playing, <code>false</code> otherwise
     */
    public synchronized boolean isPlaying() {
        return playerState == PlayerStates.PLAYING;
    }

    public boolean isPause() {
        return playerState == PlayerStates.PAUSED;
    }

    /**
     * Checks whether the player is currently stopped (not playing)
     *
     * @return <code>true</code> if playing, <code>false</code> otherwise
     */
    public synchronized boolean isStopped() {
        return playerState == PlayerStates.STOPPED;
    }
}
