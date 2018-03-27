package com.globaldelight.boom.app.adapters.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.Constants;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.player.AudioEffect;

import org.json.JSONException;

/**
 * Created by Rahul Agarwal on 04-02-17.
 */

public class HeadPhoneItemAdapter extends RecyclerView.Adapter<HeadPhoneItemAdapter.SimpleItemViewHolder> {
    TypedArray activeHeadPhoneList, inactiveHeadPhoneList, headPhoneList;
    int selectedHeadPhoneType;
    AudioEffect audioEffectPreferenceHandler;
    RecyclerView recyclerView;
    Context context;
    public HeadPhoneItemAdapter(TypedArray activeHeadPhoneList, TypedArray inactiveHeadPhoneList,
                                TypedArray headPhoneList, Context context, RecyclerView recyclerView) {
        this.activeHeadPhoneList = activeHeadPhoneList;
        this.inactiveHeadPhoneList = inactiveHeadPhoneList;
        this.headPhoneList = headPhoneList;
        this.context = context;
        this.recyclerView = recyclerView;
        audioEffectPreferenceHandler  = AudioEffect.getInstance(context);
        this.selectedHeadPhoneType = audioEffectPreferenceHandler.getHeadPhoneType();
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_headphone, parent, false);
        return new SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(SimpleItemViewHolder holder, final int position) {
        if(selectedHeadPhoneType == toHeadphoneType(position)){
            holder.earPhone.setImageDrawable(activeHeadPhoneList.getDrawable(position));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.effect_active));
        }else {
            holder.earPhone.setImageDrawable(inactiveHeadPhoneList.getDrawable(position));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.effect_inactive));
        }
        holder.type.setText(headPhoneList.getString(position));
        holder.mainView.setOnClickListener((v)->this.onItemClicked(v,position));
    }

    @Override
    public int getItemCount() {
        return headPhoneList.length();
    }

    private void onItemClicked(View v, int position) {
        audioEffectPreferenceHandler.setHeadPhoneType(toHeadphoneType(position));
        selectedHeadPhoneType = audioEffectPreferenceHandler.getHeadPhoneType();
        notifyDataSetChanged();
        recyclerView.scrollToPosition(position);
        try {
            AnalyticsHelper.trackHeadPhoneUsed(context, String.valueOf(selectedHeadPhoneType));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    int toHeadphoneType(int pos) {
        switch (pos) {
            case 0:
                return Constants.Headphone.OVER_EAR;
            case 1:
                return Constants.Headphone.IN_CANAL;
            default:
                return Constants.Headphone.ON_EAR;

        }
    }

    public static class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public TextView type;
        public ImageView earPhone;
        public View mainView;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            type = itemView.findViewById(R.id.img_pager_item_title);
            earPhone = itemView.findViewById(R.id.img_pager_item);
        }
    }
}
