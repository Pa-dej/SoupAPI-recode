//package me.Padej_.soupapi.modules;
//
//import me.Padej_.soupapi.SoupModule;
//import me.Padej_.soupapi.settings.impl.BooleanSetting;
//import me.Padej_.soupapi.settings.impl.EnumSetting;
//import me.Padej_.soupapi.settings.impl.SliderSetting;
//import me.Padej_.soupapi.utils.Palette;
//import net.minecraft.client.render.Fog;
//import net.minecraft.client.render.FogShape;
//
//import java.awt.*;
//
//public class CustomFogNewGUI extends SoupModule {
//
//    public static final BooleanSetting enabled = new BooleanSetting("Enabled", "Custom fog", false);
//    public static final SliderSetting start = new SliderSetting("Start", "Min Range", 5f, 1f, 10f);
//    public static final SliderSetting end = new SliderSetting("End", "Max Range", 5f, 1f, 10f);
//    public static final SliderSetting density = new SliderSetting("Density", "Alpha", 80f, 1f, 100f);
//    public static final EnumSetting<CustomFogShape> fogShape = new EnumSetting<>("Shape", "Fog shape", CustomFogShape.SPHERE, CustomFogShape.class);
//
//    public CustomFogNewGUI() {
//        super("CustomFog", Category.WORLD);
//    }
//
//    public static Fog getCustomFog() {
//        Color c = Palette.getColor(0);
//        float r = c.getRed() / 255f;
//        float g = c.getGreen() / 255f;
//        float b = c.getBlue() / 255f;
//        float a = density.getValue() / 100f;
//
//        return new Fog(start.getValue(), end.getValue(), getCustomFogShape(), r, g, b, a);
//    }
//
//    private static FogShape getCustomFogShape() {
//        return switch (fogShape.getValue()) {
//            case SPHERE -> FogShape.SPHERE;
//            case CYLINDER -> FogShape.CYLINDER;
//        };
//    }
//
//    public enum CustomFogShape {
//        SPHERE, CYLINDER
//    }
//}
