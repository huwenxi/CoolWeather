package com.wenxi.coolweather.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.wenxi.coolweather.gson.Weather;
import com.wenxi.coolweather.util.HttpUtil;
import com.wenxi.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Callback;
import okhttp3.Response;


public class AutoUpdateService extends Service {
    public AutoUpdateService() {
    }
    private SharedPreferences sp;
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather();
        updateBingPic();
        //设置后台服务定时间隔更新时间为8小时
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int anHour = 8 * 60 * 60 * 1000;
        long time = SystemClock.elapsedRealtime() + anHour;
        Intent intent1 = new Intent(this,AutoUpdateService.class);
        PendingIntent service = PendingIntent.getService(this, 0, intent1, 0);
        manager.cancel(service);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,time,service);
        return super.onStartCommand(intent, flags, startId);
    }
    /**更新必应每日一图*/
    private void updateBingPic() {
        String url = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {

            }

            @Override
            public void onResponse(okhttp3.Call call, Response response) throws IOException {
                String bingPic = response.body().string();
                sp = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                SharedPreferences.Editor edit = sp.edit();
                edit.putString("bing_pic",bingPic);
                edit.apply();
            }

        });
    }
    //后台服务更新天气信息
    private void updateWeather() {
        sp = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
        String weatherString = sp.getString("weather", null);
        //有缓存时直接解析天气数据
        if (weatherString != null){
            Weather weather = Utility.handleWeatherResponse(weatherString);
            String weatherId = weather.basic.weatherId;
            String weatherURL = "http://guolin.tech/api/weather?cityid=" + weatherId + "&key=bc0418b57b2d4918819d3974ac1285d9";
            HttpUtil.sendOkHttpRequest(weatherURL, new Callback() {
                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                }

                @Override
                public void onResponse(okhttp3.Call call, Response response) throws IOException {
                    String responseText = response.body().string();
                    Weather weather1 = Utility.handleWeatherResponse(responseText);
                    if (weather1 != null && "ok".equals(weather1.status)){
                        sp = PreferenceManager.getDefaultSharedPreferences(AutoUpdateService.this);
                        SharedPreferences.Editor edit = sp.edit();
                        edit.putString("weather",responseText);
                        edit.apply();
                    }
                }
            });
        }
    }
}
