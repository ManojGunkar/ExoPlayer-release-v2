package com.globaldelight.boom.app.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.globaldelight.boom.app.receivers.PlayerServiceReceiver;
import com.globaldelight.boom.R;
import com.globaldelight.boom.collection.base.IMediaElement;
import com.globaldelight.boom.collection.base.IMediaItem;
import com.globaldelight.boom.utils.Log;
import com.globaldelight.boom.utils.PlayerUtils;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

/**
 * Created by Rahul Kumar Agrawal on 6/14/2016.
 */

public class NotificationHandler  {

    private static final int NOTIFICATION_ID = 56565;
    private Context context;
    private Service service;
    private NotificationManager notificationManager;

    public NotificationHandler(Service service) {
        this.context = service;
        this.service = service;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private Notification.Builder createBuiderNotification(boolean active) {
        Intent notificationIntent = new Intent();
        notificationIntent.setAction(PlayerServiceReceiver.ACTION_NOTI_CLICK);
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, 0);
        Intent deleteIntent = new Intent();
        deleteIntent.setAction(PlayerServiceReceiver.ACTION_NOTI_REMOVE);
        PendingIntent deletePendingIntent = PendingIntent.getBroadcast(context, 0, deleteIntent, 0);
        return new Notification.Builder(context)
                .setOngoing(active)
                .setSmallIcon(R.drawable.ic_boom_status_bar_icon)
                .setContentIntent(contentIntent)
                .setDeleteIntent(deletePendingIntent);
    }


    public void update(IMediaElement item, boolean playing, boolean isLastPlayed) {

        if(item == null && !isLastPlayed){
            removeNotification();
            return;
        }
        Notification notificationCompat = createBuiderNotification(playing).build();
        RemoteViews notiLayoutBig = new RemoteViews(context.getPackageName(),
                R.layout.notification_layout);
        RemoteViews notiCollapsedView = new RemoteViews(context.getPackageName(),
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

        notificationCompat.bigContentView.setViewVisibility(R.id.noti_name, VISIBLE);
        notificationCompat.bigContentView.setViewVisibility(R.id.noti_artist, null != item.getDescription() ? VISIBLE : GONE);
        notificationCompat.bigContentView.setTextViewText(R.id.noti_name, item.getTitle());
        notificationCompat.bigContentView.setTextViewText(R.id.noti_artist, item.getDescription());

        notificationCompat.contentView.setViewVisibility(R.id.noti_name, VISIBLE);
        notificationCompat.contentView.setViewVisibility(R.id.noti_artist, null != item.getDescription() ? VISIBLE : GONE);
        notificationCompat.contentView.setTextViewText(R.id.noti_name, item.getTitle());
        notificationCompat.contentView.setTextViewText(R.id.noti_artist, item.getDescription());

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
            Uri bitmapUri = Uri.parse(item.getItemArtUrl());
            notificationCompat.bigContentView.setImageViewUri(R.id.noti_album_art, bitmapUri);
            notificationCompat.contentView.setImageViewUri(R.id.noti_album_art, bitmapUri);
        } else {
            notificationCompat.bigContentView.setImageViewResource(R.id.noti_album_art,
                    R.drawable.ic_default_art_grid);
            notificationCompat.contentView.setImageViewResource(R.id.noti_album_art,
                    R.drawable.ic_default_art_grid);
        }

        if ( playing ) {
            service.startForeground(NOTIFICATION_ID, notificationCompat);
        }
        else {
            notificationManager.notify(NOTIFICATION_ID, notificationCompat);
        }

    }

    public void removeNotification(){
        notificationManager.cancel(NOTIFICATION_ID);
    }
 }
