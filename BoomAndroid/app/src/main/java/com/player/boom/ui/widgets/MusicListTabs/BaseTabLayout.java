package com.player.boom.ui.widgets.MusicListTabs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public abstract class BaseTabLayout {

    private Context mContext;

    private CharSequence pageTitle;

    private int position;

    private ViewGroup parent;


    public View getView(){
        return createView(LayoutInflater.from(mContext),position,parent,pageTitle);
    }

    public void init(Context context,int position,CharSequence pageTitle,ViewGroup parent){
        this.pageTitle=pageTitle;
        mContext=context;
        this.parent=parent;
        this.position=position;
    }

    public Context getContext(){
        return mContext;
    }

    public abstract View createView(LayoutInflater inflater,int position,ViewGroup parent,CharSequence pageTitle);
    
    public void onTabState(View v, boolean isSelected, int position){

    }
    
    
}
