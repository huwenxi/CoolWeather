package com.wenxi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/** 城市名称，天气预报更新时间
 * Created by Administrator on 2017/2/4 0004.
 */

public class Basic {
    @SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }

}
