package com.player.boom.data.DeviceMediaCollection;

import android.os.Parcel;
import android.os.Parcelable;

import com.player.boom.data.MediaCollection.IMediaItem;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.data.MediaLibrary.MediaType;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public class MediaItem implements IMediaItem, Parcelable {

    private long ItemId;
    private String ItemTitle;
    private String ItemDisplayName;
    private String ItemUrl;
    private long ItemAlbumId;
    private String ItemAlbum;
    private long ItemArtistId;
    private String ItemArtist;
    private long Duration;
    private long DateAdded;
    private String ItemArtUrl;
    private ItemType itemType;
    private MediaType mediaType;


    public MediaItem(long ItemId, String ItemTitle, String ItemDisplayName, String ItemUrl, long ItemAlbumId, String ItemAlbum, long ItemArtistId,
                     String ItemArtist, long Duration, long DateAdded, String ItemArtUrl, ItemType itemType, MediaType mediaType){
        this.ItemId = ItemId;
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
    }

    protected MediaItem(Parcel in) {
        ItemId = in.readLong();
        ItemTitle = in.readString();
        ItemDisplayName = in.readString();
        ItemUrl = in.readString();
        ItemAlbumId = in.readLong();
        ItemAlbum = in.readString();
        ItemArtistId = in.readLong();
        ItemArtist = in.readString();
        Duration = in.readLong();
        DateAdded = in.readLong();
        ItemArtUrl = in.readString();
        itemType = ItemType.valueOf(in.readString());
        mediaType = MediaType.valueOf(in.readString());
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
    public long getItemId() {
        return ItemId;
    }

    @Override
    public String getItemTitle() {
        return ItemTitle;
    }

    @Override
    public String getItemDisplayName() {
        return ItemDisplayName;
    }

    @Override
    public long getItemAlbumId() {
        return ItemAlbumId;
    }

    @Override
    public String getItemAlbum() {
        return ItemAlbum;
    }

    @Override
    public long getItemArtistId() {
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
        return ItemArtUrl;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public long getDurationLong() {
        return Duration;
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
        dest.writeLong(ItemId);
        dest.writeString(ItemTitle);
        dest.writeString(ItemDisplayName);
        dest.writeString(ItemUrl);
        dest.writeLong(ItemAlbumId);
        dest.writeString(ItemAlbum);
        dest.writeLong(ItemArtistId);
        dest.writeString(ItemArtist);
        dest.writeLong(Duration);
        dest.writeLong(DateAdded);
        dest.writeString(ItemArtUrl);
        dest.writeString(itemType.name());
        dest.writeString(mediaType.name());
    }
}
