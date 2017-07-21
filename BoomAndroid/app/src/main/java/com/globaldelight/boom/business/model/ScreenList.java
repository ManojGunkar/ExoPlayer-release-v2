
package com.globaldelight.boom.business.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ScreenList {

    @SerializedName("LibraryScreen")
    @Expose
    private LibraryScreen libraryScreen;
    @SerializedName("EffectScreen")
    @Expose
    private EffectScreen effectScreen;
    @SerializedName("PlayerScreen")
    @Expose
    private PlayerScreen playerScreen;

    public LibraryScreen getLibraryScreen() {
        return libraryScreen;
    }

    public void setLibraryScreen(LibraryScreen libraryScreen) {
        this.libraryScreen = libraryScreen;
    }

    public EffectScreen getEffectScreen() {
        return effectScreen;
    }

    public void setEffectScreen(EffectScreen effectScreen) {
        this.effectScreen = effectScreen;
    }

    public PlayerScreen getPlayerScreen() {
        return playerScreen;
    }

    public void setPlayerScreen(PlayerScreen playerScreen) {
        this.playerScreen = playerScreen;
    }

}
