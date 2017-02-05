package com.wenxi.coolweather;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenxi.coolweather.gson.Forecast;
import com.wenxi.coolweather.gson.Weather;
import com.wenxi.coolweather.util.HttpUtil;
import com.wenxi.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        initView();
        //从数据库中获取缓存数据直接解析天气数据
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        String weatherString = sp.getString("weather", null);
//        if (weatherString != null) {
//            Weather weather = Utility.handleWeatherResponse(weatherString);
//            showWeatherInfo(weather);
//        } else {
            //没有缓存数据时去服务器查询天气
            String weatherId = getIntent().getStringExtra("weather_id");
            weather_layout.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
//    }

    /**
     * 根据天气id 请求城市天气信息
     */
    private void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
        HttpUtil.sendOkHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                        String weatherString = sp.getString("weather", null);
                        if (weatherString != null) {
                            Weather weather = Utility.handleWeatherResponse(weatherString);
                            showWeatherInfo(weather);
                        }
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
                            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this);
                            SharedPreferences.Editor edit = sp.edit();
                            edit.putString("weather", responseText);
                            edit.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
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
        if (weather.aqi != null){
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
