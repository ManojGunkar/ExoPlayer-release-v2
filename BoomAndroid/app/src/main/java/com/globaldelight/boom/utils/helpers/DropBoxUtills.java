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
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaType;

import java.util.ArrayList;

/**
 * Created by Rahul Agarwal on 09-01-17.
 */

public class DropBoxUtills {

    public static String DIR = "/";
    public static final String OVERRIDEMSG = "File name with this name already exists.Do you want to replace this file?";
    final static public String DROPBOX_APP_KEY = "unt5kbgl16jw3tx";
    final static public String DROPBOX_APP_SECRET = "nwacus6f0ykxpkm";
    public static boolean mLoggedIn = false;

    final static public Session.AccessType ACCESS_TYPE = Session.AccessType.DROPBOX;

    final static public String ACCOUNT_PREFS_NAME = "prefs";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";

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
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE,
                    accessToken);
        } else {
            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
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
        if (DropBoxUtills.getKeys(context) == null){
            App.getDropboxAPI().getSession().startAuthentication(context);
        }
    }

    public static boolean getFiles(String DIR, DropboxMediaList dropboxMediaList) {
        ArrayList<DropboxAPI.Entry> dropboxFolderList = new ArrayList<>();
        com.dropbox.client2.DropboxAPI.Entry dirent;
        try {
            dirent = App.getDropboxAPI().metadata(DIR, 10000, null, true, null);
            for (com.dropbox.client2.DropboxAPI.Entry entry : dirent.contents) {
                if (entry.isDir) {
//                    DIR = entry.path;
                    dropboxFolderList.add(entry);
                } else {
                    if(entry.mimeType.toString().startsWith("audio/")){
                        dropboxMediaList.addFileInDropboxList(new MediaItem(entry.fileName(), entry.path, ItemType.SONGS, MediaType.DROP_BOX, ItemType.SONGS));
                    }
                }
            }
        } catch (DropboxException e) {
            e.printStackTrace();
        }
        getFolderDataFiles(dropboxFolderList, dropboxMediaList);
        return true;
    }

    public static void getFolderDataFiles(ArrayList<DropboxAPI.Entry> dropboxFolderList, DropboxMediaList dropboxMediaList) {
        for (int i =0; i < dropboxFolderList.size(); i++ ) {
            getFiles(dropboxFolderList.get(i).path, dropboxMediaList);
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
}
