package com.example.app.myweather.util;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by hxb on 2016/4/6.
 * 封装了发送Http请求的方法
 * 使用HttpHttpURLConnection 来实现请求
 * IO流进行数据输入
 */
public class HttpUtil {
    private static String address;

    public static void sendLocationRequest(final String ads, final CallBackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(ads);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    listener.onFinish(response.toString());
                } catch (IOException e) {
                    listener.onError(e);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void sendWeatherRequest(String code, final CallBackListener listener) {
        if (!TextUtils.isEmpty(code)) {
            address = "http://apis.baidu.com/apistore/weatherservice/cityid?cityid=" + code;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(address);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(8000);
                    connection.setReadTimeout(8000);
                    connection.setRequestProperty("apikey", "5bba9f3e9999b5122f5d1b8fac9dc013");
                    inputStream = connection.getInputStream();
                    bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        response.append(line);
                    }
                    listener.onFinish(response.toString());
                } catch (IOException e) {
                    listener.onError(e);
                }
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
