package me.Padej_.soupapi.render;

import net.minecraft.util.math.MathHelper;
import org.joml.Math;

import java.awt.*;

public interface RenderWithAnimatedColor {
    static float getWaveInterpolation(double angle, float rotationOffset) {
        float positionOnCircle = (float) ((angle / (2 * Math.PI)) + rotationOffset) % 1.0f;
        float distanceToGradientPoint = Math.abs(positionOnCircle - 0.5f);
        return MathHelper.clamp(1.0f - distanceToGradientPoint * 2.0f, 0.0f, 1.0f);
    }

    static Color interpolateColor(Color startColor, Color endColor, float fraction) {
        int red = MathHelper.clamp((int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * fraction), 0, 255);
        int green = MathHelper.clamp((int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * fraction), 0, 255);
        int blue = MathHelper.clamp((int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * fraction), 0, 255);
        return new Color(red, green, blue);
    }
}
