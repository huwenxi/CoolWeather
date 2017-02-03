package com.wenxi.coolweather.db;

import org.litepal.crud.DataSupport;

/** 全国各县数据
 * Created by Administrator on 2017/2/3 0003.
 */

public class County extends DataSupport {
    private int id;
    private String countyName;
    private String weatherId;
    int cityId;
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountyName() {
        return countyName;
    }

    public void setCountyName(String countyName) {
        this.countyName = countyName;
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
