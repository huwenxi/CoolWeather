package com.wenxi.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**网络请求工具类
 * 对OKhttp进行封装
 * Created by Administrator on 2017/2/4 0004.
 */

public class HttpUtil {
    /**发送请求*/
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(callback);
    }
}
