package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Rahul Agarwal on 15-11-16.
 */

public class UpNextList {

    private static Context context;
    public static int PlayItemIndex = 0;
    private QueueEvent queueEvent = null;
    private long mShiftingTime = 0;
    Handler eventHandler = new Handler();
    private static UpNextList.SHUFFLE mShuffle = UpNextList.SHUFFLE.none;
    private static UpNextList.REPEAT mRepeat = UpNextList.REPEAT.none;

    private static ArrayList<IMediaItemBase> mHistoryList = new ArrayList<>();
    private static ArrayList<IMediaItemBase> mUpNextList = new ArrayList<>();
    private static ArrayList<UpNextItem> mCurrentList = new ArrayList<>();
    private static ArrayList<IMediaItemBase> mAutoNextList = new ArrayList<>();
    private static ArrayList<IMediaItemBase> mGhostList = new ArrayList<>();

    private HashMap<String, String> mAlbumArtList = new HashMap<>();
    private HashMap<Long, String> mArtistArtList = new HashMap<>();

    private static UpNextList handler;

    private UpNextList(Context application) {
        context = application;
    }

    public static UpNextList getUpNextInstance(Context application) {
        if (handler == null) {
            handler = new UpNextList(application);
        }
        return handler;
    }

    public void setAlbumArtList(HashMap<String, String> artList){
        this.mAlbumArtList = artList;
    }
    public HashMap<String, String> getAlbumArtList(){
        return mAlbumArtList;
    }

    public void setArtistArtList(HashMap<Long, String> artList){
        this.mArtistArtList = artList;
    }
    public HashMap<Long, String> getArtistArtList(){
        return mArtistArtList;
    }

    public void setQueueEvent(QueueEvent event) {
        this.queueEvent = event;
    }

    private ArrayList<IMediaItemBase> getItemList(QueueType type) {
        switch (type) {
            case History:
                if (mHistoryList != null) {
                    mHistoryList.clear();
                }
                mHistoryList.addAll(getUpNextItemList(QueueType.History));
                return mHistoryList;
            case Manual_UpNext:
                return mUpNextList;
            case Auto_UpNext:
                return mAutoNextList;
            default:
                return null;
        }
    }

    /**************************************************************************************************************************/
//Fetch Queue Items
    public ArrayList<IMediaItemBase> getHistoryList() {
        return getItemList(QueueType.History);
    }

    public ArrayList<UpNextItem> getPlayingList() {
        return mCurrentList;
    }

    public IMediaItemBase getPlayingItem() {
        return mCurrentList.size() > 0 ? mCurrentList.get(0).getUpNextItem() : null;
    }

    public ArrayList<IMediaItemBase> getManualUpNextList() {
        return getItemList(QueueType.Manual_UpNext);
    }

    public ArrayList<IMediaItemBase> getAutoUpNextList() {
        return getItemList(QueueType.Auto_UpNext);
    }

    public void setUpdatedListItem(QueueType queueType, ArrayList<IMediaItemBase> list){
        if(queueType == QueueType.Manual_UpNext){
            mUpNextList.clear();
            mUpNextList.addAll(list);
        }else if(queueType == QueueType.Auto_UpNext){
            mAutoNextList.clear();
            mAutoNextList.addAll(list);
        }
    }

    /******************************************************************************************************************/

