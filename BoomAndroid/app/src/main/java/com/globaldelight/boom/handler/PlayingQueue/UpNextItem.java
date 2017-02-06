package com.globaldelight.boom.handler.PlayingQueue;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 28-11-16.
 */

public class UpNextItem {
    private IMediaItem item;
    private QueueType type;

    public UpNextItem(UpNextItem item){
        this.item = item.getUpNextItem();
        this.type = item.getUpNextItemType();
    }

    public UpNextItem(IMediaItem item, QueueType type) {
        this.item = item;
        this.type = type;
    }

    public IMediaItem getUpNextItem() {
        return item;
    }

    public QueueType getUpNextItemType() {
        return type;
    }
}