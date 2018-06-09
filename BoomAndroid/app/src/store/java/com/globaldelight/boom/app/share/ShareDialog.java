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
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
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

    // A proxy activity to obtain share result.
    public static class ProxyActivity extends Activity {
        private static final int SHARE_REQUEST_CODE = 1234;
        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            String appId = getIntent().getStringExtra("app-name");
            String pkgName = getIntent().getStringExtra("package-name");

            String sAux = getString(R.string.share_desc);
            sAux = sAux + "\nhttps://play.google.com/store/apps/details?id=com.globaldelight.boom \n";

            if (appId.equalsIgnoreCase("facebook") ){
                ShareUtils.fbShare(this);
                finish();
            }
            else if ( appId.equalsIgnoreCase(DEFAULT_EMAIL_APP_NAME) ) {
                String uriText = "mailto:" +
                                "?subject=" + Uri.encode(getString(R.string.share_desc)) +
                                "&body=" + Uri.encode(sAux);

                Uri uri = Uri.parse(uriText);
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivityForResult(sendIntent, SHARE_REQUEST_CODE);
            }
            else if ( appId.equalsIgnoreCase(OTHER_APPS_NAME) ) {
                Intent shareIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
                shareIntent.putExtra(Intent.EXTRA_TEXT, sAux);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                startActivityForResult(Intent.createChooser(shareIntent, "share"), SHARE_REQUEST_CODE);
            }
            else {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setPackage(pkgName);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_desc));
                sendIntent.setType("text/plain");
                startActivityForResult(sendIntent, SHARE_REQUEST_CODE);
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if ( SHARE_REQUEST_CODE == requestCode ) {
                LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));
            }
            finish();
        }
    }

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

        Intent proxyIntent = new Intent(context, ProxyActivity.class);
        proxyIntent.putExtra("app-name", item.text);
        proxyIntent.putExtra("package-name", item.pkgName);
        context.startActivity(proxyIntent);

    }
}