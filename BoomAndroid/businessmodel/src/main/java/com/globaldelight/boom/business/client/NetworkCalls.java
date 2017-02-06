package com.globaldelight.boom.business.client;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.model.AcessTokenObject;
import com.globaldelight.boom.business.model.JsonResultObjects;
import com.globaldelight.boom.business.model.RespSaveEmailObject;
import com.globaldelight.boom.business.model.ResultRegiDeviceObject;
import com.globaldelight.boom.business.network.OnServerResponseListener;
import com.globaldelight.boom.business.network.PostHandleData;
import com.globaldelight.boom.business.network.PostRegisterDeviceData;
import com.globaldelight.boom.business.network.PostRequestAccessToken;
import com.globaldelight.boom.business.network.PostSaveEmailData;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Rahul Agarwal on 01-02-17.
 */

public class NetworkCalls {

    private static Context mContext;
    private static String appaccesstoken;
    private boolean isSuceess;
    private PackageInfo pInfo = null;
    private static String deviceLang;
    private static String version;
    private static String verCode;
    private static String installDate;
    private static String deviceModel;
    private static String deviceVersion;
    private static String timeZoneOffset;
    private final long ONE_DAY = 24 * 60 * 60 * 1000;
    private SimpleDateFormat formatter;
    private static NetworkCalls networkCallHandler;
    private static IBusinessNetworkInit iBusinessNetworkInit;
    private static Handler postNetworkMessage;


    private NetworkCalls(Context context){
        this.mContext = context;
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        postNetworkMessage = new Handler();

        initDeviceInfo();
    }

    public static NetworkCalls getNetworkCallsInstance(Context context){
        if(null == networkCallHandler)
            networkCallHandler = new NetworkCalls(context);
        return networkCallHandler;
    }

    public void setBusinessNetworkListener(IBusinessNetworkInit iBusinessNetworkInit){
        this.iBusinessNetworkInit = iBusinessNetworkInit;
    }

    private void initDeviceInfo() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                deviceLang = Locale.getDefault().getDisplayLanguage();

                try {
                    pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                    installDate = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime);
                    Log.d("installDate",installDate);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                version = pInfo.versionName;
                verCode = String.valueOf(pInfo.versionCode);

                deviceModel = getDeviceName();

                deviceVersion = android.os.Build.VERSION.RELEASE;

