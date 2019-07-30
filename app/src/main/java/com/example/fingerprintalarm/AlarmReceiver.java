package com.example.fingerprintalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) // 해당 시간대 조건이 맞으면 이 함수를 자동 실행함. 홈키로 나가져도 실행됨.
    {
        Toast.makeText(context,"특정 시간대 조건이 맞아 시작합니다. ", Toast.LENGTH_SHORT).show();
        Log.d("Receiver 실행","Receiver is on");

        Intent serviceIntent = new Intent(context, AlarmService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            Log.d("Receiver 실행","Receiver is starting ForegroundService");
            context.startForegroundService(serviceIntent);
        }else{
            Log.d("Receiver 실행","Receiver is starting Service");
            context.startService(serviceIntent);
        }

        //ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        /* service running check
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (AlarmService.class.getName().equals(service.service.getClassName()))
                Log.d("Receiver 실행","Service in on");
        }
        */

        context.startActivity(new Intent(context, FingerprintActivity.class)
                .setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }
}