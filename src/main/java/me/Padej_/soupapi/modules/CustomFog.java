package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;

import java.awt.*;

public class CustomFog extends ConfigurableModule {

    public static Fog getCustomFog() {
        Color c = Palette.getColor(0);
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        float a = CONFIG.customFogDensity / 100f;

        return(new Fog(CONFIG.customFogStart, CONFIG.customFogEnd, getCustomFogShape(), r, g, b, a));
    }

    private static FogShape getCustomFogShape() {
        return switch (CONFIG.customFogShape) {
            case SPHERE -> FogShape.SPHERE;
            case CYLINDER -> FogShape.CYLINDER;
        };
    }

    public enum CustomFogShape {
        SPHERE, CYLINDER
    }
}
