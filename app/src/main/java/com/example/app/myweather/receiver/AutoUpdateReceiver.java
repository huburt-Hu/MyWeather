package com.example.app.myweather.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.app.myweather.service.AutoUpdateService;

/**
 * Created by hxb on 2016/4/18.
 * 收到广播就启动AutoUpdateService去更新天气
 */
public class AutoUpdateReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, AutoUpdateService.class);
        context.startService(i);
    }
}
