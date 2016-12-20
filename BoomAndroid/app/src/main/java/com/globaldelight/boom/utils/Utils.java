package com.globaldelight.boom.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.AnyRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.globaldelight.boom.R;
import com.globaldelight.boom.analytics.AnalyticsHelper;
import com.globaldelight.boom.analytics.FlurryAnalyticHelper;
import com.globaldelight.boom.analytics.MixPanelAnalyticHelper;
import com.globaldelight.boom.data.MediaCollection.IMediaItemBase;
import com.globaldelight.boom.data.MediaLibrary.ItemType;
import com.globaldelight.boom.data.MediaLibrary.MediaController;
import com.globaldelight.boom.data.MediaLibrary.MediaType;
import com.globaldelight.boom.task.PlayerService;
import com.globaldelight.boom.ui.musiclist.adapter.AddToPlaylistDialogListAdapter;
import com.globaldelight.boom.utils.decorations.SimpleDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;


public class Utils {

    /*public static String SCREEN_SONGS = "player.songs";
    public static String SCREEN_ALBUM = "player.album";
    public static String SCREEN_ARTIST = "player.ic_artist";
    public static String SCREEN_GENRE = "player.genre";
    public static String SCREEN_PLAYLIST = "player.playlist";
    public static String SCREEN_BOOM_PLAYLIST = "player.boom.playlist";*/

    private Context context;

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

        ArrayList<? extends IMediaItemBase>  playList = MediaController.getInstance(activity).getMediaCollectionItemList(ItemType.BOOM_PLAYLIST, MediaType.DEVICE_MEDIA_LIB)/*MediaQuery.getPlayList(context)*/;

        if(fromPlaylist != null){
            for(int i=0; i< playList.size(); i++){
                if(playList.get(i).getItemTitle().equalsIgnoreCase(fromPlaylist)){
                    playList.remove(i);
                }
            }
        }

        final AddToPlaylistDialogListAdapter adapter = new AddToPlaylistDialogListAdapter(context,
                playList, songList);
        RecyclerView rv = (RecyclerView) activity.getLayoutInflater()
                .inflate(R.layout.addtoplaylist, null);
        rv.setLayoutManager(new LinearLayoutManager(context));
        rv.setAdapter(adapter);
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title(R.string.add_to_playlist)
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(Color.parseColor("#81cbc4"))
                .negativeColor(Color.parseColor("#81cbc4"))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
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
                .backgroundColor(Color.parseColor("#171921"))
                .titleColor(Color.parseColor("#ffffff"))
                .positiveColor(Color.parseColor("#81cbc4"))
                .negativeColor(Color.parseColor("#81cbc4"))
                .widgetColor(Color.parseColor("#ffffff"))
                .contentColor(Color.parseColor("#ffffff"))
                .typeface("TitilliumWeb-SemiBold.ttf", "TitilliumWeb-Regular.ttf")
                .input(context.getResources().getString(R.string.new_playlist), null, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(MaterialDialog dialog, CharSequence input) {
                        if (!input.toString().matches("")) {
                            MediaController.getInstance(context).createBoomPlaylist(input.toString());
                            addToPlaylist(activity, song, fromPlaylist);
                            context.sendBroadcast(new Intent(PlayerService.ACTION_UPDATE_BOOM_PLAYLIST));
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


    public static boolean isPhone(Activity activity){
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches= metrics.heightPixels/metrics.ydpi;
        float xInches= metrics.widthPixels/metrics.xdpi;
        double diagonalInches = Math.sqrt(xInches*xInches + yInches*yInches);
        if (diagonalInches>=6.5){
            // 6.5inch device or bigger
            return false;
        }else{
            // smaller device
            return true;
        }
    }

}
