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
    private static final String MODEL_KEY = "model";
    private static final String FINGERPRINT_KEY = "fingerprint";


    private String mEmailId;
    private String mCode;
    private String mFingerPrint;
    private String mDeviceModel;

    public Receipt(String email, String code, String fingerprint, String model ) {
        mEmailId = email;
        mCode = code;
        mFingerPrint = fingerprint;
        mDeviceModel = model;
    }

    private Receipt() {
    }

    public String getEmail() {
        return mEmailId;
    }

    public String getCode() {
        return mCode;
    }

    public String getModel() {
        return mDeviceModel;
    }

    public String getFingerPrint() {
        return mFingerPrint;
    }

    public String toJSON() {
        try {
            JSONObject writer = new JSONObject();
            writer.put(EMAIL_KEY, getEmail());
            writer.put(CODE_KEY, getCode());
            writer.put(FINGERPRINT_KEY, getFingerPrint());
            writer.put(MODEL_KEY, getModel());
            return writer.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Receipt fromJSON(String json) {
        try {
            JSONObject reader = new JSONObject(json);
            Receipt r = new Receipt();
            r.mEmailId = reader.getString(EMAIL_KEY);
            r.mCode = reader.getString(CODE_KEY);
            r.mFingerPrint = reader.getString(FINGERPRINT_KEY);
            r.mDeviceModel = reader.getString(MODEL_KEY);
            return r;
        }
        catch (JSONException e) {
            return null;
        }
    }
}
