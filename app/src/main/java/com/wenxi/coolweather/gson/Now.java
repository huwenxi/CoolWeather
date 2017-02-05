package com.wenxi.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**天气信息
 * Created by Administrator on 2017/2/4 0004.
 */

public class Now {
    @SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }
}
