package com.wenxi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 天气总数据类
 * Created by Administrator on 2017/2/4 0004.
 */

public class Weather {
    public String status;

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
