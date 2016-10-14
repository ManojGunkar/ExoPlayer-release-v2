package com.player.boom.ui.widgets.MusicListTabs;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DefaultTabLayout extends BaseTabLayout {


    @Override
    public View createView(LayoutInflater inflater, int position, ViewGroup parent, CharSequence pageTitle) {
        TextView textView=new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setText(pageTitle);
        textView.setTextColor(getContext().getResources().getColor(android.R.color.white));
        textView.setSingleLine();
        textView.setMinWidth(160);
        return textView;
    }


}
