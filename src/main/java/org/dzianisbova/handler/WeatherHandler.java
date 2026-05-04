package org.dzianisbova.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dzianisbova.model.WeatherRequest;
import org.dzianisbova.model.WeatherResponse;
import org.dzianisbova.provider.WeatherProvider;
import org.dzianisbova.provider.http.HttpJsonClient;
import org.dzianisbova.provider.openmeteo.OpenMeteoConfig;
import org.dzianisbova.provider.openmeteo.OpenMeteoWeatherProvider;
import org.dzianisbova.service.TemperatureClassifier;
import org.dzianisbova.service.WeatherService;

import java.net.http.HttpClient;
import java.time.Duration;

public class WeatherHandler implements RequestHandler<WeatherRequest, WeatherResponse> {
    private final WeatherService weatherService;

    public WeatherHandler() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        HttpJsonClient httpJsonClient = new HttpJsonClient(httpClient, new ObjectMapper());
        WeatherProvider weatherProvider = new OpenMeteoWeatherProvider(httpJsonClient, OpenMeteoConfig.load());
        TemperatureClassifier classifier = new TemperatureClassifier();

        this.weatherService = new WeatherService(weatherProvider, classifier);
    }

    public WeatherHandler(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @Override
    public WeatherResponse handleRequest(WeatherRequest request, Context context) {
        if (request == null || request.city() == null) {
            throw new IllegalArgumentException("city is required");
        }
        String city = request.city().trim();
        if (city.isEmpty()) {
            throw new IllegalArgumentException("city must not be blank");
        }
        return weatherService.getWeather(city);
    }
}
