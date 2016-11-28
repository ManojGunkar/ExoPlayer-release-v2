package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Created by Rahul Agarwal on 15-11-16.
 */

public class UpNextList {

    private static Context context;
    public static int PlayItemIndex = 0;
    private QueueEvent queueEvent = null;
    Handler eventHandler = new Handler();
    private static final boolean ISHISTORY = true;
    private static UpNextList.SHUFFLE mShuffle = UpNextList.SHUFFLE.none;
    private static UpNextList.REPEAT mRepeat = UpNextList.REPEAT.none;

    private static LinkedList<IMediaItemBase> mHistoryList = new LinkedList<>();
    private static LinkedList<IMediaItemBase> mUpNextList = new LinkedList<>();
    private static LinkedList<UpNextItem> mCurrentList = new LinkedList<>();
    private static LinkedList<IMediaItemBase> mAutoNextList = new LinkedList<>();
    private static LinkedList<IMediaItemBase> ghostList = new LinkedList<>();

    private static UpNextList handler;

    private UpNextList(Context application){
        context = application;
    }

    public static UpNextList getUpNextInstance(Context application){
        if(handler == null){
            handler = new UpNextList(application);
        }
        return handler;
    }

    public void setQueueEvent(QueueEvent event){
        this.queueEvent = event;
    }

    private LinkedList<IMediaItemBase> getItemList(QueueType type){
        switch (type){
            case History:
                if(mHistoryList != null){
                    mHistoryList.clear();
                }
                mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue());
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

    public LinkedList<IMediaItemBase> getHistoryList(){
        return getItemList(QueueType.History);
    }

    public LinkedList<UpNextItem> getPlayingList(){
        return mCurrentList;
    }

    public IMediaItemBase getPlayingItem(){
        return mCurrentList.size() > 0 ? mCurrentList.get(0).getUpNextItem() : null;
    }

    public LinkedList<IMediaItemBase> getManualUpNextList(){
        return getItemList(QueueType.Manual_UpNext);
    }

    public LinkedList<IMediaItemBase> getAutoUpNextList(){
        return getItemList(QueueType.Auto_UpNext);
    }

    /******************************************************************************************************************/

    public boolean resetShuffle() {
        mShuffle = App.getUserPreferenceHandler().resetShuffle();
        return true;
    }

    public boolean resetRepeat() {
        mRepeat = App.getUserPreferenceHandler().resetRepeat();
        return true;
    }

    public SHUFFLE getShuffle(){
        return mShuffle = App.getUserPreferenceHandler().getShuffle();
    }

    public REPEAT getRepeat(){
        return mRepeat = App.getUserPreferenceHandler().getRepeat();
    }

    public static void Terminate() {

    }

    public int size() {
        return mHistoryList.size()+mUpNextList.size()+mAutoNextList.size();
    }

    public void clearHistory() {
        MediaController.getInstance(context).clearList(true);
        getItemList(QueueType.History);
    }

    public enum REPEAT{
        one,
        all,
        none,
    }

    public enum SHUFFLE{
        all,
        none,
    }

    /**************************************************************************************************************************/
//    Callbacks to update UI

    public void PlayPause(){
        if (queueEvent != null){
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onPlayingItemClicked();
                }
            });
        }
    }
    //  Queue and Playing Item Update
    public void PlayingItemChanged(){
        if (queueEvent != null){
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onPlayingItemChanged();
                }
            });
        }
    }
    //  Only Queue update
    public void QueueUpdated(){
        if (queueEvent != null){
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onQueueUpdated();
                }
            });
        }
    }


    /******************************************************************************************************************/

    public void addToPlay(LinkedList<MediaItem> itemList, int position){
        if(position > 0){
            setItemListAsPrevious(itemList.subList(0, position));
        }
        setItemAsPlayingItem(itemList.get(position), QueueType.Auto_UpNext);
        mAutoNextList.clear();
        if(itemList.size() > position+1){
            setItemListAsUpNextFrom(itemList.subList(position+1, itemList.size()));
        }
        PlayingItemChanged();
    }
//    itemList -> list of collection
//    position -> Now Playing Item position in item list.
    public void addToPlay(ArrayList<MediaItem> itemList, int position){
        if(position > 0){
            setItemListAsPrevious(itemList.subList(0, position));
        }
        setItemAsPlayingItem(itemList.get(position), QueueType.Auto_UpNext);
        mAutoNextList.clear();
        if(itemList.size() > position+1){
            setItemListAsUpNextFrom(itemList.subList(position+1, itemList.size()));
        }
        PlayingItemChanged();
    }

