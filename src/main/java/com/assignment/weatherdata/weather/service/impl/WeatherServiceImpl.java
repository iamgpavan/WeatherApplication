package com.assignment.weatherdata.weather.service.impl;

import com.assignment.weatherdata.weather.entity.Weather;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.exception.WeatherDataNotFoundException;
import com.assignment.weatherdata.weather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WeatherServiceImpl implements WeatherService {
    private final Weather weather  = new Weather();

    public WeatherServiceImpl(){

    }
    @Override
    public ResponseEntity<HashMap<String, List<String>>> getAll() {
        List<String> cities = new ArrayList<>();
        for (Map.Entry<String, WeatherData> entry : weather.getWeatherMap().entrySet()) {
            cities.add(entry.getKey());
        }
        HashMap<String, List<String>> response = new HashMap<>();
        response.put("cities", cities);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<WeatherData> getWeatherByCity(String cityName) {
        WeatherData cityWeatherData =  weather.getValue(cityName);
        if (cityWeatherData == null) {
            throw new WeatherDataNotFoundException("Weather data for city '" + cityName + "' not found");
        }
        return new ResponseEntity<>(cityWeatherData, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<WeatherData> createWeatherData(String cityName, WeatherData weatherData) {
        // Validate input
        if (cityName == null || cityName.isEmpty()) {
            throw new ValidationException("City name must not be null or empty");
        }

        if (weatherData == null) {
            throw new ValidationException("Weather data must not be null");
        }

        if (weatherData.getTemparature() < -100 || weatherData.getTemparature() > 100) {
            throw new ValidationException("Temperature must be between -100 and 100");
        }

        WeatherData response = weather.postData(cityName, weatherData);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @Override
    public ResponseEntity<WeatherData> updateWeatherData(String cityName,  WeatherData weatherData) {
        // Validate input
        if (cityName == null || cityName.isEmpty() || weatherData == null) {
            throw new ValidationException("City name and weather data must not be null or empty");
        }

        WeatherData response =  weather.postData(cityName, weatherData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity(String cityName) {
        if (cityName == null || cityName.isEmpty()) {
            throw new ValidationException("City name must not be null or empty");
        }

        boolean isDeleted = weather.deleteById(cityName);
        if (!isDeleted) {
            throw new WeatherDataNotFoundException("Weather data for city '" + cityName + "' not found");
        }

        HashMap<String, String> response = new HashMap<>();
        String responseMessage = "Weather data for "+ cityName + " has been deleted.";
        response.put("message", responseMessage);
        return ResponseEntity.ok(response);
    }
}
