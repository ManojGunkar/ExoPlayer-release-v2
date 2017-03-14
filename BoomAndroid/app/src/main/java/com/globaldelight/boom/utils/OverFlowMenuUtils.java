package com.globaldelight.boom.utils;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.App;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.manager.PlayerServiceReceiver;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 08-03-17.
 */

public class OverFlowMenuUtils {
    public static void setDefaultPlaylistMenu(final Activity activity, View view, final IMediaItemBase itemBase) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_recent_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(MediaController.getInstance(activity).getPlayListTrackList((IMediaItemCollection) itemBase));
                            break;
                        case R.id.popup_recent_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(MediaController.getInstance(activity).getPlayListTrackList((IMediaItemCollection) itemBase));
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        pm.show();
    }

    public static void setBoomPlaylistMenu(final Activity activity, View view, final IMediaItemBase itemBase) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.popup_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(MediaController.getInstance(activity).getBoomPlayListTrackList(itemBase.getItemId()));
                            break;
                        case R.id.popup_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(MediaController.getInstance(activity).getBoomPlayListTrackList(itemBase.getItemId()));
                            break;
                        case R.id.popup_playlist_rename:
                            renameDialog(activity, itemBase.getItemTitle(), itemBase.getItemId());
                            break;
                        case R.id.popup_playlist_delete:
                            MediaController.getInstance(activity).deleteBoomPlaylist(itemBase.getItemId());
                            Toast.makeText(activity, activity.getResources().getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }catch (Exception e){}
                return false;
            }
        });
        pm.inflate(R.menu.playlist_boom_menu);
        pm.show();
    }

    public static void setFavouriteMenu(final Activity activity, View view) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_recent_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(MediaController.getInstance(activity).getFavoriteList());
                            break;
                        case R.id.popup_recent_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(MediaController.getInstance(activity).getFavoriteList());
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        pm.show();
    }

    public static void setRecentPlayedMenu(final Activity activity, View view) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_recent_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(MediaController.getInstance(activity).getRecentPlayedList());
                            break;
                        case R.id.popup_recent_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(MediaController.getInstance(activity).getRecentPlayedList());
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        pm.show();
    }

    private static void renameDialog(final Activity activity, final String itemTitle, final long itemId) {
        new MaterialDialog.Builder(activity)
                .title(R.string.dialog_txt_rename)
                .backgroundColor(ContextCompat.getColor(activity, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(activity, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(activity, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(activity, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(activity, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(activity, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .cancelable(true)
                .positiveText(activity.getResources().getString(R.string.dialog_txt_done))
                .negativeText(activity.getResources().getString(R.string.dialog_txt_cancel))
                .input(null, itemTitle, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches("")) {
                            renameDialog(activity, itemTitle, itemId);
                        } else {
                            MediaController.getInstance(activity).renamePlaylist(input.toString(), itemId);
                        }
                    }
                }).show();
    }

    public static void setBoomPlayListItemMenu(final Activity activity, View anchorView, final long itemId, final IMediaItemBase itemBase) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_song_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(itemBase);
                            break;
                        case R.id.popup_song_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(itemBase);
                            break;
                        case R.id.popup_song_add_playlist:
                            Utils util = new Utils(activity);
                            ArrayList list = new ArrayList();
                            list.add(itemBase);
                            util.addToPlaylist(activity, list, null);
                            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                            break;
                        case R.id.popup_song_add_fav:
                            if (MediaController.getInstance(activity).isFavoriteItem(itemBase.getItemId())) {
                                MediaController.getInstance(activity).removeItemToFavoriteList(itemBase.getItemId());
                                Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                            } else {
                                MediaController.getInstance(activity).addItemToFavoriteList((IMediaItem) itemBase);
                                Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case R.id.boom_item_delete:
                            MediaController.getInstance(activity).removeSongToPlayList(itemBase.getItemId(), (int) itemId);
                            break;
                    }
                }catch (Exception e){

                }
                return false;
            }
        });
        if (MediaController.getInstance(activity).isFavoriteItem(itemBase.getItemId())) {
            pm.inflate(R.menu.boomplaylist_remove_fav_item_menu);
        } else {
            pm.inflate(R.menu.boomplaylist_add_fav_item_menu);
        }
        pm.show();
    }

    public static void setPlayListItemMenu(final Activity activity, View anchorView, final IMediaItemBase itemBase) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_song_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(itemBase);
                            break;
                        case R.id.popup_song_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(itemBase);
                            break;
                        case R.id.popup_song_add_playlist:
                            Utils util = new Utils(activity);
                            ArrayList list = new ArrayList();
                            list.add(itemBase);
                            util.addToPlaylist(activity, list, null);
                            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                            break;
                    }
                }catch (Exception e){

                }
                return false;
            }
        });
        if (MediaController.getInstance(activity).isFavoriteItem(itemBase.getItemId())) {
            pm.inflate(R.menu.song_remove_fav);
        } else {
            pm.inflate(R.menu.song_add_fav);
        }
        pm.show();
    }

    public static void setArtistGenreSongItemMenu(final Activity activity, View anchorView, final IMediaItemBase itemBase) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.popup_song_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(itemBase);
                            break;
                        case R.id.popup_song_add_queue:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(itemBase);
                            break;
                        case R.id.popup_song_add_playlist:
                            Utils util = new Utils(activity);
                            ArrayList list = new ArrayList();
                            list.add(itemBase);
                            util.addToPlaylist(activity, list, null);
                            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                            break;
                        case R.id.popup_song_add_fav:
                            if (MediaController.getInstance(activity).isFavoriteItem(itemBase.getItemId())) {
                                MediaController.getInstance(activity).removeItemToFavoriteList(itemBase.getItemId());
                                Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                            } else {
                                MediaController.getInstance(activity).addItemToFavoriteList((IMediaItem) itemBase);
                                Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                            }
                            break;
                    }
                }catch (Exception e){

                }
                return false;
            }
        });
        if (MediaController.getInstance(activity).isFavoriteItem(itemBase.getItemId())) {
            pm.inflate(R.menu.song_remove_fav);
        } else {
            pm.inflate(R.menu.song_add_fav);
        }
        pm.show();
    }

    public static void setBoomPlayListHeaderMenu(final Activity activity, View anchorView, final long itemId, final String itemTitle, final ArrayList<? extends IMediaItemBase> mediaElement) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.album_header_add_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_upnext:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_playlist:
                            Utils util = new Utils(activity);
                            util.addToPlaylist(activity, mediaElement, null);
                            break;
                        case R.id.album_header_shuffle:
                            App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(mediaElement, 0);
                            activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                            break;
                        case R.id.popup_playlist_rename:
                            renameDialog(activity, itemTitle, itemId);
                            break;
                        case R.id.popup_playlist_delete:
                            MediaController.getInstance(activity).deleteBoomPlaylist(itemId);
                            Toast.makeText(activity, activity.getResources().getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }catch (Exception E){
                    Log.e("Error : ", E.getMessage());
                }
                return false;
            }
        });
        pm.inflate(R.menu.boomplaylist_header_menu);
        pm.show();
    }

    public static void setPlayListHeaderMenu(final Activity activity, View anchorView, final ArrayList<? extends IMediaItemBase> mediaElement) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.album_header_add_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_upnext:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_playlist:
                            Utils util = new Utils(activity);
                            util.addToPlaylist(activity, mediaElement, null);
                            break;
                        case R.id.album_header_shuffle:
                            App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(mediaElement, 0);
                            activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                            break;
                    }
                }catch (Exception E){
                    Log.e("Error : ", E.getMessage());
                }
                return false;
            }
        });
        pm.inflate(R.menu.album_header_menu);
        pm.show();
    }

    public static void setArtistGenreSongHeaderMenu(final Activity activity, View anchorView, final ArrayList<? extends IMediaItemBase> mediaElement) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.album_header_add_play_next:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsPlayNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_upnext:
                            App.getPlayingQueueHandler().getUpNextList().addItemAsUpNext(mediaElement);
                            break;
                        case R.id.album_header_add_to_playlist:
                            Utils util = new Utils(activity);
                            util.addToPlaylist(activity, mediaElement, null);
                            break;
                        case R.id.album_header_shuffle:
                            App.getPlayingQueueHandler().getUpNextList().addItemListToPlay(mediaElement, 0);
                            activity.sendBroadcast(new Intent(PlayerServiceReceiver.ACTION_SHUFFLE_SONG));
                            break;
                    }
                }catch (Exception E){
                    Log.e("Error : ", E.getMessage());
                }
                return false;
            }
        });
        pm.inflate(R.menu.album_header_menu);
        pm.show();
    }
}