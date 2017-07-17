package com.globaldelight.boom.app.businessmodel.ads.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.globaldelight.boom.R;
import com.google.android.gms.ads.formats.NativeContentAdView;

/**
 * Created by Manoj Kumar on 6/13/2017.
 */

public class GoogleAdViewHolder extends RecyclerView.ViewHolder {

    public NativeContentAdView adView;
    public ImageView  logoView;
    public TextView   headerView;
    public TextView   descriptionView;
    public TextView   advertiserView;
    public ImageView  imageView;
    public View       adContentView;
    public View       progressView;
    public Button     adActionBtn;

    public GoogleAdViewHolder(View itemView) {
        super(itemView);
        adView= (NativeContentAdView) itemView.findViewById(R.id.google_native_adView);
        adContentView = (View)itemView.findViewById(R.id.ad_content_view);
        progressView = (View)itemView.findViewById(R.id.ad_loading_view);
        logoView = (ImageView)adView.findViewById(R.id.ivLogo);
        headerView = (TextView)adView.findViewById(R.id.tvHeader);
        descriptionView = (TextView)adView.findViewById(R.id.tvDescription);
        advertiserView = (TextView)adView.findViewById(R.id.tvAdvertiser);
        imageView = (ImageView)adView.findViewById(R.id.ivImage);
        adActionBtn = (Button)adView.findViewById(R.id.btnAction);

        adView.setLogoView(logoView);
        adView.setHeadlineView(headerView);
        adView.setBodyView(descriptionView);
        adView.setAdvertiserView(advertiserView);
        if ( imageView != null ) {
            adView.setImageView(imageView);
        }
        adView.setCallToActionView(adActionBtn);
    }
}
