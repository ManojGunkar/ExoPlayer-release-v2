package com.globaldelight.boom.app.webconnector;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.Converter;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Manoj Kumar on 6/28/2017.
 */

public class WebClient {

    //ToDO Define web service url to access business restful api from server
    private final static String URL = "https://****prefix url****";

    private static Callback callback;
    private static HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

    public static Callback getClient() {
        if (callback == null) {
            OkHttpClient okClient = new OkHttpClient();
            okClient.setReadTimeout(160, TimeUnit.SECONDS);
            okClient.setConnectTimeout(160, TimeUnit.SECONDS);
            okClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    return response;
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            okClient.interceptors().add(httpLoggingInterceptor);

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverter(String.class, new StringConverter())
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            callback = client.create(Callback.class);
        }
        return callback;
    }

    private static class StringConverter implements Converter<String> {

        @Override
        public String fromBody(ResponseBody body) throws IOException {
            return body.string();
        }

        @Override
        public RequestBody toBody(String value) {
            return RequestBody.create(MediaType.parse("text/plain"), value);
        }
    }

    public interface Callback {

        @GET("/**postfix url****")
        Call<?> getBusinessDetails(
                @Query("**") String query);

    }

}
