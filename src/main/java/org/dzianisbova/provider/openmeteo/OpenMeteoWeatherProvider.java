package org.dzianisbova.provider.openmeteo;

import com.fasterxml.jackson.databind.JsonNode;
import org.dzianisbova.provider.ExternalApiException;
import org.dzianisbova.provider.WeatherProvider;
import org.dzianisbova.provider.http.HttpJsonClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class OpenMeteoWeatherProvider implements WeatherProvider {
    private static final String FIELD_RESULTS = "results";
    private static final String FIELD_LATITUDE = "latitude";
    private static final String FIELD_LONGITUDE = "longitude";
    private static final String FIELD_CURRENT = "current";
    private static final String FIELD_TEMPERATURE_2M = "temperature_2m";

    private final HttpJsonClient httpClient;
    private final String geocodingUrl;
    private final String forecastUrl;

    public OpenMeteoWeatherProvider(HttpJsonClient httpClient, OpenMeteoConfig config) {
        this.httpClient = httpClient;
        this.geocodingUrl = config.geocodingUrl();
        this.forecastUrl = config.forecastUrl();
    }

    @Override
    public double fetchTemperature(String city) {
        Coordinates cords = findCoordinates(city);
        return fetchTemperatureByCoordinates(cords);
    }

    private Coordinates findCoordinates(String city) {
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String url = geocodingUrl.formatted(encodedCity);

        JsonNode root = httpClient.getJson(url);
        JsonNode result = root.get(FIELD_RESULTS);

        if (result == null || result.isEmpty()) {
            throw new IllegalArgumentException("City not found: " + city);
        }

        JsonNode first = result.get(0);
        JsonNode latitude = first.get(FIELD_LATITUDE);
        JsonNode longitude = first.get(FIELD_LONGITUDE);

        if (latitude == null || longitude == null) {
            throw new ExternalApiException("Coordinates not available in response from: " + url);
        }

        return new Coordinates(latitude.asDouble(), longitude.asDouble());
    }

    private double fetchTemperatureByCoordinates(Coordinates cords) {
        String url = forecastUrl.formatted(cords.latitude, cords.longitude);

        JsonNode root = httpClient.getJson(url);
        JsonNode current = root.get(FIELD_CURRENT);

        if (current == null || current.get(FIELD_TEMPERATURE_2M) == null) {
            throw new ExternalApiException("Temperature not available in response from: " + url);
        }

        return current.get(FIELD_TEMPERATURE_2M).asDouble();
    }

    record Coordinates(double latitude, double longitude) {
    }
}
