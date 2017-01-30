package com.globaldelight.boomplayer;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public interface IPlayerEvents {
        public void onStart(String mime, int sampleRate,int channels, long duration);
        public void onPlay();
        public void onPlayUpdate(int percent, long currentms, long totalms);
        public void onFinish();
        public void onStop();
        public void onError();
        public void onErrorPlayAgain();
}
