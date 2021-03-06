package com.globaldelight.boom.app.share;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;

import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SHARE_FAILED;
import static com.globaldelight.boom.app.receivers.actions.PlayerEvents.ACTION_SHARE_SUCCESS;

/**
 * Created by Manoj Kumar on 8/28/2017.
 */

public class ShareUtils {
    private ShareUtils() {
    }

    public static void fbShare(final Activity context){
        CallbackManager callbackManager = CallbackManager.Factory.create();
        ShareDialog shareDialog = new ShareDialog(context);
        if (ShareDialog.canShow(ShareLinkContent.class)) {
            ShareLinkContent linkContent = new ShareLinkContent.Builder()
                    .setContentUrl(Uri.parse("http://developers.facebook.com/android"))
                    .build();
            shareDialog.show(linkContent, ShareDialog.Mode.FEED);
        }
        shareDialog.registerCallback(callbackManager, new FacebookCallback<Sharer.Result>() {
            @Override
            public void onSuccess(Sharer.Result result) {
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SHARE_SUCCESS));}

            @Override
            public void onCancel() {
              //  ShareFragment.this.onCancel();
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SHARE_FAILED));
            }

            @Override
            public void onError(FacebookException error) {
             //   ShareFragment.this.onCancel();
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(ACTION_SHARE_FAILED));
            }
        });
    }
}
