package com.assignment.weatherdata.weather.service.impl;

import com.assignment.weatherdata.weather.ModelMapper.WeatherDataMapper;
import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.WeatherApiResponse;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.CityNotFoundException;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.exception.WeatherDataNotFoundException;
import com.assignment.weatherdata.weather.repository.WeatherDataRepository;
import com.assignment.weatherdata.weather.service.WeatherService;
import com.assignment.weatherdata.weather.utils.ValidationUtils;
import com.assignment.weatherdata.weather.utils.ConversionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherServiceImpl implements WeatherService {
    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private WeatherDataMapper weatherDataMapper;

    @Autowired
    private final RestTemplate restTemplate;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    public WeatherServiceImpl(WeatherDataRepository weatherDataRepository, WeatherDataMapper weatherDataMapper, RestTemplate restTemplate) {
        this.weatherDataRepository = weatherDataRepository;
        this.weatherDataMapper = weatherDataMapper;
        this.restTemplate = restTemplate;
    }


    @Override
    public ResponseEntity<HashMap<String, List<String>>> getAll() {
        List<WeatherData> weatherDataList = weatherDataRepository.findAll();

        List<String> cities = weatherDataList.stream()
                .map(WeatherData::getCity)
                .distinct()
                .collect(Collectors.toList());
        HashMap<String, List<String>> response = new HashMap<>();
        response.put("cities", cities);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCity(String cityName) {
        ValidationUtils.validateCityName(cityName);

        // Retrieve WeatherData entities from the repository
        List<WeatherData> cityWeatherData = weatherDataRepository.findByCity(cityName);

        if(cityWeatherData.isEmpty()){
            throw new CityNotFoundException("City " + cityName + " not exists!");
        }

        // Convert the list of WeatherData entities to a list of WeatherDataDTOs
        List<WeatherDataDTO> cityWeatherDataDTO = cityWeatherData.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());

        // Return the list of WeatherDataDTOs in the ResponseEntity
        return new ResponseEntity<>(cityWeatherDataDTO, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<WeatherDataDTO> createWeatherData(String cityName, WeatherDataDTO weatherDataDTO) {

        weatherDataDTO.setCity(cityName);

        // Custom validation
        ValidationUtils.validateWeatherDTO(weatherDataDTO);

        // Convert WeatherDataDTO to WeatherData
        WeatherData weatherData = weatherDataMapper.convertToEntity(weatherDataDTO);

        Optional<WeatherData> existingData = weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate());
        if (existingData.isEmpty()) {
            weatherDataDTO.setCity(cityName);
            WeatherData savedWeatherData = weatherDataRepository.save(weatherData);
            WeatherDataDTO savedWeatherDataDTO = weatherDataMapper.convertToDTO(savedWeatherData);

            return new ResponseEntity<>(savedWeatherDataDTO, HttpStatus.CREATED);
        }
        throw new ValidationException("Weather data already exists you can modify the data by using PUT request");
    }


    @Override
    public ResponseEntity<WeatherDataDTO> updateWeatherData(String cityName, WeatherDataDTO weatherDataDTO) {
        weatherDataDTO.setCity(cityName);

        // Custom validation
        ValidationUtils.validateWeatherDTO(weatherDataDTO);

        // Retrieve the existing weather data by city name
        Optional<WeatherData> optionalWeatherData = weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate());
        if (optionalWeatherData.isPresent()) {
            // Update the existing weather data with the new values
            WeatherData weatherOldData = optionalWeatherData.get();
            weatherOldData.setTemperature(weatherDataDTO.getTemperature());
            weatherOldData.setDescription(weatherDataDTO.getDescription());

            // Save the updated weather data
            weatherDataRepository.save(weatherOldData);

            // Convert the updated weather data back to DTO

            WeatherDataDTO updatedWeatherDataDTO = weatherDataMapper.convertToDTO(weatherOldData);

            // Return the updated weather data DTO
            return new ResponseEntity<>(updatedWeatherDataDTO, HttpStatus.OK);
        } else {
            throw new ValidationException("City Data not found");
        }
    }


    @Override
    public ResponseEntity<HashMap<String, String>> deleteWeatherByCity(String cityName) {
        // Custom validation
        ValidationUtils.validateCityName(cityName);
        long deletedCount = weatherDataRepository.deleteByCity(cityName);

        if (deletedCount == 0) {
            throw new WeatherDataNotFoundException("Weather data for city '" + cityName + "' not found");
        }

        HashMap<String, String> response = new HashMap<>();
        String responseMessage = "Weather data for " + cityName + " has been deleted.";
        response.put("message", responseMessage);
        return ResponseEntity.ok(response);
    }

    @Override
    public List<WeatherDataDTO> getWeatherDataByCityAndDateRange(String cityName, Date startDate, Date endDate) {
        // Adjust end date to include data up to the end of the day
        LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);
        Date adjustedEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // Retrieve weather data from repository

        ValidationUtils.validateDateRange(startDate, endDate);
        List<WeatherData> weatherDataList = weatherDataRepository.findByCityAndDateBetween(cityName, startDate, adjustedEndDate);

        if(weatherDataList.isEmpty()){
            throw new WeatherDataNotFoundException("Weather data for city '" + cityName + "' in the given range not found");
        }
        // Convert WeatherData entities to WeatherDataDTOs
        return weatherDataList.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCityAndSort(String cityName, String sortBy, String order) {
        if (!"temperature".equals(sortBy)) {
            throw new ValidationException("Invalid sort_by parameter");
        }

        List<WeatherData> cityWeatherData = weatherDataRepository.findByCity(cityName);

        if(cityWeatherData.isEmpty()){
            throw new CityNotFoundException("City " + cityName + " not exists!");
        }

        // Convert WeatherData entities to WeatherDataDTOs
        List<WeatherDataDTO> weatherDataDTOs = cityWeatherData.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());

        // Sort the weather data based on the specified parameter and order
        weatherDataDTOs.sort(Comparator.comparingDouble(WeatherDataDTO::getTemperature));
        // If order is descending, reverse the sorted list
        if ("desc".equalsIgnoreCase(order)) {
            Collections.reverse(weatherDataDTOs);
        }

        // Return the sorted weather data
        return new ResponseEntity<>(weatherDataDTOs, HttpStatus.OK);
    }

    @Override
    public WeatherDataDTO getWeatherForecast(String city, String apiUrl, String apiKey) {
        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);

        try {
            WeatherApiResponse response = restTemplate.getForObject(url, WeatherApiResponse.class);
            if (response != null && response.getForecast() != null && !response.getForecast().getForecastday().isEmpty()) {
                return ConversionUtils.mapToWeatherDataDTO(response, city);
            }
            throw new Exception("No forecast data available");
        } catch (Exception e) {
            return ConversionUtils.createMockWeatherData(city);
        }
    }
}
