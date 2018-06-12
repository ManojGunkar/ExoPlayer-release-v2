package com.globaldelight.boom.business;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by adarsh on 12/06/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class BusinessPopup {

    public interface Actions {
        void onPrimaryAction();
        void onSecondaryAction();
        void onCancel();
    }

    private static final String TAG = "BusinessPopup";

    private boolean mAlertIsVisible = false;
    private Activity mCurrentActivity = null;
    private Runnable mPendingAlert = null;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public void showPending(Activity parent) {
        mCurrentActivity = parent;
        if ( mCurrentActivity != null && mPendingAlert != null ) {
            mMainHandler.post(mPendingAlert);
        }
    }


    public void show(Activity parent, final String title, final String message, final String primaryTitle, final String secondaryTitle, final Actions callback) {

        if (mAlertIsVisible) {
            Log.d(TAG, "Skipped alert with message: " + message);
        }

        mPendingAlert = new Runnable() {
            @Override
            public void run() {
                if ( mCurrentActivity == null ) {
                    return;
                }

                mAlertIsVisible = true;
                MaterialDialog.Builder builder = Utils.createDialogBuilder(mCurrentActivity);
                builder.buttonsGravity(GravityEnum.CENTER)
                        .content(message)
                        .contentGravity(GravityEnum.CENTER)
                        .canceledOnTouchOutside(false)
                        .titleGravity(GravityEnum.CENTER)
                        .title(title);

                if ( primaryTitle != null ) {
                    builder.positiveText(primaryTitle);
                }
                if ( secondaryTitle != null ) {
                    builder.negativeText(secondaryTitle);
                }

                if ( callback != null ) {
                    builder.onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            mAlertIsVisible = false;
                            callback.onSecondaryAction();

                        }
                    })
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mAlertIsVisible = false;
                                    callback.onPrimaryAction();
                                }
                            })
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mAlertIsVisible = false;
                                    callback.onCancel();
                                }
                            })
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    mAlertIsVisible = false;
                                    callback.onCancel();
                                }
                            });
                }
                builder.show();
                mPendingAlert = null;
            }
        };

        if ( mCurrentActivity != null ) {
            mMainHandler.post(mPendingAlert);
            mPendingAlert = null;
        }
    }

}
