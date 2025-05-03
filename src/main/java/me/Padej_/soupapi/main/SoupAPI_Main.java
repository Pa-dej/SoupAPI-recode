package me.Padej_.soupapi.main;

import me.Padej_.soupapi.config.SoupAPI_Config;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class SoupAPI_Main implements ModInitializer {
    public static ConfigHolder<SoupAPI_Config> configHolder;
    public static long initTime;
    public static String ac = "";

    public static final SimpleParticleType STAR = FabricParticleTypes.simple();

    @Override
    public void onInitialize() {
        initTime = System.currentTimeMillis();
        configHolder = AutoConfig.register(SoupAPI_Config.class, GsonConfigSerializer::new);
        registerParticles();

//        ExordiumMod.setForceBlend(false);
    }

    private void registerParticles() {
        Registry.register(Registries.PARTICLE_TYPE, Identifier.of("soupapi", "star"), STAR);
    }
}
