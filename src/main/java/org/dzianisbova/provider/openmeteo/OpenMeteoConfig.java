package org.dzianisbova.provider.openmeteo;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public record OpenMeteoConfig(String geocodingUrl, String forecastUrl) {
    private static final String PROPERTIES_FILE = "/openmeteo.properties";
    private static final String GEOCODING_URL_KEY = "openmeteo.geocoding-url";
    private static final String FORECAST_URL_KEY = "openmeteo.forecast-url";

    public static OpenMeteoConfig load() {
        Properties props = new Properties();
        try (InputStream in = OpenMeteoConfig.class.getResourceAsStream(PROPERTIES_FILE)) {
            if (in == null) {
                throw new IllegalStateException("Properties file not found on classpath: " + PROPERTIES_FILE);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load " + PROPERTIES_FILE, e);
        }
        return new OpenMeteoConfig(
                require(props, GEOCODING_URL_KEY),
                require(props, FORECAST_URL_KEY)
        );
    }

    private static String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return value;
    }
}
