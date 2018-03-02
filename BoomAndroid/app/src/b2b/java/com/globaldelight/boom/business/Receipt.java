package com.globaldelight.boom.business;

import android.util.JsonReader;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by adarsh on 11/12/17.
 */

public class Receipt {

    private static final String EMAIL_KEY = "emailId";
    private static final String VERSION_KEY = "version";
    private static final String BUILD_KEY = "build";
    private static final String CODE_KEY = "code";
    private static final String MODEL_KEY = "model";
    private static final String FINGERPRINT_KEY = "deviceid";


    private String mEmailId = "";
    private String mCode;
    private String mFingerPrint;
    private String mDeviceModel;
    private String mVersion;
    private int mBuild;

//
//    public Receipt(String email, String code, String fingerprint, String model ) {
//        mEmailId = email;
//        mCode = code;
//        mFingerPrint = fingerprint;
//        mDeviceModel = model;
//    }

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

    public String getVersion() {
        return mVersion;
    }


    public String toJSON() {
        try {
            JSONObject writer = new JSONObject();
            writer.put(EMAIL_KEY, mEmailId);
            writer.put(CODE_KEY, mCode);
            writer.put(FINGERPRINT_KEY, mFingerPrint);
            writer.put(MODEL_KEY, mDeviceModel);
            writer.put(VERSION_KEY, mVersion);
            writer.put(BUILD_KEY, mBuild);
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
            r.mCode = reader.getString(CODE_KEY);
            r.mFingerPrint = reader.getString(FINGERPRINT_KEY);
            r.mDeviceModel = reader.getString(MODEL_KEY);
            r.mVersion = reader.getString(VERSION_KEY);
            r.mBuild = reader.getInt(BUILD_KEY);
            if ( reader.has(EMAIL_KEY) ) {
                r.mEmailId = reader.getString(EMAIL_KEY);
            }
            return r;
        }
        catch (JSONException e) {
            return null;
        }
    }
}
