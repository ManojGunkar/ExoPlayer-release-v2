package com.globaldelight.boom.business;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;

import com.globaldelight.boom.utils.SecureStorage;
import com.google.android.gms.iid.InstanceID;

/**
 * Created by adarsh on 11/12/17.
 */

// Manages B2B App License
public class LicenseManager {

    // No network connection
    public static final int ERROR_NO_NETWORK = -1;

    // Invalid promo code
    public static final int ERROR_INVALID_CODE = -2;

    // App license doesn't match with the server (possible hack)
    public static final int ERROR_INVALID_LICENSE = -3;

    // App doesn't have a license
    public static final int ERROR_NO_LICENSE = -4;

    public interface Callback {
        void onSuccess();
        void onError(int errorCode);
    }

    private Context mContext;
    private Receipt mReceipt;

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

        //
        new AsyncTask<Void, Void, Receipt>() {
            @Override
            protected Receipt doInBackground(Void... voids) {
                SecureStorage store = new SecureStorage("receipt", mContext);
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
        new AsyncTask<Void, Void, Receipt>() {
            @Override
            protected Receipt doInBackground(Void... voids) {
                B2BApi.Result<Receipt> result = B2BApi.getInstance(mContext).verify(promoCode);
                if ( result.getStatus() == 0 ) {
                    Receipt receipt = result.getResult();
                    SecureStorage store = new SecureStorage("receipt", mContext);
                    store.store(receipt.toJSON().getBytes());
                    return receipt;
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

    // Validate the app license with the server
    public void validateLicense(Callback callback) {
    }


    private void verifyReceipt(Callback callback) {
        String iid = InstanceID.getInstance(mContext).getId();
        if ( mReceipt != null && mReceipt.getModel().equals(Build.MODEL) && mReceipt.getFingerPrint().equals(iid) ) {
            callback.onSuccess();
        }
        else {
            mReceipt = null;
            callback.onError(ERROR_NO_LICENSE);
        }
    }
}
