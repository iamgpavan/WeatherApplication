package com.assignment.weatherdata.weather.service;

import static net.minidev.asm.ConvertDate.convertToDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.assignment.weatherdata.weather.ModelMapper.WeatherDataMapper;
import com.assignment.weatherdata.weather.dto.WeatherDataDTO;
import com.assignment.weatherdata.weather.entity.Forecast;
import com.assignment.weatherdata.weather.entity.ForecastDay;
import com.assignment.weatherdata.weather.entity.WeatherApiResponse;
import com.assignment.weatherdata.weather.entity.WeatherData;
import com.assignment.weatherdata.weather.exception.CityNotFoundException;
import com.assignment.weatherdata.weather.exception.ValidationException;
import com.assignment.weatherdata.weather.exception.WeatherDataNotFoundException;
import com.assignment.weatherdata.weather.repository.WeatherDataRepository;
import com.assignment.weatherdata.weather.service.impl.WeatherServiceImpl;
import com.assignment.weatherdata.weather.utils.ConversionUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.List;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class WeatherServiceImplTest {

    @Mock
    private WeatherDataRepository weatherDataRepository;

    @Mock
    private WeatherDataMapper weatherDataMapper;
    @Mock
    private RestTemplate restTemplate;
    @InjectMocks
    private WeatherServiceImpl weatherService;

    @Value("${weather.api.key}")
    private String apiKey;

    @Value("${weather.api.url}")
    private String apiUrl;

    @Test
    void testGetAll() {
        // Arrange
        List<WeatherData> weatherDataList = new ArrayList<>();
        when(weatherDataRepository.findAll()).thenReturn(weatherDataList);

        // Act
        ResponseEntity<HashMap<String, List<String>>> responseEntity = weatherService.getAll();

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getBody().containsKey("cities"));
        assertTrue(responseEntity.getBody().get("cities").isEmpty());
    }

    @Test
    public void testGetWeatherByCity_Success() {
        // Arrange
        String cityName = "New York";
        List<WeatherData> cityWeatherData = new ArrayList<>();
        WeatherData weatherData = new WeatherData();
        weatherData.setCity(cityName);
        weatherData.setTemperature(20.5);
        weatherData.setDescription("Sunny");
        weatherData.setDate(new Date());
        cityWeatherData.add(weatherData);

        // Mock behavior of dependencies
        when(weatherDataRepository.findByCity(cityName)).thenReturn(cityWeatherData);
        when(weatherDataMapper.convertToDTO(any(WeatherData.class))).thenAnswer(invocation -> {
            WeatherData arg = invocation.getArgument(0);
            WeatherDataDTO dto = new WeatherDataDTO();
            dto.setCity(arg.getCity());
            dto.setTemperature(arg.getTemperature());
            dto.setDescription(arg.getDescription());
            dto.setDate(arg.getDate());
            return dto;
        });

        // Act
        ResponseEntity<List<WeatherDataDTO>> response = weatherService.getWeatherByCity(cityName);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(cityWeatherData.size(), response.getBody().size());
    }

    @Test
    public void testGetWeatherByCity_CityNotFound() {
        // Arrange
        String cityName = "Nonexistent City";

        // Mock behavior of dependencies
        when(weatherDataRepository.findByCity(cityName)).thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(CityNotFoundException.class, () -> weatherService.getWeatherByCity(cityName));
    }

    @Test
    void testCreateWeatherData_Success() {
        // Arrange
        String cityName = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setDate(new Date());
        when(weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate())).thenReturn(Optional.empty());
        when(weatherDataMapper.convertToEntity(any(WeatherDataDTO.class))).thenReturn(new WeatherData());
        when(weatherDataRepository.save(any(WeatherData.class))).thenReturn(new WeatherData());
        when(weatherDataMapper.convertToDTO(any(WeatherData.class))).thenReturn(new WeatherDataDTO());

        // Act
        ResponseEntity<WeatherDataDTO> responseEntity = weatherService.createWeatherData(cityName, weatherDataDTO);

        // Assert
        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
    }

    @Test
    void testCreateWeatherData_AlreadyExists() {
        // Arrange
        String cityName = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setDate(new Date());
        when(weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate())).thenReturn(Optional.of(new WeatherData()));

        // Act & Assert
        assertThrows(ValidationException.class, () -> {
            weatherService.createWeatherData(cityName, weatherDataDTO);
        });
    }

    @Test
    void createWeatherData_Success() {
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setCity("New York");
        weatherDataDTO.setTemperature(25.5);
        // Mocking repository method to return empty Optional, indicating no existing data
        when(weatherDataRepository.findByCityAndDate(eq("New York"), any())).thenReturn(Optional.empty());
        WeatherData savedWeatherData = new WeatherData();
        when(weatherDataMapper.convertToEntity(weatherDataDTO)).thenReturn(savedWeatherData);
        when(weatherDataRepository.save(savedWeatherData)).thenReturn(savedWeatherData);
        WeatherDataDTO savedWeatherDataDTO = new WeatherDataDTO();
        when(weatherDataMapper.convertToDTO(savedWeatherData)).thenReturn(savedWeatherDataDTO);

        ResponseEntity<WeatherDataDTO> response = weatherService.createWeatherData("New York", weatherDataDTO);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(savedWeatherDataDTO, response.getBody());
        verify(weatherDataRepository, times(1)).save(savedWeatherData);
    }

    @Test
    void createWeatherData_AlreadyExists() {
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setCity("London");
        weatherDataDTO.setDate(new Date());
        // Mocking repository method to return existing data
        when(weatherDataRepository.findByCityAndDate(eq("London"), any())).thenReturn(Optional.of(new WeatherData()));

        assertThrows(ValidationException.class, () -> weatherService.createWeatherData("London", weatherDataDTO));
    }

    @Test
    void createWeatherData_NullCityName() {
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        assertThrows(ValidationException.class, () -> weatherService.createWeatherData(null, weatherDataDTO));
    }

    @Test
    void createWeatherData_EmptyCityName() {
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        assertThrows(ValidationException.class, () -> weatherService.createWeatherData("", weatherDataDTO));
    }

    @Test
    void createWeatherData_InvalidTemperature() {
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setCity("Paris");
        weatherDataDTO.setTemperature(150); // Temperature out of valid range

        assertThrows(ValidationException.class, () -> weatherService.createWeatherData("Paris", weatherDataDTO));
    }

    @Test
    public void testUpdateWeatherData_Success() {
        // Arrange
        String cityName = "New York";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setCity(cityName);
        weatherDataDTO.setTemperature(25.0);
        weatherDataDTO.setDescription("Sunny");
        weatherDataDTO.setDate(new Date());

        WeatherData existingWeatherData = new WeatherData();
        existingWeatherData.setCity(cityName);
        existingWeatherData.setTemperature(20.0);
        existingWeatherData.setDescription("Cloudy");
        existingWeatherData.setDate(weatherDataDTO.getDate());

        // Mock behavior of dependencies
        when(weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate()))
                .thenReturn(Optional.of(existingWeatherData));
        when(weatherDataMapper.convertToDTO(any(WeatherData.class)))
                .thenAnswer(invocation -> {
                    WeatherData arg = invocation.getArgument(0);
                    WeatherDataDTO dto = new WeatherDataDTO();
                    dto.setCity(arg.getCity());
                    dto.setTemperature(arg.getTemperature());
                    dto.setDescription(arg.getDescription());
                    dto.setDate(arg.getDate());
                    return dto;
                });

        // Act
        ResponseEntity<WeatherDataDTO> response = weatherService.updateWeatherData(cityName, weatherDataDTO);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(weatherDataDTO.getTemperature(), response.getBody().getTemperature());
        assertEquals(weatherDataDTO.getDescription(), response.getBody().getDescription());
        assertEquals(weatherDataDTO.getDate(), response.getBody().getDate());
    }

    @Test
    public void testUpdateWeatherData_NotFound() {
        // Arrange
        String cityName = "Nonexistent City";
        WeatherDataDTO weatherDataDTO = new WeatherDataDTO();
        weatherDataDTO.setCity(cityName);
        weatherDataDTO.setTemperature(25.0);
        weatherDataDTO.setDescription("Sunny");
        weatherDataDTO.setDate(new Date());

        // Mock behavior of dependencies
        when(weatherDataRepository.findByCityAndDate(cityName, weatherDataDTO.getDate()))
                .thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ValidationException.class, () -> weatherService.updateWeatherData(cityName, weatherDataDTO));
    }
    @Test
    void deleteWeatherByCity_Success() {
        String cityName = "New York";
        // Mocking repository method to return count of deleted records
        when(weatherDataRepository.deleteByCity(cityName)).thenReturn(1L);

        ResponseEntity<HashMap<String, String>> response = weatherService.deleteWeatherByCity(cityName);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Weather data for New York has been deleted.", response.getBody().get("message"));
    }

    @Test
    void deleteWeatherByCity_NotFound() {
        String cityName = "London";
        // Mocking repository method to return 0 count, indicating no records deleted
        when(weatherDataRepository.deleteByCity(cityName)).thenReturn(0L);

        assertThrows(WeatherDataNotFoundException.class, () -> weatherService.deleteWeatherByCity(cityName));
    }

    @Test
    void deleteWeatherByCity_NullCityName() {
        assertThrows(ValidationException.class, () -> weatherService.deleteWeatherByCity(null));
    }

    @Test
    void deleteWeatherByCity_EmptyCityName() {
        assertThrows(ValidationException.class, () -> weatherService.deleteWeatherByCity(""));
    }

    @Test
    public void testGetWeatherDataByCityAndDateRange_Success() {
        // Arrange
        String cityName = "New York";

        Date startDate = ConversionUtils.convertToDate("2024-04-20");
        Date endDate = ConversionUtils.convertToDate("2024-04-22");

        // Mock behavior of dependencies
        List<WeatherData> weatherDataList = new ArrayList<>();
        WeatherData weatherData1 = new WeatherData();
        weatherData1.setCity(cityName);
        weatherData1.setTemperature(20.0);
        weatherData1.setDescription("Sunny");
        weatherData1.setDate(convertToDate("2024-04-20"));
        weatherDataList.add(weatherData1);

        WeatherData weatherData2 = new WeatherData();
        weatherData2.setCity(cityName);
        weatherData2.setTemperature(25.0);
        weatherData2.setDescription("Cloudy");
        weatherData2.setDate(convertToDate("2024-04-21"));
        weatherDataList.add(weatherData2);

        // Mock behavior of the weatherDataRepository to return the weatherDataList
        when(weatherDataRepository.findByCityAndDateBetween(eq(cityName), any(Date.class), any(Date.class)))
                .thenReturn(weatherDataList);
        when(weatherDataMapper.convertToDTO(any(WeatherData.class)))
                .thenAnswer(invocation -> {
                    WeatherData arg = invocation.getArgument(0);
                    WeatherDataDTO dto = new WeatherDataDTO();
                    dto.setCity(arg.getCity());
                    dto.setTemperature(arg.getTemperature());
                    dto.setDescription(arg.getDescription());
                    dto.setDate(arg.getDate());
                    return dto;
                });

        // Act
        List<WeatherDataDTO> result = weatherService.getWeatherDataByCityAndDateRange(cityName, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(weatherDataList.size(), result.size());
        for (int i = 0; i < weatherDataList.size(); i++) {
            WeatherDataDTO expectedDTO = weatherDataMapper.convertToDTO(weatherDataList.get(i));
            assertEquals(expectedDTO.getCity(), result.get(i).getCity());
            assertEquals(expectedDTO.getTemperature(), result.get(i).getTemperature());
            assertEquals(expectedDTO.getDescription(), result.get(i).getDescription());
            assertEquals(expectedDTO.getDate(), result.get(i).getDate());
        }
    }

    @Test
    public void testGetWeatherDataByCityAndDateRange_NotFound() {
        // Arrange
        String cityName = "Nonexistent City";
        Date startDate = new Date();
        Date endDate = new Date();

        // Adjust end date to include data up to the end of the day
        LocalDateTime endDateTime = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().atTime(LocalTime.MAX);
        Date adjustedEndDate = Date.from(endDateTime.atZone(ZoneId.systemDefault()).toInstant());

        // Mock behavior of dependencies
        when(weatherDataRepository.findByCityAndDateBetween(cityName, startDate, adjustedEndDate))
                .thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(WeatherDataNotFoundException.class, () -> weatherService.getWeatherDataByCityAndDateRange(cityName, startDate, endDate));
    }

    @Test
    public void testGetWeatherByCityAndSortAsc_Success() {
        // Arrange
        String cityName = "New York";
        String sortBy = "temperature";
        String order = "asc";

        // Mock behavior of the weatherDataRepository to return a list of WeatherData
        List<WeatherData> cityWeatherData = new ArrayList<>();
        WeatherData weatherData1 = new WeatherData();
        weatherData1.setCity(cityName);
        weatherData1.setTemperature(20.0);
        // Add more WeatherData objects as needed

        cityWeatherData.add(weatherData1);


        when(weatherDataRepository.findByCity(cityName)).thenReturn(cityWeatherData);

        // Mock behavior of the weatherDataMapper to convert WeatherData to WeatherDataDTO
        when(weatherDataMapper.convertToDTO(any(WeatherData.class)))
                .thenAnswer(invocation -> {
                    WeatherData arg = invocation.getArgument(0);
                    WeatherDataDTO dto = new WeatherDataDTO();
                    dto.setCity(arg.getCity());
                    dto.setTemperature(arg.getTemperature());
                    // Set other properties as needed
                    return dto;
                });

        // Act
        ResponseEntity<List<WeatherDataDTO>> response = weatherService.getWeatherByCityAndSort(cityName, sortBy, order);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<WeatherDataDTO> sortedWeatherData = response.getBody();
        assertNotNull(sortedWeatherData);
        assertFalse(sortedWeatherData.isEmpty());

        // Verify sorting order
        double previousTemperature = Double.MIN_VALUE; // Start with the lowest possible value
        for (WeatherDataDTO weatherDataDTO : sortedWeatherData) {
            assertTrue(weatherDataDTO.getTemperature() >= previousTemperature);
            previousTemperature = weatherDataDTO.getTemperature();
        }
    }

    @Test
    public void testGetWeatherByCityAndSortDesc_Success() {
        // Arrange
        String cityName = "New York";
        String sortBy = "temperature";
        String order = "desc";

        // Mock behavior of the weatherDataRepository to return a list of WeatherData
        List<WeatherData> cityWeatherData = new ArrayList<>();
        WeatherData weatherData1 = new WeatherData();
        weatherData1.setCity(cityName);
        weatherData1.setTemperature(20.0);

        cityWeatherData.add(weatherData1);


        when(weatherDataRepository.findByCity(cityName)).thenReturn(cityWeatherData);

        // Mock behavior of the weatherDataMapper to convert WeatherData to WeatherDataDTO
        when(weatherDataMapper.convertToDTO(any(WeatherData.class)))
                .thenAnswer(invocation -> {
                    WeatherData arg = invocation.getArgument(0);
                    WeatherDataDTO dto = new WeatherDataDTO();
                    dto.setCity(arg.getCity());
                    dto.setTemperature(arg.getTemperature());
                    return dto;
                });

        // Act
        ResponseEntity<List<WeatherDataDTO>> response = weatherService.getWeatherByCityAndSort(cityName, sortBy, order);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        List<WeatherDataDTO> sortedWeatherData = response.getBody();
        assertNotNull(sortedWeatherData);
        assertFalse(sortedWeatherData.isEmpty());

        // Verify sorting order
        double previousTemperature = Double.MIN_VALUE; // Start with the lowest possible value
        for (WeatherDataDTO weatherDataDTO : sortedWeatherData) {
            assertTrue(weatherDataDTO.getTemperature() >= previousTemperature);
            previousTemperature = weatherDataDTO.getTemperature();
        }
    }

    @Test
    public void testGetWeatherByCityAndSort_InvalidSortByParam() {
        // Arrange
        String cityName = "New York";
        String invalidSortBy = "invalid_sort_param"; // Invalid sortBy parameter
        String order = "asc";

        // Act and Assert
        assertThrows(ValidationException.class, () ->
                weatherService.getWeatherByCityAndSort(cityName, invalidSortBy, order));
    }

    @Test
    void getWeatherByCityAndSort_CityNotFound() {
        String cityName = "Nonexistent City";
        String sortBy = "temperature";
        String order = "asc";
        // Mocking repository method to return an empty list, indicating city not found
        when(weatherDataRepository.findByCity(cityName)).thenReturn(new ArrayList<>());

        // Act and Assert
        assertThrows(CityNotFoundException.class, () -> weatherService.getWeatherByCityAndSort(cityName, sortBy, order));
    }

//    @Test
//    public void testGetWeatherForecast_Success() {
//        // Arrange
//        String city = "New York";
//        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);
//
//        // Mock successful response from the API
//        WeatherApiResponse mockResponse = new WeatherApiResponse();
//        // Set up mockResponse with forecast data
//        Forecast forecast = new Forecast();
//        List<ForecastDay> forecastDays = new ArrayList<>();
//        ForecastDay forecastday = new ForecastDay();
//        forecastday.setDate("2024-04-23");
//        forecastDays.add(forecastday);
//        forecast.setForecastday(forecastDays);
//        mockResponse.setForecast(forecast);
//
//        when(restTemplate.getForObject(url, WeatherApiResponse.class)).thenReturn(mockResponse);
//
//        // Act
//        WeatherDataDTO result = weatherService.getWeatherForecast(city, apiUrl, apiKey);
//
//        // Assert
//        assertNotNull(result);
//    }

//    @Test
//    public void testGetWeatherForecast_Success() {
//        // Arrange
//        String city = "New York";
//
//        // Mock successful response from the API
//        WeatherApiResponse mockResponse = new WeatherApiResponse();
//        // Set up mockResponse with forecast data
//        Forecast forecast = new Forecast();
//        List<ForecastDay> forecastDays = new ArrayList<>();
//        ForecastDay forecastday = new ForecastDay();
//        forecastday.setDate("2024-04-23");
//        forecastDays.add(forecastday);
//        forecast.setForecastday(forecastDays);
//        mockResponse.setForecast(forecast);
//
//        // Set up the URL with mock apiUrl and apiKey
//        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);
//        when(restTemplate.getForObject(url, WeatherApiResponse.class)).thenReturn(mockResponse);
//
//        // Act
//        WeatherDataDTO result = weatherService.getWeatherForecast(city, apiUrl, apiKey);
//
//        // Assert
//        assertNotNull(result);
//    }

//    @Test
//    public void testGetWeatherForecast_Success() {
//        // Setup
//        String city = "New York";
//        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);
//
//        // Mocking the API response
//        WeatherApiResponse mockResponse = new WeatherApiResponse();
//        Forecast forecast = new Forecast();
//        forecast.setForecastday(Collections.singletonList(new ForecastDay()));
//        mockResponse.setForecast(forecast);
//        when(restTemplate.getForObject(url, WeatherApiResponse.class)).thenReturn(mockResponse);
//
//        // Test execution
//        WeatherDataDTO result = weatherService.getWeatherForecast(city, apiUrl, apiKey);
//
//        // Assertions
//        assertNotNull(result);
//        assertEquals(city, result.getCity()); // Assuming getCityName is a method in WeatherDataDTO
//    }

//    @Test
//    public void testGetWeatherForecast_NoForecastData() {
//        // Arrange
//        String city = "New York";
//        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);
//
//        // Mock response from the API where forecast data is not available
//        WeatherApiResponse mockResponse = new WeatherApiResponse();
//        // Set forecast to null or empty
//        mockResponse.setForecast(null);
//
//        when(restTemplate.getForObject(url, WeatherApiResponse.class)).thenReturn(mockResponse);
//
//        // Act
//        WeatherDataDTO result = weatherService.getWeatherForecast(city, apiUrl, apiKey);
//
//        // Assert
//        assertNotNull(result);
//    }
//
//    @Test
//    public void testGetWeatherForecast_Exception() {
//        // Arrange
//        String city = "Nonexistent City";
//        String url = String.format("%s?key=%s&q=%s&days=1&aqi=no&alerts=no", apiUrl, apiKey, city);
//
//        // Mock exception thrown by the API call
//        when(restTemplate.getForObject(url, WeatherApiResponse.class)).thenThrow(new RuntimeException("API call failed"));
//
//        // Act
//        WeatherDataDTO result = weatherService.getWeatherForecast(city, apiUrl, apiKey);
//
//        // Assert
//        assertNotNull(result);
//    }

}
