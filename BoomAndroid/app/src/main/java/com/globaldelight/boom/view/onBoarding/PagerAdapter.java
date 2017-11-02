package com.globaldelight.boom.view.onBoarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;

import java.util.Random;

/**
 * Created by manoj on 8/2/17.
 */

public class PagerAdapter extends android.support.v4.view.PagerAdapter {

    private final Random random = new Random();
    private int mSize;
    private Context context;

    public PagerAdapter(Context context) {
        this.context = context;
        mSize = 4;
    }

    @Override
    public int getCount() {
        return mSize;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup view, int position, Object object) {
        view.removeView((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup view, int position) {

        View xmlView = null;
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (position==0){
            xmlView = inflater.inflate(R.layout.onboard_0, null);
        }if (position==1){
            xmlView = inflater.inflate(R.layout.onboard_1, null);
        }if (position==2){
            xmlView = inflater.inflate(R.layout.onboard_2, null);
        }if (position==3){
            xmlView = inflater.inflate(R.layout.onboard_3, null);
        }

        view.addView(xmlView);
        return xmlView;
    }

}