package com.globaldelight.boom.app.share;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.globaldelight.boom.R;

import java.util.Random;

/**
 * Created by Manoj Kumar on 9/26/2017.
 */

public class sharePagerAdapter  extends android.support.v4.view.PagerAdapter {

    private int pagerSize;
    private Context context;

    public sharePagerAdapter(Context context,int pageSize) {
        this.context = context;
        pagerSize = pageSize;
    }

    @Override
    public int getCount() {
        return pagerSize;
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
            xmlView = inflater.inflate(R.layout.share_01, null);
        }if (position==1){
            xmlView = inflater.inflate(R.layout.share_02, null);
        }
        view.addView(xmlView);
        return xmlView;
    }

}