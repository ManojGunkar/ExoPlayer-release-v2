package com.globaldelight.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;

import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.MediaLibraryHandler;

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
    private static UpNextItem mPlayingItem;
    private static LinkedList<IMediaItemBase> mAutoNextList = new LinkedList<>();
    private static LinkedList<IMediaItemBase> mGhostList = new LinkedList<>();

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
                mHistoryList.addAll(MediaController.getInstance(context).getUpNextItemList(QueueType.History));
                return mHistoryList;
            case Manual_UpNext:
                if(mUpNextList != null){
                    mUpNextList.clear();
                }
                mUpNextList.addAll(MediaController.getInstance(context).getUpNextItemList(QueueType.Manual_UpNext));
                return mUpNextList;
            case Auto_UpNext:
                if(mAutoNextList != null){
                    mAutoNextList.clear();
                }
                mAutoNextList.addAll(MediaController.getInstance(context).getUpNextItemList(QueueType.Auto_UpNext));
                return mAutoNextList;
            case Previous:
                if(mGhostList != null){
                    mGhostList.clear();
                }
                mGhostList.addAll(MediaController.getInstance(context).getUpNextItemList(QueueType.Playing));
                return mGhostList;
            default:
                return null;
        }
    }

    public UpNextItem getPlayingUpNextItem(){
            return mPlayingItem = new UpNextItem(MediaController.getInstance(context).getPlayingItem());
    }

    public void addItemToUpNextList(IMediaItemBase media, QueueType queueType){
        MediaController.getInstance(context).addUpNextItem(media, queueType);
        getItemList(queueType);
    }

    public void addItemToUpNextList(IMediaItemBase media, int position, QueueType queueType){
        MediaController.getInstance(context).addUpNextItem(media, position, queueType);
        getItemList(queueType);
    }

    public void addItemListToUpNext(ArrayList<? extends IMediaItemBase> itemList, QueueType queueType) {
        MediaController.getInstance(context).addItemListToUpNext(itemList, queueType);
        getItemList(queueType);
    }

    public void addItemAsPlaying(IMediaItemBase media, QueueType queueType){
        MediaController.getInstance(context).addItemAsPlaying(media, queueType);
        getPlayingUpNextItem();
    }

    public IMediaItemBase removeItemFromUpNext(IMediaItemBase item, QueueType queueType){
        MediaController.getInstance(context).removeItemFromUpNext(item.getItemId(), queueType);
        getUpNextItemList(queueType);
        return item;
    }

    /**************************************************************************************************************************/
