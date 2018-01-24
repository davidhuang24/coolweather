package com.dh.coolweather.gson;

import java.security.PublicKey;

/**
 * 空气质量信息
 */

public class AQI {

    public AQICity city;

    public class AQICity{

        public String aqi;//空气污染指数

        public String pm25;
    }

}
