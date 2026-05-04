package org.dzianisbova.service;

import org.dzianisbova.model.TemperatureCategory;

import java.util.Arrays;

public class TemperatureClassifier {
    public TemperatureCategory classify(double temperature) {
        return Arrays.stream(TemperatureCategory.values())
                .filter(c -> c.matches(temperature))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No category matches temperature: " + temperature));
    }
}
