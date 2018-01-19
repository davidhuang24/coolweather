package com.dh.coolweather.db;

import org.litepal.crud.DataSupport;

/**
 * Created by root on 18-1-19.
 */

public class Province extends DataSupport{

    private int id;//主键

    private String provinceName;

    private int provinceCode;//省代号

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinceName() {
        return provinceName;
    }

    public void setProvinceName(String provinceName) {
        this.provinceName = provinceName;
    }

    public int getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(int provinceCode) {
        this.provinceCode = provinceCode;
    }
}
