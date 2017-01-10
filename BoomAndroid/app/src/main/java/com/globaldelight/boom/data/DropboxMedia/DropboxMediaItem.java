package com.globaldelight.boom.data.DropboxMedia;

import android.os.Parcel;
import android.os.Parcelable;

import com.dropbox.client2.DropboxAPI;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

/**
 * Created by Rahul Agarwal on 06-01-17.
 */

public class DropboxMediaItem implements IDropboxMediaItem, Parcelable {
    public String icon;
    public String modified;
    public String size;
    public String mimeType;
    public boolean thumbExists;
    public boolean isDeleted;
    private String ItemUrl;
    private String ItemTitle;
    private long Duration;
    private ItemType itemType;
    private MediaType mediaType;
    private ItemType parentType;

    public DropboxMediaItem(String icon, String modified, String size, String mimeType, boolean thumbExists, boolean isDeleted, String  ItemUrl, String ItemTitle, ItemType itemType, MediaType mediaType, ItemType parentType) {
        this.icon = icon;
        this.modified = modified;
        this.size = size;
        this.mimeType = mimeType;
        this.thumbExists = thumbExists;
        this.isDeleted = isDeleted;
        this.ItemUrl = ItemUrl;
        this.ItemTitle = ItemTitle;
        this.itemType = itemType;
        this.mediaType = mediaType;
        this.parentType = parentType;
    }

    protected DropboxMediaItem(Parcel in) {
        icon = in.readString();
        modified = in.readString();
        size = in.readString();
        mimeType = in.readString();
        thumbExists = in.readInt() == 1;
        isDeleted = in.readInt() == 1;
        ItemUrl = in.readString();
        ItemTitle = in.readString();
        Duration = in.readLong();
        itemType = ItemType.valueOf(in.readString());
        mediaType = MediaType.valueOf(in.readString());
        parentType = ItemType.valueOf(in.readString());
    }

    public static final Creator<DropboxMediaItem> CREATOR = new Creator<DropboxMediaItem>() {
        @Override
        public DropboxMediaItem createFromParcel(Parcel in) {
            return new DropboxMediaItem(in);
        }

        @Override
        public DropboxMediaItem[] newArray(int size) {
            return new DropboxMediaItem[size];
        }
    };

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public String getModified() {
        return modified;
    }

    @Override
    public String getSize() {
        return size;
    }

    @Override
    public String getMimeType() {
        return mimeType;
    }

    @Override
    public boolean isThumbExists() {
        return thumbExists;
    }

    @Override
    public boolean isDeleted() {
        return isDeleted;
    }

    @Override
    public long getDurationLong() {
        return Duration;
    }

    @Override
    public void setDurationLong(long Duration) {
        this.Duration = Duration;
    }

    @Override
    public String getItemTitle() {
        return ItemTitle;
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public ItemType getParentType() {
        return parentType;
    }

    @Override
    public void setParentItemType(ItemType parentType) {
        this.parentType = parentType;
    }

    @Override
    public String getItemUrl() {
        return ItemUrl;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(icon);
        dest.writeString(modified);
        dest.writeString(size);
        dest.writeString(mimeType);
        dest.writeInt(thumbExists ? 1 : 0);
        dest.writeInt(isDeleted ? 1 : 0);
        dest.writeString(ItemUrl);
        dest.writeString(ItemTitle);
        dest.writeLong(Duration);
        dest.writeString(itemType.name());
        dest.writeString(mediaType.name());
        dest.writeString(parentType.name());
    }

    @Override
    public String getItemDisplayName() {
        return null;
    }

    @Override
    public long getItemAlbumId() {
        return 0;
    }

    @Override
    public String getItemAlbum() {
        return null;
    }

    @Override
    public long getItemArtistId() {
        return 0;
    }

    @Override
    public String getItemArtist() {
        return null;
    }

    @Override
    public String getDuration() {
        return null;
    }

    @Override
    public long getDateAdded() {
        return 0;
    }

    @Override
    public void setItemUrl(String ItemUrl) {
    }

    @Override
    public long getItemId() {
        return 0;
    }


    @Override
    public String getItemArtUrl() {
        return null;
    }

    @Override
    public void setItemArtUrl(String url) {

    }

    @Override
    public long getParentId() {
        return 0;
    }

    @Override
    public void setParentId(long parentId) {

    }
}
