package com.globaldelight.boom.playbackEvent.handler;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.IntDef;

import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.app.sharedPreferences.Preferences;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.playbackEvent.controller.callbacks.IUpNextMediaEvent;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static com.globaldelight.boom.app.sharedPreferences.Preferences.PLAYING_ITEM_INDEX_IN_UPNEXT;

/**
 * Created by Rahul Agarwal on 01-03-17.
 */

public class UpNextPlayingQueue {
    
    @IntDef({REPEAT_ONE, REPEAT_ALL, REPEAT_NONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    public static final int REPEAT_NONE = 0;
    public static final int REPEAT_ONE = 1;
    public static final int REPEAT_ALL = 2;


    @IntDef({SHUFFLE_OFF, SHUFFLE_ON})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShuffleMode {
    }


    private static final String QUEUE_TYPE_KEY = "queue_type";

    public static final int SHUFFLE_OFF = 0;
    public static final int SHUFFLE_ON = 1;


    private Context context;
    private int mPlayingItemIndex = -1;
    private long mShiftingTime = 0;
    private IUpNextMediaEvent mUpNextMediaEvent = null;

    Handler eventHandler = new Handler();

    List<IMediaElement> mUpNextList;


    private static
    @ShuffleMode
    int mShuffle = SHUFFLE_OFF;
    private static
    @RepeatMode
    int mRepeat = REPEAT_NONE;

    private static final String SHUFFLED = "shuffle";
    private static final String UNSHUFFLE = "unshuffle";

    public UpNextPlayingQueue(Context context) {
        this.context = context;
        mUpNextList = new ArrayList<>();
    }

    public void setUpNextMediaEvent(IUpNextMediaEvent event) {
        this.mUpNextMediaEvent = event;
    }


    public void clearUpNext() {
        mUpNextList.clear();
        clearUpNextSavedList();
    }

    public ArrayList<? extends IMediaElement> getHistoryList() {
        return MediaController.getInstance(context).getRecentPlayedList();
    }

    private void insertToHistory(IMediaElement itemBase) {
        if (null != itemBase)
            MediaController.getInstance(context).setRecentPlayedItem(itemBase);
    }

    public int getPlayingItemIndex() {
        return mPlayingItemIndex;
    }

    public void setPlayingItemIndex(int index) {
        this.mPlayingItemIndex = index;
    }

    public IMediaElement getPlayingItem() {
        if (null != mUpNextList && mUpNextList.size() > 0 && mPlayingItemIndex >= 0 && mPlayingItemIndex < mUpNextList.size() )
            return mUpNextList.get(mPlayingItemIndex);
        return null;
    }

    public List<? extends IMediaElement> getUpNextItemList() {
        return mUpNextList;
    }

    public int getUpNextItemCount() {
        if (null != mUpNextList)
            return mUpNextList.size();
        return 0;
    }

    /**************************************************************************************************************************/
//    Callbacks to update UI
    public void PlayPause() {
        if (null != mUpNextMediaEvent) {
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
        if (null != mUpNextMediaEvent) {
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
        if (null != mUpNextMediaEvent) {
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
        if (getUpNextItemCount() > 0) {
            if (mRepeat == REPEAT_ALL)
                return mUpNextList.size() > 0 ? true : false;
            return mPlayingItemIndex > 0 ? true : false;
        }
        return false;
    }

    public boolean isNext() {
        if (getUpNextItemCount() > 0) {
            if (mRepeat == REPEAT_ALL)
                return mUpNextList.size() > 0 ? true : false;
            return mPlayingItemIndex < (mUpNextList.size() - 1) ? true : false;
        }
        return false;
    }

    public void getRepeatShuffleOnAppStart() {
        mShuffle = App.getUserPreferenceHandler().getShuffle();
        mRepeat = App.getUserPreferenceHandler().getRepeat();
    }

    public boolean resetShuffle() {
        mShuffle = App.getUserPreferenceHandler().resetShuffle();
        updateShuffleList();
        try {
            if (mShuffle == UpNextPlayingQueue.SHUFFLE_ON)
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_SHUFFLE_ON_PLAYING);
            else
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_SHUFFLE_OFF_PLAYING);
        } catch (Exception e) {
        }
        return true;
    }

    public boolean resetRepeat() {
        mRepeat = App.getUserPreferenceHandler().resetRepeat();
        try {
            if (mRepeat == UpNextPlayingQueue.REPEAT_ONE)
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_REPEAT_ONE_PLAYING);
            else if (mRepeat == UpNextPlayingQueue.REPEAT_ALL)
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_REPEAT_ALL_PLAYING);
            else
                FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_REPEAT_NONE_PLAYING);

        } catch (Exception e) {
        }
        return true;
    }

