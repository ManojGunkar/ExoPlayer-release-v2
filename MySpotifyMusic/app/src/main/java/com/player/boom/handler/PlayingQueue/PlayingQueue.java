package com.player.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntRange;

import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.data.MediaLibrary.MediaController;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import static com.player.boom.handler.PlayingQueue.QueueType.*;

/**
 * Created by Rahul Agarwal on 16-09-16.
 */
public class PlayingQueue {

    private QueueEvent queueEvent = null;
    Handler eventHandler = new Handler();

    private static final boolean ISHISTORY = true;
    private static Context context;
    private Map<QueueType, LinkedList<IMediaItemBase>> playingQueue;
    private LinkedList<IMediaItemBase> mHistoryList = new LinkedList<>();
    private LinkedList<IMediaItemBase> mUpNextList = new LinkedList<>();
    private LinkedList<IMediaItemBase> mCurrentList = new LinkedList<>();
    private LinkedList<IMediaItemBase> mAutoNextList = new LinkedList<>();

    private static PlayingQueue handler;

    private PlayingQueue(Context application){
        context = application;
        playingQueue = new HashMap<>();
    }

    public static PlayingQueue getQueueInstance(Context application){
        if(handler == null){
            handler = new PlayingQueue(application);
        }
        return handler;
    }

    public void setQueueEvent(QueueEvent event){
        this.queueEvent = event;
    }

    public Map<QueueType, LinkedList<IMediaItemBase>> getPlayingQueue(){
        playingQueue.put(History, getItemList(History));
        playingQueue.put(Playing, getItemList(Playing));
        playingQueue.put(Manual_UpNext, getItemList(Manual_UpNext));
        playingQueue.put(Auto_UpNext, getItemList(Auto_UpNext));
        return playingQueue;
    }

    public LinkedList<IMediaItemBase> getItemList(QueueType type){
        switch (type){
            case History:
                if(mHistoryList.isEmpty()){
//                    fetch data from DB
                    if(mHistoryList != null){
                        mHistoryList.clear();
                    }
                    mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
                }
                return mHistoryList;
            case Playing:
                return mCurrentList;
            case Manual_UpNext:
                return mUpNextList;
            case Auto_UpNext:
                return mAutoNextList;
            default:
                return null;
        }
    }

    public void invalidateQueue(){

    }

    public void clear(QueueType type){
        switch (type){
            case History:
                MediaController.getInstance(context).clearList(ISHISTORY);
                mHistoryList.clear();
                break;
            case Playing:
                mCurrentList.clear();
                break;
            case Manual_UpNext:
                mUpNextList.clear();
                break;
            case Auto_UpNext:
                mAutoNextList.clear();
                break;
            default:
        }
        updateQueue();
    }

    public boolean isEmpty(){
        return playingQueue.isEmpty();
    }

    public boolean isEmpty(QueueType type){
        switch (type){
            case History:
                return mHistoryList.isEmpty();
            case Playing:
                return mCurrentList.isEmpty();
            case Manual_UpNext:
                return mUpNextList.isEmpty();
            case Auto_UpNext:
                return mAutoNextList.isEmpty();
            default:
                return false;
        }
    }

    public void reOrderItemsInList(QueueType fromType, QueueType toType, int from_index, int to_index){
        if(fromType == Auto_UpNext && (toType == Auto_UpNext || toType == Manual_UpNext)){
            if(toType == Auto_UpNext){
                Collections.swap(mAutoNextList, from_index, to_index);
            }else{
                mUpNextList.add(to_index, mAutoNextList.get(from_index));
            }
        }else if(fromType == Manual_UpNext && toType == Manual_UpNext){
            Collections.swap(mUpNextList, from_index, to_index);
        }else{

        }
    }

    public IMediaItemBase getPlayingItem(){
        if(playingQueue.get(Playing).size() == 0){
            return null;
        }else {
            return playingQueue.get(Playing).get(0);
        }
    }

    public void resetPlaying(){
        if (queueEvent != null){
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onPlayingItemChanged();
                }
            });
        }
    }

    public void updateQueue(){
        if (queueEvent != null){
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    queueEvent.onQueueUpdated();
                }
            });
        }
    }

    public void addListItemToPlaying(QueueType queueType, int position){

        if(queueType == Auto_UpNext || queueType == Manual_UpNext){
            addPlayingItemToHistory();
//                Update the Playing Item
            playingQueue.get(Playing).add(playingQueue.get(queueType).remove(position));

        }else if(queueType == History){

            IMediaItemBase item = playingQueue.get(queueType).get(position);
            if(mHistoryList != null){
                mHistoryList.clear();
            }
//                    History Updated and update to database
            MediaController.getInstance(context).removeItemToList(ISHISTORY, item.getItemId());
            if(playingQueue.get(Playing).size()>0)
                MediaController.getInstance(context).addSongsToList(ISHISTORY, playingQueue.get(Playing).remove(0));
            mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));

//                Update the Playing Item
            playingQueue.get(Playing).add(item);
        }
        updateQueue();
        resetPlaying();
    }

    public void addItemToQueue(QueueType queueType, IMediaItemBase mediaItemBase, @IntRange(from=-1) int position) {
            if(mediaItemBase.getItemType() == ItemType.SONGS && queueType == Playing){
                if (!isEmpty(queueType)) {
                    addPlayingItemToHistory();
                }
//                Update the Playing Item
                playingQueue.get(queueType).add(mediaItemBase);
                resetPlaying();
            } else if(mediaItemBase.getItemType() == ItemType.SONGS && queueType != Playing) {
                if(!playingQueue.get(queueType).contains(mediaItemBase))
                    playingQueue.get(queueType).add(mediaItemBase);
            } else {
                playingQueue.get(queueType).addAll(MediaController.getInstance(context).getMediaCollectionItemsForQueue((MediaItemCollection)mediaItemBase, position));
            }
        updateQueue();
    }

    public void addPlayingItemToHistory(){
//      History Updated and update to database
        if(mHistoryList != null){
            mHistoryList.clear();
        }
        if(playingQueue.get(Playing).size()>0)
            MediaController.getInstance(context).addSongsToList(ISHISTORY, playingQueue.get(Playing).remove(0));
        mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
    }

    public static void Terminate() {
        handler = null;
    }

    public void finishTrack(boolean isFinish) {
        addPlayingItemToHistory();
        if(isFinish) {
            if (mUpNextList != null && mUpNextList.size() > 0) {
                addListItemToPlaying(Manual_UpNext, 0);
            } else if (mAutoNextList != null && mAutoNextList.size() > 0) {
                addListItemToPlaying(Auto_UpNext, 0);
            } else {
                resetPlaying();
            }
        }
    }
}
