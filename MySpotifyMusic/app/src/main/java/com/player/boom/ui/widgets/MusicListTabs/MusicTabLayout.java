package com.player.boom.ui.widgets.MusicListTabs;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.player.boom.R;


public class MusicTabLayout extends BaseTabLayout {

    private int[] res;
    private int[] selectedRes;

    public MusicTabLayout(int[] res, int[] selectedRes){
        this.res=res;
        this.selectedRes = selectedRes;
    }


    @Override
    public View createView(LayoutInflater inflater, int position, ViewGroup parent, CharSequence pageTitle) {
        View view=inflater.inflate(R.layout.view_custom_tab,parent,false);
        TextView textView= (TextView) view.findViewById(R.id.text);
        textView.setText(pageTitle);
        ImageView imageView= (ImageView) view.findViewById(R.id.icon);
        imageView.setImageResource(res[position]);
        return view;
    }

    @Override
    public void onTabState(View v, boolean isSelected, int position) {
        TextView textView= (TextView) v.findViewById(R.id.text);
        ImageView imageView= (ImageView) v.findViewById(R.id.icon);
        if (isSelected){
//            AnimationUtils.getBounceAnimation(imageView);
            imageView.setImageResource(selectedRes[position]);
            textView.setTextColor(getContext().getResources().getColor(android.R.color.white));
        }else {
//            AnimationUtils.getRotateAnimation(imageView);
            imageView.setImageResource(res[position]);
            textView.setTextColor(getContext().getResources().getColor(R.color.normal));
        }
    }

}
