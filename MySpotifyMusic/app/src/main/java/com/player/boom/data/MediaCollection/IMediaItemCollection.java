package com.player.boom.data.MediaCollection;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public interface IMediaItemCollection extends IMediaItemBase {

    String getItemSubTitle();

    int getItemCount();

    int getItemListCount();

    ArrayList<String> getArtUrlList();

    int getCurrentIndex();

    void setCurrentIndex(int currentIndex);

    void setArtUrlList(ArrayList<String> artUrlList);

    void setMediaElement(ArrayList<? extends IMediaItemBase> iMediaItemList);

    ArrayList<? extends IMediaItemBase> getMediaElement();
}
