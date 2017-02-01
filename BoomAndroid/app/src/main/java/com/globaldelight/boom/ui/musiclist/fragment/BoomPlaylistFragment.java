package com.globaldelight.boom.ui.musiclist.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItemCollection;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.BoomPlayListAdapter;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.utils.decorations.AlbumListSpacesItemDecoration;
import com.globaldelight.boom.utils.decorations.BoomPlayListFooterItemDecoration;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;
import java.util.ArrayList;

public class BoomPlaylistFragment extends Fragment {

    private RecyclerView rootView;
    private BoomPlayListAdapter boomPlayListAdapter;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BoomPlaylistFragment() {
    }


    private BroadcastReceiver mUpdateBoomPlaylistReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            MediaItem item;
            switch (intent.getAction()){
                case PlayerEvents.ACTION_UPDATE_BOOM_PLAYLIST:
                    if (boomPlayListAdapter != null) {
                        boomPlayListAdapter.updateNewList((ArrayList<? extends MediaItemCollection>) MediaController.getInstance(context).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
                    }
                    break;
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(PlayerEvents.ACTION_UPDATE_BOOM_PLAYLIST);
        getActivity().registerReceiver(mUpdateBoomPlaylistReceiver, intentFilter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = (RecyclerView) inflater.inflate(R.layout.recycler_view_layout, container, false);

        new LoadBoomPlaylist().execute();
        setForAnimation();
        return rootView;
    }

    private void setForAnimation() {
        rootView.scrollTo(0, 100);
    }

    public void listNoMoreEmpty() {
        rootView.setVisibility(View.VISIBLE);
    }

    public void updateBoomPlaylist() {
        boomPlayListAdapter.updateNewList(MediaController.getInstance(getActivity()).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB));
    }

    public class LoadBoomPlaylist  extends AsyncTask<Void, Integer, ArrayList<? extends IMediaItemBase>> {
        GridLayoutManager gridLayoutManager;
        @Override
        protected ArrayList<? extends IMediaItemBase> doInBackground(Void... params) {
            return MediaController.getInstance(getActivity()).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;
        }

        @Override
        protected void onPostExecute(ArrayList<? extends IMediaItemBase> iMediaItemList) {
            super.onPostExecute(iMediaItemList);
            boolean isPhone = Utils.isPhone(getActivity());
            if(isPhone){
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 2);
            }else{
                gridLayoutManager =
                        new GridLayoutManager(getActivity(), 3);
            }

            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (boomPlayListAdapter.whatView(position) == BoomPlayListAdapter.ITEM_VIEW_TYPE_ITEM_LIST) {
                        return 1;
                    } else {
                        return 2;
                    }
                }
            });
            gridLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
            gridLayoutManager.scrollToPosition(0);
            rootView.setLayoutManager(gridLayoutManager);
            rootView.addItemDecoration(new SimpleDividerItemDecoration(getActivity(), Utils.getWindowWidth(getActivity())));
            rootView.addItemDecoration(new AlbumListSpacesItemDecoration(Utils.dpToPx(getActivity(), 0)));
            boomPlayListAdapter = new BoomPlayListAdapter(getActivity(), BoomPlaylistFragment.this, rootView, iMediaItemList, isPhone);
            rootView.setAdapter(boomPlayListAdapter);
            rootView.addItemDecoration(new BoomPlayListFooterItemDecoration(2, boomPlayListAdapter));
//                        rootView.setHasFixedSize(true);
            if (iMediaItemList.size() < 1) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listIsEmpty();
                    }
                });
            }
        }
    }

    public void newPlaylistDialog() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.new_playlist)
                .backgroundColor(ContextCompat.getColor(getActivity(), R.color.dialog_background))
                .titleColor(ContextCompat.getColor(getActivity(), R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(getActivity(), R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(getActivity(), R.color.dialog_submit_negative))
                .neutralColor(ContextCompat.getColor(getActivity(), R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(getActivity(), R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(getActivity(), R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .input(getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(getActivity()).createBoomPlaylist(input.toString());
                            listNoMoreEmpty();
                            updateBoomPlaylist();
//                            recyclerView.scrollToPosition(boomPlayListAdapter.getItemCount()-1);
                            Toast.makeText(getActivity(), getResources().getString(R.string.playlist_created), Toast.LENGTH_SHORT).show();
                        }
                    }
                }).show();
    }

    public void listIsEmpty() {
        rootView.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(mUpdateBoomPlaylistReceiver);
        super.onDestroy();
    }
}
