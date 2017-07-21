package com.manoj.youtube.web;

import com.manoj.youtube.modal.YoutubeModal;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.logging.HttpLoggingInterceptor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.http.GET;
import retrofit.http.Query;


/**
 * Created by Manoj on 11/01/2016.
 */

public class RestClient {
    private final static String URL = "https://www.googleapis.com";
    private static GitApiInterface gitApiInterface;
    private static HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor();

    public static GitApiInterface getClient() {
        if (gitApiInterface == null) {
            OkHttpClient okClient = new OkHttpClient();
            okClient.setReadTimeout(160, TimeUnit.SECONDS);
            okClient.setConnectTimeout(160, TimeUnit.SECONDS);
            okClient.interceptors().add(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Response response = chain.proceed(chain.request());
                    return response;
                }
            });
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);
            okClient.interceptors().add(httpLoggingInterceptor);

            Retrofit client = new Retrofit.Builder()
                    .baseUrl(URL)
                    .addConverter(String.class, new ToStringConverter())
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            gitApiInterface = client.create(GitApiInterface.class);
        }
        return gitApiInterface;
    }

    public interface GitApiInterface {

        @GET("/youtube/v3/search")
        Call<YoutubeModal> getYoutubeVideosList(
                @Query("key") String developerKey,
                @Query("channelId") String channelId,
                @Query("part") String part,
                @Query("order") String order,
                @Query("maxResults") String maxResults);

        /*
        https://www.googleapis.com/youtube/v3/search?
        part=snippet&
        maxResults=25&
        q=surfing&
        type=snippet&
        key=AIzaSyB2aX7JrPCTrcYPIBHXeCC7WmzdIRPIPGY
        */
        @GET("/youtube/v3/search")
        Call<YoutubeModal> searchYoutubeVideo(
                @Query("key") String developerKey,
                @Query("part") String part,
                @Query("type") String type,
                @Query("q") String searchQuery,
                @Query("maxResults") String maxResults);
    }

}


