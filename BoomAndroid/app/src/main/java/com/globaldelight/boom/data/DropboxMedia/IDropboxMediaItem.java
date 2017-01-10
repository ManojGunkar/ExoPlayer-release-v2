package com.globaldelight.boom.data.DropboxMedia;

import com.dropbox.client2.DropboxAPI;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;

/**
 * Created by Rahul Agarwal on 06-01-17.
 */

public interface IDropboxMediaItem extends IMediaItem {

    String getIcon();

    String getModified();

    String getSize();

    String getMimeType();

    boolean isThumbExists();

    boolean isDeleted();

    void setItemUrl(String ItemUrl);

    void setDurationLong(long Duration);
}
