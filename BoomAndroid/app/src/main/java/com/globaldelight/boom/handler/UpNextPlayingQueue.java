package com.globaldelight.boom.handler;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.handler.PlayingQueue.IUpNextMediaEvent;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import static com.globaldelight.boom.utils.handlers.Preferences.PLAYING_ITEM_INDEX_IN_UPNEXT;

/**
 * Created by Rahul Agarwal on 01-03-17.
 */

public class UpNextPlayingQueue {
    private Context context;
    private static int mPlayingItemIndex = -1;
    private long mShiftingTime = 0;
    private IUpNextMediaEvent mUpNextMediaEvent = null;

    Handler eventHandler = new Handler();

    private static UpNextPlayingQueue upNextHandler;

    ArrayList<IMediaItemBase> mUpNextList;

    private HashMap<String, String> mAlbumArtList = new HashMap<>();
    private HashMap<Long, String> mArtistArtList = new HashMap<>();

    private static SHUFFLE mShuffle = SHUFFLE.none;
    private static REPEAT mRepeat = REPEAT.none;

    private static final String SHUFFLED = "shuffle";
    private static final String UNSHUFFLE = "unshuffle";
    private int playNextPosition;

    private UpNextPlayingQueue(Context context){
        this.context = context;
        mUpNextList = new ArrayList<>();
    }

    public static UpNextPlayingQueue getUpNextInstance(Context context){
        if(null == upNextHandler)
            upNextHandler = new UpNextPlayingQueue(context);
        return upNextHandler;
    }

