package com.globaldelight.boom.business;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adarsh on 11/12/17.
 */

public class Receipt {

    private static final String EMAIL_KEY = "emailId";
    private static final String CODE_KEY = "code";
    private static final String VALID_KEY = "isValid";
    private static final String FINGERPRINT_KEY = "fingerprint";


    private String mEmailId;
    private String mCode;
    private String mFingerPrint;
    private boolean mIsValid;

    public String getEmail() {
        return mEmailId;
    }

    public String getCode() {
        return mCode;
    }

    public Boolean isValid() {
        return mIsValid;
    }

    public String getFingerPrint() {
        return mFingerPrint;
    }

    public static Receipt fromJSON(String json) {
        try {
            JSONObject reader = new JSONObject(json);
            Receipt r = new Receipt();
            r.mEmailId = reader.getString(EMAIL_KEY);
            r.mCode = reader.getString(CODE_KEY);
            r.mFingerPrint = reader.getString(FINGERPRINT_KEY);
            r.mIsValid = reader.getBoolean(VALID_KEY);
            return r;
        }
        catch (JSONException e) {
            return null;
        }
    }

    public static String toJSON(Receipt receipt) {
        try {
            JSONObject writer = new JSONObject();
            writer.put(EMAIL_KEY, receipt.getEmail());
            writer.put(CODE_KEY, receipt.getCode());
            writer.put(FINGERPRINT_KEY, receipt.getFingerPrint());
            writer.put(VALID_KEY, receipt.isValid());
            return writer.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
