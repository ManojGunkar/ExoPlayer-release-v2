package com.globaldelight.boom.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.globaldelight.boom.tidal.tidalconnector.TidalRequestController;
import com.globaldelight.boom.tidal.tidalconnector.model.response.TidalBaseResponse;
import com.globaldelight.boom.tidal.ui.fragment.TidalNewFragment;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */

// Class for executing retrofit requests in sequentially
public class RequestChain {

    public interface  Callback<T> {
        void onResponse(T resp);
    }

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context context;

    public RequestChain(Context context) {
        this.context = context;
    }

    public <T> void submit(Call<T> call, Callback<T> callback) {
        executor.submit(() -> {
                T body = null;
                try {
                    if ( call != null ) {
                        Response<T> resp = call.execute();
                        if ( resp.isSuccessful() ) {
                            body = resp.body();
                        }
                    }
                }
                catch (IOException e) {
                }

                final T response = body;
                mainHandler.post(()->callback.onResponse(response));
        });
    }

    public void cancel() {
        executor.shutdownNow();
    }
}
