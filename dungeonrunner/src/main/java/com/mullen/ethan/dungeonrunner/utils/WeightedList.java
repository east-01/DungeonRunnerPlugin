package com.mullen.ethan.dungeonrunner.utils;

import java.util.*;

public class WeightedList<T> {
    private final Map<T, Double> weightedValues;
    private double totalWeight;

    public WeightedList() {
        this.weightedValues = new HashMap<>();
        this.totalWeight = 0.0;
    }

    public void addValue(T enumValue, double weight) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be greater than 0.");
        }
        double value = 0;
        if(weightedValues.containsKey(enumValue)) {
        	value = weightedValues.get(enumValue);
        }
        weightedValues.put(enumValue, value + weight);
        totalWeight += weight;
    }

    public T rollWeightedValue() {
        if (weightedValues.isEmpty()) {
            throw new IllegalStateException("No enum values added.");
        }
        double randomValue = Math.random() * totalWeight;
        double cumulativeWeight = 0.0;
        for (Map.Entry<T, Double> entry : weightedValues.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue <= cumulativeWeight) {
                return entry.getKey();
            }
        }
        // This should never happen, but just in case
        throw new IllegalStateException("Failed to roll a weighted value.");
    }
}
