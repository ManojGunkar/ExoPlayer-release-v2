package com.globaldelight.boom.spotify.apiconnector;

import com.globaldelight.boom.spotify.pojo.AlbumPlaylist;
import com.globaldelight.boom.spotify.pojo.NewReleaseAlbums;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

/**
 * Created by Manoj Kumar on 10/26/2017.
 */

public class ApiRequestController {

    public static RequestCallback requestCallback;
    private final static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();


    public static RequestCallback getClient(){

        if (requestCallback==null){
            interceptor.setLevel( HttpLoggingInterceptor.Level.BODY );

            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SpotifyApiUrls.BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            requestCallback= retrofit.create(RequestCallback.class);
        }
        return requestCallback;
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
