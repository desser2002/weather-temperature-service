package org.dzianisbova.service;

import org.dzianisbova.model.TemperatureCategory;
import org.dzianisbova.model.WeatherRequest;
import org.dzianisbova.model.WeatherResponse;
import org.dzianisbova.provider.WeatherProvider;

public class WeatherService {
    private final WeatherProvider weatherProvider;
    private final TemperatureClassifier temperatureClassifier;

    public WeatherService(WeatherProvider weatherProvider, TemperatureClassifier temperatureClassifier) {
        this.weatherProvider = weatherProvider;
        this.temperatureClassifier = temperatureClassifier;
    }

    public WeatherResponse getWeather(String city) {
        double temperature = weatherProvider.fetchTemperature(city);
        TemperatureCategory category = temperatureClassifier.classify(temperature);
        return new WeatherResponse(city, temperature, category.getLabel());
    }
}
