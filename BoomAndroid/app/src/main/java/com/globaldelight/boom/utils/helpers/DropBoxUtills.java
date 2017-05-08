package com.globaldelight.boom.utils.helpers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.android.AuthActivity;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.globaldelight.boom.App;
import com.globaldelight.boom.data.DeviceMediaCollection.MediaItem;
import com.globaldelight.boom.data.MediaCallback.DropboxMediaList;
import com.globaldelight.boom.Media.ItemType;
import com.globaldelight.boom.Media.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 09-01-17.
 */

public class DropBoxUtills {

    public static final String OVERRIDEMSG = "File name with this name already exists.Do you want to replace this file?";
    final static public String DROPBOX_APP_KEY = "unt5kbgl16jw3tx";
    final static public String DROPBOX_APP_SECRET = "nwacus6f0ykxpkm";
    private static int count = 0;
    public static boolean mLoggedIn = false;

    final static public Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    final static public String ACCOUNT_PREFS_NAME = "dropbox_prefs";
    final static public String ACCESS_KEY_NAME = "dropbox_ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "dropbox_ACCESS_SECRET";
    final static public String ACCOUNT_NAME = "dropbox_ACCOUNT_NAME";

    public static void checkAppKeySetup(Context context) {
        if (DROPBOX_APP_KEY.startsWith("CHANGE")
                || DROPBOX_APP_SECRET.startsWith("CHANGE")) {
            Toast.makeText(context, "You must apply for an app key and secret from developers.dropbox.com, and add them to the DBRoulette ap before trying it.", Toast.LENGTH_SHORT).show();
//            finish();
            return;
        }
        Intent testIntent = new Intent(Intent.ACTION_VIEW);
        String scheme = "db-" + DROPBOX_APP_KEY;
        String uri = scheme + "://" + AuthActivity.AUTH_VERSION + "/test";
        testIntent.setData(Uri.parse(uri));
        PackageManager pm = context.getPackageManager();
        if (0 == pm.queryIntentActivities(testIntent, 0).size()) {
            Toast.makeText(context, "URL scheme in your app's "
                    + "manifest is not set up correctly. You should have a "
                    + "com.dropbox.client2.android.AuthActivity with the "
                    + "scheme: " + scheme, Toast.LENGTH_SHORT).show();
//            finish();
        }
    }

    public static AndroidAuthSession buildSession(Context context) {
        AppKeyPair appKeyPair = new AppKeyPair(DROPBOX_APP_KEY,
                DROPBOX_APP_SECRET);
        AndroidAuthSession session;

        String[] stored = getKeys(context);
        if (stored != null) {
            AccessTokenPair accessToken = new AccessTokenPair(stored[0],
                    stored[1]);
            session = new AndroidAuthSession(appKeyPair, accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair);
        }

        return session;
    }

    public static void storeKeys(Context context, String key, String secret) {
        SharedPreferences prefs = context.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    public static void clearKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        SharedPreferences.Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
        if(null != DropboxMediaList.getDropboxListInstance(context)){
            DropboxMediaList.getDropboxListInstance(context).clearDropboxContent();
        }
    }

    public static String[] getKeys(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        String key = prefs.getString(ACCESS_KEY_NAME, null);
        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
        if (key != null && secret != null) {
            String[] ret = new String[2];
            ret[0] = key;
            ret[1] = secret;
            return ret;
        } else {
            return null;
        }
    }

    public static void checkDropboxAuthentication(Context context){
        if (null == DropBoxUtills.getKeys(context) && null != App.getDropboxAPI()){
            App.getDropboxAPI().getSession().startAuthentication(context);
        }
    }

    public static boolean getFiles(DropboxMediaList dropboxMediaList) {
        if(null != App.getDropboxAPI()) {
            getAllFiles(null, dropboxMediaList);
            return true;
        }
        return false;
    }

    private static void getAllFiles(String cursor, DropboxMediaList dropboxMediaList) {
        try {
            DropboxAPI.DeltaPage<DropboxAPI.Entry> page = App.getDropboxAPI().delta(cursor);
            for (DropboxAPI.DeltaEntry<DropboxAPI.Entry> entry : page.entries) {
                DropboxAPI.Entry metadata = entry.metadata;
                if ( metadata.isDeleted || metadata.isDir ) {
                    continue;
                }
                if ( metadata.mimeType != null && metadata.mimeType.toString().startsWith("audio/")) {
                    dropboxMediaList.addFileInDropboxList(new MediaItem(100000+count, entry.metadata.fileName(), entry.metadata.path, ItemType.SONGS, MediaType.DROP_BOX, ItemType.SONGS));
                    count++;
                }
            }
            if ( page.hasMore ) {
                getAllFiles(page.cursor, dropboxMediaList);
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
    }

    public static String getDropboxItemUrl(String path){
        try {
            return App.getDropboxAPI().media(path, true).url;
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void setItemCount(int itemCount) {
        count = itemCount;
    }

    public static boolean isLoggedIn(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        return (null != prefs.getString(ACCESS_KEY_NAME, null) &&
                null != prefs.getString(ACCESS_SECRET_NAME, null));
    }


    public static void setAccountName(Context context, String accountName) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        prefs.edit().putString(ACCOUNT_NAME, accountName).commit();
    }

    public static String getAccountName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        return prefs.getString(ACCOUNT_NAME, null);
    }
}
