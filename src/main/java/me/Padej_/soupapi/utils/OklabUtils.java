package me.Padej_.soupapi.utils;

import java.awt.*;

public class OklabUtils {
    public static float[] srgbToOklab(Color color) {
        float r = srgbChannelToLinear(color.getRed() / 255f);
        float g = srgbChannelToLinear(color.getGreen() / 255f);
        float b = srgbChannelToLinear(color.getBlue() / 255f);

        float l = 0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b;
        float m = 0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b;
        float s = 0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b;

        float l_ = (float) Math.cbrt(l);
        float m_ = (float) Math.cbrt(m);
        float s_ = (float) Math.cbrt(s);

        float L = 0.2104542553f * l_ + 0.7936177850f * m_ - 0.0040720468f * s_;
        float a = 1.9779984951f * l_ - 2.4285922050f * m_ + 0.4505937099f * s_;
        float b_ = 0.0259040371f * l_ + 0.7827717662f * m_ - 0.8086757660f * s_;

        return new float[]{L, a, b_};
    }

    public static Color oklabToSrgb(float[] oklab) {
        float l_ = oklab[0] + 0.3963377774f * oklab[1] + 0.2158037573f * oklab[2];
        float m_ = oklab[0] - 0.1055613458f * oklab[1] - 0.0638541728f * oklab[2];
        float s_ = oklab[0] - 0.0894841775f * oklab[1] - 1.2914855480f * oklab[2];

        float l = l_ * l_ * l_;
        float m = m_ * m_ * m_;
        float s = s_ * s_ * s_;

        float r =  4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s;
        float g = -1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s;
        float b = -0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s;

        return new Color(clamp(linearToSrgbChannel(r)), clamp(linearToSrgbChannel(g)), clamp(linearToSrgbChannel(b)));
    }

    private static float srgbChannelToLinear(float c) {
        return c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055f) / 1.055f, 2.4f);
    }

    private static float linearToSrgbChannel(float c) {
        return c <= 0.0031308f ? c * 12.92f : 1.055f * (float) Math.pow(c, 1 / 2.4) - 0.055f;
    }

    private static int clamp(float v) {
        return Math.max(0, Math.min(255, Math.round(v * 255)));
    }

    public static Color interpolate(Color a, Color b, float t) {
        float[] labA = srgbToOklab(a);
        float[] labB = srgbToOklab(b);
        float[] labResult = new float[3];
        for (int i = 0; i < 3; i++) labResult[i] = labA[i] + (labB[i] - labA[i]) * t;
        return oklabToSrgb(labResult);
    }
}

