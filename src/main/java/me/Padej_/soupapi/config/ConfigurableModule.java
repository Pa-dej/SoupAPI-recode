package me.Padej_.soupapi.config;

import me.Padej_.soupapi.main.SoupAPI_Main;
import me.shedaniel.clothconfig2.api.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public abstract class ConfigurableModule {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final SoupAPI_Config CONFIG = SoupAPI_Main.configHolder.get();

    public static void saveConfig() {
        SoupAPI_Main.configHolder.save();
    }

    public static void saveAll(Screen screen) {
        if (screen instanceof ConfigScreen configScreen) {
            configScreen.saveAll(false);
        }
    }
}
