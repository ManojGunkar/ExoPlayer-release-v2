package com.globaldelight.boom.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.base.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 08-03-17.
 */

public class OverFlowMenuUtils {

    private static boolean incompatiblePlayingItem() {
        IMediaElement playingItem = App.playbackManager().queue().getPlayingItem();
        return ( playingItem != null && (playingItem.getMediaType() == MediaType.RADIO || playingItem.getMediaType() == MediaType.TIDAL) );
    }


    public static void setDefaultPlaylistMenu(final Activity activity, View view, final IMediaElement itemBase) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.collection_play_next_item:
                            App.playbackManager().queue().addItemAsPlayNext(MediaController.getInstance(activity).getPlayListTrackList((IMediaItemCollection) itemBase));
                            break;
                        case R.id.collection_add_to_queue_item:
                            App.playbackManager().queue().addItemAsUpNext(MediaController.getInstance(activity).getPlayListTrackList((IMediaItemCollection) itemBase));
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.collection_play_next_item);
            pm.getMenu().removeItem(R.id.collection_add_to_queue_item);
        }
        pm.show();
    }

    public static void setBoomPlaylistMenu(final Activity activity, View view, final IMediaElement itemBase) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.popup_play_next:
                            App.playbackManager().queue().addItemAsPlayNext(MediaController.getInstance(activity).getBoomPlayListTrackList(itemBase));
                            break;
                        case R.id.popup_add_queue:
                            App.playbackManager().queue().addItemAsUpNext(MediaController.getInstance(activity).getBoomPlayListTrackList(itemBase));
                            break;
                        case R.id.popup_playlist_rename:
                            renameDialog(activity, itemBase);
                            FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Playlist_Edit_Button_Tapped);

                            break;
                        case R.id.popup_playlist_delete:
                            deletePlaylistDialog(activity, itemBase);
                            FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.Playlist_Deleted);
                            break;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                return false;
            }
        });
        pm.inflate(R.menu.playlist_boom_menu);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.popup_play_next);
            pm.getMenu().removeItem(R.id.popup_add_queue);
        }
        pm.show();
    }

    private static void deletePlaylistDialog(final Activity activity, final IMediaElement item) {
        String content = activity.getResources().getString(R.string.delete_dialog_txt, item.getTitle());
        Utils.createDialogBuilder(activity).title(R.string.delete_dialog_title)
                .content(content)
                .positiveText(activity.getResources().getString(R.string.ok))
                .negativeText(activity.getResources().getString(R.string.dialog_txt_cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                       @Override
                       public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                           MediaController.getInstance(activity).deleteBoomPlaylist(item);
                           Toast.makeText(activity, activity.getResources().getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
                       }
                   }).show();
    }

    public static void setFavouriteMenu(final Activity activity, View view) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.collection_play_next_item:
                            App.playbackManager().queue().addItemAsPlayNext(MediaController.getInstance(activity).getFavoriteList());
                            break;
                        case R.id.collection_add_to_queue_item:
                            App.playbackManager().queue().addItemAsUpNext(MediaController.getInstance(activity).getFavoriteList());
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.collection_play_next_item);
            pm.getMenu().removeItem(R.id.collection_add_to_queue_item);
        }
        pm.show();
    }

    public static void setRecentPlayedMenu(final Activity activity, View view) {
        PopupMenu pm = new PopupMenu(activity, view);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                try {
                    switch (item.getItemId()) {
                        case R.id.collection_play_next_item:
                            App.playbackManager().queue().addItemAsPlayNext(MediaController.getInstance(activity).getRecentPlayedList());
                            break;
                        case R.id.collection_add_to_queue_item:
                            App.playbackManager().queue().addItemAsUpNext(MediaController.getInstance(activity).getRecentPlayedList());
                            break;
                    }
                }catch (Exception e){ }
                return false;
            }
        });
        pm.inflate(R.menu.recent_popup);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.collection_play_next_item);
            pm.getMenu().removeItem(R.id.collection_add_to_queue_item);
        }
        pm.show();
    }

    private static void renameDialog(final Activity activity, final IMediaElement item) {
        Utils.createDialogBuilder(activity)
                .title(R.string.rename)
                .cancelable(true)
                .positiveText(activity.getResources().getString(R.string.done))
                .negativeText(activity.getResources().getString(R.string.dialog_txt_cancel))
                .input(null, item.getTitle(), new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (input.toString().matches("")) {
                            renameDialog(activity, item);
                        } else {
                            MediaController.getInstance(activity).renamePlaylist(input.toString(), item);
                        }
                    }
                }).show();
    }

    private static void updateFavoritesMenuItem(Context context, IMediaElement itemBase, PopupMenu popup) {
        MenuItem favItem = popup.getMenu().findItem(R.id.song_add_fav_item);
        if ( favItem != null ) {
            if ( MediaController.getInstance(context).isFavoriteItem(itemBase) ) {
                favItem.setTitle(R.string.menu_remove_boom_fav);
            }
            else {
                favItem.setTitle(R.string.menu_add_boom_fav);
            }
        }
    }

    private static void onSongMenuItemClicked(Activity activity, int itemId, IMediaElement item) {
        switch (itemId) {
            case R.id.song_play_next_item:
                App.playbackManager().queue().addItemAsPlayNext(item);
                break;
            case R.id.song_add_queue_item:
                App.playbackManager().queue().addItemAsUpNext(item);
                break;
            case R.id.song_add_playlist_item:
                ArrayList list = new ArrayList();
                list.add(item);
                Utils.addToPlaylist((Activity) activity, list, null);
//                            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);
                FlurryAnalytics.getInstance(activity.getApplicationContext()).setEvent(FlurryEvents.EVENT_ADD_ITEMS_TO_PLAYLIST_FROM_LIBRARY);

                break;
            case R.id.song_add_fav_item:
                if (MediaController.getInstance(activity).isFavoriteItem(item)) {
                    MediaController.getInstance(activity).removeItemToFavoriteList(item);
                    Toast.makeText(activity, activity.getResources().getString(R.string.removed_from_favorite), Toast.LENGTH_SHORT).show();
                } else {
                    MediaController.getInstance(activity).addItemToFavoriteList((IMediaItem) item);
                    Toast.makeText(activity, activity.getResources().getString(R.string.added_to_favorite), Toast.LENGTH_SHORT).show();
                }
                break;

        }
    }

    public static void showPlaylistItemMenu(final Activity activity, View anchorView, int resId, final IMediaElement itemBase, final IMediaElement playlist) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    default:
                        onSongMenuItemClicked(activity, item.getItemId(), itemBase);
                        break;
                    case R.id.song_delete_item:
                        MediaController.getInstance(activity).removeSongToPlayList(itemBase, playlist);
                        break;

                }
                return false;
            }
        });

        pm.inflate(resId);
        updateFavoritesMenuItem(activity, itemBase, pm);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.song_play_next_item);
            pm.getMenu().removeItem(R.id.song_add_queue_item);
        }
        pm.show();
    }


    public static void showMediaItemMenu(final Activity activity, View anchorView, int resId, final IMediaElement itemBase ) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onSongMenuItemClicked(activity, item.getItemId(), itemBase);
                return false;
            }
        });

        pm.inflate(resId);
        updateFavoritesMenuItem(activity, itemBase, pm);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.song_play_next_item);
            pm.getMenu().removeItem(R.id.song_add_queue_item);
        }
        pm.show();
    }

    public static void showCollectionMenu(final Activity activity, View anchorView, int resId, final IMediaItemCollection collection) {
        PopupMenu pm = new PopupMenu(activity, anchorView);
        pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                try {
                    switch (menuItem.getItemId()) {
                        case R.id.collection_play_next_item:
                            App.playbackManager().queue().addItemAsPlayNext(collection);
                            break;

                        case R.id.collection_add_to_queue_item:
                            App.playbackManager().queue().addItemAsUpNext(collection);
                            break;

                        case R.id.collection_add_to_playlist_item:
                            Utils.addToPlaylist(activity, collection, null);
                            break;

                        case R.id.collection_shuffle_item:
                            App.playbackManager().queue().addItemListToPlay(collection, 0, true);
                            break;

                        case R.id.playlist_rename_item:
                            renameDialog(activity, collection);
                            break;

                        case R.id.playlist_delete_item:
                            MediaController.getInstance(activity).deleteBoomPlaylist(collection);
                            Toast.makeText(activity, activity.getResources().getString(R.string.playlist_deleted), Toast.LENGTH_SHORT).show();
                            break;
                    }
                }catch (Exception E){
                    Log.e("Error : ", E.getMessage());
                }
                return false;
            }
        });
        pm.inflate(resId);
        if ( incompatiblePlayingItem() ) {
            pm.getMenu().removeItem(R.id.collection_play_next_item);
            pm.getMenu().removeItem(R.id.collection_add_to_queue_item);
        }
        pm.show();
    }



}