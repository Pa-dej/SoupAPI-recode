package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;

public class AspectRatio extends SoupModule {
    public static final BooleanSetting enabled = new BooleanSetting("Enabled", "description", false);
    public static final BooleanSetting usePreset = new BooleanSetting("Use Preset", "description", false);
    public static final EnumSetting<AspectRatio.Preset> preset = new EnumSetting<>("Preset", "description", Preset._16_9_, AspectRatio.Preset.class);
    public static final SliderSetting factor = new SliderSetting("Factor", "description", 150, 120, 220, 1);

    public AspectRatio() {
        super("Aspect Ratio", Category.HUD);
    }

    public static float getRatioByPreset() {
        if (usePreset.getValue()) {
            return switch (preset.getValue()) {
                case _16_9_ -> 16f / 9f;
                case _5_4_ -> 5f / 4f;
                case _4_3_ -> 4f / 3f;
            };
        } else {
            return factor.getValue() / 100f;
        }
    }

    public enum Preset {
        _16_9_, _5_4_, _4_3_
    }
}