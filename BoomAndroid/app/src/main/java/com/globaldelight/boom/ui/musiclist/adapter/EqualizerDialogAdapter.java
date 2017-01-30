package com.globaldelight.boom.ui.musiclist.adapter;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;

import java.util.List;

/**
 * Created by Rahul Agarwal on 23-01-17.
 */

public class EqualizerDialogAdapter extends RecyclerView.Adapter<EqualizerDialogAdapter.ViewHolder> {

    private List<String> eqNames;
    TypedArray eq_active_on, eq_active_off;
    private Context mContext;
    MaterialDialog dialog;
    private static int eqPosition = 0;
    private IEqualizerSelect equalizerListener;

    public EqualizerDialogAdapter(Context context, int eqPosition, List<String> eqNames, TypedArray eq_active_on, TypedArray eq_active_off, IEqualizerSelect equalizerListener){
        this.eqNames = eqNames;
        this.eq_active_on = eq_active_on;
        this.eq_active_off = eq_active_off;
        this.mContext = context;
        this.eqPosition = eqPosition;
        this.equalizerListener = equalizerListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_equalizer_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mEqName.setText(eqNames.get(position));

        if(position == eqPosition){
            holder.mainView.setBackgroundColor(Color.parseColor("#b6b6b6"));
            holder.mEqIcon.setImageDrawable(eq_active_on.getDrawable(position));
            holder.mEqName.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimaryDark));
        }else{
            holder.mainView.setBackgroundColor(Color.parseColor("#171921"));
            holder.mEqIcon.setImageDrawable(eq_active_off.getDrawable(position));
            holder.mEqName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
        }

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        equalizerListener.onChangeEqualizerValue(position);
                    }
                });
                updateList(position);
            }
        });
    }

    private void updateList(int position) {
        eqPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return eqNames.size();
    }

    public void setDialog(MaterialDialog dialog) {
        this.dialog = dialog;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public View mainView;
        public TextView mEqName;
        public ImageView mEqIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            mainView = itemView;
            mEqName = (TextView) itemView.findViewById(R.id.eq_name);
            mEqIcon = (ImageView) itemView.findViewById(R.id.eq_icon);
        }
    }

    public interface IEqualizerSelect {
        void onChangeEqualizerValue(int position);
    }
}
