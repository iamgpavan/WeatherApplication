package com.assignment.weatherdata.weather.service.impl;

import com.assignment.weatherdata.weather.ModelMapper.WeatherDataMapper;
import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.exception.WeatherDataNotFoundException;
import com.assignment.weatherdata.weather.repository.WeatherDataRepository;
import com.assignment.weatherdata.weather.service.WeatherService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherServiceImpl implements WeatherService {
//    private final Weather weather  = new Weather();

    @Autowired
    private WeatherDataRepository weatherDataRepository;

    @Autowired
    private WeatherDataMapper weatherDataMapper;
    @Override
    public ResponseEntity<HashMap<String, List<String>>> getAll() {
//        List<String> cities = new ArrayList<>();
//        for (Map.Entry<String, WeatherData> entry : weather.getWeatherMap().entrySet()) {
//            cities.add(entry.getKey());
//        }
        List<WeatherData> weatherDataList = weatherDataRepository.findAll();

        List<String> cities = weatherDataList.stream()
                .map(WeatherData::getCity) // Extract city name from each WeatherData object
                .collect(Collectors.toList());
        HashMap<String, List<String>> response = new HashMap<>();
        response.put("cities", cities);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCity(String cityName) {
        // Retrieve WeatherData entities from the repository
        List<WeatherData> cityWeatherData = weatherDataRepository.findByCity(cityName);

        // Convert the list of WeatherData entities to a list of WeatherDataDTOs
        List<WeatherDataDTO> cityWeatherDataDTO = cityWeatherData.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());

        // Return the list of WeatherDataDTOs in the ResponseEntity
        return new ResponseEntity<>(cityWeatherDataDTO, HttpStatus.OK);
    }
    
    @Override
    public ResponseEntity<WeatherDataDTO> createWeatherData(String cityName, WeatherDataDTO weatherDataDTO) {
        // Validate input
        if (cityName == null || cityName.isEmpty()) {
            throw new ValidationException("City name must not be null or empty");
        }

        if (weatherDataDTO == null) {
            throw new ValidationException("Weather data must not be null");
        }

        if (weatherDataDTO.getTemperature() < -100 || weatherDataDTO.getTemperature() > 100) {
            throw new ValidationException("Temperature must be between -100 and 100");
        }

        weatherDataDTO.setCity(cityName);
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
        // Validate input
        if (cityName == null || cityName.isEmpty() || weatherDataDTO == null) {
            throw new ValidationException("City name and weather data must not be null or empty");
        }

        if (weatherDataDTO.getTemperature() < -100 || weatherDataDTO.getTemperature() > 100) {
            throw new ValidationException("Temperature must be between -100 and 100");
        }

        weatherDataDTO.setCity(cityName);
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
        if (cityName == null || cityName.isEmpty()) {
            throw new ValidationException("City name must not be null or empty");
        }
        try {
            weatherDataRepository.deleteByCity(cityName);

            HashMap<String, String> response = new HashMap<>();
            String responseMessage = "Weather data for " + cityName + " has been deleted.";
            response.put("message", responseMessage);
            return ResponseEntity.ok(response);
        }catch(Exception e){
            throw new WeatherDataNotFoundException("Weather data for city '" + cityName + "' not found");
        }
    }

    @Override
    public List<WeatherDataDTO> getWeatherDataByCityAndDateRange(String city, Date startDate, Date endDate) {
        // Adjust end date to include data up to the end of the day
        LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);
        Date adjustedEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());
        // Retrieve weather data from repository
        List<WeatherData> weatherDataList = weatherDataRepository.findByCityAndDateBetween(city, startDate, adjustedEndDate);

        // Convert WeatherData entities to WeatherDataDTOs
        return weatherDataList.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ResponseEntity<List<WeatherDataDTO>> getWeatherByCityAndSort(String city, String sortBy, String order) {
        List<WeatherData> cityWeatherData = weatherDataRepository.findByCity(city);

        // Convert WeatherData entities to WeatherDataDTOs
        List<WeatherDataDTO> weatherDataDTOs = cityWeatherData.stream()
                .map(weatherDataMapper::convertToDTO)
                .collect(Collectors.toList());

        // Sort the weather data based on the specified parameter and order
        if (sortBy != null) {
            if ("temperature".equals(sortBy)) {
                weatherDataDTOs.sort(Comparator.comparingDouble(WeatherDataDTO::getTemperature));
            } else {
                throw new ValidationException("Invalid sort_by parameter");
            }

            // If order is descending, reverse the sorted list
            if ("desc".equalsIgnoreCase(order)) {
                Collections.reverse(weatherDataDTOs);
            }
        }

        // Return the sorted weather data
        return new ResponseEntity<>(weatherDataDTOs, HttpStatus.OK);
    }

}
