package com.globaldelight.boom.ui.musiclist;

/**
 * Created by Rahul Agarwal on 04-10-16.
 */

public class ListDetail {

    private String mTitle;
    private String mSubTitle;
    private String mDetail;

    public ListDetail(String title, String subTitle, String detail){
        this.mTitle = title;
        this.mSubTitle = subTitle;
        this.mDetail = detail;
    }

    public String getmTitle() {
        return mTitle;
    }

    public String getmSubTitle() {
        return mSubTitle;
    }

    public String getmDetail() {
        return mDetail;
    }
}
