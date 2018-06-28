package com.globaldelight.boom.spotify.apiconnector;

import com.globaldelight.boom.spotify.pojo.AlbumPlaylist;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

import static com.globaldelight.boom.spotify.apiconnector.SpotifyApiUrls.BASE_URL;

/**
 * Created by Manoj Kumar on 10/26/2017.
 */

public class ApiRequestController {

    private final static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    public static RequestCallback requestCallback;
    private static OkHttpClient client;


    public static RequestCallback getClient() {

        if (client == null) {
            client = new OkHttpClient.Builder()
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build();
        }

        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(RequestCallback.class);
    }


    public interface RequestCallback {

        @GET(SpotifyApiUrls.BROWSE_RELEASE_ALBUM_URL)
        Call<NewReleaseAlbums> getSpotifyAlbum(@Header("Authorization") String authToken);


        @GET(SpotifyApiUrls.ALBUM_LIST_URL)
        Call<AlbumPlaylist> getAlbumPlayList(
                @Path("albumId") String albumId,
                @Header("authorization") String authToken);

    }

}
