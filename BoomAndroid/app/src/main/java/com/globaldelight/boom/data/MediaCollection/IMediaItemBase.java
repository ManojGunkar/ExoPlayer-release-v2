package com.globaldelight.boom.data.MediaCollection;

import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItemBase {

    long getItemId();

    String getItemTitle();

    String getItemArtUrl();

    ItemType getItemType();

    MediaType getMediaType();
}
