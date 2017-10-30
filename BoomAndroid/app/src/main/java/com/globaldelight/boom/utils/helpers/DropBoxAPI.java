package com.globaldelight.boom.utils.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.ListFolderBuilder;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.collection.cloud.DropboxMediaList;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by Rahul Agarwal on 09-01-17.
 */

public class DropBoxAPI {
    final static public String DROPBOX_APP_KEY = "unt5kbgl16jw3tx";
    // final static public String DROPBOX_APP_SECRET = "nwacus6f0ykxpkm";

    final static public String ACCOUNT_PREFS_NAME = "dropbox_prefs";
    final static public String ACCESS_TOKEN = "dropbox_ACCESS_TOKEN";
    final static public String ACCOUNT_NAME = "dropbox_ACCOUNT_NAME";

    final static HashSet<String> AUDIO_FILE_TYPES = new HashSet<>(Arrays.asList(new String[]{
            ".aac", ".aif", ".aifc", ".aiff", ".au", ".flac", ".m4a", ".m4b", ".m4p", ".m4r", ".mid", ".mp3", ".oga", ".ogg", ".opus", ".ra", ".ram", ".spx", ".wav", ".wma"
    }));



    private Context mContext;
    private DbxClientV2 mDbxClient;
    private  int count = 0;

    private static DropBoxAPI sInstance;
    public static DropBoxAPI getInstance(Context context) {
        if ( sInstance == null ) {
            sInstance = new DropBoxAPI(context.getApplicationContext());
        }
        return sInstance;
    }

    private DropBoxAPI(Context context) {
        mContext = context;
    }

    public void clear() {
        SharedPreferences prefs = mContext.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        prefs.edit().clear().commit();
        DropboxMediaList.getInstance(mContext).clearDropboxContent();
        mDbxClient = null;
    }

    public void authorize(){
        if ( !isLoggedIn() ) {
            Auth.startOAuth2Authentication(mContext, DROPBOX_APP_KEY);
        }
    }

    public void finishAuthorization() {
        String token = getAccessToken();
        if ( token == null ) {
            token = Auth.getOAuth2Token();
            if ( token != null ) {
                saveAccessToken(token);
            }
        }
    }

    public void getFiles(DropboxMediaList dropboxMediaList) {
        try {
            count = 0;
            ListFolderBuilder builder = getClient().files().listFolderBuilder("");
            ListFolderResult result = builder.withRecursive(true).start();
            getAllFiles(result, dropboxMediaList);
        }
        catch (DbxException e) {

        }
    }

    private void getAllFiles(ListFolderResult result, DropboxMediaList dropboxMediaList) {
        try {
            for (Metadata entry : result.getEntries()) {
                if ( entry instanceof FileMetadata ) {
                    FileMetadata file =(FileMetadata)entry;
                    String path = file.getPathLower();
                    int i = path.lastIndexOf('.');
                    String ext = (i > 0)? path.substring(i) : "";
                    if ( AUDIO_FILE_TYPES.contains(ext) ) {
                        dropboxMediaList.addFileInDropboxList(new MediaItem(100000+count, file.getName(), path, ItemType.SONGS, MediaType.DROP_BOX, ItemType.SONGS));
                        count++;
                    }
                }
            }
            if ( result.getHasMore() ) {
                ListFolderResult next = getClient().files().listFolderContinue(result.getCursor());
                getAllFiles(next, dropboxMediaList);
            }
        } catch (DbxException e) {
            e.printStackTrace();
        }
    }

    public String getStreamingUrl(String path){
        try {
            return getClient().files().getTemporaryLink(path).getLink();
        } catch (DbxException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    public String getAccountInfo() {
        try {
            String name = getClient().users().getCurrentAccount().getName().getDisplayName();
            setAccountName(name);
            return name;
        }
        catch (DbxException e) {
            return null;
        }
    }

    public String getAccountName(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        return prefs.getString(ACCOUNT_NAME, null);
    }

    private DbxClientV2 getClient() {
        if ( mDbxClient == null && getAccessToken() != null ) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder("BoomAndroid")
                    .withHttpRequestor(new OkHttp3Requestor(OkHttp3Requestor.defaultOkHttpClient()))
                    .build();

            mDbxClient = new DbxClientV2(requestConfig, getAccessToken());
        }

        return mDbxClient;
    }


    private void saveAccessToken(String token) {
        SharedPreferences prefs = mContext.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        prefs.edit().putString(ACCESS_TOKEN, token).apply();
    }


    private String getAccessToken() {
        SharedPreferences prefs = mContext.getSharedPreferences(
                ACCOUNT_PREFS_NAME, 0);
        return prefs.getString(ACCESS_TOKEN, null);
    }


    private void setAccountName(String accountName) {
        SharedPreferences prefs = mContext.getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        prefs.edit().putString(ACCOUNT_NAME, accountName).apply();
    }

}
