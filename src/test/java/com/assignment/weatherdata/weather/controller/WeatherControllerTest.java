package com.assignment.weatherdata.weather.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.assignment.weatherdata.weather.controller.WeatherController;
import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.exception.WeatherDataNotFoundException;
import com.assignment.weatherdata.weather.service.WeatherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
public class WeatherControllerTest {

    @Mock
    private WeatherService weatherServiceMock;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @InjectMocks
    private WeatherController weatherController;

    @Test
    void testGetAll() {
        // Arrange
        WeatherService weatherServiceMock = mock(WeatherService.class);
        WeatherController weatherController = new WeatherController(weatherServiceMock);

        HashMap<String, List<String>> expectedData = new HashMap<>();
        expectedData.put("key1", new ArrayList<>());
        expectedData.put("key2", new ArrayList<>());

        when(weatherServiceMock.getAll()).thenReturn(new ResponseEntity<>(expectedData, HttpStatus.OK));

        // Act
        ResponseEntity<HashMap<String, List<String>>> responseEntity = weatherController.getAll();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedData, responseEntity.getBody());
    }
    @Test
    void testGetWeatherByCity() {
        // Arrange
        String city = "London";
        List<WeatherDataDTO> expectedWeatherDataList = new ArrayList<>();
        when(weatherServiceMock.getWeatherByCity(city)).thenReturn(new ResponseEntity<>(expectedWeatherDataList, HttpStatus.OK));

        // Act
        ResponseEntity<List<WeatherDataDTO>> responseEntity = weatherController.getWeatherByCity(city);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedWeatherDataList, responseEntity.getBody());
    }

    @Test
    void testGetWeatherByCity_CityNotFound() {
        // Arrange
        String city = "UnknownCity";
        when(weatherServiceMock.getWeatherByCity(city)).thenReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        // Act
        ResponseEntity<List<WeatherDataDTO>> responseEntity = weatherController.getWeatherByCity(city);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody()); // Ensure body is null for NOT_FOUND
    }

    @Test
    void testCreateWeatherData() {
        // Arrange
        String city = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        when(weatherServiceMock.createWeatherData(eq(city), any())).thenReturn(new ResponseEntity<>(weatherDataDTO, HttpStatus.CREATED));

        // Act
        ResponseEntity<WeatherDataDTO> responseEntity = weatherController.createWeatherData(city, weatherDataDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(weatherDataDTO, responseEntity.getBody());
    }

    @Test
    void testCreateWeatherData_NullCity() {
        // Arrange
        String city = "null";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        when(weatherServiceMock.createWeatherData(eq(city), any())).thenThrow(new ValidationException("City name must not be null or empty"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.createWeatherData(city, weatherDataDTO);
        });
    }

    @Test
    void testCreateWeatherData_TemperatureOutOfRange() {
        // Arrange
        String city = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setTemperature(150);
        when(weatherServiceMock.createWeatherData(eq(city), any())).thenThrow(new ValidationException("Temperature must be between -100 and 100"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.createWeatherData(city, weatherDataDTO);
        });
    }

    @Test
    void testCreateWeatherData_NullWeatherDataDTO() {
        // Arrange
        String city = "New York";
        WeatherDataDTO weatherDataDTO = null;
        when(weatherServiceMock.createWeatherData(eq(city), any())).thenThrow(new ValidationException("Weather data must not be null"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.createWeatherData(city, weatherDataDTO);
        });
    }

    @Test
    void testUpdateWeatherData() {
        // Arrange
        String city = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        when(weatherServiceMock.updateWeatherData(eq(city), any())).thenReturn(new ResponseEntity<>(weatherDataDTO, HttpStatus.CREATED));

        // Act
        ResponseEntity<WeatherDataDTO> responseEntity = weatherController.updateWeatherData(city, weatherDataDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(weatherDataDTO, responseEntity.getBody());
    }

    @Test
    void testUpdateWeatherData_NullCity() {
        // Arrange
        String city = null; // Null city name
        WeatherDataDTO updatedWeatherDataDto = new WeatherDataDTO();
        when(weatherServiceMock.updateWeatherData(eq(city), any())).thenThrow(new ValidationException("City name must not be null or empty"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.updateWeatherData(city, updatedWeatherDataDto);
        });
    }

    @Test
    void testUpdateWeatherData_TemperatureOutOfRange() {
        // Arrange
        String city = "New York";
        WeatherDataDTO updatedWeatherDataDto = new WeatherDataDTO();
        updatedWeatherDataDto.setTemperature(150); // Setting temperature outside the valid range
        when(weatherServiceMock.updateWeatherData(eq(city), any())).thenThrow(new ValidationException("Temperature must be between -100 and 100"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.updateWeatherData(city, updatedWeatherDataDto);
        });
    }

    @Test
    void testUpdateWeatherData_NullWeatherDataDTO() {
        // Arrange
        String city = "New York";
        WeatherDataDTO weatherDataDTO = null;
        when(weatherServiceMock.updateWeatherData(eq(city), any())).thenThrow(new ValidationException("Weather data must not be null"));

        // Act and Assert
        assertThrows(ValidationException.class, () -> {
            weatherController.updateWeatherData(city, weatherDataDTO);
        });
    }

    @Test
    void testDeleteWeatherByCity() {
        // Arrange
        WeatherService weatherServiceMock = mock(WeatherService.class);
        WeatherController weatherController = new WeatherController(weatherServiceMock);

        String city = "New York";
        HashMap<String, String> expectedResponse = new HashMap<>();
        expectedResponse.put("message", "Weather data for city " + city + " has been deleted.");

        when(weatherServiceMock.deleteWeatherByCity(city)).thenReturn(new ResponseEntity<>(expectedResponse, HttpStatus.OK));

        // Act
        ResponseEntity<HashMap<String, String>> responseEntity = weatherController.deleteWeatherByCity(city);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedResponse, responseEntity.getBody());
    }

    @Test
    void testGetWeatherHistory() {
        // Arrange
        WeatherService weatherServiceMock = mock(WeatherService.class);
        WeatherController weatherController = new WeatherController(weatherServiceMock);

        String city = "New York";
        Date startDate = new Date();
        Date endDate = new Date();

        // Mocking WeatherService to return a predefined list of WeatherDataDTO objects
        List<WeatherDataDTO> expectedWeatherDataList = new ArrayList<>();
        when(weatherServiceMock.getWeatherDataByCityAndDateRange(city, startDate, endDate)).thenReturn(expectedWeatherDataList);

        // Act
        List<WeatherDataDTO> result = weatherController.getWeatherHistory(city, startDate, endDate);

        // Assert
        assertEquals(expectedWeatherDataList, result);
    }

    @Test
    void testGetWeatherByCityAndSort() {
        // Arrange
        WeatherService weatherServiceMock = mock(WeatherService.class);
        WeatherController weatherController = new WeatherController(weatherServiceMock);

        String city = "New York";
        String sortBy = "temperature";
        String order = "desc";

        // Mocking WeatherService to return a predefined list of WeatherDataDTO objects
        List<WeatherDataDTO> expectedWeatherDataList = new ArrayList<>();
        when(weatherServiceMock.getWeatherByCityAndSort(city, sortBy, order)).thenReturn(new ResponseEntity<>(expectedWeatherDataList, HttpStatus.OK));

        // Act
        ResponseEntity<List<WeatherDataDTO>> responseEntity = weatherController.getWeatherByCityAndSort(city, sortBy, order);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(expectedWeatherDataList, responseEntity.getBody());
    }

//    @Test
//    public void testGetWeatherForecast_Success() {
//        // Arrange
//        String city = "New York";
//        WeatherDataDTO mockWeatherData = new WeatherDataDTO();
//
//        // Create mock WeatherService
//        WeatherService weatherServiceMock = mock(WeatherService.class);
//
//        // Inject mock WeatherService into WeatherController
//        WeatherController weatherController = new WeatherController(weatherServiceMock);
//
//        // Set up mockWeatherData with sample weather data
//        when(weatherServiceMock.getWeatherForecast(city, apiUrl, apiKey)).thenReturn(mockWeatherData);
//
//        // Act
//        WeatherDataDTO result = weatherController.getWeatherForecast(city);
//
//        // Assert
//        assertNotNull(result);
//    }
}
