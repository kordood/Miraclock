package com.example.fingerprintalarm;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

public class AlarmService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "AlarmChannel_01";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Alarm notification channel",
                    NotificationManager.IMPORTANCE_DEFAULT);        // NotificationChannel generate

            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);      // Channel create

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("")
                    .setContentText("").build();        // Notification build

            startForeground(1, notification);       // Notification start
        }

        Log.d("Service 실행","Service is on");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){       // able to start ringing
        Log.d("Service 실행","Service started command");
        Toast.makeText(this, "알람이 울립니다.", Toast.LENGTH_SHORT).show();
        return START_NOT_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) {
        Log.d("Service 실행","Service is binding");
        return null;
    }
    public void onDestory() {
        super.onDestroy();
        Log.d("Service 실행","Service is destroied");
    }
}
