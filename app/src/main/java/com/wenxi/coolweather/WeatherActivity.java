package com.wenxi.coolweather;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.wenxi.coolweather.gson.Forecast;
import com.wenxi.coolweather.gson.Weather;
import com.wenxi.coolweather.util.HttpUtil;
import com.wenxi.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView title_city;
    private TextView title_update_time;
    private TextView degree_tv;
    private TextView weather_info_tv;
    private LinearLayout forecast_layout;
    private TextView aqi_tv;
    private TextView pm25_tv;
    private TextView comfort_tv;
    private TextView car_wash_tv;
    private TextView sport_tv;
    private ScrollView weather_layout;
    private ImageView bing_pic_img;
    private SharedPreferences sp;
    public SwipeRefreshLayout swipe_refresh;
    private String mWeatherId;
    public DrawerLayout drawerLayout;
    private Button navBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }
        setContentView(R.layout.activity_weather);
        initView();
        showWeather();
        //获取缓存图片，没缓存就从网络加载图片
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = sp.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(bingPic).into(bing_pic_img);
        } else {
            loodBingPic();
        }
    }

    /**
     * 如果有缓存展示缓存数据，没缓存就从服务器获取数据
     */
    private void showWeather() {
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = sp.getString("weather", null);
        swipe_refresh.setColorSchemeResources(R.color.colorPrimary);
        if (weatherString != null) {
            Weather weather = Utility.handleWeatherResponse(weatherString);
            mWeatherId = weather.basic.weatherId;
            showWeatherInfo(weather);
        } else {
            //根据传过来的天气id查询对应的天气信息展示给用户
            mWeatherId = getIntent().getStringExtra("weather_id");
            weather_layout.setVisibility(View.INVISIBLE);
            requestWeather(mWeatherId);
        }
        swipe_refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(mWeatherId);
            }
        });
    }

    /**
     * 根据天气id 请求城市天气信息
     */
    public void requestWeather(final String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("weather", responseText);
                            edit.apply();
                            mWeatherId = weather.basic.weatherId;
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        swipe_refresh.setRefreshing(false);
                    }
                });
            }
        });
        loodBingPic();
    }

    /**
     * 加载必应每日一图
     */
    private void loodBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("bing_pic", bingPic);
                edit.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(bing_pic_img);
                    }
                });
            }
        });
    }

    /**
     * 处理并展示weather实体类的数据
     */
    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String degree = weather.now.temperature + "℃";
        String weatherInfo = weather.now.more.info;
        title_city.setText(cityName);
        title_update_time.setText(updateTime);
        degree_tv.setText(degree);
        weather_info_tv.setText(weatherInfo);
        forecast_layout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecast_layout, false);
            TextView dateTv = (TextView) view.findViewById(R.id.date_tv);
            TextView infoTv = (TextView) view.findViewById(R.id.info_tv);
            TextView maxTv = (TextView) view.findViewById(R.id.max_tv);
            TextView minTv = (TextView) view.findViewById(R.id.min_tv);
            dateTv.setText(forecast.date);
            infoTv.setText(forecast.more.info);
            maxTv.setText(forecast.tempeRature.max);
            minTv.setText(forecast.tempeRature.min);
            forecast_layout.addView(view);
        }
        if (weather.aqi != null) {
            aqi_tv.setText(weather.aqi.city.aqi);
            pm25_tv.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度：" + weather.suggestion.comfort.info;
        String carWash = "洗车指数：" + weather.suggestion.carWash.info;
        String sport = "运动建议：" + weather.suggestion.sport.info;
        comfort_tv.setText(comfort);
        car_wash_tv.setText(carWash);
        sport_tv.setText(sport);
        weather_layout.setVisibility(View.VISIBLE);

    }

    /**
     * 初始化控件
     */
    private void initView() {
        title_city = (TextView) findViewById(R.id.title_city);
        title_update_time = (TextView) findViewById(R.id.title_update_time);
        degree_tv = (TextView) findViewById(R.id.degree_tv);
        weather_info_tv = (TextView) findViewById(R.id.weather_info_tv);
        forecast_layout = (LinearLayout) findViewById(R.id.forecast_layout);
        aqi_tv = (TextView) findViewById(R.id.aqi_tv);
        pm25_tv = (TextView) findViewById(R.id.pm25_tv);
        comfort_tv = (TextView) findViewById(R.id.comfort_tv);
        car_wash_tv = (TextView) findViewById(R.id.car_wash_tv);
        sport_tv = (TextView) findViewById(R.id.sport_tv);
        weather_layout = (ScrollView) findViewById(R.id.weather_layout);
        bing_pic_img = (ImageView) findViewById(R.id.bing_pic_img);
        swipe_refresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        navBtn = (Button) findViewById(R.id.nav_btn);
        navBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_btn:
                //开启侧滑菜单
                drawerLayout.openDrawer(GravityCompat.START);
                break;
        }
    }
}
