package com.assignment.weatherdata.weather.controller;

import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.PathVariableMissingException;
import com.assignment.weatherdata.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/weather")
public class WeatherController {
    //    private final WeatherService weatherService;
//    public WeatherController(WeatherService weatherService) {
//        this.weatherService = weatherService;
//    }
    @Autowired
    private WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/")
    public ResponseEntity<WeatherData> getWeatherByCity() {
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }

    @PostMapping("/")
    public ResponseEntity<WeatherData> createWeatherData() {
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }

    @PutMapping("/")
    public ResponseEntity<WeatherData> updateWeatherData() {
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }

    @DeleteMapping("/")
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity() {
        throw new PathVariableMissingException("City Name is missing in requested URL");
    }

    @GetMapping("/all")
    public ResponseEntity<HashMap<String, List<String>>> getAll() {
        return weatherService.getAll();
    }

    @GetMapping("/{city}")
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCity(@PathVariable String city) {
        return weatherService.getWeatherByCity(city);
    }

    @PostMapping("/{city}")
    public ResponseEntity<WeatherDataDTO> createWeatherData(@PathVariable String city, @RequestBody WeatherDataDTO weatherDataDTO) {
        return weatherService.createWeatherData(city, weatherDataDTO);
    }

    @PutMapping("/{city}")
    public ResponseEntity<WeatherDataDTO> updateWeatherData(@PathVariable String city, @RequestBody WeatherDataDTO updatedWeatherDataDto) {
        return weatherService.updateWeatherData(city, updatedWeatherDataDto);
    }

    @DeleteMapping("/{city}")
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity(@PathVariable String city) {
        return weatherService.deleteWeatherByCity(city);
    }

    @GetMapping("/{city}/history")
    public List<WeatherDataDTO> getWeatherHistory(
            @PathVariable String city,
            @RequestParam("start_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam("end_date") @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {

        // Call service method to retrieve weather data between start and end dates
        return weatherService.getWeatherDataByCityAndDateRange(city, startDate, endDate);
    }

    @GetMapping("/{city}/")
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCityAndSort(
            @PathVariable String city,
            @RequestParam(value = "sort_by", required = false) String sortBy,
            @RequestParam(value = "order", required = false, defaultValue = "asc") String order) {
        return weatherService.getWeatherByCityAndSort(city, sortBy, order);
    }
}
