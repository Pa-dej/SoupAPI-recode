package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.screen.ConfigHudPositionsScreen;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.ButtonSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;

public class BetterHudStyles extends SoupModule {
    @SuppressWarnings("unused")
    public static ButtonSetting openHudCfg = new ButtonSetting("Config HUD", "das", () -> mc.setScreen(new ConfigHudPositionsScreen()));

    public static BooleanSetting betterHotbar = new BooleanSetting("Better Hotbar", "dasdas", false);
    public static BooleanSetting betterHotbarArmor = new BooleanSetting("Show Armor", "dasdas", false);
    public static BooleanSetting betterHotbarSmoothScroll = new BooleanSetting("Smooth Scroll", "dasdas", false);
    public static final EnumSetting<HotbarStyle> betterHotbarStyle = new EnumSetting<>("Style", "descripdstion", HotbarStyle.SIMPLE, HotbarStyle.class);

    public BetterHudStyles() {
        super("Better HUD", Category.HUD);
    }

    public enum HotbarStyle {
        GLOW,
        SIMPLE
    }
}