    public void updateShuffleList() {
        if (mUpNextList.size() > 0) {
            IMediaElement playingItem = getPlayingItem();
            if (mShuffle == UpNextPlayingQueue.SHUFFLE_ON) {
                insertUpNextList(UNSHUFFLE);
                mUpNextList.remove(getPlayingItemIndex());
                Collections.shuffle(mUpNextList, new Random(System.currentTimeMillis()));
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
        Preferences.writeInteger(context, QUEUE_TYPE_KEY, getPlayingItem().getMediaType());

    }

    private void retrieveUpNextList(String shuffleType, IMediaElement playingItem) {
        try {
            mUpNextList = fetchSavedItems(shuffleType);
            if ((null != mUpNextList || !mUpNextList.isEmpty()) && mUpNextList.size() > 0) {
                if (mUpNextList.size() == 1)
                    mPlayingItemIndex = 0;
                else {
                    int count = 0;
                    for (IMediaElement item : mUpNextList) {
                        if ( playingItem.equalTo(item) )
                            mPlayingItemIndex = count;
                        count++;
                    }
                }
            } else {
                mPlayingItemIndex = -1;
                nullCheck();
            }
        } catch (JsonSyntaxException e) {

        } catch (NullPointerException e) {

        }
    }

    public boolean repeatSong() {
        return mRepeat == REPEAT_ONE;
    }

    private void nullCheck() {
        if (null == mUpNextList)
            mUpNextList = new ArrayList<>();
    }

    private void updateUnshuffledList(int position, IMediaElement item) {
        ArrayList<IMediaElement> tempList = null;
        try {
            tempList = fetchSavedItems(UNSHUFFLE);
        } catch (JsonSyntaxException e) {

        } catch (NullPointerException e) {

        }

        if (null != tempList) {
            tempList.add(position, item);
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }

    private void updateUnshuffledList(int position, ArrayList<? extends IMediaElement> itemList) {
        ArrayList<IMediaElement> tempList = null;
        try {
            tempList = fetchSavedItems(UNSHUFFLE);
        } catch (JsonSyntaxException e) {

        } catch (NullPointerException e) {

        }

        if (null != tempList) {
            tempList.addAll(position, itemList);
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }


    private void clearUpNextSavedList() {
        Preferences.writeString(context, SHUFFLED, null);
        Preferences.writeString(context, UNSHUFFLE, null);
    }

    public void removeItem(int itemPosition) {
        if (mUpNextList.size() > itemPosition) {
            IMediaElement item = mUpNextList.remove(itemPosition);
            if (mShuffle == SHUFFLE_ON) {
                removeItemFromUnShuffledList(item);
            }
        }
    }

    private void removeItemFromUnShuffledList(IMediaElement deleted) {
        ArrayList<IMediaElement> tempList = retrieveUpNextList(UNSHUFFLE);
        if (null != tempList) {
            for (IMediaElement item : tempList) {
                if (deleted.equalTo(item)) {
                    tempList.remove(item);
                    break;
                }
            }
            Preferences.writeString(context, UNSHUFFLE, new Gson().toJson(tempList));
        }
    }

    public void setNewItemAsPlayingItem(int position) {
        if (mPlayingItemIndex == position && !App.playbackManager().isStopped()) {
            PlayPause();
        } else {
            insertToHistory(getPlayingItem());
            mPlayingItemIndex = position;
            PlayingItemChanged();
        }
    }

    public void addItemAsUpNext(IMediaElement item) {
        if (mShuffle == SHUFFLE_ON) {
            updateUnshuffledList(mUpNextList.size() - 1, item);
        }
        mUpNextList.add(item);
    }

    public void addItemAsUpNext(ArrayList<? extends IMediaElement> itemList) {
        if (mShuffle == SHUFFLE_ON) {
            updateUnshuffledList(mUpNextList.size() - 1, itemList);
        }
        mUpNextList.addAll(itemList);
    }

    public void addItemAsUpNext(IMediaItemCollection collection) {
        addItemAsUpNext(collection.getMediaElement());
    }

    public void addItemAsPlayNext(IMediaElement item) {
        if (mShuffle == SHUFFLE_ON) {
            updateUnshuffledList(getPlayNextPosition(), item);
        }
        mUpNextList.add(getPlayNextPosition(), item);
    }

    public void addItemAsPlayNext(ArrayList<? extends IMediaElement> itemList) {
        if (mShuffle == SHUFFLE_ON) {
            updateUnshuffledList(getPlayNextPosition(), itemList);
        }
        mUpNextList.addAll(getPlayNextPosition(), itemList);
    }

    public void addItemAsPlayNext(IMediaItemCollection collection) {
        addItemAsPlayNext(collection.getMediaElement());
    }

    public int getPlayNextPosition() {
        return getUpNextItemCount() <= 0 ? 0 : mPlayingItemIndex + 1;
    }

    public void setNextPlayingItem(final boolean isUser) {
        long mTime = System.currentTimeMillis();
        if ( (isNext() || repeatSong()) && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    if (mRepeat == REPEAT_ALL) {
                        if (mPlayingItemIndex < (mUpNextList.size() - 1)) {
                            mPlayingItemIndex++;
                        } else {
                            mPlayingItemIndex = 0;
                        }
                    } else if ((mRepeat == REPEAT_ONE && isUser) || mRepeat == REPEAT_NONE) {
                        if (mPlayingItemIndex < (mUpNextList.size() - 1)) {
                            mPlayingItemIndex++;
                        }
                    }
                    PlayingItemChanged();
                }
            });
        }
    }

    public void setPreviousPlayingItem() {
        long mTime = System.currentTimeMillis();
        if (isPrevious() && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    if (mRepeat == REPEAT_ALL) {
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
            });
        }
    }

