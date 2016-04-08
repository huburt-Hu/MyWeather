package com.example.app.myweather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.myweather.R;
import com.example.app.myweather.util.CallBackListener;
import com.example.app.myweather.util.HandleResponse;
import com.example.app.myweather.util.HttpUtil;

public class MainActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        linear = (LinearLayout)findViewById(R.id.linear);
        time = (TextView) findViewById(R.id.time);
        date = (TextView) findViewById(R.id.day);
        city = (TextView) findViewById(R.id.city);
        temp1 = (TextView) findViewById(R.id.l_temp);
        temp2 = (TextView) findViewById(R.id.h_temp);
        weather = (TextView) findViewById(R.id.weather);
        sunrise = (TextView) findViewById(R.id.sunrise);
        sunset = (TextView) findViewById(R.id.sunset);
        wd = (TextView) findViewById(R.id.WD);
        ws = (TextView) findViewById(R.id.WS);
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
        super.onStart();
        String countyCode = getIntent().getStringExtra("countyCode");
        if (countyCode != null) {
            queryweather(countyCode);
        } else {
            showWeather();
        }
    }

    public  void getWeather(String weatherCode){
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

    public void queryweather(String countyCode) {
        String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        HttpUtil.sendLocationRequest(address, new CallBackListener() {
            @Override
            public void onFinish(String res) {
                String[] a = res.split("\\|");
                String weathercode = null;
                if (a.length == 2) {
                    weathercode = a[1];
                    getWeather(weathercode);
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

    public void showWeather() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        time.setText(preferences.getString("time", ""));
        date.setText(preferences.getString("date", ""));
        city.setText(preferences.getString("city", ""));
        weather.setText(preferences.getString("weather", ""));
        temp1.setText(preferences.getString("l_tmp", ""));
        temp2.setText(preferences.getString("h_tmp", ""));
        wd.setText(preferences.getString("WD", ""));
        ws.setText(preferences.getString("WS", ""));
        sunrise.setText(preferences.getString("sunrise", ""));
        sunset.setText(preferences.getString("sunset", ""));
        linear.setVisibility(View.VISIBLE);
    }
}
