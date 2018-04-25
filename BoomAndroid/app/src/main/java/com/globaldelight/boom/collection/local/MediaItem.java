package com.globaldelight.boom.collection.local;

import android.os.Parcel;
import android.os.Parcelable;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.playbackEvent.utils.DeviceMediaLibrary;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public class MediaItem implements IMediaItem, Parcelable {
    private String ItemId;
    private String ItemTitle;
    private String ItemDisplayName;
    private String ItemUrl;
    private String ItemAlbumId;
    private String ItemAlbum;
    private String ItemArtistId;
    private String ItemArtist;
    private long Duration;
    private long DateAdded;
    private String ItemArtUrl;
    private @ItemType int itemType;
    private @MediaType int mediaType;
    private @ItemType int parentType;
    private String parentId;
    private String parentTitle;
    public static String UNKNOWN_ART_URL = "unknown_art_url";

    /*Only for Device Media Item*/
    public MediaItem(String ItemId, String ItemTitle, String ItemDisplayName, String ItemUrl, String ItemAlbumId, String ItemAlbum, String ItemArtistId,
                     String ItemArtist, long Duration, long DateAdded, String ItemArtUrl, @ItemType int itemType, @MediaType int mediaType, @ItemType int parentType, String parentId, String parentTitle){
        this.ItemId =ItemId;
        this.ItemTitle = ItemTitle;
        this.ItemDisplayName = ItemDisplayName;
        this.ItemUrl = ItemUrl;
        this.ItemAlbumId = ItemAlbumId;
        this.ItemAlbum = ItemAlbum;
        this.ItemArtistId = ItemArtistId;
        this.ItemArtist = ItemArtist;
        this.Duration = Duration;
        this.DateAdded = DateAdded;
        this.ItemArtUrl = ItemArtUrl;
        this.itemType = itemType;
        this.mediaType = mediaType;
        this.parentType = parentType;
        this.parentId = parentId;
        this.parentTitle = parentTitle;
    }

    /*Only for Dropbox*/
    public MediaItem(String itemId, String ItemTitle, String  ItemUrl, @ItemType int itemType, @MediaType int mediaType, @ItemType int parentType) {
        this.ItemId = itemId;
        this.ItemUrl = ItemUrl;
        this.ItemTitle = ItemTitle;
        this.itemType = itemType;
        this.mediaType = mediaType;
        this.parentType = parentType;
    }

    protected MediaItem(Parcel in) {
        ItemId = in.readString();
        ItemTitle = in.readString();
        ItemDisplayName = in.readString();
        ItemUrl = in.readString();
        ItemAlbumId = in.readString();
        ItemAlbum = in.readString();
        ItemArtistId = in.readString();
        ItemArtist = in.readString();
        Duration = in.readLong();
        DateAdded = in.readLong();
        ItemArtUrl = in.readString();
        //noinspection ResourceType
        itemType = Integer.parseInt(in.readString());
        //noinspection ResourceType
        mediaType = Integer.parseInt(in.readString());
        //noinspection ResourceType
        parentType = Integer.parseInt(in.readString());
        parentId = in.readString();
        parentTitle = in.readString();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public String getId() {
        return ItemId;
    }

    @Override
    public String getTitle() {
        return ItemTitle;
    }

    @Override
    public String getDescription() {
        return getItemArtist();
    }

    @Override
    public String getItemDisplayName() {
        return ItemDisplayName;
    }

    @Override
    public String getItemAlbumId() {
        return ItemAlbumId;
    }

    @Override
    public String getItemAlbum() {
        return ItemAlbum;
    }

    @Override
    public String getItemArtistId() {
        return ItemArtistId;
    }

    @Override
    public String getItemArtist() {
        return ItemArtist;
    }

    @Override
    public String getItemUrl() {
        return ItemUrl;
    }

    @Override
    public String getItemArtUrl() {
        if ( ItemArtUrl == null ) {
            ItemArtUrl = DeviceMediaLibrary.getInstance(App.getApplication()).getAlbumArt(getItemAlbum());
        }

        if ( ItemArtUrl == null ) {
            ItemArtUrl = UNKNOWN_ART_URL;
        }

        return ItemArtUrl;
    }

    @Override
    public void setItemArtUrl(String url) {
        this.ItemArtUrl = url;
    }

    @Override
    public @ItemType int getItemType() {
        return itemType;
    }

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public @ItemType int getParentType(){
        return parentType;
    }

    public @MediaType int getMediaType() {
        return mediaType;
    }

    @Override
    public long getDurationLong() {
        return Duration;
    }

    @Override
    public String getParentTitle(){
        return parentTitle;
    }

    @Override
    public String getDuration() {
        try {
            Long time = Duration;
            long seconds = time / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            if (seconds < 10) {
                return String.valueOf(minutes) + ":0" + String.valueOf(seconds);
            } else {
                return String.valueOf(minutes) + ":" + String.valueOf(seconds);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return String.valueOf(0);
        }
    }

    @Override
    public long getDateAdded() {
        return DateAdded;
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
        dest.writeString(ItemId);
        dest.writeString(ItemTitle);
        dest.writeString(ItemDisplayName);
        dest.writeString(ItemUrl);
        dest.writeString(ItemAlbumId);
        dest.writeString(ItemAlbum);
        dest.writeString(ItemArtistId);
        dest.writeString(ItemArtist);
        dest.writeLong(Duration);
        dest.writeLong(DateAdded);
        dest.writeString(ItemArtUrl);
        dest.writeString(Integer.toString(itemType));
        dest.writeString(Integer.toString(mediaType));
        dest.writeString(Integer.toString(parentType));
        dest.writeString(parentId);
        dest.writeString(parentTitle);
    }
}