    public void addItemToPlay(final IMediaElement item) {
        long mTime = System.currentTimeMillis();
        boolean isPlayPause = App.playbackManager().getPlayerDataSourceId() != null && App.playbackManager().getPlayerDataSourceId().equals(item.getId()) ? true : false;
        if (null != item && null != getPlayingItem() && item.equalTo(getPlayingItem()) && isPlayPause) {
            PlayPause();
        } else if (null != mUpNextList && null != item && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    mUpNextList.clear();
                    mUpNextList.add(item);
                    mPlayingItemIndex = 0;
                    PlayingItemChanged();
                }
            });
        }
    }

    public void addItemListToPlay(final List<? extends IMediaElement> itemList, final int position, final boolean shuffle) {
        long mTime = System.currentTimeMillis();
        String dsId = App.playbackManager().getPlayerDataSourceId();
        boolean isPlayPause = !shuffle && dsId != null && dsId.equals(itemList.get(position).getId()) ? true : false;
        if (null != itemList && null != getPlayingItem() && itemList.get(position).equalTo(getPlayingItem()) && isPlayPause) {
            PlayPause();
        } else if (null != mUpNextList && null != itemList && itemList.size() > 0 && mTime - mShiftingTime > 500) {
            mShiftingTime = mTime;
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    insertToHistory(getPlayingItem());
                    clearUpNext();
                    mUpNextList.addAll(itemList);
                    if ( shuffle ) {
                        Collections.shuffle(mUpNextList, new Random(System.currentTimeMillis()));
                    }
                    mPlayingItemIndex = position;
                    newShuffleList();
                    PlayingItemChanged();
                    SaveUpNextItems(true);
                }
            });
        }
    }

    public void addItemListToPlay(final ArrayList<? extends IMediaElement> itemList, final int position) {
        addItemListToPlay(itemList, position, false);
    }

    public void addItemListToPlay(IMediaItemCollection collection, int position, boolean shuffle) {
        addItemListToPlay(collection.getMediaElement(), position, shuffle);
    }

    public void addItemListToPlay(IMediaItemCollection collection, int position) {
        addItemListToPlay(collection.getMediaElement(), position);
    }


    private void newShuffleList() {
        if (mUpNextList.size() > 0) {
            IMediaElement playingItem = getPlayingItem();
            if (mShuffle == UpNextPlayingQueue.SHUFFLE_ON) {
                insertUpNextList(UNSHUFFLE);
                mUpNextList.remove(getPlayingItemIndex());
                Collections.shuffle(mUpNextList, new Random(System.currentTimeMillis()));
                mUpNextList.add(0, playingItem);
                mPlayingItemIndex = 0;
            }
        }
    }

    public void fetchSavedUpNextItems() {
        getRepeatShuffleOnAppStart();
        mUpNextList = retrieveUpNextList(mShuffle == SHUFFLE_ON ? SHUFFLED : UNSHUFFLE);
        nullCheck();
        retrievePlayingItemIndex();
    }

    private void retrievePlayingItemIndex() {
        mPlayingItemIndex = Preferences.readInteger(context, PLAYING_ITEM_INDEX_IN_UPNEXT, -1);
    }

    private ArrayList<IMediaElement> retrieveUpNextList(String shuffleType) {
        try {
            return fetchSavedItems(shuffleType);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
        return null;
    }

    public void SaveUpNextItems(boolean saveList) {
        savePlayingItemIndex();
        if ( saveList ) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (mShuffle == SHUFFLE_ON) {
                            insertUpNextList(SHUFFLED);
                        } else {
                            insertUpNextList(UNSHUFFLE);
                            Preferences.writeString(context, SHUFFLED, null);
                        }
                    }
                    catch (Exception e) {

                    }
                }
            }).start();
        }
    }

    private void savePlayingItemIndex() {
        Preferences.writeInteger(context, PLAYING_ITEM_INDEX_IN_UPNEXT, mPlayingItemIndex);
    }

    private ArrayList<IMediaElement> fetchSavedItems(String shuffleType) {
        try {
            List list = null;
            int queueType = Preferences.readInteger(context, QUEUE_TYPE_KEY, MediaType.DEVICE_MEDIA_LIB);
            switch (queueType) {
                case MediaType.RADIO:
                    list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, shuffleType, null), RadioStationResponse.Content[].class));
                    break;

                case MediaType.TIDAL:
                    list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, shuffleType, null), Item[].class));
                    break;

                default:
                    list = Arrays.asList(new Gson().fromJson(Preferences.readString(context, shuffleType, null), MediaItem[].class));
                    break;
            }

            return new ArrayList<>(list);
        }
        catch (Exception e) {
            return null;
        }
    }

}