//    selected Collection, Like Album, ic_artist.
//    index of now Playing item of the collection
    public void addToPlay(IMediaItemCollection collection, int position){
        if(position > 0){
            setItemListAsPrevious(collection.getMediaElement().subList(0, position));
        }
        setItemAsPlayingItem(collection.getMediaElement().get(position), QueueType.Auto_UpNext);
        mAutoNextList.clear();
        if(collection.getMediaElement().size() > position+1){
            setItemListAsUpNextFrom(collection.getMediaElement().subList(position+1, collection.getMediaElement().size()));
        }
        PlayingItemChanged();
    }

    public void addToPlay(QueueType queueType, int position){
        switch (queueType){
            case History:
                managePlayedItem(true);
                mCurrentList.add(new UpNextItem(mHistoryList.get(position), queueType));
                PlayingItemChanged();
                break;
            case Playing:
                PlayPause();
                break;
            case Manual_UpNext:
                managePlayedItem(true);
                mCurrentList.add(new UpNextItem(mUpNextList.remove(position), queueType));
                PlayingItemChanged();
                break;
            case Auto_UpNext:
                managePlayedItem(true);
                PlayItemIndex = position;
                /* Shuffle will not effect on random selection*/
                if(mRepeat != REPEAT.all/* && mShuffle == SHUFFLE.none*/){
                    for(int i =0; i< PlayItemIndex ; PlayItemIndex--){
                        ghostList.add(mAutoNextList.remove(i));
                    }
                    if(PlayItemIndex < 0)
                        PlayItemIndex = 0;
//                selected item comes on top, so remove only top (0) item
                    mCurrentList.add(new UpNextItem(mAutoNextList.remove(0), queueType));
                }
                else if(mRepeat == REPEAT.all/* && mShuffle == SHUFFLE.none*/){
                    mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), queueType));
                }
                PlayingItemChanged();
                break;
        }
    }

    public void addItemListToUpNext(IMediaItemBase itemList){
        mUpNextList.addAll(((MediaItemCollection)itemList).getMediaElement());
        QueueUpdated();
    }

    public void addItemListToUpNext(MediaItem item){
        mUpNextList.add(item);
        QueueUpdated();
    }

    public void setNextPlayingItem(boolean isUser){
        getRepeat();
        getShuffle();
        /* no repeat and no shuffle or Only Repeat one and user interaction is true*/
        if((mRepeat == REPEAT.none && mShuffle == SHUFFLE.none) || (mRepeat == REPEAT.one && isUser)) {
            if (mUpNextList.size() > 0) {
                managePlayedItem(true);
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            } else if (mAutoNextList.size() > 0) {
                for(int i =0; i < PlayItemIndex ; PlayItemIndex--){
                    addItemAsPrevious(new UpNextItem(mAutoNextList.remove(i), QueueType.Auto_UpNext));
                }
                managePlayedItem(true);
                if(PlayItemIndex < 0){
                    PlayItemIndex = 0;
                }
                mCurrentList.add(new UpNextItem(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext));
            } else {
                managePlayedItem(true);
            }
        }
        /* Repeat is One and user interaction is false*/
        else if(mRepeat == REPEAT.one && !isUser/* && mShuffle == SHUFFLE.none*/){
//            same item will play again, so just call playing item change listener

        }
        /* Only Repeat is All and Shuffle if Off*/
        else if(mRepeat == REPEAT.all && mShuffle == SHUFFLE.none){
            if(mCurrentList.size() > 0 && mCurrentList.get(0).getUpNextItemType() == QueueType.Auto_UpNext &&
                    !mAutoNextList.contains(mCurrentList.get(0).getUpNextItem())){
                mAutoNextList.add(mCurrentList.get(0).getUpNextItem());
            }
            managePlayedItem(true);
            if(mUpNextList.size() > 0){
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            }else if(mAutoNextList.size() > 0){
                if((mAutoNextList.size() - 1) > PlayItemIndex) {
                    PlayItemIndex++;
                }else{
                    PlayItemIndex = 0;
                }
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
            }
        }
        /* Only Shuffle is On and Repeat is off*/

        else if(mShuffle == SHUFFLE.all && mRepeat == REPEAT.none){
            managePlayedItem(true);
            Random rand = new Random();
            if(mUpNextList.size() > 0){
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            }else if (mAutoNextList.size() > 0){
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                mCurrentList.add(new UpNextItem(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext));
            }
        }
        /* Repeat All And Shuffle is On*/
        else if (mShuffle == SHUFFLE.all && mRepeat == REPEAT.all){
            Random rand = new Random();
            if(mCurrentList.size() > 0 && mCurrentList.get(0).getUpNextItemType() == QueueType.Auto_UpNext &&
                    !mAutoNextList.contains(mCurrentList.get(0).getUpNextItem())){
                mAutoNextList.add(mCurrentList.get(0).getUpNextItem());
            }
            managePlayedItem(true);
            if(mUpNextList.size() > 0){
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), QueueType.Manual_UpNext));
            }else if(mAutoNextList.size() > 0){
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
            }
        }
        PlayingItemChanged();
    }

    public void setPreviousPlayingItem(){
        if(mRepeat == REPEAT.all && mShuffle == SHUFFLE.none && mAutoNextList.size() > 0){
            if(mCurrentList.size() > 0)
                addItemToHistory(mCurrentList.remove(0).getUpNextItem());

            if(PlayItemIndex == 0) {
                PlayItemIndex = mAutoNextList.size() - 1;
            }else{
                PlayItemIndex--;
            }
            mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext));
            PlayingItemChanged();
        }else if(((mRepeat == REPEAT.all && mShuffle == SHUFFLE.all) || mRepeat != REPEAT.all ) && mAutoNextList.size() != -1){
            int prevSize = ghostList.size();
            MediaItem playingItem = null;
            if (prevSize > 0) {
                playingItem = (MediaItem) ghostList.remove(prevSize - 1);
            }
            managePreviousItem(true);
            if (null != playingItem) {
                mCurrentList.add(new UpNextItem(playingItem, QueueType.Auto_UpNext));
            }
            PlayingItemChanged();
        }
    }

    private void managePreviousItem(boolean isRemove) {
        if(mCurrentList.size() > 0){
            if(isRemove){
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemToUpNextFrom(mCurrentList.remove(0));
                mCurrentList.clear();
            }else {
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemToUpNextFrom(mCurrentList.get(0));
            }
        }
    }

    private void addItemToUpNextFrom(UpNextItem item) {
        if(item.getUpNextItemType() == QueueType.Auto_UpNext) {
            mAutoNextList.add(PlayItemIndex, item.getUpNextItem());
            ghostList.add(item.getUpNextItem());
        }
    }

    public void addItemToUpNextFrom(IMediaItemBase item){
        mAutoNextList.add(item);
    }

    public void addItemListToUpNextFrom(List<? extends IMediaItemBase> itemList){
        mAutoNextList.addAll(itemList);
    }

    public boolean isPrevious(){
        return ghostList != null && ghostList.size() > 0 ? true : false;
    }

    public boolean isNext(){
        return mUpNextList != null && (mUpNextList.size() > 0 || mAutoNextList.size() > 0) ? true : false;
    }

    private void setItemAsPlayingItem(IMediaItemBase item, QueueType queueType) {
        if(mCurrentList.size() == 1)
            addItemToHistory(mCurrentList.remove(0).getUpNextItem());
        mCurrentList.add(new UpNextItem(item, queueType));
    }

    private void setItemListAsPrevious(List<? extends IMediaItemBase> itemList) {
        ghostList.clear();
        ghostList.addAll(itemList);
    }

    private void setItemListAsUpNextFrom(List<? extends IMediaItemBase> itemList) {
        mAutoNextList.addAll(itemList);
    }

    public void managePlayedItem(boolean isRemove) {
        if(mCurrentList.size() > 0){
            if(isRemove){
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemAsPrevious(mCurrentList.remove(0));
                mCurrentList.clear();
            }else{
                addItemToHistory(mCurrentList.get(0).getUpNextItem());
                addItemAsPrevious(mCurrentList.get(0));
            }
        }
    }

    private void addItemAsPrevious(UpNextItem item) {
        if(item.getUpNextItemType() == QueueType.Auto_UpNext) {
            if (ghostList.contains(item.getUpNextItem())) {
                ghostList.remove(item.getUpNextItem());
            }
            ghostList.add(item.getUpNextItem());
        }
    }

    private void addItemToHistory(IMediaItemBase item){
        MediaController.getInstance(context).addSongsToList(ISHISTORY, item);
        invalidateHistory();
        QueueUpdated();
    }

    private void invalidateHistory(){
        mHistoryList.clear();
        mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue());
    }

    public class UpNextItem{
        private IMediaItemBase item;
        private QueueType type;

        public UpNextItem(IMediaItemBase item, QueueType type){
            this.item = item;
            this.type = type;
        }

        public IMediaItemBase getUpNextItem(){
            return item;
        }
        public QueueType getUpNextItemType(){
            return type;
        }
    }
}
