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
import com.globaldelight.boom.tidal.tidalconnector.model.Item;

import java.util.HashMap;
import java.util.List;

/**
 * Created by Manoj Kumar on 03-05-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class TidalRisingAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private HashMap<String, List<Item>> mMapItems;
    private String[] mKeys;

    public TidalRisingAdapter(Context context, HashMap<String, List<Item>> mapItems) {
        this.mContext = context;
        this.mMapItems = mapItems;
        this.mKeys = mMapItems.keySet().toArray(new String[mMapItems.size()]);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new CustomViewHolder(inflater.inflate(R.layout.item_rising, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CustomViewHolder customViewHolder = (CustomViewHolder) holder;
        String key = mKeys[position];
        TidalItemAdapter adapter = new TidalItemAdapter(mContext, mMapItems.get(key));
        LinearLayoutManager llm;
        if (key.equalsIgnoreCase("Tracks")) {
            llm = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
            customViewHolder.recyclerView.setLayoutManager(llm);
            customViewHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            customViewHolder.recyclerView.setAdapter(new TidalTrackAdapter(mContext,mMapItems.get(key)));
        } else {
            llm = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
            customViewHolder.recyclerView.setLayoutManager(llm);
            customViewHolder.recyclerView.setItemAnimator(new DefaultItemAnimator());
            customViewHolder.recyclerView.setAdapter(adapter);
        }

    }

    @Override
    public int getItemCount() {
        return mMapItems == null ? 0 : mMapItems.size();
    }

    protected class CustomViewHolder extends RecyclerView.ViewHolder {

        private RecyclerView recyclerView;

        public CustomViewHolder(View itemView) {
            super(itemView);

            recyclerView = itemView.findViewById(R.id.rv_rising_item);
        }
    }
}
