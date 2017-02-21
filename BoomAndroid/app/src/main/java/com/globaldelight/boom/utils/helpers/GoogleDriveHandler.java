package com.globaldelight.boom.utils.helpers;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.globaldelight.boom.App;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCallback.GoogleDriveMediaList;
import com.globaldelight.boom.task.MediaLoader.LoadGoogleDriveList;
import com.globaldelight.boom.ui.musiclist.fragment.GoogleDriveListFragment;
import com.globaldelight.boom.ui.musiclist.fragment.SettingFragment;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.query.Query;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.drive.DriveScopes;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * Created by Rahul Agarwal on 11-01-17.
 */

public class GoogleDriveHandler implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "GoogleDriveHandler";
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private static GoogleApiClient client;
    public static GoogleAccountCredential mCredential;
    public static final int REQUEST_ACCOUNT_PICKER = 1000;
    public static final int REQUEST_AUTHORIZATION = 1001;
    public static final int REQUEST_GOOGLE_PLAY_SERVICES = 1002;
    public static final int REQUEST_PERMISSION_GET_ACCOUNTS = 1003;
    public static final int REQUEST_CODE_RESOLUTION = 1;
//    Client ID : 312070820740-he3m1noeh3dggs8gc538qu11in10ifg0.apps.googleusercontent.com
    private static final String[] SCOPES = {DriveScopes.DRIVE_METADATA, DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA, DriveScopes.DRIVE};
