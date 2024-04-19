package com.assignment.weatherdata.weather.entity;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Document(collection = "WeatherData")
@CompoundIndex(name = "city_date_index", def = "{'city': 1, 'date': 1}", unique = true)
public class WeatherData {
    @Id
    private String id;
    private String city;
    private double temperature;
    private String description;
    private Date date;
    public WeatherData() {
    }

//    // Add a custom constructor to generate compound key
//    public WeatherData(String city, Date date) {
//        this.city = city;
//        this.date = date;
//        this.id = generateId(city, date);
//    }
//
//    // Generate compound key
//    private String generateId(String city, Date date) {
//        return city + "_" + date;
//    }
}
