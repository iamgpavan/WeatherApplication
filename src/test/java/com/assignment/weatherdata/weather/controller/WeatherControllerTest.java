//package com.assignment.weatherdata.weather.controller;
//
//import com.assignment.weatherdata.weather.entity.WeatherData;
//import com.assignment.weatherdata.weather.exception.PathVariableMissingException;
//import com.assignment.weatherdata.weather.service.WeatherService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.MockitoAnnotations;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Objects;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//class WeatherControllerTest {
//
//    @Mock
//    private WeatherService weatherService;
//
//    @InjectMocks
//    private WeatherController weatherController;
//
//    @BeforeEach
//    void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testGetWeatherByCity() {
//        String city = "New York";
//        WeatherData mockWeatherData = new WeatherData();  // Assume WeatherData is properly instantiated
//
//        when(weatherService.getWeatherByCity(city)).thenReturn(new ResponseEntity<>(mockWeatherData, HttpStatus.OK));
//
//        ResponseEntity<WeatherData> response = weatherController.getWeatherByCity(city);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(mockWeatherData, response.getBody());
//
//        verify(weatherService).getWeatherByCity(city);
//    }
//
//    @Test
//    public void testCreateWeatherData() {
//        String city = "Los Angeles";
//        WeatherData weatherData = new WeatherData(); // Set up weatherData as needed
//
//        when(weatherService.createWeatherData(eq(city), any(WeatherData.class))).thenReturn(new ResponseEntity<>(weatherData, HttpStatus.CREATED));
//
//        ResponseEntity<WeatherData> response = weatherController.createWeatherData(city, weatherData);
//        assertEquals(HttpStatus.CREATED, response.getStatusCode());
//        assertEquals(weatherData, response.getBody());
//
//        verify(weatherService).createWeatherData(city, weatherData);
//    }
//
//    @Test
//    public void testUpdateWeatherData() {
//        String city = "Chicago";
//        WeatherData updatedWeatherData = new WeatherData(); // Setup updatedWeatherData appropriately
//
//        when(weatherService.updateWeatherData(eq(city), any(WeatherData.class))).thenReturn(new ResponseEntity<>(updatedWeatherData, HttpStatus.OK));
//
//        ResponseEntity<WeatherData> response = weatherController.updateWeatherData(city, updatedWeatherData);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(updatedWeatherData, response.getBody());
//
//        verify(weatherService).updateWeatherData(city, updatedWeatherData);
//    }
//
//    @Test
//    public void testDeleteWeatherByCity() {
//        String city = "Miami";
//
//        HashMap<String, String> responseMap = new HashMap<>();
//        responseMap.put("message", "Weather data deleted successfully");
//
//        when(weatherService.deleteWeatherByCity(city)).thenReturn(new ResponseEntity<>(responseMap, HttpStatus.OK));
//
//        ResponseEntity<HashMap<String, String>> response = weatherController.deleteWeatherByCity(city);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals("Weather data deleted successfully", Objects.requireNonNull(response.getBody()).get("message"));
//
//        verify(weatherService).deleteWeatherByCity(city);
//    }
//
////    @Test
////    public void testGetAllWeatherData() {
////        HashMap<String, List<String>> allWeatherData = new HashMap<>();
////        allWeatherData.put("Cities", List.of("New York", "Los Angeles", "Miami"));
////
////        when(weatherService.getAll()).thenReturn(new ResponseEntity<>(allWeatherData, HttpStatus.OK));
////
////        ResponseEntity<HashMap<String, List<WeatherData>>> response = weatherController.getAll();
////        assertEquals(HttpStatus.OK, response.getStatusCode());
////        assertEquals(3, Objects.requireNonNull(response.getBody()).get("Cities").size());
////        assertTrue(response.getBody().get("Cities").contains("Miami"));
////
////        verify(weatherService).getAll();
////    }
//
//    @Test
//    public void testExceptionHandlingMissingCityName() {
//        Exception exception = assertThrows(PathVariableMissingException.class, () -> {
//            weatherController.getWeatherByCity();
//        });
//
//        String expectedMessage = "City Name is missing in requested URL";
//        String actualMessage = exception.getMessage();
//        assertTrue(actualMessage.contains(expectedMessage));
//    }
//
//}
