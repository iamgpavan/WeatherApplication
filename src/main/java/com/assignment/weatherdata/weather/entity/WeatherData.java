package com.assignment.weatherdata.weather.entity;

import com.assignment.weatherdata.weather.exception.ValidationException;
import lombok.Getter;
import lombok.Setter;

import javax.sql.rowset.spi.SyncResolver;

@Setter
@Getter
public class WeatherData {
    private String city;
    private double temparature;
    private String description;
    public WeatherData() {

    }
    public WeatherData(double temparature, String description){
        this.temparature = temparature;
        this.description = description;
    }
}
