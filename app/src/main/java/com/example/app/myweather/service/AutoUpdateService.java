package com.example.app.myweather.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.example.app.myweather.R;
import com.example.app.myweather.activities.MainActivity;
import com.example.app.myweather.receiver.AutoUpdateReceiver;
import com.example.app.myweather.util.CallBackListener;
import com.example.app.myweather.util.HandleResponse;
import com.example.app.myweather.util.HttpUtil;

/**
 * Created by hxb on 2016/4/18.
 * 自动更新天气的服务
 * IntentService会自动在onHandleIntent开启一个子线程去执行任务
 */
public class AutoUpdateService extends IntentService {
    public AutoUpdateService() {
        super("AutoUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        查询天气
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherCode = preferences.getString("weather_code", null);
        String weather = preferences.getString("weather", "");
        String temp1 = preferences.getString("l_tmp", "");
        String temp2 = preferences.getString("h_tmp", "");
        HttpUtil.sendWeatherRequest(weatherCode, new CallBackListener() {
            @Override
            public void onFinish(String res) {
                HandleResponse.getInstance().handleWeatherResponse(AutoUpdateService.this, res);
            }

            @Override
            public void onError(Exception e) {
                e.printStackTrace();
            }
        });
//        每当完成天气更新就发送一个通知
//        判断活动是否存在
        Intent intent1 = new Intent();
        intent1.setClassName("com.example.app.myweather", "MainActivity");
        if (intent1.resolveActivity(getPackageManager()) == null) {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification notification = new Notification.Builder(this)
                    .setAutoCancel(false)
                    .setContentTitle("今天天气：")
                    .setContentText(weather + "   " + temp1 + "~" + temp2 + "\u2103")
                    .setSmallIcon(R.drawable.sun)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setContentIntent(PendingIntent.getActivity(this, 1, new Intent(this, MainActivity.class), 0))
                    .build();
            notificationManager.notify(0x1234, notification);
        }
//        设置一个定时器，每隔4小时就启动AutoUpdateReceiver广播
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int hour4 = 4 * 60 * 60 * 1000;
//        int hour4 = 5000;//测试用的时间
        long targetTime = SystemClock.elapsedRealtime() + hour4;
        Intent i = new Intent(this, AutoUpdateReceiver.class);
        PendingIntent p = PendingIntent.getBroadcast(this, 0, i, 0);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, targetTime, p);
    }
}
