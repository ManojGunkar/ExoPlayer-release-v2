package com.globaldelight.boom.data.DeviceMediaCollection;

import android.os.Parcel;
import android.os.Parcelable;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaType;
import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 8/4/2016.
 */
public class MediaItemCollection implements IMediaItemCollection, Parcelable {

    private long mItemId;
    private String mItemTitle;
    private String mItemSubTitle;
    private String mItemArtUrl;
    private ItemType mItemType;
    private MediaType mMediaType;
    private ItemType mParentType;
    private int mItemCount;
    private int mItemListCount;
    protected ArrayList<? extends IMediaItemBase> mMediaElement = new ArrayList<>();
    protected ArrayList<String> mArtUrlList = new ArrayList<>();
    private int mCurrentIndex;

    public MediaItemCollection(long mItemId, String mItemTitle, String mItemSubTitle, String mItemArtUrl,
                               int mItemCount, int mItemListCount, ItemType mItemType, MediaType mMediaType, ItemType mParentType){
        this.mItemId = mItemId;
        this.mItemTitle = mItemTitle;
        this.mItemSubTitle = mItemSubTitle;
        this.mItemArtUrl = mItemArtUrl;
        this.mItemType = mItemType;
        this.mMediaType = mMediaType;
        this.mParentType = mParentType;
        this.mItemCount = mItemCount;
        this.mItemListCount = mItemListCount;
    }

    protected MediaItemCollection(Parcel in) {
        mItemId = in.readLong();
        mItemTitle = in.readString();
        mItemSubTitle = in.readString();
        mItemArtUrl = in.readString();
        mItemCount = in.readInt();
        mItemListCount = in.readInt();
        mArtUrlList = in.createStringArrayList();
        mItemType = mItemType.valueOf(in.readString());
        mMediaType = mMediaType.valueOf(in.readString());
        this.mParentType = mItemType.valueOf(in.readString());
        mCurrentIndex = in.readInt();
        mMediaElement = in.readArrayList(IMediaItemBase.class.getClassLoader());

    }

    public static final Creator<MediaItemCollection> CREATOR = new Creator<MediaItemCollection>() {
        @Override
        public MediaItemCollection createFromParcel(Parcel in) {
            return new MediaItemCollection(in);
        }

        @Override
        public MediaItemCollection[] newArray(int size) {
            return new MediaItemCollection[size];
        }
    };

    @Override
    public long getItemId() {
        return mItemId;
    }

    @Override
    public String getItemTitle() {
        return mItemTitle;
    }

    @Override
    public String getItemArtUrl() {
        return mItemArtUrl;
    }

    @Override
    public void setItemArtUrl(String url) {
        this.mItemArtUrl = url;
    }

    @Override
    public ItemType getItemType() {
        return mItemType;
    }

    @Override
    public MediaType getMediaType(){
        return mMediaType;
    }

    @Override
    public ItemType getParentType(){
        return mParentType;
    }

    @Override
    public void setItemCount(int itemCount) {
        this.mItemCount = itemCount;
    }

    @Override
    public int getItemCount() {
        return mItemCount;
    }

    @Override
    public int getItemListCount() {
        return mItemListCount;
    }

    @Override
    public String getItemSubTitle() {
        return mItemSubTitle;
    }

    @Override
    public ArrayList<? extends IMediaItemBase> getMediaElement(){
        return this.mMediaElement;
    }

    @Override
    public ArrayList<String> getArtUrlList(){
        return this.mArtUrlList;
    }

    @Override
    public int getCurrentIndex() {
        return this.mCurrentIndex;
    }

    
    /*setter*/
    
    @Override
    public void setCurrentIndex(int mCurrentIndex) {
        this.mCurrentIndex = mCurrentIndex;
    }

    @Override
    public void setArtUrlList(ArrayList<String> mArtUrlList) {
        this.mArtUrlList = mArtUrlList;
    }

    @Override
    public void setMediaElement(ArrayList<? extends IMediaItemBase> iMediaItemBases) {
        this.mMediaElement = iMediaItemBases;
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
        dest.writeLong(mItemId);
        dest.writeString(mItemTitle);
        dest.writeString(mItemSubTitle);
        dest.writeString(mItemArtUrl);
        dest.writeInt(mItemCount);
        dest.writeInt(mItemListCount);
        dest.writeStringList(mArtUrlList);
        dest.writeString(mItemType.name());
        dest.writeString(mMediaType.name());
        dest.writeString(mParentType.name());
        dest.writeInt(mCurrentIndex);
        dest.writeList(mMediaElement);
    }
}
