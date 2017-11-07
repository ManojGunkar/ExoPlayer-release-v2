package com.globaldelight.boom.app.share;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.view.onBoarding.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SHARE_SUCCESS;

/**
 * Created by adarsh on 24/08/17.
 */

public class ShareDialog implements SharePagerAdapter.OnItemClickListener {

    private Activity mActivity;
    private Dialog mDialog = null;
    private ViewPager viewpager;
    private TabLayout indicator;

    private static final String OTHER_APPS_NAME = "other";
    private static final String DEFAULT_EMAIL_APP_NAME = "email";

    private static final String[] SUPPORTED_APPS = new String[] {
            "com.facebook.katana",
            "com.twitter.android",
            "com.whatsapp",
            "com.android.mms",
            "com.bsb.hike",
    };


    public ShareDialog(Activity activity) {
        mActivity = activity;
    }

    private ArrayList<ShareItem> getShareableAppList() {
        final ArrayList<ShareItem> items = new ArrayList<>();

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setDataAndType(Uri.parse("Boom android music app"), "text/*");
        List<ResolveInfo> shareableApps = mActivity.getPackageManager().queryIntentActivities(intent, 0);
        for ( String supportedAppPackage : SUPPORTED_APPS ) {
            for ( ResolveInfo info : shareableApps ) {
                if ( info.activityInfo.packageName.equalsIgnoreCase(supportedAppPackage) ) {
                    String label = info.loadLabel(mActivity.getPackageManager()).toString();
                    Drawable icon = info.loadIcon(mActivity.getPackageManager());
                    String packageName = info.activityInfo.packageName;
                    ShareItem item = new ShareItem(label, packageName, icon);
                    items.add(item);
                    break;
                }
            }
        }

        //add email
        items.add(new ShareItem(DEFAULT_EMAIL_APP_NAME, "", mActivity.getDrawable(R.drawable.ic_email)));

        //add other
        items.add(new ShareItem(OTHER_APPS_NAME, "", mActivity.getDrawable(R.drawable.ic_others)));

        return items;
    }

    public void show() {
        mDialog = new Dialog(mActivity);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setCancelable(true);
        mDialog.setContentView(R.layout.popup_share);

        viewpager = (ViewPager) mDialog.findViewById(R.id.pager_share);
        indicator = (TabLayout) mDialog.findViewById(R.id.indicator_home_tab_layout);
        viewpager.setAdapter(new SharePagerAdapter(mActivity, getShareableAppList(), this));
        indicator.setupWithViewPager(viewpager, true);
        viewpager.setCurrentItem(0);

        mDialog.show();
    }

    @Override
    public void onClick(ShareItem item) {
        Context context = mActivity;
        mDialog.dismiss();
        if (item.text.equalsIgnoreCase("facebook") ){
            ShareUtils.getInstance(context).fbShare();
        }
        else if ( item.text.equalsIgnoreCase(DEFAULT_EMAIL_APP_NAME) ) {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"));
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_desc));
            intent.putExtra(Intent.EXTRA_TEXT, "");
            Intent mailer = Intent.createChooser(intent, null);
            context.startActivity(mailer);
        }
        else if ( item.text.equalsIgnoreCase(OTHER_APPS_NAME) ) {
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
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));
    }
}