    public boolean resetShuffle() {
        mShuffle = App.getUserPreferenceHandler().resetShuffle();
        updateShuffleList();
        try {
            if (mShuffle == SHUFFLE.all) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SHUFFLE_ON_PLAYING);
            } else {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SHUFFLE_OFF_PLAYING);
            }
        }catch (Exception e){}
        return true;
    }

    public boolean resetRepeat() {
        mRepeat = App.getUserPreferenceHandler().resetRepeat();
        if(mRepeat == REPEAT.none && mUpNextList.size() == 0){
            for(int i=0;i<mAutoNextList.size();i++) {
                if (getPlayingItem().getItemId() == mAutoNextList.get(i).getItemId()){
                    PlayItemIndex = i+1;
                    break;
                }
            }
        }
        updateRepeatList();
        try {
            if (mRepeat == REPEAT.one) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_ONE_PLAYING);
            } else if (mRepeat == REPEAT.all) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_ALL_PLAYING);
            } else {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_NONE_PLAYING);
            }
        }catch (Exception e){}
        return true;
    }

    private void updateShuffleList(){
        if(mShuffle == SHUFFLE.all && mAutoNextList.size() > 0){
            Collections.shuffle(mAutoNextList, new Random(mAutoNextList.size()));
        }else if(mAutoNextList.size() > 0){
            ArrayList<IMediaItemBase> tempList1 = (ArrayList<IMediaItemBase>) getUnShuffledList();
            ArrayList<IMediaItemBase> tempList2 = new ArrayList<>();
            for(int i = 0; i< tempList1.size() ; i++){
                for(int j = 0; j < mAutoNextList.size() ; j++){
                    if(mAutoNextList.get(j).getItemId() == tempList1.get(i).getItemId()){
                        tempList2.add(tempList1.get(i));
                    }
                }
            }
            mAutoNextList.clear();
            mAutoNextList.addAll(tempList2);
            tempList1.clear();
            tempList2.clear();
        }
        QueueUpdated();
    }

    private void updateRepeatList(){
        if(mRepeat == REPEAT.all){
            mAutoNextList.clear();
            mAutoNextList.addAll(getUnShuffledList());
            if(mShuffle == SHUFFLE.all && mAutoNextList.size() > 0){
                Collections.shuffle(mAutoNextList, new Random(mAutoNextList.size()));
            }
        }else{
            if(mAutoNextList.size() > 0)
            for (int i = 0; i < PlayItemIndex; PlayItemIndex--) {
                addItemAsPrevious(new UpNextItem(mAutoNextList.remove(i), QueueType.Auto_UpNext));
            }
            if(PlayItemIndex == -1){
                PlayItemIndex = 0;
            }
        }
        QueueUpdated();
    }

    public void updateRepeatShuffleOnAppStart(){
        mShuffle = App.getUserPreferenceHandler().getShuffle();
        mRepeat = App.getUserPreferenceHandler().getRepeat();
        updateShuffleList();
        updateRepeatList();
    }


    public static void Terminate() {

    }

    public int size() {
        return mHistoryList.size() + mUpNextList.size() + mAutoNextList.size();
    }


    public void clearUpNext(QueueType queueType) {
        clearUpNextList(queueType);
        switch (queueType){
            case Manual_UpNext:
                mUpNextList.clear();
                break;
            case Auto_UpNext:
                mAutoNextList.clear();
                break;
            case History:
                mHistoryList.clear();
                break;
            case Playing:
                mCurrentList.clear();
                break;
            case Previous:
                mGhostList.clear();
                break;
        }
    }

    public void removeItem(QueueType listType, int itemPosition) {
        if(getItemList(listType).size() > 0)
            getItemList(listType).remove(itemPosition);
    }
    public enum REPEAT {
        one,
        all,
        none,
    }

    public enum SHUFFLE {
        all,
        none,
    }

    public void addUpNextItem(IMediaItemBase song, QueueType queueType) {
        MediaController.getInstance(context).addUpNextItem(song, queueType);
    }

    public void addUpNextItem(ArrayList<? extends IMediaItemBase> songs, QueueType queueType) {
        MediaController.getInstance(context).addUpNextItem(songs, queueType);
    }

    public void insertUnShuffledList(final List<? extends IMediaItemBase> songs, final boolean isAppend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaController.getInstance(context).insertUnShuffledList(songs, QueueType.unshuffled, isAppend);
            }
        }).start();
    }

    public ArrayList<? extends IMediaItemBase> getUpNextItemList(QueueType queueType) {
        return MediaController.getInstance(context).getUpNextItemList(queueType);
    }

    public ArrayList<? extends IMediaItemBase> getUnShuffledList() {
        return MediaController.getInstance(context).getUnShuffledList(QueueType.unshuffled);
    }

    public void clearUpNextList(QueueType queueType) {
        MediaController.getInstance(context).clearUpNextList(queueType);
        if(queueType == QueueType.Auto_UpNext)
            MediaController.getInstance(context).clearUpNextList(QueueType.unshuffled);
    }

    public void addUpNextItemsToDB(){
        if(mCurrentList.size() > 0){
            /*Add Now Playing Item to relevant up-next list*/
            if(mCurrentList.get(0).getUpNextItemType() == QueueType.Manual_UpNext){
                mUpNextList.add(0 ,mCurrentList.get(0).getUpNextItem());
                Log.d("Add_to_Manual", "in UP_NEXT");
            }else if(mCurrentList.get(0).getUpNextItemType() == QueueType.Auto_UpNext){
                mAutoNextList.add(0 ,mCurrentList.get(0).getUpNextItem());
                Log.d("Add_to_Auto", "in UP_NEXT");
            }
        }
        if(mUpNextList.size() > 0)
            addUpNextItem(mUpNextList, QueueType.Manual_UpNext);
        if(mAutoNextList.size() > 0)
            addUpNextItem(mAutoNextList, QueueType.Auto_UpNext);
        if(mGhostList.size() > 0)
            addUpNextItem(mGhostList, QueueType.Previous);

        /*If service is not destroyed remove current item from upnext*/
        if (mUpNextList.size() > 0) {
            mUpNextList.remove(0);
        } else if (mAutoNextList.size() > 0) {
            mAutoNextList.remove(0);
        }
    }

    public void fetchUpNextItemsToDB(){
        mUpNextList.clear();
        mUpNextList = (ArrayList<IMediaItemBase>) getUpNextItemList(QueueType.Manual_UpNext);

        mAutoNextList.clear();
        mAutoNextList = (ArrayList<IMediaItemBase>) getUpNextItemList(QueueType.Auto_UpNext);

        mGhostList.clear();
        mGhostList = (ArrayList<IMediaItemBase>) getUpNextItemList(QueueType.Previous);

        /*Fetch Now Playing Item from relevant up-next list*/
        mCurrentList.clear();
        if (mUpNextList.size() > 0) {
            mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            Log.d("Add_to_Manual", "out UP_NEXT");
        } else if (mAutoNextList.size() > 0) {
            mCurrentList.add(new UpNextItem(mAutoNextList.remove(0), QueueType.Auto_UpNext));
            Log.d("Add_to_Auto", "out UP_NEXT");
        }
//                overFillingPlayingItem();
    }

    private void overFillingPlayingItem(){
        if(mCurrentList.size() > 1){
            for(int i =0; i< mCurrentList.size()-1; i++)
                mCurrentList.remove(i);
        }
    }

    public void clearAllUpNext(){
        clearUpNextList(QueueType.Manual_UpNext);
        clearUpNextList(QueueType.Auto_UpNext);
        clearUpNextList(QueueType.Previous);
    }

    /**************************************************************************************************************************/
