package com.globaldelight.boom.app.share;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.activities.MainActivity;
import com.globaldelight.boom.app.share.ShareAdapter;
import com.globaldelight.boom.app.share.ShareItem;

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


    public ShareDialog(Activity activity) {
        mActivity = activity;
    }

    private List<ShareItem> getShareableAppList(){
        final List<ShareItem> items=new ArrayList<>();

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setDataAndType(Uri.parse("Boom android music app"), "text/*");
        List<ResolveInfo> audio = mActivity.getPackageManager().queryIntentActivities(intent, 0);
        for (ResolveInfo info : audio){
            String label = info.loadLabel(mActivity.getPackageManager()).toString();
            Drawable icon = info.loadIcon(mActivity.getPackageManager());
            String packageName = info.activityInfo.packageName;
            String name = info.activityInfo.name.toLowerCase();
            ShareItem item=new ShareItem(label , packageName ,icon );
            if (name.contains("facebook")) {
                items.add(item);
            }else if (name.contains("twitter")) {
                items.add(item);
            }else if (name.contains("gmail")){
                items.add(item);
            }else if (name.contains("inbox")){
                items.add(item);
            }else if (name.contains("email")){
                items.add(item);
            }else if (name.contains("outlook")){
                items.add(item);
            }else if (name.contains("linkedin")){
                items.add(item);
            }else if (name.contains("whatsapp")){
                items.add(item);
            }else if (packageName.contains("message")){
                items.add(item);
            }

        }
        return items;
    }

    public void show() {
        ShareAdapter adapter=new ShareAdapter(mActivity, getShareableAppList(), new ShareAdapter.Callback() {
            @Override
            public void onItemSelected() {
                LocalBroadcastManager.getInstance(mActivity).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));
                mDialog.dismiss();
            }
        });
        RecyclerView recyclerView = (RecyclerView)mActivity.getLayoutInflater()
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
        mDialog.getWindow().setLayout((ScreenWidth *80)/100, (ScreenHeight *60)/100);
    }


}