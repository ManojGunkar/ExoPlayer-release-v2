package com.globaldelight.boom.handler.PlayingQueue;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-11-16.
 */

public class UpNextItem{
    private MediaItem item;
    private QueueType type;

    public UpNextItem(MediaItem item, QueueType type){
        this.item = item;
        this.type = type;
    }

    public MediaItem getUpNextItem(){
        return item;
    }

    public QueueType getUpNextItemType(){
        return type;
    }
}
