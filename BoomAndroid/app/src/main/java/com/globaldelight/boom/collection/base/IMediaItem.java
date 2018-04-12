package com.globaldelight.boom.collection.base;

import com.globaldelight.boom.playbackEvent.utils.ItemType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItem extends IMediaElement {

    String getItemDisplayName();

    String getItemAlbumId();

    String getItemAlbum();

    String getItemArtistId();

    String getItemArtist();

    long getDurationLong();

    String getDuration();

    long getDateAdded();

    String getItemUrl();

    @ItemType int getItemType();

    String getParentId();

    String getParentTitle();

    @ItemType int getParentType();

}
