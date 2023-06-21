package com.mullen.ethan.dungeonrunner.utils;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class PerlinNoise {

	private static final int OCTAVES = 4;
    private static final double PERSISTENCE = 0.5;
    private static final double LACUNARITY = 2.0;
    
	public static BufferedImage generatePerlinNoiseImage(int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        CustomPerlinNoise perlinNoise = new CustomPerlinNoise(OCTAVES, PERSISTENCE, LACUNARITY);

        double[][] noise = new double[width][height];
        double minNoise = Double.MAX_VALUE;
        double maxNoise = Double.MIN_VALUE;

        // Generate Perlin noise values
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double value = perlinNoise.noise(x, y);
                noise[x][y] = value;
                if (value < minNoise) {
                    minNoise = value;
                }
                if (value > maxNoise) {
                    maxNoise = value;
                }
            }
        }

        // Normalize the noise values to the range [0, 1]
        double range = maxNoise - minNoise;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                double normalizedValue = (noise[x][y] - minNoise) / range;
                int grayscale = (int) (normalizedValue * 255);
                Color color = new Color(grayscale, grayscale, grayscale);
                image.setRGB(x, y, color.getRGB());
            }
        }
	
        return image;
	
	}
	
}

class CustomPerlinNoise {
    private int octaves;
    private double persistence;
    private double lacunarity;

    public CustomPerlinNoise(int octaves, double persistence, double lacunarity) {
        this.octaves = octaves;
        this.persistence = persistence;
        this.lacunarity = lacunarity;
    }

    public double noise(double x, double y) {
        double total = 0.0;
        double frequency = 1.0;
        double amplitude = 1.0;
        double maxValue = 0.0;

        for (int i = 0; i < octaves; i++) {
            total += interpolatedNoise(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            frequency *= lacunarity;
            amplitude *= persistence;
        }

        return total / maxValue;
    }

    private double interpolatedNoise(double x, double y) {
        int xi = (int) x;
        int yi = (int) y;
        double xf = x - xi;
        double yf = y - yi;

        double v1 = smoothNoise(xi, yi);
        double v2 = smoothNoise(xi + 1, yi);
        double v3 = smoothNoise(xi, yi + 1);
        double v4 = smoothNoise(xi + 1, yi + 1);

        double i1 = interpolate(v1, v2, xf);
        double i2 = interpolate(v3, v4, xf);

        return interpolate(i1, i2, yf);
    }

    private double smoothNoise(int x, int y) {
        double corners = (noise(x - 1, y - 1) + noise(x + 1, y - 1) + noise(x - 1, y + 1) + noise(x + 1, y + 1)) / 16.0;
        double sides = (noise(x - 1, y) + noise(x + 1, y) + noise(x, y - 1) + noise(x, y + 1)) / 8.0;
        double center = noise(x, y) / 4.0;

        return corners + sides + center;
    }

    private double noise(int x, int y) {
        long n = x + y * 57;
        n = (n << 13) ^ n;
        return (1.0 - ((n * (n * n * 15731 + 789221) + 1376312589) & 0x7fffffff) / 1073741824.0);
    }

    private double interpolate(double a, double b, double x) {
        double ft = x * Math.PI;
        double f = (1 - Math.cos(ft)) * 0.5;

        return a * (1 - f) + b * f;
    }
}
