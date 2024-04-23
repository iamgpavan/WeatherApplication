package com.assignment.weatherdata.weather.utils;

import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.exception.ValidationException;
import java.util.Date;

public class ValidationUtils {
    public static void validateCityName(String cityName) {
        if (cityName == null || cityName.isEmpty() || "null".equalsIgnoreCase(cityName)) {
            throw new ValidationException("City name must not be null or empty");
        }
    }

    public static void validateWeatherDTO(WeatherDataDTO weatherDataDTO){
        if (weatherDataDTO == null) {
            throw new ValidationException("Weather data must not be null");
        }
        validateCityName(weatherDataDTO.getCity());
        validateTemperatureRange(weatherDataDTO.getTemperature());
//        validateDate(weatherDataDTO.getDate());
    }

    public static void validateTemperatureRange(double temperature) {
        if (temperature < -100 || temperature > 100) {
            throw new ValidationException("Temperature must be between -100 and 100");
        }
    }

    public static void validateDateRange(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            throw new ValidationException("Start date and end date must not be null");
        }

        if (endDate.before(startDate)) {
            throw new ValidationException("End date must be greater than or equal to start date");
        }
    }
}