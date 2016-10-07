package com.player.ui.widgets;


import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.player.myspotifymusic.R;

public class HeaderView extends LinearLayout{

    /*@InjectView(R.id.header_view_title) */TextView title;
    /*@InjectView(R.id.header_view_sub_title) */TextView subTitle;
    /*@InjectView(R.id.header_view_count) */TextView count;

    public HeaderView(Context context) {
        super(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public HeaderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        /*ButterKnife.inject(this);*/
    }

    public void bindTo(String title) {
        init();
        bindTo(title, "");
    }

    public void bindTo(String title, String subTitle) {
        init();
        hideOrSetText(this.title, title);
        hideOrSetText(this.subTitle, subTitle);
    }

    public void bindTo(String title, String subTitle, String count) {
        init();
        hideOrSetText(this.title, title);
        hideOrSetText(this.subTitle, subTitle);
        hideOrSetText(this.count, count);
    }

    private void hideOrSetText(TextView tv, String text) {
        if (text == null || text.equals(""))
            tv.setVisibility(GONE);
        else
            tv.setText(text);
    }

    private void setBackgroundColor(Color color){
        this.setBackgroundColor(color);
    }

    private void init(){
        this.title = (TextView)findViewById(R.id.header_view_title) ;
        this.subTitle = (TextView)findViewById(R.id.header_view_sub_title) ;
        this.count = (TextView)findViewById(R.id.header_view_count) ;
    }


}
