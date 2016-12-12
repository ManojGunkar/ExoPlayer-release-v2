package com.globaldelight.boom.ui.musiclist.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.HeadSetType;
import com.globaldelight.boom.utils.Logger;

import java.util.ArrayList;

public class HeadSetListAdapter extends RecyclerView
        .Adapter<HeadSetListAdapter
        .DataObjectHolder> {
    private static String LOG_TAG = "HeadSetListAdapter";
    private static HeadsetClickListener myClickListener;
    private ArrayList<HeadSetType> mDataset;
    private Context mContext;

    public HeadSetListAdapter(ArrayList<HeadSetType> myDataset, Context context) {
        mContext = context;
        mDataset = myDataset;
    }

    public void setOnItemClickListener(HeadsetClickListener myClickListener) {
        this.myClickListener = myClickListener;
    }

    @Override
    public DataObjectHolder onCreateViewHolder(ViewGroup parent,
                                               int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_settings_headphone, parent, false);

        DataObjectHolder dataObjectHolder = new DataObjectHolder(view);
        return dataObjectHolder;
    }

    @Override
    public void onBindViewHolder(DataObjectHolder holder, int position) {
        HeadSetType item = mDataset.get(position);
        if (item.isActive()) {
            holder.label.setText(item.getTitle());
            holder.label.setTextColor(mContext.getResources().getColor(R.color.white));
            holder.imgView.setImageDrawable(mContext.getResources().getDrawable(mDataset.get(position).getImageActiveResource()));
        } else {
            holder.label.setText(item.getTitle());
            holder.imgView.setImageDrawable(mContext.getResources().getDrawable(mDataset.get(position).getImageResource()));
            holder.label.setTextColor(mContext.getResources().getColor(R.color.white_shade));
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
 
   /* public void addItem(HeadSetType dataObj, int index) {
        mDataset.add(dataObj);
        notifyItemInserted(index);
    }
 
    public void deleteItem(int index) {
        mDataset.remove(index);
        notifyItemRemoved(index);
    }*/

    public interface HeadsetClickListener {
        public void onItemClick(int position, View v);
    }

    public static class DataObjectHolder extends RecyclerView.ViewHolder
            implements View
            .OnClickListener {
        TextView label;
        ImageView imgView;

        public DataObjectHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.img_pager_item_title);
            imgView = (ImageView) itemView.findViewById(R.id.img_pager_item);
            Logger.LOGI(LOG_TAG, "Adding Listener");
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            myClickListener.onItemClick(getPosition(), v);
        }
    }
}