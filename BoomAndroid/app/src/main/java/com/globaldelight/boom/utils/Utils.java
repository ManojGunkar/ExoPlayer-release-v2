package com.globaldelight.boom.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.AnyRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.app.App;
import com.globaldelight.boom.app.analytics.flurry.FlurryAnalytics;
import com.globaldelight.boom.app.analytics.flurry.FlurryEvents;
import com.globaldelight.boom.collection.local.callback.IMediaItemCollection;
import com.globaldelight.boom.playbackEvent.controller.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.app.analytics.AnalyticsHelper;
import com.globaldelight.boom.app.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.collection.local.callback.IMediaItemBase;
import com.globaldelight.boom.app.receivers.ConnectivityReceiver;
import com.globaldelight.boom.app.activities.ActivityContainer;
import com.globaldelight.boom.app.adapters.utils.AddToPlaylistAdapter;
import com.globaldelight.boom.view.BoomDialogView;
import com.globaldelight.boom.app.sharedPreferences.Preferences;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;

import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class Utils {
    private static BoomDialogView progressLoader;
    public static final int SHARE_COMPLETE = 1001;
    public static final int PURCHASE_FLOW_LAUNCH = 1002;
    public static final int LARGE_IMAGE_SIZE_DP = 128;
    public static final int SMALL_IMAGE_SIZE_DP = 62;

    private Utils(Context context) {
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getWindowWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.widthPixels;
    }

    public static int getWindowHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        return metrics.heightPixels;
    }

    /**
     * get uri to any resource type
     *
     * @param context - context
     * @param resId   - resource id
     * @return - Uri to resource by given id
     * @throws Resources.NotFoundException if the given ID does not exist.
     */
    public static final Uri getUriToResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        /** Return a Resources instance for your application's package. */
        Resources res = context.getResources();
        /**
         * Creates a Uri which parses the given encoded URI string.
         * @param uriString an RFC 2396-compliant, encoded URI
         * @throws NullPointerException if uriString is null
         * @return Uri for this given uri string
         */
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
        /** return uri */
        return resUri;
    }

    public static void  addToPlaylist(final Activity activity, final ArrayList<? extends IMediaItemBase> songList, final String fromPlaylist) {
        Context context = activity;
        if(songList.size() == 0)
            return;

        ArrayList<? extends IMediaItemBase>  playList = MediaController.getInstance(activity).getBoomPlayList();

        if(fromPlaylist != null){
            for(int i=0; i< playList.size(); i++){
                if(playList.get(i).getItemTitle().equalsIgnoreCase(fromPlaylist)){
                    playList.remove(i);
                }
            }
        }

        final AddToPlaylistAdapter adapter = new AddToPlaylistAdapter(context,
                playList, songList);
        RecyclerView rv = (RecyclerView) activity.getLayoutInflater()
                .inflate(R.layout.addtoplaylist, null);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);
        MaterialDialog dialog = Utils.createDialogBuilder(context)
                .title(R.string.menu_add_boom_playlist)
                .customView(rv, false)
                .positiveText(R.string.new_playlist)
                .negativeText(R.string.dialog_txt_cancel)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        newPlaylistDialog(activity, songList, fromPlaylist);

                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .show();
        adapter.setDialog(dialog);
    }

    public static void  addToPlaylist(Activity activity, IMediaItemCollection songCollection, String fromPlaylist) {
        addToPlaylist(activity, songCollection.getMediaElement(),fromPlaylist);
    }


    public static void newPlaylistDialog(final Activity activity, final ArrayList<? extends IMediaItemBase> song, final String fromPlaylist) {
        final Context context = activity;
        Utils.createDialogBuilder(activity)
                .title(R.string.new_playlist)
                .input(context.getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(context).createBoomPlaylist(input.toString());
                            addToPlaylist(activity, song, fromPlaylist);
                        //    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);
                            FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_CREATED_NEW_PLAYLIST);
                            MixPanelAnalyticHelper.getInstance(context).setPeopleAnalytics(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST, input.toString());
                        }
                    }
                })
                .show();
    }

    public static boolean isPhone(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        if(null != activity) {
            activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

            float yInches = metrics.heightPixels / metrics.ydpi;
            float xInches = metrics.widthPixels / metrics.xdpi;
            double diagonalInches = Math.sqrt(xInches * xInches + yInches * yInches);
            if (diagonalInches >= 6.7) {
                // 6.7 inch device or bigger
                return false;
            } else {
                // smaller device
                return true;
            }
        }
        return true;
    }


    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            return true;
        }
        return false;
    }

    public static void networkAlert(Context context) {
        MaterialDialog.Builder builder = Utils.createDialogBuilder(context);
        builder.title(R.string.title_internet_alert);
        builder.content(R.string.check_network);
        builder.cancelable(false);
        builder.positiveText(R.string.ok);
        builder.show();
    }


    public static boolean isMoreThan24Hour() {
        Date installTime = null;
        try {
            PackageManager pm = App.getApplication().getApplicationContext().getPackageManager();
            PackageInfo packageInfo = pm.getPackageInfo(App.getApplication().getPackageName(), PackageManager.GET_PERMISSIONS);

            installTime = new Date( packageInfo.firstInstallTime );
            if(System.currentTimeMillis() - installTime.getTime() > 3600000){
                return true;
            }
        }catch (PackageManager.NameNotFoundException e1) {
        } catch (SecurityException e1) {
        } catch (IllegalArgumentException e1) {
        }catch (Exception e){}
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//        Log.d("Installed: " , dateFormat.format( installTime));
        return false;
    }

    public static void showProgressLoader(Context context){
        if((null == progressLoader || !progressLoader.isShowing()) && ConnectivityReceiver.isNetworkAvailable(context, true)) {
            progressLoader = new BoomDialogView(context);
            progressLoader.setCanceledOnTouchOutside(false);
            progressLoader.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    progressLoader = null;
                }
            });
            progressLoader.show();
        }
    }

    public static boolean isProgressLoaderActive(){
        return (null == progressLoader || !progressLoader.isShowing()) ? false : true;
    }

    public static void dismissProgressLoader() {
        if(null != progressLoader && progressLoader.isShowing()) {
            progressLoader.dismiss();
            progressLoader = null;
        }
    }

    public static MaterialDialog.Builder createDialogBuilder(Context context) {
        return new MaterialDialog.Builder(context)
                .titleColor(ContextCompat.getColor(context, R.color.dialog_title))
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                .typeface(ResourcesCompat.getFont(context, R.font.titilliumweb_semibold), ResourcesCompat.getFont(context, R.font.titilliumweb_regular));

    }

    public static int SMALL_IMAGE_SIZE = -1;
    public static int smallImageSize(Context context) {
        if ( SMALL_IMAGE_SIZE == -1 ) {
            SMALL_IMAGE_SIZE = dpToPx(context, SMALL_IMAGE_SIZE_DP);
        }
        return SMALL_IMAGE_SIZE;
    }

    public static int LARGE_IMAGE_SIZE = -1;
    public static int largeImageSize(Context context) {
        if ( LARGE_IMAGE_SIZE == -1 ) {
            LARGE_IMAGE_SIZE = dpToPx(context, LARGE_IMAGE_SIZE_DP);
        }
        return LARGE_IMAGE_SIZE;
    }

    // Unique device id
    public static String getFingerPrint(Context context) {
        return Settings.Secure.getString(context.getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);
    }
}