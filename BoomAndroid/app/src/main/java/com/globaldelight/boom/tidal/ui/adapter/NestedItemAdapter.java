package com.globaldelight.boom.tidal.ui.adapter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.tidal.utils.NestedItemDescription;

import java.util.List;

/**
 * Created by Manoj Kumar on 30-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class NestedItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<NestedItemDescription> mItems;

    public NestedItemAdapter(Context context, List<NestedItemDescription> mapItems) {
        this.mContext = context;
        this.mItems = mapItems;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CustomViewHolder(inflater.inflate(R.layout.item_album_tidal, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CustomViewHolder customViewHolder = (CustomViewHolder) holder;
        NestedItemDescription description = mItems.get(position);
        customViewHolder.txtTitleItem.setText(description.titleResId);
        TidalItemAdapter adapter = new TidalItemAdapter(mContext, description.itemList);
        LinearLayoutManager llm;
        if (description.type == NestedItemDescription.LIST_VIEW) {
            llm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            customViewHolder.recyclerView.setLayoutManager(llm);
            customViewHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            customViewHolder.recyclerView.setAdapter(new TidalTrackAdapter(mContext, description.itemList));
        } else {
            llm = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            customViewHolder.recyclerView.setLayoutManager(llm);
            customViewHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            customViewHolder.recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public int getItemCount() {
        return mItems == null ? 0 : mItems.size();
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {

        private TextView txtTitleItem;
        private Button btnMoreItem;
        private RecyclerView recyclerView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            txtTitleItem = itemView.findViewById(R.id.txt_title_album);
            btnMoreItem = itemView.findViewById(R.id.btn_more_album);
            recyclerView = itemView.findViewById(R.id.rv_tidal_album);
        }
    }
}
