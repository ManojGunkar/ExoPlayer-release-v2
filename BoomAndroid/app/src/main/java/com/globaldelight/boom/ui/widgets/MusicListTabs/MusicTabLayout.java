package com.globaldelight.boom.ui.widgets.MusicListTabs;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.ui.widgets.RegularTextView;

/**
 * Created by Rahul Agarwal on 03-02-17.
 */

public class MusicTabLayout extends BaseTabLayout {

    private Context mContext;

    public MusicTabLayout(Context context){
        this.mContext = context;
    }


    @Override
    public View createView(LayoutInflater inflater, int position, ViewGroup parent, CharSequence pageTitle) {
        View view=inflater.inflate(R.layout.view_custom_tab,parent,false);
        TextView textView= (TextView) view.findViewById(R.id.tab_txt);
        textView.setText(pageTitle.toString().toUpperCase());
        return view;
    }

    @Override
    public void onTabState(View v, boolean isSelected, int position) {
        RegularTextView textView= (RegularTextView) v.findViewById(R.id.tab_txt);
        if (isSelected){
//            AnimationUtils.getBounceAnimation(imageView);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.music_tab_normal_color));
        }else {
//            AnimationUtils.getRotateAnimation(imageView);
            textView.setTextColor(ContextCompat.getColor(getContext(), R.color.music_tab_selected_color));
        }
    }

}
