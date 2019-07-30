package com.example.fingerprintalarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    FingerprintActivity fingerprintActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        setAlarm();
    }

    protected void setAlarm(){
        Calendar calendar;
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 14);     // calendar.set(Calendar.HOUR_OF_DAY, 시간(int)); 저녁10시->22시
        calendar.set(Calendar.MINUTE, 48);          // calendar.set(Calendar.MINUTE, 분(int));

        final AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);      // Alarm Manager generate
        Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);     //
        final PendingIntent sender = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        long TimeUntilTrigger;
        if (System.currentTimeMillis() > calendar.getTimeInMillis()){       // 현재시간 > calendar 설정 시간 meaning: 설정된 시간을 지났다면
            TimeUntilTrigger = calendar.getTimeInMillis() + 86400000;       // 다음날 시간으로 설정
            //Toast.makeText(getApplicationContext(),"저장",Toast.LENGTH_LONG).show();
        }else {
            TimeUntilTrigger = calendar.getTimeInMillis();                  // 설정 시간으로 설정
            //Toast.makeText(getApplicationContext(), "저장완료", Toast.LENGTH_LONG).show();
        }

        am.setRepeating(AlarmManager.RTC_WAKEUP,TimeUntilTrigger, 24 * 60 * 60 * 1000, sender);     // 알람을 "매일" 해당 시간 될 때마다 반복.
        Log.d("Alarm 실행", "실행됨");
    }

}