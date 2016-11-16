package com.player.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;

import com.player.boom.data.MediaCollection.IMediaItem;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaCollection.IMediaItemCollection;
import com.player.boom.data.MediaLibrary.MediaController;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static com.player.boom.handler.PlayingQueue.QueueType.Auto_UpNext;
import static com.player.boom.handler.PlayingQueue.QueueType.History;
import static com.player.boom.handler.PlayingQueue.QueueType.Manual_UpNext;
import static com.player.boom.handler.PlayingQueue.QueueType.Playing;

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
                mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
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
        return getItemList(History);
    }

    public UpNextItem getPlayingItem(){
        return mCurrentList.get(0);
    }

    public LinkedList<IMediaItemBase> getManualUpNextList(){
        return getItemList(Manual_UpNext);
    }

    public LinkedList<IMediaItemBase> getAutoUpNextList(){
        return getItemList(Auto_UpNext);
    }

    /******************************************************************************************************************/

    public void setShuffle() {
        //mShuffle = App.getUserPreferenceHandler().getShuffle();
    }

    public void setRepeat() {
        //mRepeat = App.getUserPreferenceHandler().getRepeat();
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

    /******************************************************************************************************************/

//    itemList -> list of collection
//    position -> Now Playing Item position in item list.
    public void addToPlay(ArrayList<IMediaItem> itemList, int position){
        if(position > 0){
            setItemListAsPrevious(itemList.subList(0, position));
        }
        setItemAsPlayingItem(itemList.get(position), Auto_UpNext);
        if(itemList.size() > position+1){
            setItemListAsUpNextFrom(itemList.subList(position+1, itemList.size()));
        }
    }

//    selected Collection, Like Album, artist.
//    index of now Playing item of the collection
    public void addToPlay(IMediaItemCollection collection, int position){
        if(position > 0){
            setItemListAsPrevious(collection.getMediaElement().subList(0, position));
        }
        setItemAsPlayingItem(collection.getMediaElement().get(position), Auto_UpNext);
        if(collection.getMediaElement().size() > position+1){
            setItemListAsUpNextFrom(collection.getMediaElement().subList(position+1, collection.getMediaElement().size()));
        }
    }

    public void addToPlay(QueueType queueType, int position){
        if(mCurrentList.size() == 1){
            managePlayedItem(mCurrentList.remove(0));
        }

        switch (queueType){
            case History:
                mCurrentList.add(new UpNextItem(mHistoryList.remove(position), queueType));
                break;
            case Manual_UpNext:
                mCurrentList.add(new UpNextItem(mUpNextList.remove(position), queueType));
                break;
            case Auto_UpNext:
                PlayItemIndex = position;
                /* Shuffle will not effect on random selection*/
                if(mRepeat == REPEAT.none/* && mShuffle == SHUFFLE.none*/){
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
                break;
        }
    }

    public void addItemListToUpNext(List<? extends IMediaItemBase> itemList){
        mUpNextList.addAll(itemList);
    }

    public void setNextPlayingItem(boolean isUser){
        /* no repeat and no shuffle or Only Repeat one and user interaction is true*/
        if((mRepeat == REPEAT.none && mShuffle == SHUFFLE.none) || (mRepeat == REPEAT.one && isUser)) {
            if (mUpNextList.size() > 0) {
                managePlayedItem(mCurrentList.remove(0));
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), Manual_UpNext));
            } else if (mAutoNextList.size() > 0) {
                for(int i =0; i <= PlayItemIndex ; PlayItemIndex--){
                    ghostList.add(mAutoNextList.remove(i));
                }
                if(PlayItemIndex < 0){
                    PlayItemIndex = 0;
                }

                addItemToHistory(mCurrentList.remove(0).getUpNextItem());
                mCurrentList.add(new UpNextItem(mAutoNextList.remove(PlayItemIndex), Auto_UpNext));
            } else {

            }
        }
        /* Repeat is One and user interaction is false*/
        else if(mRepeat == REPEAT.one && !isUser/* && mShuffle == SHUFFLE.none*/){
//            same item will play again, so just call playing item change listener
        }
        /* Only Repeat is All and Shuffle if Off*/
        else if(mRepeat == REPEAT.all && mShuffle == SHUFFLE.none){
            managePlayedItem(mCurrentList.get(0));
            if(mUpNextList.size() > 0){
                mCurrentList.clear();
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), Manual_UpNext));
            }else if(mAutoNextList.size() > 0){
                mCurrentList.clear();
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), Auto_UpNext));
            }
        }
        /* Only Shuffle is On and Repeat is off*/

        else if(mShuffle == SHUFFLE.all && mRepeat == REPEAT.none){
            managePlayedItem(mCurrentList.get(0));
            Random rand = new Random();
            if(mUpNextList.size() > 0){
                mCurrentList.clear();
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), Manual_UpNext));
            }else if (mAutoNextList.size() > 0){
                mCurrentList.clear();
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                mCurrentList.add(new UpNextItem(mAutoNextList.remove(PlayItemIndex), Auto_UpNext));
            }
        }
        /* Repeat All And Shuffle is On*/
        else if (mShuffle == SHUFFLE.all && mRepeat == REPEAT.all){
            managePlayedItem(mCurrentList.get(0));
            Random rand = new Random();
            if(mUpNextList.size() > 0){
                mCurrentList.clear();
                mCurrentList.add(new UpNextItem(mUpNextList.remove(0), Manual_UpNext));
            }else if (mAutoNextList.size() > 0){
                mCurrentList.clear();
                PlayItemIndex = rand.nextInt(mAutoNextList.size());
                mCurrentList.add(new UpNextItem(mAutoNextList.get(PlayItemIndex), Auto_UpNext));
            }
        }
    }

    public void setPreviousPlayingItem(){
        managePlayedItem(mCurrentList.remove(0));
        int prevSize = ghostList.size();
        if(prevSize > 0){
            mCurrentList.add(new UpNextItem(ghostList.get(prevSize - 1), Auto_UpNext));
        }
    }

    public boolean isPrevious(){
        return ghostList.size() > 0 ? true : false;
    }

    public boolean isNext(){
        return (mUpNextList.size() > 0 || mAutoNextList.size() > 0) ? true : false;
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
        mAutoNextList.clear();
        mAutoNextList.addAll(itemList);
    }

    private void managePlayedItem(UpNextItem item) {
        addItemToHistory(item.getUpNextItem());
        addItemAsPrevious(item);
    }

    private void addItemAsPrevious(UpNextItem item) {
        if(item.getUpNextItemType() == Auto_UpNext)
            if(ghostList.contains(item.getUpNextItem())) {
                ghostList.remove(item.getUpNextItem());
                ghostList.add(item.getUpNextItem());
            }
    }

    private void addItemToHistory(IMediaItemBase item){
        MediaController.getInstance(context).addSongsToList(ISHISTORY, item);
        invalidateHistory();
    }

    private void invalidateHistory(){
        mHistoryList.clear();
        mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
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
