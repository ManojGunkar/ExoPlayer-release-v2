package com.globaldelight.boom.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.globaldelight.boom.R;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class PermissionChecker {
    private static final String TAG = "PermissionChecker";

    public static final int STORAGE_READ_PERMISSION = 1000;
    public static final int STORAGE_WRITE_PERMISSION = 1001;
    public static final int ACCOUNTS_PERMISSION = 1002;

    private OnPermissionResponse response;
    private Context context;
    private Activity activity;
    private View baseView;
    private Handler handler;
    private int mRequestCode;

    public PermissionChecker(Activity activity, View baseView, int requestCode) {
        this.context = activity;
        this.activity = activity;
        this.baseView = baseView;
        mRequestCode = requestCode;
        handler = new Handler();
    }

    public void check(final String permission, final String customMsg,
                      final OnPermissionResponse response) {
        check(permission, customMsg, response, false);
    }

    public void check(final String permission, final String customMsg,
                      final OnPermissionResponse response, final boolean checkDirectly) {
        this.response = response;
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (ContextCompat.checkSelfPermission(context, permission) ==
                        PackageManager.PERMISSION_GRANTED) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            response.onAccepted();
                        }
                    });
                } else {
                    /*Log.v(TAG, "Waiting");
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                            permission) && null != activity && !checkDirectly && null != baseView ) {
                        try {
                            Snackbar.make(baseView, customMsg,
                                    Snackbar.LENGTH_INDEFINITE)
                                    .setAction(R.string.ok, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            ActivityCompat.requestPermissions(activity,
                                                    new String[]{permission},
                                                    REQUEST_CODE);
                                        }
                                    })
                                    .show();
                        }catch (Exception e){}
                    } else {*/
                        ActivityCompat.requestPermissions(activity,
                                new String[]{permission},
                                mRequestCode);
                    /*}*/
                }
            }
        }).start();
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, final
    @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        // Storage permission has been granted
                        response.onAccepted();
                    } else {
                        //Storage permission has been denied
                        response.onDecline();
                    }
                }
            });
        }
    }

    public interface OnPermissionResponse {
        void onAccepted();

        void onDecline();
    }

}
