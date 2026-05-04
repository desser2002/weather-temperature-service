package org.dzianisbova.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dzianisbova.model.WeatherResponse;
import org.dzianisbova.provider.WeatherProvider;
import org.dzianisbova.provider.http.HttpJsonClient;
import org.dzianisbova.provider.openmeteo.OpenMeteoWeatherProvider;
import org.dzianisbova.service.TemperatureClassifier;
import org.dzianisbova.service.WeatherService;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

public class WeatherHttpHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final String CITY_PARAM = "city";

    private final WeatherService weatherService;
    private final ObjectMapper objectMapper;

    public WeatherHttpHandler(WeatherService weatherService, ObjectMapper objectMapper) {
        this.weatherService = weatherService;
        this.objectMapper = objectMapper;
    }

    public WeatherHttpHandler() {
        ObjectMapper mapper = new ObjectMapper();
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        HttpJsonClient httpJsonClient = new HttpJsonClient(httpClient, mapper);
        WeatherProvider weatherProvider = new OpenMeteoWeatherProvider(httpJsonClient);
        TemperatureClassifier classifier = new TemperatureClassifier();

        this.weatherService = new WeatherService(weatherProvider, classifier);
        this.objectMapper = mapper;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        Map<String, String> queryParams = event.getQueryStringParameters();
        String city = queryParams != null ? queryParams.get(CITY_PARAM) : null;
        if (city != null) {
            city = city.trim();
        }

        if (city == null || city.isEmpty()) {
            return buildResponse(400, Map.of("error", "Missing required query parameter: " + CITY_PARAM));
        }

        try {
            WeatherResponse weather = weatherService.getWeather(city);
            return buildResponse(200, weather);
        } catch (IllegalArgumentException e) {
            return buildResponse(404, Map.of("error", e.getMessage()));
        } catch (Exception e) {
            context.getLogger().log("Unexpected error: " + e);
            return buildResponse(500, Map.of("error", "Internal server error"));
        }
    }

    private APIGatewayV2HTTPResponse buildResponse(int statusCode, Object body) {
        try {
            return APIGatewayV2HTTPResponse.builder()
                    .withStatusCode(statusCode)
                    .withHeaders(Map.of("Content-Type", "application/json"))
                    .withBody(objectMapper.writeValueAsString(body))
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize response body", e);
        }
    }
}
