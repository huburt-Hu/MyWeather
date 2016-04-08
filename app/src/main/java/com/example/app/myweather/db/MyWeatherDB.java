package com.example.app.myweather.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hxb on 2016/4/6.
 * 对数据的增加和查询进行了封装
 * 通过 SQListOpenHelper的子类调用getWritableDatabase()方法得到的SQLiteDatabase对象
 * 再调用insert(),query()实现
 */
public class MyWeatherDB {
    private SQLiteDatabase sqLiteDatabase;
    private Cursor cursor;
    private static MyWeatherDB myWeatherDB;

    private MyWeatherDB(Context context) {
        MyWeatherSQListOpenHelper helper = new MyWeatherSQListOpenHelper(context, "MyWeather", null, 1);
        sqLiteDatabase = helper.getWritableDatabase();
    }

    public static MyWeatherDB getInstance(Context context) {
        if (myWeatherDB == null) {
            synchronized (MyWeatherDB.class) {
                if (myWeatherDB == null) {
                    myWeatherDB = new MyWeatherDB(context);
                }
            }
        }
        return myWeatherDB;
    }

    public void saveProvince(Province province) {
        ContentValues values = new ContentValues();
        values.put("province_name", province.getProvinceName());
        values.put("province_code", province.getProvinceCode());
        sqLiteDatabase.insert("Province", null, values);
        values.clear();
    }

    public List<Province> loadProvince() {
        List<Province> provinceList = new ArrayList<Province>();
        cursor = sqLiteDatabase.query("Province", null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                Province province = new Province();
                province.setProvinceName(cursor.getString(cursor.getColumnIndex("province_name")));
                province.setProvinceCode(cursor.getString(cursor.getColumnIndex("province_code")));
                provinceList.add(province);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return provinceList;
    }

    public void saveCity(City city) {
        ContentValues values = new ContentValues();
        values.put("city_name", city.getCityName());
        values.put("city_code", city.getCityCode());
        values.put("province_id",city.getProvinceId());
        sqLiteDatabase.insert("City", null, values);
        values.clear();
    }

    public List<City> loadCity(String provinceId) {
        List<City> cityList = new ArrayList<City>();
        cursor = sqLiteDatabase.query("City", null, "province_id = ?", new String[]{provinceId}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                City city = new City();
                city.setCityName(cursor.getString(cursor.getColumnIndex("city_name")));
                city.setCityCode(cursor.getString(cursor.getColumnIndex("city_code")));
                city.setProvinceId(cursor.getString(cursor.getColumnIndex("province_id")));
                cityList.add(city);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return cityList;
    }

    public void saveCounty(County county) {
        ContentValues values = new ContentValues();
        values.put("county_name", county.getCountyName());
        values.put("county_code", county.getCountyCode());
        values.put("city_id",county.getCityId());
        sqLiteDatabase.insert("County", null, values);
        values.clear();
    }

    public List<County> loadCounty(String cityId) {
        List<County> countyList = new ArrayList<County>();
        cursor = sqLiteDatabase.query("County", null, "city_id = ?", new String[]{cityId}, null, null, null);
        if (cursor.moveToFirst()) {
            do {
                County county = new County();
                county.setCountyName(cursor.getString(cursor.getColumnIndex("county_name")));
                county.setCountyCode(cursor.getString(cursor.getColumnIndex("county_code")));
                county.setCityId(cursor.getString(cursor.getColumnIndex("city_id")));
                countyList.add(county);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return countyList;
    }
}
