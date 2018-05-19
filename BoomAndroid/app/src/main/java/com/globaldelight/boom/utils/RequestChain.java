package com.globaldelight.boom.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Response;

/**
 * Created by adarsh on 03/05/18.
 * ©Global Delight Technologies Pvt. Ltd.
 */

// Class for executing retrofit requests in sequentially
public class RequestChain {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private Context context;
    public RequestChain(Context context) {
        this.context = context;
    }

    public <T> void submit(Call<T> call, Callback<T> callback) {
        submit(() -> {
                    Response<T> response = call.execute();
                    if (response.isSuccessful()) {
                        return Result.success(response.body());
                    }
                    return Result.error(response.code(), response.message());
                },
                (result) -> {
                    if (result.isSuccess()) {
                        callback.onResponse(result.get());
                    }
                });
    }

    public <T> void submit(Callable<Result<T>> call, Callback<Result<T>> callback) {
        executor.submit(()->{
            Result<T> result;
            try {
                result = call.call();
            }
            catch (Exception e) {
                result = Result.error(-1, e.getMessage());
            }

            if ( callback != null ) {
                final Result<T> res = result;
                mainHandler.post(()->callback.onResponse(res));
            }
        });
    }

    // Just submit a callback - will be executed after all previous operations are done
    public <T> void submit(Callback<Result<T>> callback) {
        executor.submit(()->mainHandler.post(()->callback.onResponse(Result.success(null))));
    }

    public void cancel() {
        executor.shutdownNow();
    }

    public interface Callback<T> {
        void onResponse(T resp);
    }
}
