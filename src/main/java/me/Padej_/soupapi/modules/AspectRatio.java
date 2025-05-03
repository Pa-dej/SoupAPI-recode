package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import net.minecraft.client.MinecraftClient;

public class AspectRatio extends ConfigurableModule {

    public static float getRatioByPreset() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return switch (CONFIG.aspectRatioPreset) {
            case _16_9_ -> 16f / 9f;
            case _5_4_  -> 5f / 4f;
            case _4_3_  -> 4f / 3f;
        };
    }

    public enum Preset {
        _16_9_, _5_4_, _4_3_
    }
}