package com.globaldelight.boom.app.share;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.Utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Manoj Kumar on 9/26/2017.
 */

public class SharePagerAdapter extends android.support.v4.view.PagerAdapter implements View.OnClickListener {

    public static final int ITEMS_PER_PAGE = 4;

    public interface OnItemClickListener {
        void onClick(ShareItem item);
    }

    private int pagerSize;
    private Context context;
    ArrayList<ShareItem> mInfo;
    OnItemClickListener mListener;


    public SharePagerAdapter(Context context, ArrayList<ShareItem> info, OnItemClickListener listener) {
        this.context = context;
        mInfo = info;
        mListener = listener;
        pagerSize = (info.size() / ITEMS_PER_PAGE);
        if ( info.size() % ITEMS_PER_PAGE > 0 ) {
            pagerSize += 1;
        }
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

        View pageView = null;
        LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pageView = inflater.inflate(R.layout.share_01, null);
        view.addView(pageView);

        ImageView itemViews[] = new ImageView[ITEMS_PER_PAGE];
        itemViews[0] = (ImageView)pageView.findViewById(R.id.img_share_item1);
        itemViews[1] = (ImageView)pageView.findViewById(R.id.img_share_item2);
        itemViews[2] = (ImageView)pageView.findViewById(R.id.img_share_item3);
        itemViews[3] = (ImageView)pageView.findViewById(R.id.img_share_item4);

        for ( int i = 0; i < ITEMS_PER_PAGE; i++ ) {
            itemViews[i].setVisibility(View.INVISIBLE);
            itemViews[i].setTag(null);
            int itemPos = (position * ITEMS_PER_PAGE + i);
            if (  itemPos < mInfo.size()) {
                ShareItem item = mInfo.get(itemPos);
                itemViews[i].setImageDrawable(item.icon);
                itemViews[i].setTag(item);
                itemViews[i].setVisibility(View.VISIBLE);
            }
            itemViews[i].setOnClickListener(this);
        }

        return pageView;
    }

    @Override
    public void onClick(View v) {
        Object tag = v.getTag();
        if ( tag == null || !(tag instanceof ShareItem) ) {
            return;
        }

        if ( mListener != null ) {
            ShareItem item = (ShareItem)tag;
            mListener.onClick(item);
        }
    }
}