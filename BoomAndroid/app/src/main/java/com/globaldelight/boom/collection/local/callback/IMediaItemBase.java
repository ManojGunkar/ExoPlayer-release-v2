package com.globaldelight.boom.collection.local.callback;

import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItemBase {

    long getItemId();

    String getItemTitle();

    String getItemArtUrl();

    void setItemArtUrl(String url);

    @ItemType int getItemType();

    @MediaType int getMediaType();
}
