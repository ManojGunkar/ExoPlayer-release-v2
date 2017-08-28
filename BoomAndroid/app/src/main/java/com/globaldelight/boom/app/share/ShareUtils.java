package com.globaldelight.boom.app.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import static com.globaldelight.boom.app.fragments.ShareFragment.ACTION_SHARE_FAILED;

/**
 * Created by Manoj Kumar on 8/28/2017.
 */

public class ShareUtils {
    private static ShareUtils ourInstance;
    private static Context context;

    public static ShareUtils getInstance(Context context) {
        if (ourInstance==null)ourInstance=new ShareUtils(context);
        return ourInstance;
    }

    private ShareUtils(Context context) {
        this.context=context;
    }
    public static void fbShare(){
        CallbackManager callbackManager = CallbackManager.Factory.create();
        ShareDialog shareDialog = new ShareDialog((Activity) context);
        if (ShareDialog.canShow(ShareLinkContent.class)) {


            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();
            shareDialog.show(linkContent, ShareDialog.Mode.FEED);
        }
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
               // ShareFragment.this.onSucess();
                Toast.makeText(context,"OnSuccess",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancel() {
              //  ShareFragment.this.onCancel();
                context.sendBroadcast(new Intent(ACTION_SHARE_FAILED));
                Toast.makeText(context,"OnCancel",Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onError(FacebookException error) {
             //   ShareFragment.this.onCancel();
                context.sendBroadcast(new Intent(ACTION_SHARE_FAILED));
                Toast.makeText(context,"OnError"+error.getMessage(),Toast.LENGTH_SHORT).show();
            }
        });
    }
}