    public void setUpNextMediaEvent(IUpNextMediaEvent event) {
        this.mUpNextMediaEvent = event;
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

    public void clearUpNext() {
        mUpNextList.clear();
        clearUpNextSavedList();
    }

    public ArrayList<? extends IMediaItemBase> getHistoryList() {
        return MediaController.getInstance(context).getRecentPlayedList();
    }

    private void insertToHistory(IMediaItemBase itemBase){
        MediaController.getInstance(context).setRecentPlayedItem(itemBase);
    }

    public int getPlayingItemIndex(){
        return mPlayingItemIndex;
    }

    public void setPlayingItemIndex(int index){
        this.mPlayingItemIndex = index;
    }

    public IMediaItemBase getPlayingItem(){
        if(null != mUpNextList && mUpNextList.size() > 0 && mPlayingItemIndex >= 0)
            return mUpNextList.get(mPlayingItemIndex);
        return null;
    }

    public ArrayList<? extends IMediaItemBase> getUpNextItemList() {
        return mUpNextList;
    }

    public int getUpNextItemCount(){
        if(null != mUpNextList)
            return mUpNextList.size();
        return 0;
    }

    /**************************************************************************************************************************/
//    Callbacks to update UI
    public void PlayPause() {
        if (mUpNextMediaEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUpNextMediaEvent.onPlayingItemClicked();
                }
            });
        }
    }

    //  Queue and Playing Item Update
    public void PlayingItemChanged() {
        if (mUpNextMediaEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUpNextMediaEvent.onPlayingItemChanged();
                }
            });
        }
    }

    //  Only Queue update
    public void QueueUpdated() {
        if (mUpNextMediaEvent != null) {
            eventHandler.post(new Runnable() {
                @Override
                public void run() {
                    mUpNextMediaEvent.onQueueUpdated();
                }
            });
        }
    }

    /******************************************************************************************************************/

    public boolean isPrevious() {
        if(getUpNextItemCount() > 0) {
            if (mRepeat == REPEAT.all)
                return mUpNextList.size() > 0 ? true : false;
            return mPlayingItemIndex > 0 ? true : false;
        }
        return false;
    }

    public boolean isNext() {
        if(getUpNextItemCount() > 0) {
            if (mRepeat == REPEAT.all)
                return mUpNextList.size() > 0 ? true : false;
            return mPlayingItemIndex < (mUpNextList.size() - 1) ? true : false;
        }
        return false;
    }

    public void updateRepeatShuffleOnAppStart(){
        mShuffle = App.getUserPreferenceHandler().getShuffle();
        mRepeat = App.getUserPreferenceHandler().getRepeat();
    }

    public boolean resetShuffle(){
        mShuffle = App.getUserPreferenceHandler().resetShuffle();
        updateShuffleList();
        try {
            if (mShuffle == UpNextPlayingQueue.SHUFFLE.all) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SHUFFLE_ON_PLAYING);
            } else {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_SHUFFLE_OFF_PLAYING);
            }
        }catch (Exception e){}
        return true;
    }

    public boolean resetRepeat(){
        mRepeat = App.getUserPreferenceHandler().resetRepeat();
        try {
            if (mRepeat == UpNextPlayingQueue.REPEAT.one) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_ONE_PLAYING);
            } else if (mRepeat == UpNextPlayingQueue.REPEAT.all) {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_ALL_PLAYING);
            } else {
                FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_REPEAT_NONE_PLAYING);
            }
        }catch (Exception e){}
        return true;
    }

    public void updateShuffleList() {
        if(mUpNextList.size() > 0) {
            IMediaItemBase playingItem = getPlayingItem();
            if (mShuffle == UpNextPlayingQueue.SHUFFLE.all) {
                insertUpNextList(UNSHUFFLE);
                mUpNextList.remove(getPlayingItemIndex());
                Collections.shuffle(mUpNextList, new Random(mUpNextList.size()));
                mUpNextList.add(0, playingItem);
                mPlayingItemIndex = 0;
            } else {
                retrieveUpNextList(UNSHUFFLE, playingItem);
            }
            QueueUpdated();
        }
    }

    private void insertUpNextList(String shuffleType) {
        Preferences.writeString(context, shuffleType, new Gson().toJson(mUpNextList));
    }

    private void retrieveUpNextList(String shuffleType, IMediaItemBase playingItem) {
        try {
            List list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, shuffleType, null), MediaItem[].class));
            mUpNextList = new ArrayList<>(list);
            if ((null != mUpNextList || !mUpNextList.isEmpty()) && mUpNextList.size() > 0) {
                if (mUpNextList.size() == 1)
                    mPlayingItemIndex = 0;
                else {
                    int count = 0;
                    for (IMediaItemBase item : mUpNextList) {
                        if (playingItem.getItemId() == item.getItemId())
                            mPlayingItemIndex = count;
                        count++;
                    }
                }
            } else {
                mPlayingItemIndex = -1;
                nullCheck();
            }
        }catch (JsonSyntaxException e){

        }catch (NullPointerException e){

        }
    }

    private void nullCheck() {
        if(null == mUpNextList)
            mUpNextList = new ArrayList<>();
    }

    private void updateUnshuffledList(int position, IMediaItemBase item) {
        ArrayList<IMediaItemBase> tempList = null;
        try {
            List list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, UNSHUFFLE, null), MediaItem[].class));
            tempList = new ArrayList<>(list);
        }catch (JsonSyntaxException e){

        }catch (NullPointerException e){

        }

        if(null != tempList) {
            tempList.add(position, item);
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }

    private void updateUnshuffledList(int position, ArrayList<? extends IMediaItemBase> itemList) {
        ArrayList<IMediaItemBase> tempList = null;
        try {
            List list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, UNSHUFFLE, null), MediaItem[].class));
            tempList = new ArrayList<>(list);
        }catch (JsonSyntaxException e){

        }catch (NullPointerException e){

        }

        if(null != tempList) {
            tempList.addAll(position, itemList);
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }

    private void clearUpNextSavedList(){
        Preferences.writeString(context, SHUFFLED, null);
        Preferences.writeString(context, UNSHUFFLE, null);
    }

    public void removeItem(int itemPosition) {
        if(mUpNextList.size() > itemPosition) {
            IMediaItemBase item = mUpNextList.remove(itemPosition);
            if(mShuffle == SHUFFLE.all) {
                removeItemFromUnShuffledList(item.getItemId());
            }
        }
    }

    private void removeItemFromUnShuffledList(long deletedId) {
        ArrayList<IMediaItemBase> tempList = retrieveUpNextList(UNSHUFFLE);
        if(null != tempList) {
            for (IMediaItemBase item : tempList) {
                if (deletedId == item.getItemId()) {
                    tempList.remove(item);
                    break;
                }
            }
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }

    public void setNewItemAsPlayingItem(int position){
        if(mPlayingItemIndex == position && !App.getPlayerEventHandler().isStopped()){
            PlayPause();
        }else if(mPlayingItemIndex == position && !App.getPlayerEventHandler().isStopped()){
            PlayingItemChanged();
        }else{
            insertToHistory(getPlayingItem());
            mPlayingItemIndex = position;
            PlayingItemChanged();
        }
    }

    public void addItemAsUpNext(IMediaItemBase item){
        if(mShuffle == SHUFFLE.all) {
            updateUnshuffledList(mUpNextList.size() - 1, item);
        }
        mUpNextList.add(item);
    }

    public void addItemAsUpNext(ArrayList<? extends IMediaItemBase> itemList){
        if(mShuffle == SHUFFLE.all) {
            Collections.shuffle(itemList, new Random(itemList.size()));
            updateUnshuffledList(mUpNextList.size() - 1, itemList);
        }
        mUpNextList.addAll(itemList);
    }

    public void addItemAsPlayNext(IMediaItemBase item) {
        if (mShuffle == SHUFFLE.all) {
            updateUnshuffledList(getPlayNextPosition(), item);
        }
        mUpNextList.add(getPlayNextPosition(), item);
    }

    public void addItemAsPlayNext(ArrayList<? extends IMediaItemBase> itemList){
        if(mShuffle == SHUFFLE.all) {
            updateUnshuffledList(getPlayNextPosition(), itemList);
        }
        mUpNextList.addAll(getPlayNextPosition(), itemList);
    }

    public int getPlayNextPosition() {
        return getUpNextItemCount() <= 0 ? 0 : mPlayingItemIndex + 1;
    }

    public void setNextPlayingItem(final boolean isUser) {
        long mTime = System.currentTimeMillis();
        if(isNext() && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    if (mRepeat == REPEAT.all) {
                        if (mPlayingItemIndex < (mUpNextList.size() - 1)) {
                            mPlayingItemIndex++;
                        } else {
                            mPlayingItemIndex = 0;
                        }
                    } else if ((mRepeat == REPEAT.one && isUser) || mRepeat == REPEAT.none) {
                        if (mPlayingItemIndex < (mUpNextList.size() - 1)) {
                            mPlayingItemIndex++;
                        }
                    }
                    PlayingItemChanged();
                }
            }, 200);
        }else{
            context.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_LAST_PLAYED_SONG));
        }
    }

    public void setPreviousPlayingItem() {
        long mTime = System.currentTimeMillis();
        if(isPrevious() && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    if (mRepeat == REPEAT.all) {
                        if (mPlayingItemIndex > 0) {
                            mPlayingItemIndex--;
                        } else {
                            mPlayingItemIndex = mUpNextList.size() - 1;
                        }
                    } else {
                        if (mPlayingItemIndex > 0) {
                            mPlayingItemIndex--;
                        }
                    }
                    PlayingItemChanged();
                }
            }, 200);
        }
    }

    public void addItemToPlay(final IMediaItemBase item){
        long mTime = System.currentTimeMillis();
        if(null != mUpNextList && null != item && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mUpNextList.clear();
                    mUpNextList.add(item);
                    mPlayingItemIndex = 0;
                    PlayingItemChanged();
                }
            }, 100);
        }
    }

    public void addItemListToPlay(final ArrayList<? extends IMediaItemBase> itemList, final int position){
        long mTime = System.currentTimeMillis();
        if(null != mUpNextList && null != itemList && itemList.size() > 0 && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    clearUpNext();
                    mUpNextList.addAll(itemList);
                    if(mShuffle == SHUFFLE.all){
                        insertUpNextList(UNSHUFFLE);
                        Collections.shuffle(itemList, new Random(itemList.size()));
                    }
                    mPlayingItemIndex = position;
                    PlayingItemChanged();
                }
            }, 100);
        }
    }

    public void fetchSavedUpNextItems() {
        updateRepeatShuffleOnAppStart();
        mUpNextList = retrieveUpNextList(mShuffle == SHUFFLE.all ? SHUFFLED : UNSHUFFLE);
        nullCheck();
        retrievePlayingItemIndex();
    }

    private void retrievePlayingItemIndex() {
        mPlayingItemIndex = Preferences.readInteger(context, PLAYING_ITEM_INDEX_IN_UPNEXT, -1);
    }

    private ArrayList<IMediaItemBase> retrieveUpNextList(String shuffleType) {
        try {
            List list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, shuffleType, null), MediaItem[].class));
            return new ArrayList<>(list);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e){

        }
        return null;
    }

    public void SaveUpNextItems() {
        Log.d("mPlayingItemIndex", mPlayingItemIndex+"");
        if(mShuffle == SHUFFLE.all) {
            insertUpNextList(SHUFFLED);
        }else{
            insertUpNextList(UNSHUFFLE);
            Preferences.writeString(context, SHUFFLED, null);
        }
        savePlayingItemIndex();
    }

    private void savePlayingItemIndex() {
        Preferences.writeInteger(context, PLAYING_ITEM_INDEX_IN_UPNEXT, mPlayingItemIndex);
    }

    public void Terminate() {

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
}