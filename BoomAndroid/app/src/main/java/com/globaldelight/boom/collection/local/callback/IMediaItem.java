package com.globaldelight.boom.collection.local.callback;

import com.globaldelight.boom.playbackEvent.utils.ItemType;

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

    ItemType getItemType();

    long getParentId();

    String getParentTitle();

    void setParentId(long parentId);

    ItemType getParentType();

    void setParentItemType(ItemType parentType);
}
