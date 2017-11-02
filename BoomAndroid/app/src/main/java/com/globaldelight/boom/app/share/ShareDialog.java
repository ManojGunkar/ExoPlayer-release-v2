package com.globaldelight.boom.app.share;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Window;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.view.onBoarding.CircleIndicator;

import java.util.ArrayList;
import java.util.List;

import static com.globaldelight.boom.app.fragments.ShareFragment.ACTION_SHARE_FAILED;
import static com.globaldelight.boom.app.fragments.ShareFragment.ACTION_SHARE_SUCCESS;

/**
 * Created by adarsh on 24/08/17.
 */

public class ShareDialog {

    private Activity mActivity;
    private MaterialDialog mDialog = null;
    private ViewPager viewpager;
    private CircleIndicator indicator;

    private static final String[] SUPPORTED_APPS = new String[] {
            "com.facebook.katana",
            "com.twitter.android",
            "com.whatsapp",
            "com.android.mms",
            "com.linkedin.android"
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
        items.add(new ShareItem("email", "", mActivity.getDrawable(R.drawable.ic_email)));

        //add other
        items.add(new ShareItem("other", "", mActivity.getDrawable(R.drawable.ic_others)));

        return items;
    }

    public void show() {
        final Dialog dialog = new Dialog(mActivity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.popup_share);

        viewpager = (ViewPager) dialog.findViewById(R.id.pager_share);
        indicator = (CircleIndicator) dialog.findViewById(R.id.indicator_share);
        viewpager.setAdapter(new SharePagerAdapter(mActivity, getShareableAppList()));
        indicator.setViewPager(viewpager);
        viewpager.setCurrentItem(0);

        dialog.show();
    }

/*
    public void show() {
        ShareAdapter adapter = new ShareAdapter(mActivity, getShareableAppList(), new ShareAdapter.Callback() {
            @Override
            public void onItemSelected() {
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));
                mDialog.dismiss();
            }
        });
        RecyclerView recyclerView = (RecyclerView) mActivity.getLayoutInflater()
                .inflate(R.layout.recycler_view_layout, null);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        recyclerView.setAdapter(adapter);

        mDialog = new MaterialDialog.Builder(mActivity)
                .title("Share Now")
                .backgroundColor(ContextCompat.getColor(mActivity, R.color.white))
                .titleColor(ContextCompat.getColor(mActivity, R.color.black))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .customView(recyclerView, false)
                .autoDismiss(false)
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(ACTION_SHARE_FAILED));
                    }
                })
                .show();
        Point point = new Point();
        mActivity.getWindowManager().getDefaultDisplay().getSize(point);
        int ScreenWidth = point.x;
        int ScreenHeight = point.y;
        mDialog.getWindow().setLayout((ScreenWidth * 80) / 100, (ScreenHeight * 60) / 100);
    }
*/

}