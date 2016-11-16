package com.player.boom.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v7.graphics.Palette;
import android.util.Log;
import android.widget.RemoteViews;

import com.player.boom.App;
import com.player.boom.R;
import com.player.boom.data.DeviceMediaCollection.MediaItem;
import com.player.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by architjn on 15/12/15.
 */
public class NotificationHandler {

    private static final int NOTIFICATION_ID = 56565;
    private Context context;
    private PlayerService service;
    private boolean notificationActive;
    RemoteViews notiLayoutBig, notiCollapsedView;
    private static Notification notificationCompat;
    private static NotificationManager notificationManager;

    public NotificationHandler(Context context, PlayerService service) {
        this.context = context;
        this.service = service;
    }

    private Notification.Builder createBuiderNotification(boolean removable) {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(PlayerService.ACTION_NOTI_CLICK);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(PlayerService.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        if (removable)
            return new Notification.Builder(context)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
        else
            return new Notification.Builder(context)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_audiotrack_white_24dp)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
    }

    public void setNotificationPlayer(boolean removable) {
        notificationCompat = createBuiderNotification(removable).build();
        notiLayoutBig = new RemoteViews(context.getPackageName(),
                R.layout.notification_layout);
        notiCollapsedView = new RemoteViews(context.getPackageName(),
                R.layout.notification_small);
        if (Build.VERSION.SDK_INT >= 16) {
            notificationCompat.bigContentView = notiLayoutBig;
        }
        notificationCompat.contentView = notiCollapsedView;
        notificationCompat.priority = Notification.PRIORITY_MAX;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (!removable)
            service.startForeground(NOTIFICATION_ID, notificationCompat);
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
        notificationActive = true;
    }

    public void changeNotificationDetails(MediaItem item, boolean playing) {
        if(item == null){
            notificationCompat.bigContentView.setViewVisibility(R.id.noti_name, GONE);
            notificationCompat.bigContentView.setViewVisibility(R.id.noti_artist, GONE);

            notificationCompat.contentView.setViewVisibility(R.id.noti_name, GONE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_artist, GONE);

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_play_button, GONE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_play_button, GONE);

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_next_button, GONE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_next_button, GONE);

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_prev_button, GONE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_prev_button, GONE);

            setNoTrackImageView();
            notificationManager.notify(NOTIFICATION_ID, notificationCompat);
            return;
        }else if(Build.VERSION.SDK_INT >= 16){
            notificationCompat.bigContentView.setViewVisibility(R.id.noti_name, VISIBLE);
            notificationCompat.bigContentView.setViewVisibility(R.id.noti_artist, VISIBLE);
            notificationCompat.bigContentView.setTextViewText(R.id.noti_name, item.getItemTitle());
            notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, item.getItemArtist());

            notificationCompat.contentView.setViewVisibility(R.id.noti_name, VISIBLE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_artist, VISIBLE);
            notificationCompat.contentView.setTextViewText(R.id.noti_name, item.getItemTitle());
            notificationCompat.contentView.setTextViewText(R.id.noti_artist, item.getItemArtist());

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_play_button, VISIBLE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_play_button, VISIBLE);
            Intent playClick = new Intent();
            playClick.setAction(PlayerService.ACTION_PLAY_PAUSE_SONG);
            PendingIntent playClickIntent = PendingIntent.getBroadcast(context, 10101, playClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
            Log.d("Playing : ", ""+playing);
            if (playing) {
                notificationCompat.bigContentView
                        .setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause);
                notificationCompat.contentView
                        .setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause);
            }else {
                notificationCompat.bigContentView
                        .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play);
                notificationCompat.contentView
                        .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play);
            }

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_next_button, VISIBLE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_next_button, VISIBLE);
            Intent nextClick = new Intent();
            nextClick.setAction(PlayerService.ACTION_NEXT_SONG);
            PendingIntent nextClickIntent = PendingIntent.getBroadcast(context, 10102, nextClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);

            notificationCompat.bigContentView.setViewVisibility(R.id.noti_prev_button, VISIBLE);
            notificationCompat.contentView.setViewVisibility(R.id.noti_prev_button, VISIBLE);
            Intent prevClick = new Intent();
            prevClick.setAction(PlayerService.ACTION_PREV_SONG);
            PendingIntent prevClickIntent = PendingIntent.getBroadcast(context, 10103, prevClick, 0);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);

            if (isPathValid(item.getItemArtUrl())) {
                Picasso.with(context).load(new File(item.getItemArtUrl())).into(new Target() {
                    @Override
                    public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                        notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                        notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                        notificationManager.notify(NOTIFICATION_ID, notificationCompat);

//                        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//                            @Override
//                            public void onGenerated(Palette palette) {
//                                notificationCompat.color = palette.getDarkVibrantColor(
//                                        palette.getDarkMutedColor(
//                                                palette.getMutedColor(0xffffffff)));
////                                notificationManager.notify(NOTIFICATION_ID, notificationCompat);
//                            }
//                        });
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {
                        setDefaultImageView();
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {
                    }
                });
            } else {
                setDefaultImageView();
            }

        }
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    private boolean fileExist(String albumArtPath) {
        File imgFile = new File(albumArtPath);
        return imgFile.exists();
    }

    public boolean isPathValid(String path) {
        return path != null && fileExist(path);
    }

    private void setNoTrackImageView() {
        Utils utils = new Utils(context);
        notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.no_song_selected,
                        utils.dpToPx(context, 100), utils.dpToPx(context, 100)));
        notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.no_song_selected,
                        utils.dpToPx(context, 50), utils.dpToPx(context, 50)));
//        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    private void setDefaultImageView() {
        Utils utils = new Utils(context);
        notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.default_album_art,
                        utils.dpToPx(context, 100), utils.dpToPx(context, 100)));
        notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.default_album_art,
                        utils.dpToPx(context, 50), utils.dpToPx(context, 50)));
//        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public void updateNotificationView() {
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public void stopNotification(){
//       need to stop
    }

    public boolean isNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }

    public Notification getNotificationCompat() {
        return notificationCompat;
    }
}
