package com.player.ui.musiclist.adapter;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.App;
import com.player.manager.AudioEffect;
import com.player.myspotifymusic.R;
import com.player.ui.musiclist.activity.Surround3DActivity;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.player.manager.AudioEffect.AUDIO_EFFECT_POWER;
import static com.player.manager.AudioEffect.AUDIO_EFFECT_SETTING;
import static com.player.manager.AudioEffect.EQUALIZER_POSITION;
import static com.player.manager.AudioEffect.EQUALIZER_POWER;
import static com.player.manager.AudioEffect.POWER_OFF;
import static com.player.manager.AudioEffect.SELECTED_EQUALIZER_POSITION;
import static com.player.manager.AudioEffect.equalizer.off;
import static com.player.manager.AudioEffect.equalizer.on;

/**
 * Created by Rahul Agarwal on 05-10-16.
 */

public class EqualizerViewAdapter extends RecyclerView.Adapter<EqualizerViewAdapter.SimpleItemViewHolder> {

    private Activity context;
    TypedArray eq_active_on, eq_active_off, eq_inactive_on, eq_inactive_off;
    List<String> eq_names;
    ArrayList<AudioEffect.equalizer> selection;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public EqualizerViewAdapter(Surround3DActivity surround3DActivity, List<String> eq_names, TypedArray eq_active_on,
                                TypedArray eq_active_off, TypedArray eq_inactive_on, TypedArray eq_inactive_off) {
        this.context = surround3DActivity;
        this.eq_names = eq_names;
        this.eq_active_on = eq_active_on;
        this.eq_active_off = eq_active_off;
        this.eq_inactive_on = eq_inactive_on;
        this.eq_inactive_off = eq_inactive_off;
        pref = App.getApplication().getSharedPreferences(AUDIO_EFFECT_SETTING, MODE_PRIVATE);
        this.selection = new ArrayList<>();
        int pos = pref.getInt(SELECTED_EQUALIZER_POSITION, EQUALIZER_POSITION);
        for(int i=0;i<eq_active_on.length(); i++){
            if(i == pos){
                selection.add(on);
            }else{
                selection.add(off);
            }
        }
    }

    @Override
    public SimpleItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.colum_equalizer, parent, false);
        return new EqualizerViewAdapter.SimpleItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SimpleItemViewHolder holder, final int position) {
        boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
        boolean isEqualizerOn = pref.getBoolean(EQUALIZER_POWER, POWER_OFF);

        Typeface tf;

        if(isPowerOn && isEqualizerOn){
            if(selection.get(position)== on){
                holder.eqImg.setImageDrawable(eq_active_on.getDrawable(position));
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-SemiBold.ttf");
            }else /*if(selection.get(position)== Surround3DActivity.equalizer.off)*/{
                holder.eqImg.setImageDrawable(eq_inactive_on.getDrawable(position));
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-Regular.ttf");
            }
        }else{
            if(selection.get(position)== on){
                holder.eqImg.setImageDrawable(eq_active_off.getDrawable(position));
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-SemiBold.ttf");
            }else /*if(selection.get(position)== Surround3DActivity.equalizer.off)*/{
                holder.eqImg.setImageDrawable(eq_inactive_off.getDrawable(position));
                tf = Typeface.createFromAsset(context.getAssets(), "fonts/TitilliumWeb-Regular.ttf");
            }
        }
        holder.eqTxt.setTypeface(tf);
        holder.eqTxt.setText(eq_names.get(position));
        onClick(holder, position);
    }

    private void onClick(SimpleItemViewHolder holder, final int position) {
        holder.eqImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isPowerOn = pref.getBoolean(AUDIO_EFFECT_POWER, POWER_OFF);
                boolean isEqualizerOn = pref.getBoolean(EQUALIZER_POWER, POWER_OFF);
                if(isPowerOn && isEqualizerOn) {
                    editor = pref.edit();
                    editor.putInt(SELECTED_EQUALIZER_POSITION, position);
                    editor.commit();
                    selection.clear();
                    for (int i = 0; i < eq_active_on.length(); i++) {
                        if (i == position) {
                            selection.add(on);
                        } else {
                            selection.add(i, off);
                        }
                    }
                    updateList();
                }
            }
        });
    }

    public void updateList() {
        if(eq_active_on !=null)
            notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eq_active_on.length();
    }

    public class SimpleItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView eqImg;
        public TextView eqTxt;
        public View mainView;
        public SimpleItemViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            eqImg = (ImageView) itemView.findViewById(R.id.eq_img);
            eqTxt = (TextView)  itemView.findViewById(R.id.eq_txt);
        }
    }
}
