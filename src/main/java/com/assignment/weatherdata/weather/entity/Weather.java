package com.assignment.weatherdata.weather.entity;

import lombok.Getter;

import java.util.HashMap;

@Getter
public class Weather{
    private final HashMap<String, WeatherData> weatherMap = new HashMap<>();

    public Weather(){

    }
    public WeatherData postData(String city_name, WeatherData weatherData){
        weatherData.setCity(city_name);
        weatherMap.put(city_name, weatherData);
        return weatherData;
    }

    public WeatherData getValue(String key){
        return weatherMap.get(key);
    }

    public boolean deleteById(String city_name) {
        WeatherData removedWeatherData =  weatherMap.remove(city_name);
        return removedWeatherData != null;
    }
}