//Fetch Queue Items

    public LinkedList<IMediaItemBase> getUpNextItemList(QueueType queueType){
        return getItemList(queueType);
    }

    public IMediaItemBase getPlayingItem(){
        return getPlayingUpNextItem() != null ? mPlayingItem.getUpNextItem() : null;
    }

    public void clearUpNextList(QueueType queueType) {
        MediaController.getInstance(context).clearUpNextList(queueType);
        if(queueType != QueueType.Playing){
            getItemList(queueType);
        }
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
        clearUpNextList(QueueType.Auto_UpNext);
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
        clearUpNextList(QueueType.Auto_UpNext);
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
        clearUpNextList(QueueType.Auto_UpNext);
        if(collection.getMediaElement().size() > position+1){
            setItemListAsUpNextFrom(collection.getMediaElement().subList(position+1, collection.getMediaElement().size()));
        }
        PlayingItemChanged();
    }

    public void addToPlay(QueueType queueType, int position){
        switch (queueType){
            case History:
                managePlayedItem(true);
                addItemAsPlaying(mHistoryList.get(position), queueType);
                PlayingItemChanged();
                break;
            case Playing:
                PlayPause();
                break;
            case Manual_UpNext:
                managePlayedItem(true);
                addItemAsPlaying(removeItemFromUpNext(mUpNextList.remove(position), queueType), queueType);
                PlayingItemChanged();
                break;
            case Auto_UpNext:
                managePlayedItem(true);
                PlayItemIndex = position;
                /* Shuffle will not effect on random selection*/
                if(mRepeat != REPEAT.all/* && mShuffle == SHUFFLE.none*/){
                    for(int i =0; i< PlayItemIndex ; PlayItemIndex--){
                        addItemToUpNextList(mAutoNextList.remove(i), QueueType.Auto_UpNext);
                    }
                    if(PlayItemIndex < 0)
                        PlayItemIndex = 0;
//                selected item comes on top, so remove only top (0) item
                    addItemAsPlaying(removeItemFromUpNext(mAutoNextList.remove(0), queueType), queueType);
                }
                else if(mRepeat == REPEAT.all/* && mShuffle == SHUFFLE.none*/){
                    addItemAsPlaying(removeItemFromUpNext(mAutoNextList.get(PlayItemIndex), queueType), queueType);
                }
                PlayingItemChanged();
                break;
        }
    }

    public void addItemListToUpNext(IMediaItemBase itemList){
        addItemListToUpNext(((MediaItemCollection)itemList).getMediaElement(), QueueType.Manual_UpNext);
        QueueUpdated();
    }

    public void addItemListToUpNext(MediaItem item){
        addItemToUpNextList(item, QueueType.Manual_UpNext);
        QueueUpdated();
    }

    public void setNextPlayingItem(boolean isUser){
        getRepeat();
        getShuffle();
        /* no repeat and no shuffle or Only Repeat one and user interaction is true*/
        if((mRepeat == REPEAT.none && mShuffle == SHUFFLE.none) || (mRepeat == REPEAT.one && isUser)) {
            if (mUpNextList.size() > 0) {
                managePlayedItem(true);
                addItemAsPlaying(removeItemFromUpNext(mUpNextList.remove(0), QueueType.Manual_UpNext), QueueType.Manual_UpNext);
            } else if (mAutoNextList.size() > 0) {
                for(int i =0; i < PlayItemIndex ; PlayItemIndex--){
                    addItemToUpNextList(removeItemFromUpNext(mAutoNextList.remove(i), QueueType.Auto_UpNext), QueueType.Previous);
                }
                managePlayedItem(true);
                if(PlayItemIndex < 0){
                    PlayItemIndex = 0;
                }
                addItemAsPlaying(removeItemFromUpNext(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext), QueueType.Playing);
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
            if(mPlayingItem != null && mPlayingItem.getUpNextItemType() == QueueType.Auto_UpNext &&
                    !mAutoNextList.contains(mPlayingItem.getUpNextItem())){
                addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.Auto_UpNext);
            }
            managePlayedItem(true);
            if(mUpNextList.size() > 0){
                addItemAsPlaying(removeItemFromUpNext(mUpNextList.remove(0), QueueType.Manual_UpNext), QueueType.Manual_UpNext);
            }else if(mAutoNextList.size() > 0){
                if((mAutoNextList.size() - 1) > PlayItemIndex) {
                    PlayItemIndex++;
                }else{
                    PlayItemIndex = 0;
                }
                addItemAsPlaying(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext);
            }
        }
        /* Only Shuffle is On and Repeat is off*/

        else if(mShuffle == SHUFFLE.all && mRepeat == REPEAT.none){
            managePlayedItem(true);
            Random rand = new Random();
            if(mUpNextList.size() > 0){
                addItemAsPlaying(removeItemFromUpNext(mUpNextList.remove(0), QueueType.Manual_UpNext), QueueType.Manual_UpNext);
            }else if (mAutoNextList.size() > 0){
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                addItemAsPlaying(removeItemFromUpNext(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext), QueueType.Auto_UpNext);
            }
        }
        /* Repeat All And Shuffle is On*/
        else if (mShuffle == SHUFFLE.all && mRepeat == REPEAT.all){
            Random rand = new Random();
            if(mPlayingItem != null && mPlayingItem.getUpNextItemType() == QueueType.Auto_UpNext &&
                    !mAutoNextList.contains(mPlayingItem.getUpNextItem())){
                addItemAsPlaying(mPlayingItem.getUpNextItem(), QueueType.Auto_UpNext);
            }
            managePlayedItem(true);
            if(mUpNextList.size() > 0){
                addItemAsPlaying(removeItemFromUpNext(mUpNextList.remove(0), QueueType.Manual_UpNext), QueueType.Manual_UpNext);
            }else if(mAutoNextList.size() > 0){
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                addItemAsPlaying(removeItemFromUpNext(mAutoNextList.remove(PlayItemIndex), QueueType.Auto_UpNext), QueueType.Auto_UpNext);
            }
        }
        PlayingItemChanged();
    }

    public void setPreviousPlayingItem(){
        if(mRepeat == REPEAT.all && mShuffle == SHUFFLE.none && mAutoNextList.size() > 0){
            if(mPlayingItem != null)
                addItemAsPlaying(mPlayingItem.getUpNextItem(), QueueType.History);

            if(PlayItemIndex == 0) {
                PlayItemIndex = mAutoNextList.size() - 1;
            }else{
                PlayItemIndex--;
            }
            addItemAsPlaying(mAutoNextList.get(PlayItemIndex), QueueType.Auto_UpNext);
            PlayingItemChanged();
        }else if(((mRepeat == REPEAT.all && mShuffle == SHUFFLE.all) || mRepeat != REPEAT.all ) && mAutoNextList.size() != -1){
            int prevSize = mGhostList.size();
            IMediaItemBase playingItem = null;
            if (prevSize > 0) {
                playingItem = removeItemFromUpNext(mGhostList.remove(prevSize - 1), QueueType.Previous);
            }
            managePreviousItem(true);
            if (null != playingItem) {
                addItemAsPlaying(playingItem, QueueType.Auto_UpNext);
            }
            PlayingItemChanged();
        }
    }

    private void managePreviousItem(boolean isRemove) {
        if(mPlayingItem != null){
            if(isRemove){
                addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.History);
                addItemToUpNextFrom(mPlayingItem);
                clearUpNextList(QueueType.Playing);
            }else {
                addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.History);
                addItemToUpNextFrom(mPlayingItem);
            }
        }
    }

    private void addItemToUpNextFrom(UpNextItem item) {
        if(item.getUpNextItemType() == QueueType.Auto_UpNext) {
            addItemToUpNextList(item.getUpNextItem(), PlayItemIndex, QueueType.Auto_UpNext);
            addItemToUpNextList(item.getUpNextItem(), QueueType.Previous);
        }
    }

    public void addItemToUpNextFrom(IMediaItemBase item){
        mAutoNextList.add(item);
    }

    public void addItemListToUpNextFrom(List<? extends IMediaItemBase> itemList){
        mAutoNextList.addAll(itemList);
    }

    public boolean isPrevious(){
        return mGhostList != null && mGhostList.size() > 0 ? true : false;
    }

    public boolean isNext(){
        return mUpNextList != null && (mUpNextList.size() > 0 || mAutoNextList.size() > 0) ? true : false;
    }

    private void setItemAsPlayingItem(IMediaItemBase item, QueueType queueType) {
        if(mPlayingItem != null) {
            addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.History);
            clearUpNextList(QueueType.Playing);
        }
        addItemAsPlaying(item, queueType);
    }

    private void setItemListAsPrevious(List<? extends IMediaItemBase> itemList) {
        clearUpNextList(QueueType.Previous);
        ArrayList list = new ArrayList();
        list.addAll(itemList);
        addItemListToUpNext(list, QueueType.Previous);
    }

    private void setItemListAsUpNextFrom(List<? extends IMediaItemBase> itemList) {
        ArrayList list = new ArrayList();
        list.addAll(itemList);
        addItemListToUpNext(list, QueueType.Auto_UpNext);
    }

    public void managePlayedItem(boolean isRemove) {
        if(mPlayingItem != null){
            if(isRemove){
                addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.History);
                addItemAsPrevious(mPlayingItem);
                clearUpNextList(QueueType.Playing);
            }else{
                addItemToUpNextList(mPlayingItem.getUpNextItem(), QueueType.History);
                addItemAsPrevious(mPlayingItem);
            }
        }
    }

    private void addItemAsPrevious(UpNextItem item) {
        if(item.getUpNextItemType() == QueueType.Auto_UpNext) {
            if (mGhostList.contains(item.getUpNextItem())) {
                removeItemFromUpNext(item.getUpNextItem(), QueueType.Previous);
            }
            addItemToUpNextList(item.getUpNextItem(), QueueType.Previous);
        }
    }
}
