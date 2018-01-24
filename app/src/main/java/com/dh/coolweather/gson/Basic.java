package com.dh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 城市(县级别)基本信息
 */

public class Basic {

    @SerializedName("city")
    public String cityName;

    @SerializedName("id")//注解
    public String weatherId;

    public Update update;

    public class Update{

        @SerializedName("loc")
        public String updateTime;
    }
}
