package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.radio.webconnector.responsepojo.RadioStationResponse;

import java.util.List;

/**
 * Created by Manoj Kumar on 17-04-2018.
 * Copyright (C) 2018. Global Delight Technologies Pvt. Ltd. All rights reserved.
 */
public class RadioSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<RadioStationResponse.Content> mContents;

    public RadioSearchAdapter(Context context,List<RadioStationResponse.Content> contents){
        this.mContext=context;
        this.mContents=contents;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SearchVH searchVH= (SearchVH) holder;

    }

    @Override
    public int getItemCount() {
        return mContents==null?0:mContents.size();
    }

    protected class SearchVH extends RecyclerView.ViewHolder{

        public SearchVH(View itemView) {
            super(itemView);
        }
    }
}
