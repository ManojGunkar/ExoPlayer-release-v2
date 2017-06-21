package com.globaldelight.boom.app.adapters.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.view.RegularTextView;

import java.util.List;

/**
 * Created by Rahul Agarwal on 23-01-17.
 */

public class EqualizerDialogAdapter extends RecyclerView.Adapter<EqualizerDialogAdapter.ViewHolder> {

    private List<String> mEqNames;
    private TypedArray mEqIcons;
    private Context mContext;
    private int eqPosition = 0;
    private IEqualizerSelect equalizerListener;

    public EqualizerDialogAdapter(Context context, int eqPosition, List<String> eqNames, TypedArray eqIcons, IEqualizerSelect equalizerListener){
        this.mEqNames = eqNames;
        this.mEqIcons = eqIcons;
        this.mContext = context;
        this.eqPosition = eqPosition;
        this.equalizerListener = equalizerListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.card_equalizer_item, parent, false);
        final ViewHolder holder = new ViewHolder(itemView);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int position = holder.getAdapterPosition();
                equalizerListener.onChangeEqualizerValue(position);
                updateList(position);
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mEqName.setText(mEqNames.get(position));
        holder.mEqIcon.setImageDrawable(mEqIcons.getDrawable(position));

        boolean selected = (position == eqPosition);
        holder.itemView.setSelected(selected);
        holder.mEqIcon.setSelected(selected);
        holder.mEqName.setSelected(selected);
    }

    private void updateList(int position) {
        eqPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mEqNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        public RegularTextView mEqName;
        public ImageView mEqIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            mEqName = (RegularTextView) itemView.findViewById(R.id.eq_name);
            mEqIcon = (ImageView) itemView.findViewById(R.id.eq_icon);
        }
    }

    public interface IEqualizerSelect {
        void onChangeEqualizerValue(int position);
    }
}
