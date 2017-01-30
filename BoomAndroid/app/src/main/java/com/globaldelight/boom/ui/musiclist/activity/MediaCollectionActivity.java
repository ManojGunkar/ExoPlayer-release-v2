package com.globaldelight.boom.ui.musiclist.activity;

import android.app.ActivityManager;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.AnimRes;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaCollection.IMediaItemCollection;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.ui.musiclist.fragment.ItemSongListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.MediaCollectionFragment;
import com.globaldelight.boom.ui.musiclist.fragment.MediaItemListFragment;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static android.R.anim.fade_in;
import static android.R.anim.fade_out;

/**
 * Created by Rahul Agarwal on 26-01-17.
 */

public class MediaCollectionActivity extends MasterActivity {
    private TableLayout tblAlbumArt;
    private ImageView albumArt,artImg1,artImg2,artImg3,artImg4;
    private static int currentItem = -1;
    private ActionBar actionBar;
    private CollapsingToolbarLayout appBarLayout;
    MediaCollectionFragment mCollectionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        MediaItem mMediaItem = getIntent().getParcelableExtra("media_item");
        switch (mMediaItem.getParentType()) {
            case SONGS:
            case FAVOURITE:
                setContentView(R.layout.activity_item_list);
                initToolBar();
                initItemViews(mMediaItem);
                break;
            default:
//            case ALBUM:
//            case ARTIST:
//            case PLAYLIST:
//            case GENRE:
//            case BOOM_PLAYLIST:
                setContentView(R.layout.activity_album_song_list);
                initCollapsingToolBar();
                initCollectionViews(mMediaItem);
                break;
        }
    }

    private void initCollapsingToolBar() {
        appBarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
    }

    private void initItemViews(MediaItem mMediaItem) {
        findViewById(R.id.fab).setVisibility(View.GONE);
        Bundle arguments = new Bundle();
        ItemSongListFragment mFragment;
        switch (mMediaItem.getParentType()){
            case SONGS:
                if (mMediaItem.getMediaType() == MediaType.DEVICE_MEDIA_LIB) {
                    fragmentSwitcher(new MediaItemListFragment(),  0, getResources().getString(R.string.songs), fade_in, fade_out);
                }else if (mMediaItem.getMediaType() == MediaType.DROP_BOX) {
                    arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.SONGS.ordinal());
                    arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.DROP_BOX.ordinal());
                    mFragment = new ItemSongListFragment();
                    mFragment.setArguments(arguments);
                    fragmentSwitcher(mFragment,  1, getResources().getString(R.string.drop_box), fade_in, fade_out);
                }else {
                    arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.SONGS.ordinal());
                    arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.GOOGLE_DRIVE.ordinal());
                    mFragment = new ItemSongListFragment();
                    mFragment.setArguments(arguments);
                    fragmentSwitcher(mFragment,  2, getResources().getString(R.string.google_drive), fade_in, fade_out);
                }
                break;
            case FAVOURITE:
                arguments.putInt(ItemSongListFragment.ARG_ITEM_TYPE, ItemType.FAVOURITE.ordinal());
                arguments.putInt(ItemSongListFragment.ARG_MEDIA_TYPE, MediaType.DEVICE_MEDIA_LIB.ordinal());
                mFragment = new ItemSongListFragment();
                mFragment.setArguments(arguments);
                fragmentSwitcher(mFragment,  3, getResources().getString(R.string.favourite_list), fade_in, fade_out);
                break;
        }



    }

    private void initToolBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void initCollectionViews(MediaItem mMediaItem) {
        artImg1 = (ImageView)findViewById(R.id.song_detail_list_art_img1);
        artImg2 = (ImageView)findViewById(R.id.song_detail_list_art_img2);
        artImg3 = (ImageView)findViewById(R.id.song_detail_list_art_img3);
        artImg4 = (ImageView)findViewById(R.id.song_detail_list_art_img4);
        tblAlbumArt = (TableLayout)findViewById(R.id.song_detail_list_art_table);

        albumArt = (ImageView) findViewById(R.id.activity_album_art);
        FloatingActionButton mActionPlaySongs = (FloatingActionButton) findViewById(R.id.fab);
        mActionPlaySongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCollectionFragment.onFloatActionPlaySong();
            }
        });

        new LoadCollectionAlbumArt(mMediaItem).execute();
    }

    public void fragmentSwitcher(Fragment fragment, int itemId,
                                 String fname, @AnimRes int animationEnter,
                                 @AnimRes int animationExit) {
        if (currentItem == itemId) {
            // Don't allow re-selection of the currently active item
            return;
        }
        currentItem = itemId;

        setTitle(String.valueOf(fname));

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(animationEnter, animationExit)
                .replace(R.id.item_detail_container, fragment)
                .commitAllowingStateLoss();
    }

    private void setTitle(String title){
        if(currentItem >= 4){
            if(null != appBarLayout)
                appBarLayout.setTitle(title);
        }else{
            if(null != actionBar)
                actionBar.setTitle(title);
        }
    }

    private class LoadCollectionAlbumArt extends AsyncTask<Void, Object, IMediaItemBase> {
        int screenWidth;
        MediaItem mMediaItem;

        public LoadCollectionAlbumArt(MediaItem mMediaItem){
            this.mMediaItem = mMediaItem;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            switch (mMediaItem.getParentType()) {
            case ALBUM:
            case ARTIST:
            case PLAYLIST:
            case GENRE:
            case BOOM_PLAYLIST:
                screenWidth = Utils.getWindowWidth(getBaseContext());
                setAlbumArtSize(screenWidth);
                break;
            }
        }

        @Override
        protected IMediaItemBase doInBackground(Void... voids) {
            return MediaController.getInstance(getBaseContext()).getMediaCollectionItem(getBaseContext(), mMediaItem.getParentId(), mMediaItem.getParentType(), mMediaItem.getMediaType());
        }

        @Override
        protected void onPostExecute(IMediaItemBase mItemBase) {
            super.onPostExecute(mItemBase);
            Bundle arguments = new Bundle();
            mCollectionFragment = new MediaCollectionFragment();
            arguments.putParcelable(MediaCollectionFragment.ARG_ITEM_COLLECTION, (MediaItemCollection)mItemBase);
            mCollectionFragment.setArguments(arguments);
            int itemNo = 0;
            switch (mMediaItem.getParentType()){
                case ALBUM:
                    albumArt.setVisibility(View.VISIBLE);
                    tblAlbumArt.setVisibility(View.GONE);
                    setAlbumArt((IMediaItemCollection) mItemBase, screenWidth);
                    itemNo =4;
                    break;
                case ARTIST:
                    albumArt.setVisibility(View.VISIBLE);
                    tblAlbumArt.setVisibility(View.GONE);
                    setAlbumArt((IMediaItemCollection) mItemBase, screenWidth);
                    itemNo =5;
                    break;
                case PLAYLIST:
                    if(((IMediaItemCollection)mItemBase).getArtUrlList().size() >= 1 &&
                            PlayerUtils.isPathValid(((IMediaItemCollection)mItemBase).getArtUrlList().get(0))) {
                        albumArt.setVisibility(View.GONE);
                        tblAlbumArt.setVisibility(View.VISIBLE);
                        setSongsArtImage(screenWidth, ((IMediaItemCollection)mItemBase).getArtUrlList());
                    }else{
                        albumArt.setVisibility(View.VISIBLE);
                        tblAlbumArt.setVisibility(View.GONE);
                        setDefaultImage(null);
                    }
                    itemNo =6;
                    break;
                case GENRE:
                    albumArt.setVisibility(View.VISIBLE);
                    tblAlbumArt.setVisibility(View.GONE);
                    setAlbumArt((IMediaItemCollection)mItemBase, screenWidth);
                    itemNo =7;
                    break;
                case BOOM_PLAYLIST:
                    if(((IMediaItemCollection)mItemBase).getArtUrlList().size() >= 1 &&
                            PlayerUtils.isPathValid(((IMediaItemCollection)mItemBase).getArtUrlList().get(0))) {
                        albumArt.setVisibility(View.GONE);
                        tblAlbumArt.setVisibility(View.VISIBLE);
                        setSongsArtImage(screenWidth, ((IMediaItemCollection)mItemBase).getArtUrlList());
                    }else{
                        albumArt.setVisibility(View.VISIBLE);
                        tblAlbumArt.setVisibility(View.GONE);
                        setDefaultImage(null);
                    }
                    itemNo =8;
                    break;
            }
            fragmentSwitcher(mCollectionFragment, itemNo, mItemBase.getItemTitle(), fade_in, fade_out);
        }
    }

    private void setAlbumArtSize(int width) {
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams(width, width);
        albumArt.setLayoutParams(param);
    }

    public void setAlbumArt(IMediaItemCollection collection, int width) {
        String imagePath = collection.getItemArtUrl();
        if(null != albumArt) {
            if (PlayerUtils.isPathValid(imagePath)) {
                Picasso.with(this)
                        .load(new File(imagePath)).resize(width, width)
                        .error(getResources().getDrawable(R.drawable.ic_default_album_header, null)).noFade()
                        .into(albumArt);
            } else {
                setDefaultImage(imagePath);
            }
        }
        if (Build.VERSION.SDK_INT >= 21) {
            int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);
            ActivityManager.TaskDescription taskDescription = new
                    ActivityManager.TaskDescription(collection.getItemTitle(),
                    BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), colorPrimary);
            setTaskDescription(taskDescription);
        }
    }

    private void setDefaultImage(String imagePath) {
        if (imagePath == null || imagePath.equals(MediaItem.UNKNOWN_ART_URL)) {
            albumArt.setImageDrawable(getResources().getDrawable(R.drawable.ic_default_album_header));
        }
    }

    private void setSongsArtImage(final int size, final ArrayList<String> Urls) {

        int count = Urls.size() > 4 ? 4 : Urls.size();
        TableRow.LayoutParams param = new TableRow.LayoutParams(size / 2, size / 2);
        artImg1.setLayoutParams(param);
        artImg2.setLayoutParams(param);
        artImg3.setLayoutParams(param);
        artImg4.setLayoutParams(param);


        switch (count){
            case 1:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 2:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            case 3:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
            default:
                Picasso.with(this).load(new File(Urls.get(0))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg1);
                Picasso.with(this).load(new File(Urls.get(1))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg2);
                Picasso.with(this).load(new File(Urls.get(2))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg3);
                Picasso.with(this).load(new File(Urls.get(3))).error(getResources().getDrawable(R.drawable.ic_default_album_grid, null))
                        /*.centerCrop().resize(size/2, size/2)*//*.memoryPolicy(MemoryPolicy.NO_CACHE)*/.into(artImg4);
                break;
        }
    }

    @Override
    protected void onResume() {
        registerPlayerReceiver(MediaCollectionActivity.this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        unregisterPlayerReceiver(MediaCollectionActivity.this);
        super.onPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            currentItem = -1;
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        currentItem = -1;
        super.onBackPressed();
    }
}

