package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.render.RenderWithAnimatedColor;

import java.awt.*;

public class Palette {

    public static int getRawColor1() {
        return SoupAPI_Main.configHolder.get().c1;
    }

    public static int getRawColor2() {
        return SoupAPI_Main.configHolder.get().c2;
    }

    public static int getRawColor3() {
        return SoupAPI_Main.configHolder.get().c3;
    }

    public static int getRawColor4() {
        return SoupAPI_Main.configHolder.get().c4;
    }

    public static Color getInterpolatedPaletteColor(float progress) {
        Palette.ColorsStyle style = SoupAPI_Main.configHolder.get().paletteStyle;
        int c1 = SoupAPI_Main.configHolder.getConfig().c1;
        int c2 = SoupAPI_Main.configHolder.getConfig().c2;
        int c3 = SoupAPI_Main.configHolder.getConfig().c3;
        int c4 = SoupAPI_Main.configHolder.getConfig().c4;

        switch (style) {
            case SOLO:
                return new Color(c1);
            case DUO:
                return RenderWithAnimatedColor.interpolateColor(new Color(c1), new Color(c2), progress);
            case TRIO:
                if (progress < 0.5f) {
                    return RenderWithAnimatedColor.interpolateColor(new Color(c1), new Color(c2), progress * 2f);
                } else {
                    return RenderWithAnimatedColor.interpolateColor(new Color(c2), new Color(c3), (progress - 0.5f) * 2f);
                }
            case QUARTET:
                if (progress < 1f / 3f) {
                    return RenderWithAnimatedColor.interpolateColor(new Color(c1), new Color(c2), progress * 3f);
                } else if (progress < 2f / 3f) {
                    return RenderWithAnimatedColor.interpolateColor(new Color(c2), new Color(c3), (progress - 1f / 3f) * 3f);
                } else {
                    return RenderWithAnimatedColor.interpolateColor(new Color(c3), new Color(c4), (progress - 2f / 3f) * 3f);
                }
            default:
                return new Color(c1); // fallback
        }
    }

    public static ColorsStyle getStyle() {
        return SoupAPI_Main.configHolder.get().paletteStyle;
    }

    public static Color getColor(float position) {
        ColorsStyle style = getStyle();

        return switch (style) {
            case SOLO -> new Color(getRawColor1());
            case DUO -> interpolate(position, new Color(getRawColor1()), new Color(getRawColor2()));
            case TRIO ->
                    interpolate3(position, new Color(getRawColor1()), new Color(getRawColor2()), new Color(getRawColor3()));
            case QUARTET ->
                    interpolate4(position, new Color(getRawColor1()), new Color(getRawColor2()), new Color(getRawColor3()), new Color(getRawColor4()));
        };
    }

    private static Color interpolate(float t, Color c1, Color c2) {
        return new Color(
                lerp(c1.getRed(), c2.getRed(), t),
                lerp(c1.getGreen(), c2.getGreen(), t),
                lerp(c1.getBlue(), c2.getBlue(), t)
        );
    }

    private static Color interpolate3(float t, Color c1, Color c2, Color c3) {
        if (t < 0.5f) {
            return interpolate(t * 2f, c1, c2);
        } else {
            return interpolate((t - 0.5f) * 2f, c2, c3);
        }
    }

    private static Color interpolate4(float t, Color c1, Color c2, Color c3, Color c4) {
        if (t < 1f / 3f) {
            return interpolate(t * 3f, c1, c2);
        } else if (t < 2f / 3f) {
            return interpolate((t - 1f / 3f) * 3f, c2, c3);
        } else {
            return interpolate((t - 2f / 3f) * 3f, c3, c4);
        }
    }

    private static int lerp(int a, int b, float t) {
        return Math.round(a + (b - a) * t);
    }

    public enum ColorsStyle {
        SOLO,
        DUO,
        TRIO,
        QUARTET
    }
}

