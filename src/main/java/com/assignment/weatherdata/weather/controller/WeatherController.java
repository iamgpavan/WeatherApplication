package com.assignment.weatherdata.weather.controller;

import com.assignment.weatherdata.weather.entity.Weather;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.PathVariableMissingException;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.service.WeatherService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    private final WeatherService weatherService;
    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    @GetMapping("/")
    public ResponseEntity<WeatherData> getWeatherByCity(){
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }
    @PostMapping("/")
    public ResponseEntity<WeatherData> createWeatherData(){
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }
    @PutMapping("/")
    public ResponseEntity<WeatherData> updateWeatherData(){
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }
    @DeleteMapping("/")
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity(){
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }
    @GetMapping("/all")
    public ResponseEntity<HashMap<String, List<String>>> getAll(){
        return weatherService.getAll();
    }
    @GetMapping("/{city}")
    public ResponseEntity<WeatherData> getWeatherByCity(@PathVariable String city){
        return weatherService.getWeatherByCity(city);
    }
    @PostMapping("/{city}")
    public ResponseEntity<WeatherData> createWeatherData(@PathVariable String city, @RequestBody WeatherData weatherData) {
        return weatherService.createWeatherData(city, weatherData);
    }
    @PutMapping("/{city}")
    public ResponseEntity<WeatherData> updateWeatherData(@PathVariable String city, @RequestBody WeatherData updatedWeatherData) {
        return weatherService.updateWeatherData(city, updatedWeatherData);
    }
    @DeleteMapping("/{city}")
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity(@PathVariable String city) {
        return weatherService.deleteWeatherByCity(city);
    }
}
