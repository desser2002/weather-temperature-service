package org.dzianisbova.provider.openmeteo;

import com.fasterxml.jackson.databind.JsonNode;
import org.dzianisbova.provider.ExternalApiException;
import org.dzianisbova.provider.WeatherProvider;
import org.dzianisbova.provider.http.HttpJsonClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OpenMeteoWeatherProvider implements WeatherProvider {
    private static final String GEOCODING_URL = "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1";
    private static final String FORECAST_URL =   "https://api.open-meteo.com/v1/forecast?latitude=%s&longitude=%s&current=temperature_2m";


    private final HttpJsonClient httpClient;

    public OpenMeteoWeatherProvider(HttpJsonClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public double fetchTemperature(String city) {
        Coordinates cords = findCoordinates(city);
        return fetchTemperatureByCoordinates(cords);
    }

    private Coordinates findCoordinates(String city) {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = GEOCODING_URL.formatted(encodedCity);

        JsonNode root = httpClient.getJson(url);
        JsonNode result = root.get("results");

        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("City not found: " + city);
        }

        JsonNode first = result.get(0);
        JsonNode latitude = first.get("latitude");
        JsonNode longitude = first.get("longitude");

        if (latitude == null || longitude == null) {
            throw new ExternalApiException("Coordinates not available in response from: " + url);
        }

        return new Coordinates(latitude.asDouble(), longitude.asDouble());
    }

    private double fetchTemperatureByCoordinates(Coordinates cords) {
        String url = FORECAST_URL.formatted(cords.latitude, cords.longitude);

        JsonNode root = httpClient.getJson(url);
        JsonNode current = root.get("current");

        if (current == null || current.get("temperature_2m") == null) {
            throw new ExternalApiException("Temperature not available in response from: " + url);
        }

        return current.get("temperature_2m").asDouble();
    }

    record Coordinates(double latitude, double longitude) {
    }
}
