package com.audio.player.customtab.tablayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.audio.player.customtab.AnimationUtils;
import com.audio.player.customtab.R;

public class CustomTabLayout extends BaseTabLayout {

    private int[] res;

    public CustomTabLayout(int[] res){
        this.res=res;
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
    public void onTabState(View v, boolean isSelected,int position) {
        TextView textView= (TextView) v.findViewById(R.id.text);
        ImageView imageView= (ImageView) v.findViewById(R.id.icon);
        if (isSelected){
//            if (position%2==0){
//                AnimationUtils.getRotateAnimation(imageView);
//            }else {
                AnimationUtils.getBounceAnimation(imageView);
//            }
            textView.setTextColor(getContext().getResources().getColor(android.R.color.white));
        }else {
            textView.setTextColor(getContext().getResources().getColor(R.color.normal));
        }
    }

}
