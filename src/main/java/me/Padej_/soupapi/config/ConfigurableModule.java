package me.Padej_.soupapi.config;

import me.Padej_.soupapi.main.SoupAPI_Main;
import net.minecraft.client.MinecraftClient;

public abstract class ConfigurableModule {
    public static final MinecraftClient mc = MinecraftClient.getInstance();
    public static final SoupAPI_Config CONFIG = SoupAPI_Main.configHolder.get();
}
