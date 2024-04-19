package com.assignment.weatherdata.weather.ModelMapper;

import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.WeatherData;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WeatherDataMapper {

    @Autowired
    private ModelMapper modelMapper;

    public WeatherData convertToEntity(WeatherDataDTO weatherDataDTO) {
        return modelMapper.map(weatherDataDTO, WeatherData.class);
    }

    public WeatherDataDTO convertToDTO(WeatherData weatherData) {
        return modelMapper.map(weatherData, WeatherDataDTO.class);
    }
}
