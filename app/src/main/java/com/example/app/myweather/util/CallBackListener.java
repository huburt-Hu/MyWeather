package com.example.app.myweather.util;

/**
 * Created by hxb on 2016/4/6.
 * 回调方法的接口
 */
public interface CallBackListener {
     void onFinish(String res);
     void onError(Exception e);
}
