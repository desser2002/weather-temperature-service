package org.dzianisbova.provider.http;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.dzianisbova.provider.ExternalApiException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class HttpJsonClient {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public HttpJsonClient(HttpClient httpClient, ObjectMapper objectMapper) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public JsonNode getJson(String url) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, ofString());
        } catch (IOException e) {
            throw new ExternalApiException("Failed to fetch json from: " + url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException("Interrupted while fetching json from: " + url, e);
        }

        if (response.statusCode() != 200) {
            throw new ExternalApiException(
                    "Request failed with status code: " + response.statusCode() + ": " + url);
        }

        try {
            return objectMapper.readTree(response.body());
        } catch (IOException e) {
            throw new ExternalApiException("Failed to parse json from: " + url, e);
        }
    }
}
