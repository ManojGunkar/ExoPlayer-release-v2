package com.globaldelight.boom.spotify.apiconnector;


import com.globaldelight.boom.spotify.pojo.Album;
import com.globaldelight.boom.spotify.pojo.Artist;
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
import retrofit.http.Header;
import retrofit.http.Query;
import retrofit.http.Url;


/**
 * Created by Manoj on 23/10/2017.
 */

public class SpotifyApiConnector {

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
                    .baseUrl(SpotifyApiUrls.BASE_URL)
                    .addConverter(String.class, new ToStringConverter())
                    .client(okClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            gitApiInterface = client.create(GitApiInterface.class);
        }
        return gitApiInterface;
    }

    public interface GitApiInterface {

        @GET(SpotifyApiUrls.BROWSE_RELEASE_ALBUM_URL)
        Call<Album> getSpotifyAlbum(
                @Header("authorization") String authToken,
                @Header("Accept") String accept,
                @Header("Content-Type") String contentType);

        @GET
        Call<Artist> getSongList(
                @Url String url,
                @Header("authorization") String authToken,
                @Header("Accept") String accept,
                @Header("Content-Type") String contentType);

    }

}


