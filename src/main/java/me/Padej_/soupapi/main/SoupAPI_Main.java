package me.Padej_.soupapi.main;

import me.Padej_.soupapi.config.SoupAPI_Config;
import me.Padej_.soupapi.utils.MC_Tiers;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;
import net.minecraft.util.Identifier;

public class SoupAPI_Main implements ModInitializer {
    public static ConfigHolder<SoupAPI_Config> configHolder;
    public static long initTime;
    public static String ac = "";
    public static MC_Tiers.TierGameModes MC_TiersGameMode;

    @Override
    public void onInitialize() {
        initTime = System.currentTimeMillis();
        configHolder = AutoConfig.register(SoupAPI_Config.class, GsonConfigSerializer::new);

        System.out.println("SoupAPI initialized");
        Identifier shaderId = Identifier.of("soupapi", "custom");
        System.out.println("Shader registered: " + shaderId);
    }

}
