package com.player.player;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AbsListView;

import com.player.myspotifymusic.R;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 19-09-16.
 */
public class AudioListActivity extends Activity {

    private PermissionChecker permissionChecker;
    private SongListAdapter songListAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_activity_audio_list);

        recyclerView = (RecyclerView)findViewById(R.id.albumsListContainer);

        checkPermissions();

    }

    private void checkPermissions() {
        permissionChecker = new PermissionChecker(this, this, findViewById(R.id.albumsListContainer));
        permissionChecker.check(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                getResources().getString(R.string.storage_permission),
                new PermissionChecker.OnPermissionResponse() {
                    @Override
                    public void onAccepted() {
                        setSongList();
                    }

                    @Override
                    public void onDecline() {
                        AudioListActivity.this.finish();
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void setPermissionChecker(PermissionChecker permissionChecker) {
        this.permissionChecker = permissionChecker;
    }

    private void setSongList() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ArrayList<MediaItem> songList = getSongList(AudioListActivity.this);
                final LinearLayoutManager llm = new LinearLayoutManager(AudioListActivity.this);
                 runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(llm);
                        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(AudioListActivity.this, 0));
                        recyclerView.setHasFixedSize(true);
                        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                            @Override
                            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                                super.onScrolled(recyclerView, dx, dy);
                                songListAdapter.recyclerScrolled();
                            }

                            @Override
                            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                super.onScrollStateChanged(recyclerView, newState);

                                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                                    // Do something
                                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                                    // Do something
                                } else {
                                    // Do something
                                }
                            }
                        });
                        songListAdapter = new SongListAdapter(AudioListActivity.this, AudioListActivity.this, songList);
                        recyclerView.setAdapter(songListAdapter);

                        songListAdapter.setOnItemClickListener(new SongListAdapter.OnItemClickListener() {
                            @Override
                            public void onItemClick(View view, int position) {
                                Intent returnIntent = getIntent();
                                returnIntent.putExtra("mediaItem",songList.get(position));
                                AudioListActivity.this.setResult(Activity.RESULT_OK, returnIntent);
                                AudioListActivity.this.finish();
                            }
                        });
                    }
                });
            }
        }).start();
    }

    public static ArrayList<MediaItem> getSongList(Context context){
        ArrayList<MediaItem> songList = new ArrayList<>();

        final String where = MediaStore.Audio.Media.IS_MUSIC + "=1";

        final String orderBy = MediaStore.Audio.Media.TITLE;
        Cursor songListCursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                null, where, null, orderBy);
        if (songListCursor != null && songListCursor.moveToFirst()) {

            int Song_Id_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media._ID);

            int Song_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.TITLE);

            int Song_Display_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DISPLAY_NAME);

            int Song_Path_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATA);

            int Album_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM_ID);

            int Album_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ALBUM);

            int Artist_ID_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST_ID);

            int Artist_Name_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.ARTIST);

            int Duration_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DURATION);

            int Date_Added_Column = songListCursor.getColumnIndex
                    (MediaStore.Audio.Media.DATE_ADDED);

            do{
                songList.add(new MediaItem(songListCursor.getLong(Song_Id_Column), songListCursor.getString(Song_Name_Column), songListCursor.getString(Song_Display_Name_Column), songListCursor.getString(Song_Path_Column), songListCursor.getLong(Album_ID_Column), songListCursor.getString(Album_Name_Column), songListCursor.getLong(Artist_ID_Column), songListCursor.getString(Artist_Name_Column), songListCursor.getLong(Duration_Column), songListCursor.getLong(Date_Added_Column), getAlbumArtByAlbum(context, songListCursor.getString(Album_Name_Column))));
            }while (songListCursor.moveToNext());

        }
        if (songListCursor != null) {
            songListCursor.close();
        }
        return songList;
    }

    public static String getAlbumArtByAlbum(Context context, String album) {
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Audio.Albums.ALBUM_ART},
                MediaStore.Audio.Albums.ALBUM + "=?",
                new String[]{String.valueOf(album)},
                null);
        String str = null;
        if (cursor != null && cursor.moveToFirst()) {
            str = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART));
        }

        if (cursor != null) {
            cursor.close();
        }
        return str;
    }
}
