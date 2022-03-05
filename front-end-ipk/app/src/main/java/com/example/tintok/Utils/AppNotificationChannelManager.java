package com.example.tintok.Utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;


import com.example.tintok.R;

import java.util.ArrayList;

public class AppNotificationChannelManager {
    public static Context applicationContext;
    private  static AppNotificationChannelManager instance;
    private AppNotificationChannelManager(){
        createdChannels = new ArrayList<>();
    }
    public static synchronized AppNotificationChannelManager getInstance(){
        if(instance == null)
            instance = new AppNotificationChannelManager();
        return instance;
    }

    private ArrayList<String> createdChannels;

    public void createNotificationChannel(String CHANNEL_ID) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = CHANNEL_ID;
            String description = "New Channel named "+CHANNEL_ID+" has been created";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = applicationContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            createdChannels.add(CHANNEL_ID);
        }
    }

    public void pushNotificationBasic(String CHANNEL_ID, String title, String content, Intent pendingIntent){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            if(!createdChannels.contains(CHANNEL_ID))
                return;
        }

        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(applicationContext, CHANNEL_ID);

        PendingIntent intent = PendingIntent.getActivity(applicationContext, 0, pendingIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        notiBuilder.setSmallIcon(R.drawable.ic_arrow_back_black_24dp)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentText(content)
                .setContentIntent(intent)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setColor(Color.BLUE);


        NotificationManager notificationManager = applicationContext.getSystemService(NotificationManager.class);
        notificationManager.notify(createdChannels.indexOf(CHANNEL_ID), notiBuilder.build());
    }
}
