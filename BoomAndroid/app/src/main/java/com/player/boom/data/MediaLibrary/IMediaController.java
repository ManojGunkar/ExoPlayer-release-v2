package com.player.boom.data.MediaLibrary;

import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/12/2016.
 */
public interface IMediaController {

    ArrayList<String> getArtUrlList(MediaItemCollection Collection);

    ArrayList<? extends IMediaItemBase> getMediaCollectionItemList(ItemType itemType, MediaType mediaType);

    ArrayList<? extends IMediaItemBase> getMediaCollectionItemDetails(IMediaItemCollection collection);

    void doSearch(MediaType mediaType, String query);
}