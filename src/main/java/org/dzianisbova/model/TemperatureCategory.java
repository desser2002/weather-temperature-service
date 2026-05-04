package org.dzianisbova.model;

public enum TemperatureCategory {
    FREEZING("Freezing", Double.NEGATIVE_INFINITY, 0),
    COLD("Cold", 0, 10),
    MILD("Mild", 10, 20),
    WARM("Warm", 20, 30),
    HOT("Hot", 30, Double.POSITIVE_INFINITY);

    private final String label;
    private final double minInclusive;
    private final double maxExclusive;

    TemperatureCategory(String label, double minInclusive, double maxExclusive) {
        this.label = label;
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
    }

    public String getLabel() {
        return label;
    }

    public boolean matches(double temperature) {
        return temperature >= minInclusive && temperature < maxExclusive;
    }
}
