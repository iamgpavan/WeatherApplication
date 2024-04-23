package com.assignment.weatherdata.weather.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class Forecast {
    private List<ForecastDay> forecastday;

}
