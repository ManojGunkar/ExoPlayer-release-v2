package com.globaldelight.boom.collection.base;

import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItemCollection extends IMediaElement {

    String getItemSubTitle();

    int getItemCount();

    int getItemListCount();

    ArrayList<String> getArtUrlList();

    void setArtUrlList(ArrayList<String> artUrlList);

    void setMediaElement(ArrayList<? extends IMediaElement> iMediaItemList);

    ArrayList<? extends IMediaElement> getMediaElement();

    @ItemType int getItemType();

    @MediaType int getMediaType();

    @ItemType int getParentType();

    IMediaElement getItemAt(int index);

    int count();
}
