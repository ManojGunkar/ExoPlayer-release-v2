package com.globaldelight.boom.spotify.utils;

/**
 * Created by Manoj Kumar on 29-06-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public interface SpotifyLoginHandler {

    void onSuccessLoggedIn(String token);
    void onLogout();

}
