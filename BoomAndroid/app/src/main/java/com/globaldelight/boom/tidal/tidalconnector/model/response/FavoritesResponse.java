package com.globaldelight.boom.tidal.tidalconnector.model.response;

import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.globaldelight.boom.tidal.tidalconnector.model.ItemCollection;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by adarsh on 22/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class FavoritesResponse {
    @SerializedName("ALBUM")
    @Expose
    private List<String> albums;
    @SerializedName("TRACK")
    @Expose
    private List<String> tracks;
    @SerializedName("ARTIST")
    @Expose
    private List<String> artists;
    @SerializedName("PLAYLIST")
    @Expose
    private List<String> playlists;

    public List<String> getAlbums() {
        return albums;
    }

    public List<String> getArtists() {
        return artists;
    }

    public List<String> getTracks() {
        return tracks;
    }

    public List<String> getPlaylists() {
        return playlists;
    }
}
