package com.globaldelight.boom.radio.webconnector;


import android.content.Context;

import com.globaldelight.boom.R;
import com.globaldelight.boom.radio.webconnector.model.AccessTokenModel;
import com.globaldelight.boom.radio.webconnector.model.BaseResponse;
import com.globaldelight.boom.radio.webconnector.model.CategoryResponse;
import com.globaldelight.boom.radio.webconnector.model.Chapter;
import com.globaldelight.boom.radio.webconnector.model.RadioStationResponse;
import com.globaldelight.boom.radio.webconnector.model.RadioPlayResponse;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by Manoj Kumar on 05-04-2018.
 */

public class RadioRequestController {

    private static HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    private static OkHttpClient client;


    public static RequestCallback getClient(Context context, String url) throws CertificateException,
            NoSuchAlgorithmException, KeyStoreException,
            KeyManagementException, IOException,
            UnrecoverableKeyException {
        if (client == null) {
            client = new OkHttpClient.Builder()
                    .sslSocketFactory(sslCertificatePinning(context).getSocketFactory())
                    .addInterceptor(new CustomInterceptor(context))
                    .addInterceptor(interceptor)
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .hostnameVerifier((hostname, session) -> hostname.equals("service.globaldelight.api.radioline.fr")
                            || hostname.equals("test.auth.radioline.fr"))
                    .build();
        }
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        interceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        interceptor.setLevel(HttpLoggingInterceptor.Level.HEADERS);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(RequestCallback.class);

    }

    private static SSLContext sslCertificatePinning(Context context) throws IOException,
            KeyStoreException, NoSuchAlgorithmException, KeyManagementException,
            java.security.cert.CertificateException, UnrecoverableKeyException {

        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        Certificate ca;
        try (InputStream cert = context.getResources().openRawResource(R.raw.ca_sha2)) {
            ca = cf.generateCertificate(cert);
        }

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        InputStream in = context.getResources().openRawResource(R.raw.gdcert);
        keyStore.load(in, RadioApiUtils.CA_CERT_PASSWORD.toCharArray());
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, RadioApiUtils.CA_CERT_PASSWORD.toCharArray());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    public interface RequestCallback {

        @FormUrlEncoded
        @POST(RadioApiUtils.AUTH_URL)
        Call<AccessTokenModel> getToken(
                @Field(("grant_type")) String grantType,
                @Field(("device_serial")) String deviceSerial,
                @Field(("scope")) String scope,
                @Field(("client_id")) String clientId);

        @FormUrlEncoded
        @POST(RadioApiUtils.AUTH_URL)
        Call<AccessTokenModel> getRefreshToken(
                @Field(("grant_type")) String grantType,
                @Field(("refresh_token")) String refreshToken,
                @Field(("client_id")) String clientId);

        @GET("/Pillow/search")
        Call<RadioStationResponse> getLocalRadio(
                @Query("country") String location,
                @Query("type") String type,
                @Query("priority") String priority,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/search")
        Call<RadioStationResponse> getTagsRadioStation(
                @Query("tags") String tag,
                @Query("country") String location,
                @Query("type") String type,
                @Query("priority") String priority,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/{category}")
        Call<RadioStationResponse> getRadioStation(
                @Path(value = "category",encoded = true) String category,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        // Pillow/podcasts/historical_india_podcast/chapters?page=1&pageSize=10

        @GET("Pillow/{podcast}/chapters")
        Call<BaseResponse<Chapter>> getPodcastChapters(
                @Path(value = "podcast",encoded = true) String podcast,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/{category}")
        Call<CategoryResponse> getCategoryWiseStation(
                @Path(value = "category",encoded = true) String category,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/search")
        Call<RadioStationResponse> getSearchResult(
                @Query("type") String type,
                @Query("query") String query,
                @Query("priority") String priority,
                @Query("page") String page,
                @Query("pageSize") String pageSize);


        @GET("/Pillow/categories/radioline_countries")
        Call<CategoryResponse> getCountry(
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/search")
        Call<RadioStationResponse> getPopularStation(
                @Query("type") String type,
                @Query("priority") String priority,
                @Query("page") String page,
                @Query("pageSize") String pageSize);

        @GET("/Pillow/{permalink}/play")
        Call<RadioPlayResponse> getRadioPlayService(
                @Path(encoded = true, value = "permalink") String id);

    }

}
