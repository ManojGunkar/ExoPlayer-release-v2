package com.globaldelight.boom.collection.base;

import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaElement {

    String getId();

    String getTitle();

    String getDescription();

    String getItemArtUrl();

    void setItemArtUrl(String url);

    @ItemType int getItemType();

    @MediaType int getMediaType();

    default boolean equalTo(IMediaElement another) {
        return another == this
                || (another != null
                        && (this.getId() != null && another.getId() != null && this.getId().equals(another.getId()))
                        && this.getMediaType() == another.getMediaType());
    }
}
