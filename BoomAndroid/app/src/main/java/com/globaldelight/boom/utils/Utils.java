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
import android.support.annotation.AnyRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static Bitmap getBitmapOfVector(Context context, @DrawableRes int id, int height, int width) {
        Drawable vectorDrawable = context.getDrawable(id);
        if (vectorDrawable != null)
            vectorDrawable.setBounds(0, 0, width, height);
        Bitmap bm = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bm);
        if (vectorDrawable != null)
            vectorDrawable.draw(canvas);
        return bm;
    }

    public static int pxToDp(Context context, int px) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int dp = Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return dp;
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
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.menu_add_boom_playlist)
                .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(context, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
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
        new MaterialDialog.Builder(context)
                .title(R.string.new_playlist)
                .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                .titleColor(ContextCompat.getColor(context, R.color.dialog_title))
                .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .input(context.getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(context).createBoomPlaylist(input.toString());
                            addToPlaylist(activity, song, fromPlaylist);
                        //    FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);
                            FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.EVENT_CREATED_NEW_PLAYLIST);

                            MixPanelAnalyticHelper.getInstance(context).getPeople().set(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST, input.toString());
                        }
                    }
                }).show();
    }

    public static void newPlaylistDialog(Activity activity, IMediaItemCollection songs, String fromPlaylist) {
        newPlaylistDialog(activity,songs.getMediaElement(), fromPlaylist);
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
        AlertDialog.Builder builder =
                new AlertDialog.Builder(context, R.style.DialogTheme);
        builder.setTitle(R.string.title_internet_alert);
        builder.setMessage(R.string.check_network);
        builder.setCancelable(false);
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    public static int getStatusBarHeight(Context context){
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static void shareStart(Context context) {
        if(ConnectivityReceiver.isNetworkAvailable(context, true)) {
            try {
                Intent shareIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                String sAux = "\nDownload Boom Music Player\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.globaldelight.boom \n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, sAux);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                context.startActivity(Intent.createChooser(shareIntent, "share"));
            } catch (Exception e) {
            }
        }
    }

    public static void shareStart(Context context, Fragment fragment) {
        if(ConnectivityReceiver.isNetworkAvailable(context, true)) {
            try {
                Intent shareIntent = new Intent(
                        android.content.Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getResources().getString(R.string.app_name));
                String sAux = "\nDownload Boom Music Player\n\n";
                sAux = sAux + "https://play.google.com/store/apps/details?id=com.globaldelight.boom \n\n";
                shareIntent.putExtra(Intent.EXTRA_TEXT, sAux);
                shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
                fragment.startActivityForResult(Intent.createChooser(shareIntent, "share"), SHARE_COMPLETE);
            } catch (Exception e) {
            }
        }
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

    public static String getDeviceDensity(Activity context){
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch(metrics.densityDpi){
            case DisplayMetrics.DENSITY_LOW:
                return "LDP";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "MDP";
            case DisplayMetrics.DENSITY_HIGH:
                return "HDP";
            case DisplayMetrics.DENSITY_XHIGH:
                return "XHDP";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "XXHDP";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "XXXHDP";
        }
        return "Defeult";
    }

    public static void SharePopup(final Context context) {
        new MaterialDialog.Builder(context)
                .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .negativeText(R.string.share_button)
                .positiveText(R.string.continue_button)
                .customView(R.layout.share_popup, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            FlurryAnalyticHelper.logEvent(UtilAnalytics.Share_Opened_from_Dialog);
                        FlurryAnalytics.getInstance(context).setEvent(FlurryEvents.Share_Opened_from_Dialog);

                        shareStart(context);
                    }
                })
                .show();
    }

    public static void EmailPopup(final Context context) {
            new MaterialDialog.Builder(context)
                    .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                    .title(R.string.sub_email_header)
                    .titleColor(ContextCompat.getColor(context, R.color.dialog_title))
                    .icon(context.getDrawable(R.drawable.ic_icon_for_popup))
                    .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                    .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                    .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                    .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                    .positiveText(R.string.submit)
                    .autoDismiss(false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                    .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                    .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                    .inputType(TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
                    .input(context.getResources().getString(R.string.email_text_hint), null, new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            if (!input.toString().matches("") && ConnectivityReceiver.isNetworkAvailable(context, true)) {
                            }
                        }
                    }).show();

    }

    public static void ExpirePopup(final Context context) {
        new MaterialDialog.Builder(context)
                .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .negativeText(R.string.buy_button)
                .positiveText(R.string.continue_button)
                .customView(R.layout.expire_pop_up, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EmailPopup(context);
                            }
                        }, 5000);
                        dialog.dismiss();

                    }
                })
                .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            jumpToStore(context);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                EmailPopup(context);
                            }
                        }, 5000);
                    }
                })
                .show();
    }

    public static void InternetPopup(final Activity activity){
        new MaterialDialog.Builder(activity)
                .backgroundColor(ContextCompat.getColor(activity, R.color.dialog_background))
                .positiveColor(ContextCompat.getColor(activity, R.color.dialog_submit_positive))
                .negativeColor(ContextCompat.getColor(activity, R.color.dialog_submit_negative))
                .widgetColor(ContextCompat.getColor(activity, R.color.dialog_widget))
                .contentColor(ContextCompat.getColor(activity, R.color.dialog_content))
                .negativeText(R.string.buy_button)
                .positiveText(R.string.settings_button)
                .customView(R.layout.off_line_pop_up, false)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        activity.startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        jumpToStore(activity);
                    }
                })
                .show();
    }

    public static void jumpToStore(Activity activity) {
        Intent intent = new Intent(activity, ActivityContainer.class);
        intent.putExtra("container", R.string.store_title);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        activity.startActivity(intent);
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
}