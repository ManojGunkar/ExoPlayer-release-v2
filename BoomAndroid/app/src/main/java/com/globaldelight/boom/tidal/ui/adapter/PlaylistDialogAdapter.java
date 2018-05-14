package com.globaldelight.boom.tidal.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.tidalconnector.model.Item;

import java.util.Collections;
import java.util.List;

/**
 * Created by Manoj Kumar on 14-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class PlaylistDialogAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Item> mItems= Collections.emptyList();

    public PlaylistDialogAdapter(Context context, List<Item> items) {
        this.mContext = context;
        this.mItems = items;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemViewHolder vh = new ItemViewHolder(inflater.inflate(R.layout.card_grid_item, parent, false));
        return vh;

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Item item = mItems.get(position);
        ItemViewHolder viewHolder = (ItemViewHolder) holder;
        viewHolder.txtTitle.setText(item.getTitle());
        viewHolder.txtSubTitle.setText("Song "+item.getNumberOfTracks());
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    protected class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitle;
        private TextView txtSubTitle;

        public ItemViewHolder(View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.card_grid_title);
            txtSubTitle = itemView.findViewById(R.id.card_grid_sub_title);
            itemView.findViewById(R.id.card_grid_menu).setVisibility(View.GONE);

        }
    }
}
