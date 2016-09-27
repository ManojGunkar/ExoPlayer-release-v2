package com.example.openslplayer;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public interface PlayerEvents {
        public void onStart(String mime, int sampleRate,int channels, long duration);
        public void onPlay();
        public void onPlayUpdate(int percent, long currentms, long totalms);
        public void onStop();
        public void onError();
}
