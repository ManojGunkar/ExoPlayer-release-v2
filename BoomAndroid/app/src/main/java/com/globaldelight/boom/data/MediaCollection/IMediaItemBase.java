package com.globaldelight.boom.data.MediaCollection;

import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItemBase {

    long getItemId();

    String getItemTitle();

    String getItemArtUrl();

    void setItemArtUrl(String url);

    ItemType getItemType();

    MediaType getMediaType();
}
