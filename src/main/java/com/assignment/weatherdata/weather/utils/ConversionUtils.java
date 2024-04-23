package com.assignment.weatherdata.weather.utils;

import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.ForecastDay;
import com.assignment.weatherdata.weather.entity.WeatherApiResponse;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ConversionUtils {

    public static WeatherDataDTO mapToWeatherDataDTO(WeatherApiResponse response, String city) {
        ForecastDay forecastDay = response.getForecast().getForecastday().get(0);
        WeatherDataDTO dto = new WeatherDataDTO();
        dto.setCity(response.getLocation().getName());
        dto.setDate(java.sql.Date.valueOf(forecastDay.getDate()));
        dto.setDescription("Average temperature forecast");
        dto.setTemperature(forecastDay.getDay().getAvgtemp_c());
        return dto;
    }
    public static WeatherDataDTO createMockWeatherData(String city) {
        WeatherDataDTO dto = new WeatherDataDTO();
        dto.setCity(city);
        dto.setDate(new Date());
        dto.setDescription("Mock average temperature");
        dto.setTemperature(25.0);
        return dto;
    }

    // Used for testGetWeatherDataByCityAndDateRange_Success() to create date
    public static Date convertToDate(String dateString) {
        LocalDate localDate = LocalDate.parse(dateString, DateTimeFormatter.ISO_DATE);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
