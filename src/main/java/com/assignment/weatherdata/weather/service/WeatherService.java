package com.assignment.weatherdata.weather.service;
import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.WeatherData;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public interface WeatherService {
    ResponseEntity<HashMap<String, List<String>>> getAll();
    ResponseEntity<List<WeatherDataDTO>> getWeatherByCity(String cityName);
    ResponseEntity<WeatherDataDTO> createWeatherData(String cityName, WeatherDataDTO weatherDataDTO);
    ResponseEntity<WeatherDataDTO> updateWeatherData(String cityName,  WeatherDataDTO weatherDataDTO);
    ResponseEntity<HashMap<String, String>> deleteWeatherByCity(String cityName);
    List<WeatherDataDTO> getWeatherDataByCityAndDateRange(String city, Date startDate, Date endDate);
    ResponseEntity<List<WeatherDataDTO>>  getWeatherByCityAndSort(String city, String sortBy, String order);
    WeatherDataDTO getWeatherForecast(String city, String apiUrl, String apiKey);
}
