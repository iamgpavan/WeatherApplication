package com.assignment.weatherdata.weather.service;
import com.assignment.weatherdata.weather.entity.Weather;
import com.assignment.weatherdata.weather.entity.WeatherData;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

public interface WeatherService {
    ResponseEntity<HashMap<String, List<String>>> getAll();
    ResponseEntity<WeatherData> getWeatherByCity(String cityName);
    ResponseEntity<WeatherData> createWeatherData(String cityName, WeatherData weatherData);
    ResponseEntity<WeatherData> updateWeatherData(String cityName,  WeatherData weatherData);
    ResponseEntity<HashMap<String, String>> deleteWeatherByCity(String cityName);
}