//    Callbacks to update UI
    public void PlayPause() {
        if (queueEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onPlayingItemClicked();
                }
            });
        }
    }

    //  Queue and Playing Item Update
    public void PlayingItemChanged() {
        if (queueEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onPlayingItemChanged();
                }
            });
        }
    }

    //  Only Queue update
    public void QueueUpdated() {
        if (queueEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onQueueUpdated();
                }
            });
        }
    }


    /******************************************************************************************************************/
    public void insertUnShuffledListWithUpdateUpNext(final List<? extends IMediaItemBase> songs, final boolean isAppend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaController.getInstance(context).insertUnShuffledList(songs, QueueType.unshuffled, isAppend);
                if(SHUFFLE.all == mShuffle){
                    updateShuffleList();
                }
                if(REPEAT.all == mRepeat){
                    updateRepeatList();
                }
            }
        }).start();
    }

    public void insertUnShuffledListWithUpdateUpNext(final IMediaItemBase item, final boolean isAppend) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MediaController.getInstance(context).insertUnShuffledList(item, QueueType.unshuffled, isAppend);
                if(SHUFFLE.all == mShuffle){
                    updateShuffleList();
                }
                if(REPEAT.all == mRepeat){
                    updateRepeatList();
                }
            }
        }).start();
    }

    public void addToPlay(final ArrayList<MediaItem> itemList, final int position, boolean isPlayAll) {
        long mTime = System.currentTimeMillis();
        boolean isNowPlaying = App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused();
        if(null != getPlayingItem() && !isPlayAll && itemList.get(position).getItemId() == getPlayingItem().getItemId() && isNowPlaying){
            PlayPause();
        }else  if(mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(null != itemList && itemList.size() > 0) {
                        if (position > 0) {
                            setItemListAsPrevious(itemList.subList(0, position));
                        }
                        setItemAsPlayingItem(itemList.get(position), QueueType.Auto_UpNext);
                        mAutoNextList.clear();
                        if (itemList.size() > position + 1) {
                            setItemListAsUpNextFrom(itemList.subList(position + 1, itemList.size()));
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                insertUnShuffledListWithUpdateUpNext(itemList.subList(position, itemList.size()), false);
                            }
                        }).start();

                        PlayingItemChanged();
                    }
                }
            }, 100);
        }
    }

    //    itemList -> list of collection
