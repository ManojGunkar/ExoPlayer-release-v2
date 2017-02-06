package com.globaldelight.boom.ui.musiclist.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.ui.widgets.RegularTextView;
import com.globaldelight.boomplayer.AudioEffect;

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
        audioEffectPreferenceHandler  = AudioEffect.getAudioEffectInstance(context);
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
        if(selectedHeadPhoneType == position){
            holder.earPhone.setImageDrawable(activeHeadPhoneList.getDrawable(position));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.effect_active));
        }else {
            holder.earPhone.setImageDrawable(inactiveHeadPhoneList.getDrawable(position));
            holder.type.setTextColor(ContextCompat.getColor(context, R.color.effect_inactive));
        }
        holder.type.setText(headPhoneList.getString(position));
        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioEffectPreferenceHandler.setHeadPhoneType(position);
                selectedHeadPhoneType = position;
                App.getPlayerEventHandler().setHeadPhoneType(position);
                notifyDataSetChanged();
                recyclerView.scrollToPosition(position);
                try {
                    AnalyticsHelper.trackHeadPhoneUsed(context, String.valueOf(position));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return headPhoneList.length();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {

        public RegularTextView type;
        public ImageView earPhone;
        public View mainView;

        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            type = (RegularTextView) itemView.findViewById(R.id.img_pager_item_title);
            earPhone = (ImageView) itemView.findViewById(R.id.img_pager_item);
        }
    }
}
