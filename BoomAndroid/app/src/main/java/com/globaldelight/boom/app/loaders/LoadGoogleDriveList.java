package com.globaldelight.boom.app.loaders;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;

import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.cloud.GoogleDriveMediaList;
import com.globaldelight.boom.collection.local.MediaItem;
import com.globaldelight.boom.playbackEvent.utils.ItemType;
import com.globaldelight.boom.playbackEvent.utils.MediaType;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An asynchronous task that handles the Drive API call.
 * Placing the API calls in their own task ensures the UI stays responsive.
 */
public class LoadGoogleDriveList extends AsyncTask<Void, Void, List<String>> {
    private com.google.api.services.drive.Drive mService = null;
    private Exception mLastError = null;
    private Fragment fragment;
    private GoogleDriveMediaList mediaListInstance;
    String access_token =    null;
    private Context mContext;
    private static int  file_count;
    private String mediaUrl_1 = "https://www.googleapis.com/drive/v3/files/";
    private String mediaUrl_2 = "?alt=media&access_token=";

    public LoadGoogleDriveList(Context context, Fragment fragment, GoogleAccountCredential googleAccountCredential, int count) {
        this.fragment = fragment;
        mContext = context;
        file_count = count;
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, googleAccountCredential)
                .setApplicationName(fragment.getResources().getString(R.string.app_name))
                .build();
        mediaListInstance = GoogleDriveMediaList.getInstance(fragment.getContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mediaListInstance.clearGoogleDriveMediaContent();
    }

    /**
     * Background task to call Drive API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        mLastError = null;
        List<String> itemList = null;
        try {
            itemList = getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            e.printStackTrace();
        }

        return itemList;
    }

    /**
     * Fetch a list of up to 10 file names and IDs.
     *
     * @return List of Strings describing files, or an empty list if no files
     * found.
     * @throws IOException
     */
    private List<String> getDataFromApi() throws IOException {
        // Get a list of up to 10 files.
        List<String> fileInfo = new ArrayList<String>();
        FileList result = mService.files().list()
                .execute();

        GoogleDriveHandler googleDriveHandler = GoogleDriveMediaList.getInstance(mContext).getGoogleDriveHandler();
        if(null != googleDriveHandler ) {
            googleDriveHandler.retrieveNextPage();
            try {
                access_token = googleDriveHandler.getGoogleAccountCredential().getToken();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
            List<File> files = result.getFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.get("mimeType").toString().startsWith("audio")) {
                        String id = String.valueOf(200000+file_count);
                        mediaListInstance.addFileInGoogleDriveMediaList(new MediaItem(id, file.getName(),
                                mediaUrl_1 + file.getId() + mediaUrl_2, ItemType.SONGS, MediaType.GOOGLE_DRIVE, ItemType.SONGS));
                        file_count++;
                    }
                }
            }
        }
        return fileInfo;
    }

    @Override
    protected void onPostExecute(List<String> output) {
        if ( mLastError != null ) {
            onError();
        }

        if ((output == null || output.size() == 0) && mediaListInstance.getGoogleDriveMediaList().size() <= 0) {
            mediaListInstance.onEmptyList();
        } else {
            mediaListInstance.finishGoogleDriveMediaLoading();
        }
    }

    @Override
    protected void onCancelled() {
        onError();
    }

    public void onError() {
        try {
            if (mLastError != null) {
                GoogleDriveHandler driveHandler  = GoogleDriveMediaList.getInstance(mContext).getGoogleDriveHandler();
                if (null != driveHandler && mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                    driveHandler.showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError)
                            .getConnectionStatusCode());
                } else if (mLastError instanceof UserRecoverableAuthIOException) {
                    fragment.startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            GoogleDriveHandler.REQUEST_AUTHORIZATION);
                } else {
                    mediaListInstance.onErrorOccurred(null == mLastError.getMessage() ? fragment.getResources().getString(R.string.error_fetch_data) : mLastError.getMessage());
                }
            } else {
                mediaListInstance.onRequestCancelled();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setCancelLoading(){
        this.cancel(true);
    }
}