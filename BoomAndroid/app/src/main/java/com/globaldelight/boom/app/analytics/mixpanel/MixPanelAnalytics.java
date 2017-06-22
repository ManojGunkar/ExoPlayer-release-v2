package com.globaldelight.boom.app.analytics.mixpanel;

import android.content.Context;
import android.provider.Settings;

import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.mixpanel.android.mpmetrics.MixpanelAPI;

/**
 * Created by Manoj Kumar on 6/19/2017.
 */

public class MixPanelAnalytics {

    private final static String MIX_PANEL_TOKEN="79a795acec1fbcc3d9d28f792c51fc24";
    private final static String MIX_PANEL_SECRET="b251d9a92d59bb458ece6a93225190fd";

    private Context context;
    private static MixPanelAnalytics instance;

    private MixpanelAPI mixpanelAPI;

    private MixPanelAnalytics(Context context){
        this.context=context;
        mixpanelAPI=MixpanelAPI.getInstance(this.context,MIX_PANEL_TOKEN);
    }

    public synchronized static MixPanelAnalytics getInstance(Context context){
        if (instance==null)instance=new MixPanelAnalytics(context);
        return instance;
    }

    private String getAndroidId(){
       return Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    public void finish(){
        if (mixpanelAPI!=null)mixpanelAPI.flush();
    }
}
