package com.wenxi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**未来天气情况
 * Created by Administrator on 2017/2/4 0004.
 */

public class Forecast {
    public String date;

    @SerializedName("tmp")
    public TempeRature tempeRature;
    @SerializedName("cond")
    public More more;

    public class TempeRature{
        public String max;
        public String min;
    }

    public class More{
        @SerializedName("txt_d")
        public String info;
    }
}
