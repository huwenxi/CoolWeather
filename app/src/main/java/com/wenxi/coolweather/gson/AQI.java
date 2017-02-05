package com.wenxi.coolweather.gson;

/** 城市空气质量指数
 * Created by Administrator on 2017/2/4 0004.
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
