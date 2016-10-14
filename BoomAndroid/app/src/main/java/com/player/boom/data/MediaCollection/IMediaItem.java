package com.player.boom.data.MediaCollection;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItem extends IMediaItemBase {

    String getItemDisplayName();

    long getItemAlbumId();

    String getItemAlbum();

    long getItemArtistId();

    String getItemArtist();

    long getDurationLong();

    String getDuration();

    long getDateAdded();

    String getItemUrl();
}
