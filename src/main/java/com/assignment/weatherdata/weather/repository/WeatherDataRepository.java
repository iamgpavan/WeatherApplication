package com.assignment.weatherdata.weather.repository;

import com.assignment.weatherdata.weather.entity.WeatherData;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface WeatherDataRepository extends MongoRepository<WeatherData, String> {
    List<WeatherData> findByCity(String cityName);
    Optional<WeatherData> findByCityAndDate(String city, Date date);
    List<WeatherData> findByCityAndDateBetween(String city, Date startDate, Date endDate);
    void deleteByCity(String cityName);
}
