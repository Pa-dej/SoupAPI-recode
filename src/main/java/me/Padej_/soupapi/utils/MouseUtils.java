package me.Padej_.soupapi.utils;

public class MouseUtils {
    public static double cursorX = 0;
    public static double cursorY = 0;

    public static void update(double dx, double dy) {
        float speed = 0.05f;
        double maxDistance = 30;
        double maxSmoothingFactor = 0.8;

        double distance = Math.sqrt(cursorX * cursorX + cursorY * cursorY);
        double attractionFactor = Math.min(1.0, distance / maxDistance);
        double SMOOTHING_FACTOR = maxSmoothingFactor * attractionFactor;

        cursorX += dx * speed;
        cursorY += dy * speed;

        cursorX += (0 - cursorX) * SMOOTHING_FACTOR;
        cursorY += (0 - cursorY) * SMOOTHING_FACTOR;
    }
}



