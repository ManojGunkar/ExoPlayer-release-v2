package com.globaldelight.boom.business;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;

import com.globaldelight.boom.utils.SecureStorage;
import com.globaldelight.boom.utils.Utils;

/**
 * Created by adarsh on 11/12/17.
 */

// Manages B2B App License
public class LicenseManager {

    public interface Callback {
        void onSuccess();
        void onError(@ErrorCode int errorCode);
    }

    private Context mContext;
    private Receipt mReceipt;

    private static final String RECEIPT_NAME = "receipt";

    private static LicenseManager sInstance = null;

    public static LicenseManager getInstance(Context context) {
        if ( sInstance == null ) {
            sInstance = new LicenseManager(context.getApplicationContext());
        }
        return sInstance;
    }

    public LicenseManager(Context context) {
        mContext = context;
    }

    // Check if the app has license
    public void checkLicense(final Callback callback) {

        // if the receipt is already loaded just verify the receipt
        if ( mReceipt != null ) {
            verifyReceipt(callback);
            return;
        }

        if ( !SecureStorage.exists(mContext,RECEIPT_NAME) ) {
            callback.onError(ErrorCode.NO_LICENSE);
            return;
        }

        //
        new AsyncTask<Void, Void, Receipt>() {
            @Override
            protected Receipt doInBackground(Void... voids) {
                SecureStorage store = new SecureStorage(RECEIPT_NAME, mContext);
                byte[] data = store.load();
                if ( data != null ) {
                    return Receipt.fromJSON(new String(data));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Receipt receipt) {
                super.onPostExecute(receipt);
                mReceipt = receipt;
                verifyReceipt(callback);
            }
        }.execute();
    }

    // Verify the promo code
    public void verifyCode(String code, final Callback callback) {
        final String promoCode = code;
        new AsyncTask<Void, Void, Result<Receipt>>() {
            @Override
            protected Result<Receipt> doInBackground(Void... voids) {
                Result<Receipt> result = B2BApi.getInstance(mContext).verify(promoCode);
                if ( result.isSuccess() ) {
                    Receipt receipt = result.getObject();
                    SecureStorage store = new SecureStorage(RECEIPT_NAME, mContext);
                    store.store(receipt.toJSON().getBytes());
                }
                return result;
            }

            @Override
            protected void onPostExecute(Result<Receipt> result) {
                super.onPostExecute(result);
                if ( result.isSuccess() ) {
                    mReceipt = result.getObject();
                    verifyReceipt(callback);
                }
                else {
                    callback.onError(result.getStatus());
                }
            }
        }.execute();
    }

    // Validate the app license with the server
    public void validateLicense(Callback callback) {
    }


    private void verifyReceipt(Callback callback) {
        String iid = Utils.getFingerPrint(mContext);
        if ( mReceipt != null && mReceipt.getModel().equals(Build.MODEL) && mReceipt.getFingerPrint().equals(iid) ) {
            callback.onSuccess();
        }
        else {
            mReceipt = null;
            callback.onError(ErrorCode.NO_LICENSE);
        }
    }
}
