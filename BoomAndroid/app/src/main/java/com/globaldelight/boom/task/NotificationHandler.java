package com.globaldelight.boom.task;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.globaldelight.boom.App;
import com.globaldelight.boom.manager.PlayerServiceReceiver;
import com.globaldelight.boom.R;
import com.globaldelight.boom.data.MediaCollection.IMediaItem;
import com.globaldelight.boom.utils.PlayerUtils;
import com.globaldelight.boom.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
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
        notificationIntent.setAction(PlayerServiceReceiver.ACTION_NOTI_CLICK);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(PlayerServiceReceiver.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        if (removable)
            return new Notification.Builder(context)
                    .setOngoing(false)
                    .setSmallIcon(R.drawable.ic_boom_status_bar_icon)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
        else
            return new Notification.Builder(context)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_boom_status_bar_icon)
                    .setContentIntent(contentIntent)
                    .setDeleteIntent(deletePendingIntent);
    }

    public void setNotificationPlayer(boolean removable) {
        if ( notificationCompat == null ) {
            notificationCompat = createBuiderNotification(true).build();
            notiLayoutBig = new RemoteViews(context.getPackageName(),
                    R.layout.notification_layout);
            notiCollapsedView = new RemoteViews(context.getPackageName(),
                    R.layout.notification_small);
            if (Build.VERSION.SDK_INT >= 16) {
                notificationCompat.bigContentView = notiLayoutBig;
            }
            notificationCompat.contentView = notiCollapsedView;

            Intent playClick = new Intent();
            playClick.putExtra("requestCode",10101);
            playClick.setAction(PlayerServiceReceiver.ACTION_PLAY_PAUSE_SONG);
            PendingIntent playClickIntent = PendingIntent.getBroadcast(context, 10101, playClick, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_play_button, playClickIntent);

            Intent nextClick = new Intent();
            nextClick.putExtra("requestCode",10102);
            nextClick.setAction(PlayerServiceReceiver.ACTION_NEXT_SONG);
            PendingIntent nextClickIntent = PendingIntent.getBroadcast(context, 10102, nextClick, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_next_button, nextClickIntent);

            Intent prevClick = new Intent();
            prevClick.putExtra("requestCode",10103);
            prevClick.setAction(PlayerServiceReceiver.ACTION_PREV_SONG);
            PendingIntent prevClickIntent = PendingIntent.getBroadcast(context, 10103, prevClick, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationCompat.bigContentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);
            notificationCompat.contentView.setOnClickPendingIntent(R.id.noti_prev_button, prevClickIntent);

            notificationCompat.priority = Notification.PRIORITY_MAX;
        }

        final int stickyFlag = Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;
        if ( !removable ) {
            notificationCompat.flags = notificationCompat.flags | stickyFlag;
            service.startForeground(NOTIFICATION_ID, notificationCompat);
        }
        else {
            notificationCompat.flags = notificationCompat.flags & ~stickyFlag;
        }

        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
        notificationActive = true;
    }

    public void changeNotificationDetails(IMediaItem item, boolean playing, boolean isLastPlayed) {

        if(item == null && !isLastPlayed){
            removeNotification();
            return;
        }
        notificationCompat.bigContentView.setViewVisibility(R.id.noti_name, VISIBLE);
        notificationCompat.bigContentView.setViewVisibility(R.id.noti_artist, null != item.getItemArtist() ? VISIBLE : GONE);
        notificationCompat.bigContentView.setTextViewText(R.id.noti_name, item.getItemTitle());
        notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, item.getItemArtist());

        notificationCompat.contentView.setViewVisibility(R.id.noti_name, VISIBLE);
        notificationCompat.contentView.setViewVisibility(R.id.noti_artist, null != item.getItemArtist() ? VISIBLE : GONE);
        notificationCompat.contentView.setTextViewText(R.id.noti_name, item.getItemTitle());
        notificationCompat.contentView.setTextViewText(R.id.noti_artist, item.getItemArtist());

        notificationCompat.bigContentView.setViewVisibility(R.id.noti_play_button, VISIBLE);
        notificationCompat.contentView.setViewVisibility(R.id.noti_play_button, VISIBLE);
        Log.d("Playing : ", ""+playing);
        if (playing) {
            notificationCompat.bigContentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause_notification);
            notificationCompat.contentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_pause_notification);
        }else {
            notificationCompat.bigContentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play_notification);
            notificationCompat.contentView
                    .setImageViewResource(R.id.noti_play_button, R.drawable.ic_play_notification);
        }

        if (PlayerUtils.isPathValid(item.getItemArtUrl())) {
            Picasso.with(context).load(new File(item.getItemArtUrl())).into(new Target() {
                @Override
                public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                    notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                    notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art, bitmap);
                    notificationManager.notify(NOTIFICATION_ID, notificationCompat);
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
        notificationManager.notify(NOTIFICATION_ID, notificationCompat);
    }

    public void removeNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }

    private void setDefaultImageView() {
        Utils utils = new Utils(context);
        notificationCompat.bigContentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.ic_default_art_grid,
                        utils.dpToPx(context, 100), utils.dpToPx(context, 100)));
        notificationCompat.contentView.setImageViewBitmap(R.id.noti_album_art,
                utils.getBitmapOfVector(context, R.drawable.ic_default_art_grid,
                        utils.dpToPx(context, 50), utils.dpToPx(context, 50)));
    }

    public boolean isNotificationActive() {
        return notificationActive;
    }

    public void setNotificationActive(boolean notificationActive) {
        this.notificationActive = notificationActive;
    }
}
