package com.globaldelight.boom.data.MediaCollection;

import com.globaldelight.boom.data.MediaLibrary.ItemType;

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

    void setParentId(long parentId);

    ItemType getParentType();

    void setParentItemType(ItemType parentType);
}
