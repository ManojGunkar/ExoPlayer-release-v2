package com.globaldelight.boom.business;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.globaldelight.boom.BuildConfig;

import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.security.auth.x500.X500Principal;

import static android.content.Context.MODE_PRIVATE;

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
        if ( mReceipt != null && mReceipt.isValid() ) {
            callback.onSuccess();
            return;
        }

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
                if ( receipt != null && receipt.isValid() ) {
                    mReceipt = receipt;
                    callback.onSuccess();
                }
                else {
                    callback.onError(ERROR_NO_LICENSE);
                }
            }
        }.execute();
    }

    // Verify the promo code
    public void verifyCode(String code, final Callback callback) {
        final String promoCode = code;
        new AsyncTask<Void, Void, Receipt>() {
            @Override
            protected Receipt doInBackground(Void... voids) {
                SecureStorage store = new SecureStorage("receipt", mContext);
                if ( promoCode.equalsIgnoreCase("GDPL12345") ) {
                    String json = "{\"emailId\": \"someone@somehwere.com\", \"isValid\": true, \"code\": \"GDPL12345\", \"fingerprint\": \"12345\"}";
                    store.store(json.getBytes());
                    return Receipt.fromJSON(json);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Receipt receipt) {
                super.onPostExecute(receipt);
                if ( receipt != null && receipt.isValid() ) {
                    mReceipt = receipt;
                    callback.onSuccess();
                }
                else {
                    callback.onError(ERROR_INVALID_CODE);
                }
            }
        }.execute();
    }

    // Validate the app license with the server
    public void validateLicense(Callback callback) {
    }

}
