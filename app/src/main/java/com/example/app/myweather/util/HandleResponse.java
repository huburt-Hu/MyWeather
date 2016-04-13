package com.example.app.myweather.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import com.example.app.myweather.db.City;
import com.example.app.myweather.db.County;
import com.example.app.myweather.db.MyWeatherDB;
import com.example.app.myweather.db.Province;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by hxb on 2016/4/6.
 * 是一个单例类；
 * 对返回的数据进行处理；
 * handleProvinceResponse将省信息导入SQlite数据库
 * handleCityResponse将城市信息导入SQlite数据库
 * handleCountyResponse将县信息导入SQlite数据库
 * handleWeatherResponse将JSON格式的天气信息解析成字符串并存入SharedPreferences
 */
public class HandleResponse {
    private static HandleResponse handleResponse;

    private HandleResponse() {
    }

    public static HandleResponse getInstance() {
        if (handleResponse == null) {
            synchronized (HandleResponse.class) {
                if (handleResponse == null) {
                    handleResponse = new HandleResponse();
                }
            }
        }
        return handleResponse;
    }

    public boolean handleProvinceResponse(MyWeatherDB myWeatherDB, String response) {
        if (!TextUtils.isEmpty(response)) {
            String[] a = response.split(",");
            if (a != null && a.length > 0) {
                for (String b : a) {
                    String[] c = b.split("\\|");
                    Province province = new Province();
                    province.setProvinceCode(c[0]);
                    province.setProvinceName(c[1]);
                    myWeatherDB.saveProvince(province);
                }
                return true;
            }
        }
        return false;
    }

    public boolean handleCityResponse(MyWeatherDB myWeatherDB, String response,String provinceId) {
        if (!TextUtils.isEmpty(response)) {
            String[] a = response.split(",");
            if (a != null && a.length > 0) {
                for (String b : a) {
                    String[] c = b.split("\\|");
                    City city = new City();
                    city.setCityCode(c[0]);
                    city.setCityName(c[1]);
                    city.setProvinceId(provinceId);
                    myWeatherDB.saveCity(city);
                }
                return true;
            }
        }
        return false;
    }

    public boolean handleCountyResponse(MyWeatherDB myWeatherDB, String response,String cityId) {
        if (!TextUtils.isEmpty(response)) {
            String[] a = response.split(",");
            if (a != null && a.length > 0) {
                for (String b : a) {
                    String[] c = b.split("\\|");
                    County county = new County();
                    county.setCountyCode(c[0]);
                    county.setCountyName(c[1]);
                    county.setCityId(cityId);
                    myWeatherDB.saveCounty(county);
                }
                return true;
            }
        }
        return false;
    }

    public void handleWeatherResponse(Context context, String response) {
        if (!TextUtils.isEmpty(response)) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject weatherinfo = jsonObject.getJSONObject("retData");
                String city = weatherinfo.getString("city");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日", Locale.CHINA);
                String date =sdf.format(new Date()) ;
                String time = weatherinfo.getString("time");
                String weather = weatherinfo.getString("weather");
                String l_tmp = weatherinfo.getString("l_tmp");
                String h_tmp = weatherinfo.getString("h_tmp");
                String WD = weatherinfo.getString("WD");
                String WS = weatherinfo.getString("WS");
                String sunrise = weatherinfo.getString("sunrise");
                String sunset = weatherinfo.getString("sunset");
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
                editor.putString("city", city);
                editor.putString("date", date);
                editor.putString("time", time);
                editor.putString("weather", weather);
                editor.putString("l_tmp", l_tmp);
                editor.putString("h_tmp", h_tmp);
                editor.putString("WD", WD);
                editor.putString("WS", WS);
                editor.putString("sunrise", sunrise);
                editor.putString("sunset", sunset);
                editor.apply();

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}

