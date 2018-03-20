package com.globaldelight.boom.business;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;

import com.globaldelight.boom.BuildConfig;
import com.globaldelight.boom.utils.SecureStorage;
import com.globaldelight.boom.utils.Utils;
import com.globaldelight.boom.webapiconnector.Headset;
import com.globaldelight.boom.webapiconnector.RequestBody;
import com.globaldelight.boom.webapiconnector.ResponseBody;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by adarsh on 15/02/18.
 */

public class HeadsetConfiguration {

    public interface Callback {
        void onSuccess(List<Headset> headphones);
        void onFaiure();
    }

    private static final String CONFIG_FILENAME = "config";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private final static String HEADSET_CONFIG_URL=" http://boomb2b.globaldelight.net/v1/native/deviceconfig/";


    private static final String TAG = "HeadsetConfiguration";
    private Context mContext;
    private List<Headset> mSupportedHeadphones = null;


    public HeadsetConfiguration(Context context) {
        mContext = context;
    }

    public void fetchHeadsets(Callback callback) {
        if ( mSupportedHeadphones != null ) {
            callback.onSuccess(mSupportedHeadphones);
        }
        else if ( SecureStorage.exists(mContext, CONFIG_FILENAME) ) {
            load(callback);
            download(callback); // update the configuration
        }
        else {
            download(callback);
        }
    }


    private void download(final Callback callback) {
        try {

            final RequestBody requestBody=new RequestBody();
            requestBody.setAppid("com.tunesaudio.com");
            requestBody.setBuild(Integer.toString(BuildConfig.VERSION_CODE));
            requestBody.setCountry(Utils.getCountry(mContext));
            requestBody.setDeviceid(Utils.getDeviceId(mContext));
            requestBody.setLocale(Utils.getLocale(mContext));
            requestBody.setModel(Utils.getModelNumber());
            requestBody.setVendor("audiotunes");
            requestBody.setVersion(Utils.getVersionCode());

            okhttp3.RequestBody body = okhttp3.RequestBody.create(JSON, new Gson().toJson(requestBody));
            Request request = new Request.Builder()
                    .url(HEADSET_CONFIG_URL)
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient();
            client.newCall(request).enqueue(new okhttp3.Callback() {
                private final Handler mHandler = new Handler();

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    mHandler.post(()-> {
                        if ( callback != null ) {
                            callback.onFaiure();
                        }
                    });
                }

                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                    if ( response.isSuccessful() ) {
                        mHandler.post(()->{
                            try {
                                ResponseBody body = new Gson().fromJson(response.body().string(), ResponseBody.class);
                                analyzeResponse(body, callback);
                                store(body);
                            }
                            catch (IOException e) {
                                if ( callback != null ) {
                                    callback.onFaiure();
                                }
                            }
                        });
                    }

                }
            });

        }
        catch (Exception e) {
            if ( callback != null ) {
                callback.onFaiure();
            }
        }
    }


    private void load(final Callback callback) {
        new AsyncTask<Void, Void, ResponseBody>() {
            @Override
            protected ResponseBody doInBackground(Void... voids) {
                SecureStorage storage = new SecureStorage(CONFIG_FILENAME,mContext);
                byte[] data = storage.load();
                if ( data != null ) {
                    Gson gson = new GsonBuilder().create();
                    return gson.fromJson(new String(data), ResponseBody.class);
                }

                return null;
            }

            @Override
            protected void onPostExecute(ResponseBody responseBody) {
                super.onPostExecute(responseBody);
                if ( responseBody != null ) {
                    analyzeResponse(responseBody, callback);
                }
                else {
                    download(callback);
                }
            }
        }.execute();
    }

    private void store(final ResponseBody body) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    SecureStorage storage = new SecureStorage(CONFIG_FILENAME,mContext);
                    String json = new Gson().toJson(body);
                    byte[] data = json.getBytes("UTF-8");
                    storage.store(data);
                }
                catch (UnsupportedEncodingException e) {

                }

                return null;
            }
        }.execute();
    }


    private void analyzeResponse(ResponseBody response, Callback callback) {
        if ( response.getAppActiveState() ) {
            // Store the device list
            mSupportedHeadphones = response.getSupportedHeadphones();
            if ( callback != null ) {
                callback.onSuccess(mSupportedHeadphones);
            }
        }
        else {
            if ( callback != null ) {
                callback.onFaiure();
            }

            System.exit(1);
        }
    }
}
