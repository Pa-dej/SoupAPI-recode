package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.FogShape;

import java.awt.*;

public class CustomFog extends SoupModule {
    public static final BooleanSetting enabled = new BooleanSetting("Enabled", "description", false);
    public static final BooleanSetting thick = new BooleanSetting("Enabled", "description", false);
    public static final EnumSetting<CustomFog.CustomFogShape> shape = new EnumSetting<>("Shape", "description", CustomFogShape.SPHERE, CustomFogShape.class);
    public static final SliderSetting density = new SliderSetting("Density", "desc", 80, 1, 100, 1);
    public static final SliderSetting start = new SliderSetting("Start", "desc", 5, 1, 100, 1);
    public static final SliderSetting end = new SliderSetting("End", "desc", 20, 1, 100, 1);

    public CustomFog() {
        super("Custom Fog", Category.WORLD);
    }

    public static Fog getCustomFog() {
        Color c = Palette.getColor(0);
        float r = c.getRed() / 255f;
        float g = c.getGreen() / 255f;
        float b = c.getBlue() / 255f;
        float a = density.getValue() / 100f;

        return (new Fog(start.getValue(), end.getValue(), getCustomFogShape(), r, g, b, a));
    }

    private static FogShape getCustomFogShape() {
        return switch (shape.getValue()) {
            case SPHERE -> FogShape.SPHERE;
            case CYLINDER -> FogShape.CYLINDER;
        };
    }

    public enum CustomFogShape {
        SPHERE, CYLINDER
    }
}