                timeZoneOffset = installDate;
            }
        }).start();
    }

    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        }
        return capitalize(manufacturer) + " " + model;
    }

    private static String capitalize(String str) {
        if (TextUtils.isEmpty(str)) {
            return str;
        }
        char[] arr = str.toCharArray();
        boolean capitalizeNext = true;

//        String phrase = "";
        StringBuilder phrase = new StringBuilder();
        for (char c : arr) {
            if (capitalizeNext && Character.isLetter(c)) {
//                phrase += Character.toUpperCase(c);
                phrase.append(Character.toUpperCase(c));
                capitalizeNext = false;
                continue;
            } else if (Character.isWhitespace(c)) {
                capitalizeNext = true;
            }
//            phrase += c;
            phrase.append(c);
        }

        return phrase.toString();
    }

    public void getAccessToken() {

        if(null == appaccesstoken) {
            PostRequestAccessToken.getInstance(mContext).posttReqAccessToken(BusinessUtils.appid, BusinessUtils.apptype, BusinessUtils.getDeviceID(mContext), BusinessUtils.country, BusinessUtils.secretkey, new OnServerResponseListener<AcessTokenObject, String>() {
                @Override
                public void onSuccess(AcessTokenObject successParam) {
                    appaccesstoken = successParam.getAppaccesstoken();
                    postNetworkMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            iBusinessNetworkInit.onGetAccessToken(true);
                        }
                    });
                    Toast.makeText(mContext, "AccessToken Successfully Recieved ", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onFailure(final String failureParam) {
                    postNetworkMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            iBusinessNetworkInit.onGetAccessToken(false);
                        }
                    });
                    Toast.makeText(mContext, "Failed to Fetch data  ", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void registerDevice() {

        PostRegisterDeviceData.getInstance(mContext).postRegisterDeviceData(appaccesstoken, BusinessUtils.appid, BusinessUtils.country, deviceLang, verCode, version, deviceModel, null, null, deviceVersion, timeZoneOffset, new OnServerResponseListener<ResultRegiDeviceObject, String>() {
            @Override
            public void onSuccess(ResultRegiDeviceObject successParam) {
                postNetworkMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        iBusinessNetworkInit.onRegisterDevice(true);
                    }
                });
                Toast.makeText(mContext, "Successfully Registred Device ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String failureParam) {
                postNetworkMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        iBusinessNetworkInit.onRegisterDevice(false);
                    }
                });
                Toast.makeText(mContext, "Failed to Fetch data  ", Toast.LENGTH_LONG).show();
            }

        });
    }

    public void configApp() {
        PostHandleData.getInstance(mContext).postApplication(BusinessUtils.appid, BusinessUtils.apptype, BusinessUtils.country, BusinessUtils.getDeviceID(mContext), BusinessUtils.country, BusinessUtils.secretkey, new OnServerResponseListener<JsonResultObjects, String>() {
            @Override

            public void onSuccess(JsonResultObjects successParam) {

                if(null != successParam) {
                    BusinessUtils.setAppExpireDays(Integer.parseInt(successParam.getShareSettings().getFreeUsageWithoutAdsDays()));

                    BusinessUtils.setAddEnable(Integer.parseInt(successParam.getAdsAnabled()) == 1 ? true : false);

                    BusinessUtils.setAddsDisplayIntervals(Long.parseLong(successParam.getShareSettings().getShareDialogDisplayIntervalinsec()));

                    BusinessUtils.setAddSources(Integer.parseInt(successParam.getAdsSettings().getAdSource()));

                    BusinessUtils.setScreenType(successParam.getAdsSettings().getScreenList());

                    BusinessUtils.setAskEmailPopupExpire(Integer.parseInt(successParam.getShareSettings().getShouldAskEmailAfterExpiry()) == 1 ? true : false);

                    postNetworkMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            iBusinessNetworkInit.onGetBusinessConfiguration(true);
                        }
                    });
                }
            }

            @Override
            public void onFailure(String failureParam) {
                if(null != failureParam) {
                    postNetworkMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            iBusinessNetworkInit.onGetBusinessConfiguration(false);
                        }
                    });
                    Toast.makeText(mContext, "Failed to Fetch data  ", Toast.LENGTH_LONG).show();
                }
            }

        });
    }

    public void isAppTrailVersion() {
        timeZoneOffset = installDate;
        if (installDate == null) {
            try {
                pInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0);
                installDate = String.valueOf(mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 0).firstInstallTime);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            Date before = new Date();;
            try {
                before = (Date) formatter.parse(installDate);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(null != before) {
                Date now = new Date();
                long diff = now.getTime() - before.getTime();
                long days = diff / ONE_DAY;
                if (days >= BusinessUtils.getAppExpireDays()) {
                    // Expired !!!
                    postNetworkMessage.post(new Runnable() {
                        @Override
                        public void run() {
                            iBusinessNetworkInit.onAppTrailExpired(true);
                        }
                    });
                    return;
                }
            }
        }
        postNetworkMessage.post(new Runnable() {
            @Override
            public void run() {
                iBusinessNetworkInit.onAppTrailExpired(false);
            }
        });
    }

    public void saveEmailAddress(BusinessUtils.EmailSource emailSource, String emailid, boolean newsletteroptin) {
        String source = emailSource.name();
        PostSaveEmailData.getInstance(mContext).PostSaveEmailData(appaccesstoken, BusinessUtils.getDeviceID(mContext), newsletteroptin, source, emailid, new OnServerResponseListener<RespSaveEmailObject, String>() {
            @Override
            public void onSuccess(RespSaveEmailObject successParam) {
                postNetworkMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        iBusinessNetworkInit.onEmailSubmition(true);
                    }
                });
                Toast.makeText(mContext, "Succesfully saved Eamil Addreess  ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String failureParam) {
                postNetworkMessage.post(new Runnable() {
                    @Override
                    public void run() {
                        iBusinessNetworkInit.onEmailSubmition(false);
                    }
                });
                Toast.makeText(mContext, "Failed to Fetch data  ", Toast.LENGTH_LONG).show();
            }

        });

    }
}
