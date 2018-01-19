package com.dh.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * 市区、区、县、县级市,对应天气数据
 */

public class Country extends DataSupport {
    private int id;//主键

    private String countryName;//市区、区、县、县级市名

    private String weatherId;//天气数据

    private int cityId;//所属市

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getWeatherId() {
        return weatherId;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }
}
