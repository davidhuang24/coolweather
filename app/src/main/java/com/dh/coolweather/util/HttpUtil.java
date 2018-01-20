package com.dh.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * 用OkHttp方式发送http请求,从服务器获取数据
 */

public class HttpUtil {
    public static void sendRequestWithOkHttp(String address,okhttp3.Callback callback){
        //用OkHttp方式发送http请求,自带回调接口,回调方法在子线程中执行;自动开启子线程
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder()
                .url(address)
                .build();//默认是GET方式请求
        client.newCall(request).enqueue(callback);//发送http请求,获取服务器返回数据,执行回调接口
    }
}
