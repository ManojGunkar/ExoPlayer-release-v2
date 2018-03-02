package com.globaldelight.boom.business.ads.viewholder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.ads.MediaView;
import com.globaldelight.boom.R;

/**
 * Created by Manoj Kumar on 6/2/2017.
 */

public class FbViewHolder extends RecyclerView.ViewHolder {

    public ImageView imgAdIcon;
    public TextView txtAdTitle;
    public MediaView mediaViewAd;
    public TextView txtAdSocialContext;
    public TextView txtAdBody;
    public Button btnAdCallToAction;
    public LinearLayout llAdChoicesContainer;
    public LinearLayout llAdContainer;
    public boolean loaded;

    public FbViewHolder(View view) {
        super(view);
        llAdContainer = (LinearLayout) view.findViewById(R.id.fb_native_ad_container);
        imgAdIcon = (ImageView) view.findViewById(R.id.native_ad_icon);
        txtAdTitle = (TextView) view.findViewById(R.id.native_ad_title);
        mediaViewAd = (MediaView) view.findViewById(R.id.native_ad_media);
        txtAdSocialContext = (TextView) view.findViewById(R.id.native_ad_social_context);
        txtAdBody = (TextView) view.findViewById(R.id.native_ad_body);
        btnAdCallToAction = (Button) view.findViewById(R.id.native_ad_call_to_action);
        llAdChoicesContainer = (LinearLayout) view.findViewById(R.id.ad_choices_container);
        loaded=false;
    }

    public Context getContext() {
        return llAdContainer.getContext();
    }
}
