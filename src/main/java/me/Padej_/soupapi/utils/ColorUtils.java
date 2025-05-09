package me.Padej_.soupapi.utils;

import java.awt.*;

public class ColorUtils {

    public static float[] rgbToHsl(int rgb) {
        float r = ((rgb >> 16) & 0xFF) / 255.0f;
        float g = ((rgb >> 8) & 0xFF) / 255.0f;
        float b = (rgb & 0xFF) / 255.0f;

        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float h = 0.0f;
        float s = (max == 0) ? 0 : (delta / max);
        float l = (max + min) / 2.0f;

        if (delta != 0) {
            if (max == r) {
                h = ((g - b) / delta) % 6;
            } else if (max == g) {
                h = ((b - r) / delta) + 2;
            } else {
                h = ((r - g) / delta) + 4;
            }
            h /= 6;
        }

        return new float[]{h, s, l};
    }

    public static int hslToRgb(float h, float l) {
        float s = 1.0f;

        float q = (l < 0.5f) ? (l * (1 + s)) : ((l + s) - (l * s));
        float p = 2 * l - q;

        float r = hueToRgb(p, q, h + (1f / 3f));
        float g = hueToRgb(p, q, h);
        float b = hueToRgb(p, q, h - (1f / 3f));

        return new Color(clamp(r), clamp(g), clamp(b)).getRGB();
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1.0f;
        if (t > 1) t -= 1.0f;
        if (t < (1f / 6f)) return p + (q - p) * 6 * t;
        if (t < (1f / 2f)) return q;
        if (t < (2f / 3f)) return p + (q - p) * ((2f / 3f) - t) * 6;
        return p;
    }

    public static int getMaxSaturationColor(int rgb) {
        float[] hsl = rgbToHsl(rgb);
        return hslToRgb(hsl[0], hsl[2]);
    }

    private static float clamp(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
