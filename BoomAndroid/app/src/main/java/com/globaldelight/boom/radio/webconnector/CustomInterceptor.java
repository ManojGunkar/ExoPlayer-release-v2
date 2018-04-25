package com.globaldelight.boom.radio.webconnector;

import android.content.Context;

import com.globaldelight.boom.radio.webconnector.model.AccessTokenModel;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;

/**
 * Created by Manoj Kumar on 05-04-2018.
 */

public class CustomInterceptor implements Interceptor {

    private String accessToken;
    private String refreshToken;
    private Context context;

    public CustomInterceptor(Context context){
        this.context=context;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (chain.request().url().host().equals("test.auth.radioline.fr")) {
            return chain.proceed(original);
        }

        Request request = original.newBuilder()
                .header("Authorization", "Bearer "+accessToken)
                .method(original.method(), original.body())
                .build();

        Response response =  chain.proceed(request);
        if (response.code() == 401||response.code()==403){
            try {
                RadioRequestController.RequestCallback requestCallback= RadioRequestController.getClient(context,RadioApiUtils.TOKEN_URL);
               // Call call= requestCallback.getRefreshToken("refresh_token",refreshToken,RadioApiUtils.CLIENT_ID);
                Call call= requestCallback.getToken("password","test","read",RadioApiUtils.CLIENT_ID);
                AccessTokenModel accessTokenPojo= (AccessTokenModel) call.execute().body();
                accessToken=accessTokenPojo.getAccessToken();
                refreshToken=accessTokenPojo.getRefreshToken();

                Request updateRequest = original.newBuilder()
                        .header("Authorization", "Bearer "+accessToken)
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(updateRequest);

            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            }
            return response;
        }
        return response;
    }
}
