package com.player.data.MediaCollection;

import com.player.data.MediaLibrary.MediaType;
import com.player.data.MediaLibrary.ItemType;

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
