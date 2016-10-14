package com.player.boom.data.MediaLibrary;

import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/12/2016.
 */
public interface IMediaLibrary {

    ArrayList<String> requestArtUrlList(MediaItemCollection collection);

    ArrayList<? extends IMediaItemBase> requestMediaCollectionList(ItemType itemType, MediaType mediaType);

    ArrayList<? extends IMediaItemBase> requestMediaCollectionItemDetails(IMediaItemBase collection);

    void requestMediaSearch(MediaType mediaType, String query);
}
