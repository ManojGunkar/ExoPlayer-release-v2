package com.globaldelight.boom.radio.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Manoj Kumar on 09-04-2018.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class LocalRadioListAdapter extends RecyclerView.Adapter<LocalRadioListAdapter.LocalViewHolder> {

    private Context mContext;

    public LocalRadioListAdapter(Context context){
        this.mContext=context;
    }

    @Override
    public LocalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(LocalViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class LocalViewHolder extends RecyclerView.ViewHolder{

        public LocalViewHolder(View itemView) {
            super(itemView);
        }
    }
}