//    private Activity mActivity;
    private Fragment mFragment;
    private Context mContext;

    public GoogleDriveHandler(Context context){
        this.mContext = context;
    }

    public GoogleDriveHandler(Fragment mFragment){
        if(mFragment instanceof GoogleDriveListFragment)
            this.mContext = ((GoogleDriveListFragment)mFragment).getFragmentContext();
        else if (mFragment instanceof SettingFragment)
            this.mContext = ((SettingFragment)mFragment).getFragmentContext();
        this.mFragment = mFragment;
    }

    public GoogleAccountCredential getGoogleAccountCredential(){
        // Initialize credentials and service object.
        if(null == mCredential) {
            mCredential = GoogleAccountCredential.usingOAuth2(mContext, Arrays.asList(SCOPES))
                    .setBackOff(new ExponentialBackOff());
        }
        return mCredential;
    }

    public GoogleApiClient getGoogleApiClient(){
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        if(null == client) {
            client = new GoogleApiClient.Builder(mContext).addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER) // required for App Folder sample
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
        return client;
    }

    public void setSelectedGoogleAccountName(String accountName){
        getGoogleAccountCredential().setSelectedAccountName(accountName);
    }

    public void connectGoogleAccount(){
        if(null != client) {
            client.connect();
//            AppIndex.AppIndexApi.start(client, getIndexApiAction());
        }
    }

    public void disconnectToGoogleAccount() {
        if (null != client){
//            AppIndex.AppIndexApi.end(client, getIndexApiAction());
            client.disconnect();
        }
    }

    public void resetKeys(Context context){
        App.getUserPreferenceHandler().setGoogleAccountName(null);
        GoogleDriveMediaList.geGoogleDriveMediaListInstance(context).clearGoogleDriveMediaContent();
        mFragment.startActivityForResult(
                mCredential.newChooseAccountIntent(),
                REQUEST_ACCOUNT_PICKER);
    }

    public String getSelectedAccountName(){
        String accountName = getGoogleAccountCredential().getSelectedAccountName();
        if(null != accountName){
            setSelectedGoogleAccountName(accountName);
            return accountName;
        }
        return null;
    }

    /**
     * Attempt to call the API, after verifying that all the preconditions are
     * satisfied. The preconditions are: Google Play Services installed, an
     * account was selected and the device currently has online access. If any
     * of the preconditions are not satisfied, the app will prompt the user as
     * appropriate.
     */
    public void getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccountName() == null) {
            chooseAccount();
        } else if (!isDeviceOnline()) {
            GoogleDriveMediaList.geGoogleDriveMediaListInstance(mContext).onErrorOccurred(mFragment.getResources().getString(R.string.network_error));
        } else {
            new LoadGoogleDriveList(mFragment, mCredential, 0).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public void connectToGoogleAccount(){
        getGoogleAccountCredential();
        getGoogleApiClient();
        connectGoogleAccount();
        String accountName = App.getUserPreferenceHandler().getGoogleAccountName();
        if (accountName != null)
            mCredential.setSelectedAccountName(accountName);
    }

    public String getAccessTokenApi() {
        mCredential = getGoogleAccountCredential();
        if (isGooglePlayServicesAvailable() && null != mCredential && mCredential.getSelectedAccountName() != null && isDeviceOnline()) {
            try {
                return mCredential.getToken();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GoogleAuthException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Checks whether the device currently has a network connection.
     *
     * @return true if the device has a network connection, false otherwise.
     */
    private boolean isDeviceOnline() {
        ConnectivityManager connMgr =
                (ConnectivityManager) (mContext).getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }

    /**
     * Check that Google Play services APK is installed and up to date.
     *
     * @return true if Google Play Services is available and up to
     * date on this device; false otherwise.
     */
    private boolean isGooglePlayServicesAvailable() {
        int connectionStatusCode = 0;
        try {
            GoogleApiAvailability apiAvailability =
                    GoogleApiAvailability.getInstance();
            connectionStatusCode =
                    apiAvailability.isGooglePlayServicesAvailable(mContext);
        }catch (Exception e){
            return connectionStatusCode != ConnectionResult.SUCCESS;
        }
        return connectionStatusCode == ConnectionResult.SUCCESS;
    }

    /**
     * Attempt to resolve a missing, out-of-date, invalid or disabled Google
     * Play Services installation via a user dialog, if possible.
     */
    private void acquireGooglePlayServices() {
        GoogleApiAvailability apiAvailability =
                GoogleApiAvailability.getInstance();
        if(null != apiAvailability && null != mContext) {
            final int connectionStatusCode =
                    apiAvailability.isGooglePlayServicesAvailable(mContext);
            if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
                showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            }
        }
    }


    /**
     * Display an error dialog showing that Google Play Services is missing
     * or out of date.
     *
     * @param connectionStatusCode code describing the presence (or lack of)
     *                             Google Play Services on this device.
     */
    public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        Dialog dialog = apiAvailability.getErrorDialog(mFragment.getActivity(), connectionStatusCode, REQUEST_GOOGLE_PLAY_SERVICES);
        dialog.show();
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public com.google.android.gms.appindexing.Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("MainActiivtySub Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new com.google.android.gms.appindexing.Action.Builder(com.google.android.gms.appindexing.Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(com.google.android.gms.appindexing.Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    /**
     * Attempts to set the account used with the API credentials. If an account
     * name was previously saved it will use that one; otherwise an account
     * picker dialog will be shown to the user. Note that the setting the
     * account to use with the credentials object requires the app to have the
     * GET_ACCOUNTS permission, which is requested here if it is not already
     * present. The AfterPermissionGranted annotation indicates that this
     * function will be rerun automatically whenever the GET_ACCOUNTS permission
     * is granted.
     */
    @AfterPermissionGranted(REQUEST_PERMISSION_GET_ACCOUNTS)
    public void chooseAccount() {
        if (EasyPermissions.hasPermissions(mContext,
                Manifest.permission.GET_ACCOUNTS)) {
            String accountName = App.getUserPreferenceHandler().getGoogleAccountName();
            if (accountName != null) {
                mCredential.setSelectedAccountName(accountName);
                getResultsFromApi();
            } else {
                // Start a dialog from which the user can choose an account
                mFragment.startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        REQUEST_ACCOUNT_PICKER);
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            if(mFragment instanceof GoogleDriveListFragment)
                ((GoogleDriveListFragment)mFragment).checkPermissions();
            else if(mFragment instanceof SettingFragment)
                ((SettingFragment)mFragment).checkPermissions();
        }
    }

    public void retrieveNextPage() {
        Query query = new Query.Builder()
                .build();
        Drive.DriveApi.query(client, query)
                .setResultCallback(metadataBufferCallback);
    }

    private final ResultCallback<DriveApi.MetadataBufferResult> metadataBufferCallback = new
            ResultCallback<DriveApi.MetadataBufferResult>() {
                @Override
                public void onResult(DriveApi.MetadataBufferResult result) {
                    if (!result.getStatus().isSuccess()) {
                        return;
                    }
                }
            };

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "GoogleApiClient connected");
    }

    /**
     * Called when {@code mGoogleApiClient} is disconnected.
     */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if(null != result) {
            if (!result.hasResolution()) {
                // show the localized error dialog.
                GoogleApiAvailability.getInstance().getErrorDialog(mFragment.getActivity(), result.getErrorCode(), 0).show();
                return;
            }
            try {
                result.startResolutionForResult(mFragment.getActivity(), GoogleDriveHandler.REQUEST_CODE_RESOLUTION);
            } catch (IntentSender.SendIntentException e) {
                Log.e(TAG, "Exception while starting resolution activity", e);
            } catch (Exception e){}
        }
    }
}
