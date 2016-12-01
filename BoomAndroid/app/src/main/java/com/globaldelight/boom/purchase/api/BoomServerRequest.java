package com.globaldelight.boom.purchase.api;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.globaldelight.boom.R;
import com.globaldelight.boom.purchase.InAppPurchaseActivity;
import com.globaldelight.boom.utils.Logger;
import com.globaldelight.boom.utils.handlers.Preferences;
import com.globaldelight.boomplayer.AudioEffect;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by nidhin on 29/11/16.
 */

public class BoomServerRequest {
    private final static String SECRET_KEY = "b32a9642113a9e52fe9d200a031f14792eef5d2e589ecad2638f5dd189dfcb23";
    private final static String APP_TYPE = "android";
    private final static String APP_ID = "com.globaldelight.boom";
    private final static String BASE_URL = "http://devboom.globaldelight.net/";
    String accessToken;
    Context context;
    AlertDialog ad;
    AudioEffect audioEffectPreferenceHandler;
    ProgressDialog pd;
    String emailid = "";

    public BoomServerRequest(Context context) {

        this.context = context;
        audioEffectPreferenceHandler = AudioEffect.getAudioEffectInstance(context);

    }

    public final static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static int gen() {
        Random r = new Random(System.currentTimeMillis());
        return 1000 + r.nextInt(10000);
    }

    public void showExtendInitialDialog() {

        new MaterialDialog.Builder(context)
                .title(R.string.title_fiveday_offer_expired)
                .content(R.string.desc_fiveday_offer_expired)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_buynow)
                .negativeText(R.string.btn_txt_extend)

                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        Intent intent = new Intent(context, InAppPurchaseActivity.class);
                        context.startActivity(intent);

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        showExtendInputMailDialog();
                    }
                })

                .show();
    }

    public void showExtendInputMailDialog() {


        new MaterialDialog.Builder(context)
                .title(R.string.title_extend_with_email)
                .content(R.string.desc_extend_with_email)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_sendcode)
                //.negativeText(R.string.btn_txt_extend)

                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("") && isValidEmail(input.toString())) {
                            Logger.LOGD(input.toString());
                            emailid = input.toString();
                            if (!emailid.toString().matches("") && isValidEmail(emailid)) {
                                sendExtendCode(emailid);

                            }
                        } else {
                            Toast.makeText(context, context.getResources().getString(R.string.txt_invalid_email), Toast.LENGTH_SHORT).show();
                            showExtendInputMailDialog();
                        }
                    }
                })


                .show();
    }

    public void showConfirmMailDialog() {

        new MaterialDialog.Builder(context)
                .title(R.string.confirm_email)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_resend_code)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {

                            sendExtendCode(input.toString());

                        }
                    }
                })


                .show();
    }

    public void showCongratulateDialog() {
        audioEffectPreferenceHandler.setUserPurchaseType(AudioEffect.purchase.EXTENDED_FIVE_DAY_OFFER);
        SimpleDateFormat myFormat = new SimpleDateFormat("dd-MM-yyyy");

        String currentDate = myFormat.format(new Date());
        Preferences.writeString(context, Preferences.EXTENDED_DATE, currentDate);
        new MaterialDialog.Builder(context)
                .title(R.string.title_congratulate)
                .content(R.string.desc_congratulate)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .show();

    }

    public String sendExtendCode(final String email) {
        pd = new ProgressDialog(context);
        // pd.setMessage("loading");
        pd.show();
        final String URL = BASE_URL + "appauthentication/";
        String deviceid = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("secretkey", SECRET_KEY);
        params.put("apptype", APP_TYPE);
        params.put("appid", APP_ID);
        params.put("deviceid", deviceid);

        JsonObjectRequest req = new JsonObjectRequest(URL, new JSONObject(params),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            accessToken = response.getString("appaccesstoken");
                            extendWithUserMail(email);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(req);

        return accessToken;
    }

    public void extendWithUserMail(final String email) {
        final String URL = BASE_URL + "extendwithemail/";
        String deviceid = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        final String token = gen() + "";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("appaccesstoken", accessToken);
        params.put("deviceid", deviceid);
        params.put("emailid", email);
        params.put("token", token);
        JSONObject js = new JSONObject(params);
        JsonObjectRequest req = new JsonObjectRequest(URL, js,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (pd != null) {
                            pd.dismiss();
                        }

                        try {
                            int status = response.getInt("status");
                            int reason = response.getInt("reason");
                            if (status == 0 && reason == 1021) {
                                showAlertForVerify(token, email);
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        });

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(req);
    }

    public void showAlertForVerify(final String code, final String email) {

        new MaterialDialog.Builder(context)
                .title(R.string.title_verify_email)
                .content(R.string.desc_verify_email)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(context.getResources().getColor(R.color.colorPrimary))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .positiveText(R.string.btn_txt_verify)
                .negativeText(R.string.btn_txt_resend)
                .input(null, null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {

                            if (code.equals(input.toString())) {
                                showCongratulateDialog();
                            } else {
                                Toast.makeText(context, context.getResources().getString(R.string.txt_invalid_code), Toast.LENGTH_SHORT).show();
                                showAlertForVerify(code, email);
                            }

                        }
                    }
                })

                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        showConfirmMailDialog();
                    }
                })

                .show();
    }

}
