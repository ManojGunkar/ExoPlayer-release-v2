package com.globaldelight.boom.view.onBoarding;

import android.content.Context;
import android.content.res.TypedArray;
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
    private Context context;
    private TypedArray pages;

    public PagerAdapter(Context context) {
        this.context = context;
        pages = context.getResources().obtainTypedArray(R.array.onboarding_pages);
    }

    @Override
    public int getCount() {
        return pages.length();
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
        int layoutId = pages.getResourceId(position, -1);
        xmlView = inflater.inflate(layoutId, null);
        view.addView(xmlView);
        return xmlView;
    }

}