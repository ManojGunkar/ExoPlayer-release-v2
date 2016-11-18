package com.globaldelight.boom.data.MediaLibrary;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;

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
