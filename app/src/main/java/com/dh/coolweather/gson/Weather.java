package com.dh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * 总的天气类.
 */

public class Weather {

    public String status;//请求天气服务器状态

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<ForeCast> foreCastList;
}