//    position -> Now Playing Item position in item list.
    public void addToPlay(final ArrayList<MediaItem> itemList, final int position, final boolean fromSongList, boolean isPlayAll) {
        long mTime = System.currentTimeMillis();
        boolean isNowPlaying = App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused();
        if(null != getPlayingItem() && !isPlayAll && itemList.get(position).getItemId() == getPlayingItem().getItemId() && isNowPlaying){
            PlayPause();
        }else if(mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(null != itemList && itemList.size() > 0) {
                        if (position > 0) {
                            setItemListAsPrevious(itemList.subList(0, position));
                        }
                        setItemAsPlayingItem(itemList.get(position), QueueType.Auto_UpNext);
                        mAutoNextList.clear();
                        if (itemList.size() > position + 1) {
                            setItemListAsUpNextFrom(itemList.subList(position + 1, itemList.size()));
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                if(fromSongList) {
                                    IMediaItem item = new MediaItem(0, context.getResources().getString(R.string.all_songs), null, null, 0, null, 0, null, 0, 0, null, ItemType.SONGS, MediaType.DEVICE_MEDIA_LIB, ItemType.SONGS, 0);
                                    insertUnShuffledListWithUpdateUpNext(item, false);
                                }else {
                                    insertUnShuffledListWithUpdateUpNext(itemList.subList(position, itemList.size()), false);
                                }
                            }
                        }).start();

                        PlayingItemChanged();
                    }
                }
            }, 100);
        }
    }

    //    selected Collection, Like Album, ic_artist.
