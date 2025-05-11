package me.Padej_.soupapi.font;

import me.Padej_.soupapi.main.SoupAPI_Main;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontRenderer modules;
    public static FontRenderer sf_bold;
    public static FontRenderer sf_bold_17;
    public static FontRenderer sf_bold_12;
    public static FontRenderer sf_bold_mini;
    public static FontRenderer sf_medium;
    public static FontRenderer minecraft;

    public static @NotNull FontRenderer create(float size, String name) throws IOException, FontFormatException {
        return new FontRenderer(
                Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(SoupAPI_Main.class.getClassLoader().getResourceAsStream("assets/soupapi/fonts/" + name + ".ttf")))
                        .deriveFont(Font.PLAIN, size / 2f),
                size / 2f
        );
    }
}