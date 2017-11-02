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

    public static final int ITEMS_PER_PAGE = 3;

    private int pagerSize;
    private Context context;
    ArrayList<ShareItem> mInfo;

    public SharePagerAdapter(Context context, ArrayList<ShareItem> info) {
        this.context = context;
        mInfo = info;
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

        ImageView itemViews[] = new ImageView[3];
        itemViews[0] = (ImageView)pageView.findViewById(R.id.img_share_item1);
        itemViews[1] = (ImageView)pageView.findViewById(R.id.img_share_item2);
        itemViews[2] = (ImageView)pageView.findViewById(R.id.img_share_item3);

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

        ShareItem item = (ShareItem)tag;
        if (item.text.equalsIgnoreCase("facebook") ){
            ShareUtils.getInstance(context).fbShare();
        }
        else if ( item.text.equalsIgnoreCase("email") ) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_desc));
            intent.putExtra(Intent.EXTRA_TEXT, "");
            Intent mailer = Intent.createChooser(intent, null);
            context.startActivity(mailer);
        }
        else if ( item.text.equalsIgnoreCase("other") ) {
            Utils.shareStart(context);
        }
        else {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setPackage(item.pkgName);
            sendIntent.putExtra(Intent.EXTRA_TEXT, context.getString(R.string.share_desc));
            sendIntent.setType("text/plain");
            context.startActivity(sendIntent);
        }
    }


}