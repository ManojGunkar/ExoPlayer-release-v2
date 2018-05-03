package com.globaldelight.boom.tidal.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.fragment.TidalNewFragment;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */
public class RequestChain {

    public interface APICall {
        Call<TidalBaseResponse> operation(String token,String countryCode,String offSet,String limit);
    }

    public interface  Callback {
        void onResponse(TidalBaseResponse tidalBaseResponse);
    }


    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context context;

    public RequestChain(Context context) {
        this.context = context;
    }

    public void submit(APICall api, Callback callback) {
        Call<TidalBaseResponse> call = api != null? api.operation(TidalRequestController.AUTH_TOKEN, "US", "0", "6") : null;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                TidalBaseResponse body = null;
                try {
                    if ( call != null ) {
                        Response<TidalBaseResponse> resp = call.execute();
                        if ( resp.isSuccessful() ) {

                            body = resp.body();
                        }
                    }
                }
                catch (IOException e) {
                }

                final TidalBaseResponse response = body;
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onResponse(response);
                    }
                });
            }
        });
    }

    public void cancel() {
        executor.shutdownNow();
    }

}
