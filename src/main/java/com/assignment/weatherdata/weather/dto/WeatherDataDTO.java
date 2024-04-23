package com.assignment.weatherdata.weather.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import java.util.Date;

@Getter
@Setter
public class WeatherDataDTO {
    private String city;
    private double temperature;
    private String description;
    private Date date;
    public WeatherDataDTO(){

    }
}

