package com.globaldelight.boom.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.AnyRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.Media.MediaController;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.business.BusinessPreferences;
import com.globaldelight.boom.business.BusinessUtils;
import com.globaldelight.boom.business.client.BusinessHandler;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.manager.ConnectivityReceiver;
import com.globaldelight.boom.task.PlayerEvents;
import com.globaldelight.boom.ui.musiclist.adapter.utils.AddToPlaylistAdapter;
import com.globaldelight.boom.ui.widgets.BoomDialogView;
import com.globaldelight.boom.utils.handlers.Preferences;

import java.io.File;
import java.util.ArrayList;

import static android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED;
import static com.globaldelight.boom.business.BusinessPreferences.ACTION_APP_SHARED_DATE;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class Utils {
    private Context context;
    private static BoomDialogView progressLoader;
    public static final int SHARE_COMPLETE = 200;

    public Utils(Context context) {
        this.context = context;
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

    public void addToPlaylist(final Activity activity, final ArrayList<? extends IMediaItemBase> songList, final String fromPlaylist) {
        if (songList.size() == 0)
            return;

        ArrayList<? extends IMediaItemBase> playList = MediaController.getInstance(activity).getBoomPlayList();

        if (fromPlaylist != null) {
            for (int i = 0; i < playList.size(); i++) {
                if (playList.get(i).getItemTitle().equalsIgnoreCase(fromPlaylist)) {
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

    public void newPlaylistDialog(final Activity activity, final ArrayList<? extends IMediaItemBase> song, final String fromPlaylist) {
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
                            context.sendBroadcast(new Intent(PlayerEvents.ACTION_UPDATE_BOOM_PLAYLIST));
                            FlurryAnalyticHelper.logEvent(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST);
                            MixPanelAnalyticHelper.getInstance(context).getPeople().set(AnalyticsHelper.EVENT_CREATED_NEW_PLAYLIST, input.toString());
                        }
                    }
                }).show();
    }

    /*public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }*/

    private void setAsRingtone(String filepath) {
        File ringtoneFile = new File(filepath);

        ContentValues content = new ContentValues();
        content.put(MediaStore.MediaColumns.DATA, ringtoneFile.getAbsolutePath());
        content.put(MediaStore.MediaColumns.TITLE, ringtoneFile.getName());
        content.put(MediaStore.MediaColumns.SIZE, 215454);
        content.put(MediaStore.MediaColumns.MIME_TYPE, "audio/*");
        content.put(MediaStore.Audio.Media.DURATION, 230);
        content.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        content.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        content.put(MediaStore.Audio.Media.IS_ALARM, true);
        content.put(MediaStore.Audio.Media.IS_MUSIC, true);

        Uri uri = MediaStore.Audio.Media.getContentUriForPath(
                ringtoneFile.getAbsolutePath());


//        context.getContentResolver().delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtoneFile.getAbsolutePath() + "\"",
//                null);
        Uri newUri = context.getContentResolver().insert(uri, content);
        RingtoneManager.setActualDefaultRingtoneUri(
                context, RingtoneManager.TYPE_RINGTONE,
                newUri);
    }

    private Spanned getString(@StringRes int string) {
        return Html.fromHtml(context.getResources().getString(string));
    }


    public static boolean isPhone(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        if (null != activity) {
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

    public static void showNetworkAlert(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(context.getResources().getString(R.string.network_alart));
        builder.setMessage(context.getResources().getString(R.string.check_network));
        builder.setNeutralButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static void shareStart(Context context) {
        if (ConnectivityReceiver.isNetworkAvailable(context)) {
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
        if (ConnectivityReceiver.isNetworkAvailable(context)) {
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

                BusinessPreferences.writeBoolean(context, ACTION_APP_SHARED, true);
                BusinessPreferences.writeLong(context, ACTION_APP_SHARED_DATE, System.currentTimeMillis());
            } catch (Exception e) {
            }
        }
    }

    public static boolean isMoreThan24Hour(Context context) {
        long firstTimeLaunchDate = 0;
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        try {
            firstTimeLaunchDate = Long.parseLong(preferences.getString("Tool_install_date", "n/a"));
        } catch (NumberFormatException ex) {
        }
        if ((System.currentTimeMillis() - firstTimeLaunchDate) > 3600000) {
            return true;
        }
        return false;
    }

    public static boolean isShareExpireHour(Context context) {
        if ((System.currentTimeMillis() - BusinessPreferences.readLong(context, ACTION_APP_SHARED_DATE, System.currentTimeMillis())) > (3600000 * 5)) {
            return true;
        }
        return false;
    }

    public static void showProgressLoader(Context context) {
        if ((null == progressLoader || !progressLoader.isShowing()) && ConnectivityReceiver.isNetworkAvailable(context)) {
            progressLoader = new BoomDialogView(context);
            progressLoader.setCanceledOnTouchOutside(false);
            progressLoader.show();
        }
    }

    public static void dismissProgressLoader() {
        if (null != progressLoader && progressLoader.isShowing())
            progressLoader.dismiss();
    }

    public static String getDeviceDensity(Activity context) {
        DisplayMetrics metrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        switch (metrics.densityDpi) {
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
        if (!BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_APP_SHARED_DIALOG_SHOWN, false) &&
                BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_IN_APP_PURCHASE, false) &&
                BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_APP_SHARED, false)) {
            new MaterialDialog.Builder(context)
                    .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                    .icon(context.getResources().getDrawable(R.drawable.com_facebook_button_icon, null))
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
                            shareStart(context);
                        }
                    })
                    .show();
            BusinessPreferences.writeBoolean(context, BusinessPreferences.ACTION_APP_SHARED_DIALOG_SHOWN, true);
        }
    }

    public static void EmailPopup(final Context context) {
        if (BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)) {
            new MaterialDialog.Builder(context)
                    .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                    .icon(context.getResources().getDrawable(R.drawable.com_facebook_button_icon, null))
                    .positiveColor(ContextCompat.getColor(context, R.color.dialog_submit_positive))
                    .negativeColor(ContextCompat.getColor(context, R.color.dialog_submit_negative))
                    .widgetColor(ContextCompat.getColor(context, R.color.dialog_widget))
                    .contentColor(ContextCompat.getColor(context, R.color.dialog_content))
                    .positiveText(R.string.submit)
                    .customView(R.layout.subsribe_email, false)
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
                            if (!input.toString().matches("") && ConnectivityReceiver.isNetworkAvailable(context)) {
                                BusinessHandler.getBusinessHandlerInstance(context).saveEmailAddress(BusinessUtils.EmailSource.library, input.toString(), true);
                            }
                        }
                    }).show();
        }
    }

    public static void ExpirePopup(final Context context) {
        if (!BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_APP_EXPIRE_DIALOG_SHOWN, false) &&
                BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)) {
            new MaterialDialog.Builder(context)
                    .backgroundColor(ContextCompat.getColor(context, R.color.dialog_background))
                    .icon(context.getResources().getDrawable(R.drawable.com_facebook_button_icon, null))
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
                            if (ConnectivityReceiver.isNetworkAvailable(context)) {
                                final String appPackageName = context.getPackageName(); // package name of the app
                                try {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                                }
                            }
                        }
                    })
                    .show();
            BusinessPreferences.readBoolean(context, BusinessPreferences.ACTION_APP_EXPIRE_DIALOG_SHOWN, true);
        }
    }

    public static void InternetPopup(final Context activity) {
        if (!BusinessPreferences.readBoolean(activity, BusinessPreferences.ACTION_APP_INTERNET_DIALOG_SHOWN, false) && isMoreThan24Hour(activity) && !ConnectivityReceiver.isNetworkAvailable(activity) && BusinessPreferences.readBoolean(activity, BusinessPreferences.ACTION_IN_APP_PURCHASE, false)) {
            new MaterialDialog.Builder(activity)
                    .backgroundColor(ContextCompat.getColor(activity, R.color.dialog_background))
                    .icon(activity.getResources().getDrawable(R.drawable.com_facebook_button_icon, null))
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
                    .show();
            BusinessPreferences.writeBoolean(activity, BusinessPreferences.ACTION_APP_INTERNET_DIALOG_SHOWN, true);
        }
    }
}