//    index of now Playing item of the collection
    public void addToPlay(final IMediaItemCollection collection, final int position, boolean isPlayAll) {
        boolean isNowPlaying = App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused();
        long mTime = System.currentTimeMillis();
        if(null != getPlayingItem() && collection.getMediaElement().size() > 0 && !isPlayAll &&
                collection.getMediaElement().get(position).getItemId() == getPlayingItem().getItemId() && isNowPlaying){
            PlayPause();
        }else if(mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(null != collection && collection.getMediaElement().size() > 0) {
                        if (position > 0) {
                            setItemListAsPrevious(collection.getMediaElement().subList(0, position));
                        }
                        setItemAsPlayingItem(collection.getMediaElement().get(position), QueueType.Auto_UpNext);
                        mAutoNextList.clear();
                        if (collection.getMediaElement().size() > position + 1) {
                            setItemListAsUpNextFrom(collection.getMediaElement().subList(position + 1, collection.getMediaElement().size()));
                        }
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                insertUnShuffledListWithUpdateUpNext(collection.getMediaElement().subList(position , collection.getMediaElement().size()), false);
                            }
                        }).start();

                        PlayingItemChanged();
                    }
                }
            }, 100);
        }
    }

    public void addToPlay(final QueueType queueType, final int position) {
        long mTime = System.currentTimeMillis();
        if(mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    switch (queueType) {
                        case History:
                            UpNextItem item = new UpNextItem(mHistoryList.remove(position), queueType);
                            managePlayedItem(true);
                            mCurrentList.add(item);
                            overFillingPlayingItem();
                            PlayingItemChanged();
                            break;
                        case Playing:
                            if(App.getPlayerEventHandler().isPlaying() || App.getPlayerEventHandler().isPaused()) {
                                PlayPause();
                            }else{
                                PlayingItemChanged();
                            }
                            break;
                        case Manual_UpNext:
                            managePlayedItem(true);
                            mCurrentList.add(new UpNextItem(mUpNextList.remove(position), queueType));
                            overFillingPlayingItem();
                            PlayingItemChanged();
                            break;
                        case Auto_UpNext:
                            managePlayedItem(true);
                            PlayItemIndex = position;
                /* Shuffle will not effect on random selection*/
                            if (mRepeat != REPEAT.all/* && mShuffle == SHUFFLE.none*/) {
                                for (int i = 0; i < PlayItemIndex; PlayItemIndex--) {
                                    mGhostList.add(mAutoNextList.remove(i));
                                }
                                PlayItemIndex = 0;
//                selected item comes on top, so remove only top (0) item
                                mCurrentList.add(new UpNextItem(mAutoNextList.remove(0), queueType));
                            } else if (mRepeat == REPEAT.all/* && mShuffle == SHUFFLE.none*/) {
                                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), queueType));
                            }
                            overFillingPlayingItem();
                            PlayingItemChanged();
                            break;
                    }
                }
            }, 100);
        }
    }

    public void addItemListToUpNext(IMediaItemBase itemList) {
        if(null != itemList && ((MediaItemCollection) itemList).getMediaElement().size() > 0) {
            mUpNextList.addAll(((MediaItemCollection) itemList).getMediaElement());
            QueueUpdated();
        }
    }

    public void addItemListToUpNext(MediaItem item) {
        if(null != item) {
            mUpNextList.add(item);
            QueueUpdated();
        }
    }

    public void setNextPlayingItem(boolean isUser) {
        /* no repeat and no shuffle or Only Repeat one and user interaction is true*/
        if (mRepeat == REPEAT.none || (mRepeat == REPEAT.one && isUser)) {
            if (mUpNextList.size() > 0) {
                managePlayedItem(true);
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            } else if (mAutoNextList.size() > 0) {
                for (int i = 0; i < PlayItemIndex; PlayItemIndex--) {
                    addItemAsPrevious(new UpNextItem(mAutoNextList.remove(i), QueueType.Auto_UpNext));
                }
                managePlayedItem(true);
                if (PlayItemIndex < 0) {
                    PlayItemIndex = 0;
                }
                mCurrentList.add(new UpNextItem(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext));
            } else {
                managePlayedItem(true);
            }
        }
        /* Repeat is One and user interaction is false*/
        else if (mRepeat == REPEAT.one && !isUser/* && mShuffle == SHUFFLE.none*/) {
//            same item will play again, so just call playing item change listener

        }
        /* Only Repeat is All and Shuffle if Off*/
        else if (mRepeat == REPEAT.all) {
            managePlayedItem(false);

            if (mUpNextList.size() > 0) {
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            } else if (mAutoNextList.size() > 0 && (mAutoNextList.size() - 1) > PlayItemIndex) {
                boolean isContain = false;
                for(int i = 0; i< mAutoNextList.size() ; i++){
                    if(mCurrentList.size() > 0 && mCurrentList.get(0).getUpNextItem().getItemId() == mAutoNextList.get(i).getItemId()){
                        PlayItemIndex = i+1;
                        isContain = true;
                        break;
                    }
                }
                if(!isContain){
                    PlayItemIndex++;
                }
                mCurrentList.clear();
                if((mAutoNextList.size() - 1) >= PlayItemIndex) {
                    mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
                }else {
                    PlayItemIndex = 0;
                    mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
                }
            } else if (mAutoNextList.size() > 0){
                PlayItemIndex = 0;
                mCurrentList.clear();
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
            }

        }
        overFillingPlayingItem();
        PlayingItemChanged();
    }

    public void setPreviousPlayingItem() {
        if(isPrevious()) {
            if (mRepeat == REPEAT.all && mAutoNextList.size() > 0) {
                if (mCurrentList.size() > 0)
                    addItemToHistory(mCurrentList.remove(0).getUpNextItem());

                if (PlayItemIndex == 0) {
                    PlayItemIndex = mAutoNextList.size() - 1;
                } else {
                    PlayItemIndex--;
                }
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
            } else /*if (((mRepeat == REPEAT.all && mShuffle == SHUFFLE.all) || mRepeat != REPEAT.all) && mAutoNextList.size() != -1)*/ {
                int prevSize = mGhostList.size();
                MediaItem playingItem = null;
                if (prevSize > 0) {
                    playingItem = (MediaItem) mGhostList.remove(prevSize - 1);
                }
                managePreviousItem(true);
                if (null != playingItem) {
                    mCurrentList.add(new UpNextItem(playingItem, QueueType.Auto_UpNext));
                }
            }
            overFillingPlayingItem();
            PlayingItemChanged();
        }
    }

    private void managePreviousItem(boolean isRemove) {
        if (null != mCurrentList && mCurrentList.size() > 0) {
            if (isRemove) {
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemToUpNextFrom(mCurrentList.remove(0));
            } else {
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemToUpNextFrom(mCurrentList.get(0));
            }
        }
    }

    private void addItemToUpNextFrom(UpNextItem item) {
        if (null != item && item.getUpNextItemType() == QueueType.Auto_UpNext)
            mAutoNextList.add(PlayItemIndex, item.getUpNextItem());
    }

    public void addItemToUpNextFrom(IMediaItemBase item) {
        if(null != item) {
            ArrayList list = new ArrayList();
            list.add(item);
            insertUnShuffledList(list, true);
            mAutoNextList.add(item);
            QueueUpdated();
        }
    }

    public void addItemListToUpNextFrom(IMediaItemBase itemList) {
        if(null != itemList && ((MediaItemCollection) itemList).getMediaElement().size() > 0) {
            insertUnShuffledList(((MediaItemCollection) itemList).getMediaElement(), true);
            if(mShuffle == SHUFFLE.all){
                Collections.shuffle(((MediaItemCollection) itemList).getMediaElement());
            }
            mAutoNextList.addAll(((MediaItemCollection) itemList).getMediaElement());
            QueueUpdated();
        }
    }

    public boolean isPrevious() {
        if(mRepeat == REPEAT.all){
            return (null != mUpNextList && mUpNextList.size() > 0) || (null != mAutoNextList && mAutoNextList.size() > 0)
                    || (mGhostList != null && mGhostList.size() > 0) ? true : false;
        }
        return mGhostList != null && mGhostList.size() > 0 ? true : false;
    }

    public boolean isNext() {
        if(mRepeat == REPEAT.all){
            return (null != mUpNextList && mUpNextList.size() > 0) || (null != mAutoNextList && mAutoNextList.size() > 0)
                    ? true : false;
        }
        return (mUpNextList != null && mUpNextList.size() > 0) || (null != mAutoNextList && mAutoNextList.size() > 0) ? true : false;
    }

    private void setItemAsPlayingItem(IMediaItemBase item, QueueType queueType) {
        if (mCurrentList.size() == 1)
            addItemToHistory(mCurrentList.remove(0).getUpNextItem());

        if(null != item) {
            mCurrentList.add(new UpNextItem(item, queueType));
            overFillingPlayingItem();
        }
    }

    private void setItemListAsPrevious(List<? extends IMediaItemBase> itemList) {
        mGhostList.clear();
        if(null != itemList && itemList.size() > 0)
            mGhostList.addAll(itemList);
    }

    private void setItemListAsUpNextFrom(List<? extends IMediaItemBase> itemList) {
        if(null != itemList && itemList.size() > 0)
            mAutoNextList.addAll(itemList);
    }

    public void managePlayedItem(boolean isRemove) {
        if (null != mCurrentList && mCurrentList.size() > 0) {
            if (isRemove) {
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemAsPrevious(mCurrentList.remove(0));
                mCurrentList.clear();
            } else {
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemAsPrevious(mCurrentList.get(0));
            }
        }
    }

    private void addItemAsPrevious(UpNextItem item) {
        if (null != item && item.getUpNextItemType() == QueueType.Auto_UpNext) {
            if (mGhostList.contains(item.getUpNextItem())) {
                mGhostList.remove(item.getUpNextItem());
            }
            mGhostList.add(item.getUpNextItem());
        }
    }

    private void addItemToHistory(IMediaItemBase item) {
        if(null != item) {
            addUpNextItem(item, QueueType.History);
            invalidateHistory();
            QueueUpdated();
        }
    }

    private void invalidateHistory() {
        mHistoryList.clear();
        mHistoryList.addAll(getUpNextItemList(QueueType.History));
    }

    public class UpNextItem {
        private IMediaItemBase item;
        private QueueType type;

        public UpNextItem(IMediaItemBase item, QueueType type) {
            this.item = item;
            this.type = type;
        }

        public IMediaItemBase getUpNextItem() {
            return item;
        }

        public QueueType getUpNextItemType() {
            return type;
        }
    }

}