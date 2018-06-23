package com.globaldelight.boom.player;

import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Connectivity;
import com.spotify.sdk.android.player.Metadata;
import com.spotify.sdk.android.player.PlaybackBitrate;
import com.spotify.sdk.android.player.PlaybackState;
import com.spotify.sdk.android.player.Player;

/**
 * Created by Manoj Kumar on 22-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class SpotifyBoomPlayer implements Player{
    @Override
    public boolean addNotificationCallback(NotificationCallback notificationCallback) {
        return false;
    }

    @Override
    public boolean removeNotificationCallback(NotificationCallback notificationCallback) {
        return false;
    }

    @Override
    public void initialize(Config config) {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean login(String s) {
        return false;
    }

    @Override
    public boolean logout() {
        return false;
    }

    @Override
    public boolean addConnectionStateCallback(ConnectionStateCallback connectionStateCallback) {
        return false;
    }

    @Override
    public boolean removeConnectionStateCallback(ConnectionStateCallback connectionStateCallback) {
        return false;
    }

    @Override
    public void playUri(OperationCallback operationCallback, String s, int i, int i1) {

    }

    @Override
    public void queue(OperationCallback operationCallback, String s) {

    }

    @Override
    public void pause(OperationCallback operationCallback) {

    }

    @Override
    public void resume(OperationCallback operationCallback) {

    }

    @Override
    public void skipToNext(OperationCallback operationCallback) {

    }

    @Override
    public void skipToPrevious(OperationCallback operationCallback) {

    }

    @Override
    public void seekToPosition(OperationCallback operationCallback, int i) {

    }

    @Override
    public void setShuffle(OperationCallback operationCallback, boolean b) {

    }

    @Override
    public void setRepeat(OperationCallback operationCallback, boolean b) {

    }

    @Override
    public void setPlaybackBitrate(OperationCallback operationCallback, PlaybackBitrate playbackBitrate) {

    }

    @Override
    public void setConnectivityStatus(OperationCallback operationCallback, Connectivity connectivity) {

    }

    @Override
    public void refreshCache() {

    }

    @Override
    public Metadata getMetadata() {
        return null;
    }

    @Override
    public PlaybackState getPlaybackState() {
        return null;
    }
}
