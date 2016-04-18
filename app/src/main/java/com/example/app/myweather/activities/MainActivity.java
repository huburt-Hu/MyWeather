package com.example.app.myweather.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.myweather.R;
import com.example.app.myweather.service.AutoUpdateService;
import com.example.app.myweather.ui.PullToRefreshView;
import com.example.app.myweather.util.CallBackListener;
import com.example.app.myweather.util.HandleResponse;
import com.example.app.myweather.util.HttpUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

public class MainActivity extends Activity {
    private HandleResponse handleResponse = HandleResponse.getInstance();
    private TextView time;
    private TextView date;
    private TextView city;
    private TextView temp1;
    private TextView temp2;
    private TextView weather;
    private TextView sunrise;
    private TextView sunset;
    private TextView wd;
    private TextView ws;
    private LinearLayout linear;
    private ImageView imageView;
    private String weathercode;
    private PullToRefreshView pullToRefreshView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        RelativeLayout reLt = (RelativeLayout) findViewById(R.id.relt);
//        调用点击抖动的方法
        setOnclickShake(reLt, 5);
        pullToRefreshView = (PullToRefreshView) findViewById(R.id.pullToRefresh);

        linear = (LinearLayout) findViewById(R.id.linear);
        time = (TextView) findViewById(R.id.time);
        setOnclickShake(time, 2);
        date = (TextView) findViewById(R.id.day);
        setOnclickShake(date, 4);
        city = (TextView) findViewById(R.id.city);
        setOnclickShake(city, 1);
        temp1 = (TextView) findViewById(R.id.l_temp);
        temp2 = (TextView) findViewById(R.id.h_temp);
        weather = (TextView) findViewById(R.id.weather);
        setOnclickShake(weather, 3);
        sunrise = (TextView) findViewById(R.id.sunrise);
        sunset = (TextView) findViewById(R.id.sunset);
        wd = (TextView) findViewById(R.id.WD);
        ws = (TextView) findViewById(R.id.WS);
        imageView = (ImageView) findViewById(R.id.weather_pic);
        Button location = (Button) findViewById(R.id.button);
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Location.class);
                startActivity(intent);
                finish();
            }
        });
        linear.setVisibility(View.INVISIBLE);
        String countyCode = getIntent().getStringExtra("countyCode");
        if (countyCode != null) {
            queryweather(countyCode);
        } else {
            showWeather();
        }
        //    实现下拉刷新
        pullToRefreshView.setOnRefreshListener(new PullToRefreshView.PullToRefreshListener() {
            @Override
            public void onRefresh() {
                InputStream inputStream = null;
                BufferedReader bufferedReader = null;
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://apis.baidu.com/apistore/weatherservice/cityid?cityid=" + weathercode);
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
                    handleResponse.handleWeatherResponse(MainActivity.this, response.toString());
                } catch (IOException e) {
                    e.printStackTrace();
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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
//                完成刷新任务调用此方法
                pullToRefreshView.finishRefreshing();
            }
        }, 0);
        Intent i = new Intent(this, AutoUpdateService.class);
        startService(i);
    }

    /*
    * 点击抖动对应的方法
    */
    public void setOnclickShake(final View view, final int type) {
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake));
                switch (type) {
                    case 1:
                        Toast.makeText(MainActivity.this, "讨厌！o(>﹏<)o干嘛点人家~", Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        Toast.makeText(MainActivity.this, "别点我~(╯﹏╰）", Toast.LENGTH_SHORT).show();
                        break;
                    case 3:
                        Toast.makeText(MainActivity.this, "拿开你的手指(╰_╯)#", Toast.LENGTH_SHORT).show();
                        break;
                    case 4:
                        Toast.makeText(MainActivity.this, "不要停！~(≧▽≦)/~", Toast.LENGTH_SHORT).show();
                        break;
                    case 5:
                        Toast.makeText(MainActivity.this, "一库！o(>﹏<)o", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    /*根据天气代码查询天气*/
    public void getWeather(String weatherCode) {
        HttpUtil.sendWeatherRequest(weatherCode, new CallBackListener() {
            @Override
            public void onFinish(String res) {
                handleResponse.handleWeatherResponse(MainActivity.this, res);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linear.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this,
                                "加载失败！请确认网络是否正常，并重试！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*
    * 查询县代码对应的天气代码
    * */
    public void queryweather(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        HttpUtil.sendLocationRequest(address, new CallBackListener() {
            @Override
            public void onFinish(String res) {
                String[] a = res.split("\\|");
                if (a.length == 2) {
                    weathercode = a[1];
                    getWeather(weathercode);
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(MainActivity.this).edit();
                    editor.putString("weather_code", weathercode);
                    editor.apply();
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linear.setVisibility(View.INVISIBLE);
                        Toast.makeText(MainActivity.this,
                                "加载失败！请确认网络是否正常，并重试！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /*在Activity中显示天气的方法*/
    public void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        time.setText(preferences.getString("time", ""));
        date.setText(preferences.getString("date", ""));
        city.setText(preferences.getString("city", ""));
        String w = preferences.getString("weather", "");
        weather.setText(w);
        setImageView(w);
        temp1.setText(preferences.getString("l_tmp", ""));
        temp2.setText(preferences.getString("h_tmp", ""));
        wd.setText(preferences.getString("WD", ""));
        ws.setText(preferences.getString("WS", ""));
        sunrise.setText(preferences.getString("sunrise", ""));
        sunset.setText(preferences.getString("sunset", ""));
        linear.setVisibility(View.VISIBLE);
    }

    /*根据天气情况显示对应图片的方法*/
    public void setImageView(String w) {
        switch (w) {
            case "晴":
                ContentResolver cv = getBaseContext().getContentResolver();
                String strTimeFormat =
                        android.provider.Settings.System.getString(cv, android.provider.Settings.System.TIME_12_24);
                if (strTimeFormat != null && strTimeFormat.equals("24")) {
                    int t = Integer.parseInt(strTimeFormat);
                    if (t > 6 && t < 18) {
                        imageView.setImageResource(R.drawable.sun);
                    } else {
                        imageView.setImageResource(R.drawable.night);
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    int t1 = c.get(Calendar.HOUR_OF_DAY);
                    if (c.get(Calendar.AM_PM) == Calendar.AM) {
                        if (t1 > 5) {
                            imageView.setImageResource(R.drawable.sun);
                        } else {
                            imageView.setImageResource(R.drawable.night);
                        }
                    } else {
                        if (t1 < 6) {
                            imageView.setImageResource(R.drawable.sun);
                        } else {
                            imageView.setImageResource(R.drawable.night);
                        }
                    }
                }
                break;
            case "多云":
                ContentResolver cv2 = getBaseContext().getContentResolver();
                String strTimeFormat2 =
                        android.provider.Settings.System.getString(cv2, android.provider.Settings.System.TIME_12_24);
                if (strTimeFormat2 != null && strTimeFormat2.equals("24")) {
                    int t = Integer.parseInt(strTimeFormat2);
                    if (t > 6 && t < 18) {
                        imageView.setImageResource(R.drawable.cloudy);
                    } else {
                        imageView.setImageResource(R.drawable.cloudy_night);
                    }
                } else {
                    Calendar c = Calendar.getInstance();
                    int t1 = c.get(Calendar.HOUR_OF_DAY);
                    if (c.get(Calendar.AM_PM) == Calendar.AM) {
                        if (t1 > 5) {
                            imageView.setImageResource(R.drawable.cloudy);
                        } else {
                            imageView.setImageResource(R.drawable.cloudy_night);
                        }
                    } else {
                        if (t1 < 6) {
                            imageView.setImageResource(R.drawable.cloudy);
                        } else {
                            imageView.setImageResource(R.drawable.cloudy_night);
                        }
                    }
                }
                break;
            case "阴天":
                imageView.setImageResource(R.drawable.cloudy_2);
                break;
            case "小雨":
                imageView.setImageResource(R.drawable.rain);
                break;
            case "中雨":
                imageView.setImageResource(R.drawable.rain);
                break;
            case "大雨":
                imageView.setImageResource(R.drawable.rain_2);
                break;
            case "爆雨":
                imageView.setImageResource(R.drawable.rain_2);
                break;
            case "雷阵雨":
                imageView.setImageResource(R.drawable.strom_rain);
                break;
            case "雨夹雪":
                imageView.setImageResource(R.drawable.snow_rain);
                break;
            case "小雪":
                imageView.setImageResource(R.drawable.snow);
                break;
            case "中雪":
                imageView.setImageResource(R.drawable.snow);
                break;
            case "大雪":
                imageView.setImageResource(R.drawable.snow);
                break;
            case "爆雪":
                imageView.setImageResource(R.drawable.snow);
                break;
            case "大风":
                imageView.setImageResource(R.drawable.windy);
                break;
            default:
                imageView.setVisibility(View.INVISIBLE);
        }
    }
}
