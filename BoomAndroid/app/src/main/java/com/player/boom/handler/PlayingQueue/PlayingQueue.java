package com.player.boom.handler.PlayingQueue;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntRange;
import android.util.Log;

import com.player.boom.App;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.player.boom.data.MediaCollection.IMediaItemBase;
import com.player.boom.data.MediaLibrary.ItemType;
import com.player.boom.data.MediaLibrary.MediaController;

import java.util.ArrayList;
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
                if(mHistoryList != null){
                    mHistoryList.clear();
                }
                mHistoryList.addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
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

    /**************************************************************************************************************************/
//Fetch Queue Items

    public LinkedList<IMediaItemBase> getHistoryList(){
        if(playingQueue.get(History).size() == 0){
            return null;
        }else {
            return playingQueue.get(History);
        }
    }

    public IMediaItemBase getPlayingItem(){
        if(playingQueue.get(Playing).size() == 0){
            return null;
        }else {
            return playingQueue.get(Playing).get(0);
        }
    }

    public LinkedList<IMediaItemBase> getManualUpNextList(){
        if(playingQueue.get(Manual_UpNext).size() == 0){
            return null;
        }else {
            return playingQueue.get(Manual_UpNext);
        }
    }

    public LinkedList<IMediaItemBase> getAutoUpNextList(){
        if(playingQueue.get(Auto_UpNext).size() == 0){
            return null;
        }else {
            return playingQueue.get(Auto_UpNext);
        }
    }

    /***************************************************************************************************************************/
//    Queue Manipulation

    public void invalidateHistory(){
        if(playingQueue.get(History) != null){
            playingQueue.get(History).clear();
        }
        playingQueue.get(History).addAll(MediaController.getInstance(context).getHistoryItemsForQueue(ISHISTORY));
    }

    public void addItemToHistory(IMediaItemBase item){
        MediaController.getInstance(context).addSongsToList(ISHISTORY, item);
        invalidateHistory();
    }

    public void removeHistoryItem(IMediaItemBase item){
        MediaController.getInstance(context).removeItemToList(ISHISTORY, item.getItemId());
        invalidateHistory();
    }

    public void addHistoryItemToPlay(int position){
        IMediaItemBase item = playingQueue.get(History).get(position);
        addPlayingItemToHistory();
        playingQueue.get(Playing).add(item);
        removeHistoryItem(item);
        PlayingItemChanged();
    }

    public void addPlayingItemToHistory(){
        if(playingQueue.get(Playing).size()>0){
            addItemToHistory(playingQueue.get(Playing).remove(0));
            playingQueue.get(Playing).clear();
        }
    }

    public void addUpNextToPlay(int position, QueueType queueType){
        addPlayingItemToHistory();
        if(queueType == Auto_UpNext){
            playingQueue.get(Playing).add(playingQueue.get(Auto_UpNext).remove(position));
        }else{//Manual_UpNext
            playingQueue.get(Playing).add(playingQueue.get(Manual_UpNext).remove(position));
        }
        PlayingItemChanged();
    }

    public synchronized void addMediaItemToPlay(IMediaItemBase item){
        if(playingQueue.get(Playing).size() > 0){
            if(playingQueue.get(Playing).get(0).getItemId() != item.getItemId()) {
                addPlayingItemToHistory();
                playingQueue.get(Playing).add(item);
                PlayingItemChanged();
            }else{
                PlayPause();
                QueueUpdated();
            }
        }else {
            playingQueue.get(Playing).add(item);
            PlayingItemChanged();
        }
    }

    public void addMediaItemToAutoUpNext(IMediaItemBase item){
        playingQueue.get(Auto_UpNext).add(item);
        QueueUpdated();
    }

    public void addMediaItemToManualUpNext(IMediaItemBase item){
        playingQueue.get(Manual_UpNext).add(item);
        QueueUpdated();
    }

    public void addMediaItemsToManualUpNext(IMediaItemBase items, int position){
        playingQueue.get(Manual_UpNext).addAll(MediaController.getInstance(context).getMediaCollectionItemsForQueue((MediaItemCollection)items, position));
        QueueUpdated();
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

    public void finishTrack(boolean isFinish) {
    }

    /****************************************************************************************************************************/

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

    public static void Terminate() {
        handler = null;
    }


    public void setNextPlayingItem() {
        addPlayingItemToHistory();
        if(null != getManualUpNextList() && getManualUpNextList().size()>0){
            playingQueue.get(Playing).add(playingQueue.get(Manual_UpNext).remove(0));
        }else if(null != getAutoUpNextList() && getAutoUpNextList().size()>0){
            playingQueue.get(Playing).add(playingQueue.get(Auto_UpNext).remove(0));
        }
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
}
