package com.example.app.myweather.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.app.myweather.R;
import com.example.app.myweather.db.City;
import com.example.app.myweather.db.County;
import com.example.app.myweather.db.MyWeatherDB;
import com.example.app.myweather.db.Province;
import com.example.app.myweather.util.CallBackListener;
import com.example.app.myweather.util.HandleResponse;
import com.example.app.myweather.util.HttpUtil;

import java.util.ArrayList;
import java.util.List;

public class Location extends AppCompatActivity {
    HandleResponse handleResponse = HandleResponse.getInstance();
    MyWeatherDB myWeatherDB;
    private TextView tv;
    private ListView listView;
    private List<Province> provinceList;
    private List<City> cityList;
    private List<County> countyList;
    private List<String> list = new ArrayList<>();
    private ArrayAdapter<String> adapter;
    private ProgressDialog progressDialog;
    private Province selectProvince;
    private City selectCity;
    private County selectCounty;
    private int currentLevel;
    private static final int LEVEL_PROVINCE = 1;
    private static final int LEVEL_CITY = 2;
    private static final int LEVEL_COUNTY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);
        tv=(TextView)findViewById(R.id.tv11);
        tv.setText("请选择所在省");
        listView = (ListView) findViewById(R.id.listView);
        myWeatherDB = MyWeatherDB.getInstance(this);
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (currentLevel == LEVEL_PROVINCE) {
                    tv.setText("请选择所在市");
                    selectProvince = provinceList.get(position);
                    queryCity();
                } else if (currentLevel == LEVEL_CITY) {
                    tv.setText("请选择所在县");
                    selectCity = cityList.get(position);
                    queryCounty();
                }else if (currentLevel ==LEVEL_COUNTY){
                    selectCounty = countyList.get(position);
                    Intent intent = new Intent(Location.this,MainActivity.class);
                    intent.putExtra("countyCode",selectCounty.getCountyCode());
                    startActivity(intent);
                    finish();
                }
            }
        });
        queryProvince();
    }

    public void queryProvince() {
        provinceList = myWeatherDB.loadProvince();
        if (provinceList.size() > 0) {
            list.clear();
            for (Province p : provinceList) {
                list.add(p.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromInternet(null, "province");
        }
    }

    public void queryCity() {
        cityList = myWeatherDB.loadCity(selectProvince.getProvinceCode());
        if (cityList.size() > 0) {
            list.clear();
            for (City c : cityList) {
                list.add(c.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryFromInternet(selectProvince.getProvinceCode(), "city");
        }
    }

    public void queryCounty() {
        countyList = myWeatherDB.loadCounty(selectCity.getCityCode());
        if (countyList.size() > 0) {
            list.clear();
            for (County c : countyList) {
                list.add(c.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromInternet(selectCity.getCityCode(), "county");
        }
    }

    public void queryFromInternet(final String code, final String type) {
        String address;
        if (!TextUtils.isEmpty(code)) {
            address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
        } else {
            address = "http://www.weather.com.cn/data/list3/city.xml";
        }
        showDialog();
        HttpUtil.sendLocationRequest(address, new CallBackListener() {
            @Override
            public void onFinish(String res) {
                boolean result = false;
                if ("province".equals(type)) {
                    result = handleResponse.handleProvinceResponse(myWeatherDB, res);
                } else if ("city".equals(type)) {
                    result = handleResponse.handleCityResponse(myWeatherDB, res,code);
                } else if ("county".equals(type)) {
                    result = handleResponse.handleCountyResponse(myWeatherDB, res,code);
                }
                if (result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            closeDialog();
                            if ("province".equals(type)) {
                                queryProvince();
                            } else if ("city".equals(type)) {
                                queryCity();
                            } else if ("county".equals(type)) {
                                queryCounty();
                            }
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        closeDialog();
                        Toast.makeText(Location.this,
                                "加载失败！请确认网络是否正常，并重试！", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    public void showDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(Location.this);
            progressDialog.setTitle("请稍候！");
            progressDialog.setMessage("正在初始化位置信息...");
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();
        }
    }

    public void closeDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        if(currentLevel ==LEVEL_PROVINCE){
            Intent intent = new Intent(Location.this,MainActivity.class);
            startActivity(intent);
            finish();
        }else if(currentLevel ==LEVEL_CITY){
            queryProvince();
        }else if(currentLevel ==LEVEL_COUNTY){
            queryCity();
        }
    }
}
