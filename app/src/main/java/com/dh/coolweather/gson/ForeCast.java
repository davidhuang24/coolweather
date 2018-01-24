package com.dh.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * 未来几天天气信息
 */

public class ForeCast {

    public String date;

    @SerializedName("tmp")
    public Temperature temperature;

    @SerializedName("cond")
    public More more;//更多天气信息

    public class Temperature{

        public String max;

        public String min;
    }

    public class More{

        @SerializedName("txt_d")
        public String info;
    }
}
