package com.globaldelight.boom.task;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.globaldelight.boom.utils.helpers.GoogleDriveHandler;
import com.google.api.client.extensions.android.http.AndroidHttp;
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
    private ProgressDialog mProgress;
    private Activity activity;
    private GoogleDriveHandler googleDriveHandler;
    String access_token = null;

    public LoadGoogleDriveList(Activity activity, GoogleDriveHandler googleDriveHandler) {
        this.activity = activity;
        this.googleDriveHandler = googleDriveHandler;
        mProgress = new ProgressDialog(activity);
        mProgress.setMessage("Calling Drive API ...");
        HttpTransport transport = AndroidHttp.newCompatibleTransport();
        JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
        mService = new com.google.api.services.drive.Drive.Builder(
                transport, jsonFactory, googleDriveHandler.getGoogleAccountCredential())
                .setApplicationName("Drive API Android Quickstart")
                .build();
    }

    /**
     * Background task to call Drive API.
     *
     * @param params no parameters needed for this task.
     */
    @Override
    protected List<String> doInBackground(Void... params) {
        try {
            return getDataFromApi();
        } catch (Exception e) {
            mLastError = e;
            cancel(true);
            return null;
        }
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
//            String str = "mimeType";
//            String compareString = "audio";
//            Pattern p = Pattern.compile("score");
//            Matcher m = p.matcher(stringToSearch);
        Log.d("starteExcuteService : Start ", System.currentTimeMillis() + "");

        FileList result = mService.files().list()
                .execute();
        googleDriveHandler.retrieveNextPage();
        access_token = googleDriveHandler.getGoogleAccessToken();
        List<File> files = result.getFiles();
        if (files != null) {
            for (File file : files) {
//                    file.get
//                    DocsList.getFileById(fileId).getUrl()
//                    file.getU
                if (file.get("mimeType").toString().startsWith("audio")) {
                    Log.d("weblink", file.getWebContentLink() + "");
                    Log.d("weblink", file.getWebViewLink() + "");
                    fileInfo.add(String.format("%s (%s)\n",
                            file.getName(), file.getId()));
                }
            }
        }
        Log.d("starteExcuteService : End ", System.currentTimeMillis() + "");
        return fileInfo;
    }

    @Override
    protected void onPreExecute() {
        mProgress.show();
    }

    @Override
    protected void onPostExecute(List<String> output) {
        mProgress.hide();
        if (output == null || output.size() == 0) {
            Toast.makeText(activity, "No results returned.", Toast.LENGTH_SHORT).show();
        } else {
            output.add(0, "Data retrieved using the Drive API:");
            Toast.makeText(activity, TextUtils.join("\n", output), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCancelled() {
        mProgress.hide();
        if (mLastError != null) {
            if (mLastError instanceof GooglePlayServicesAvailabilityIOException) {
                googleDriveHandler.showGooglePlayServicesAvailabilityErrorDialog(((GooglePlayServicesAvailabilityIOException) mLastError)
                        .getConnectionStatusCode());
            } else if (mLastError instanceof UserRecoverableAuthIOException) {
                activity.startActivityForResult(
                        ((UserRecoverableAuthIOException) mLastError).getIntent(),
                        GoogleDriveHandler.REQUEST_AUTHORIZATION);
            } else {
                Toast.makeText(activity, "The following error occurred:\n"
                        + mLastError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(activity, "Request cancelled.", Toast.LENGTH_SHORT).show();
        }
    }
}