package me.Padej_.soupapi.mixin.modifyReturnValue;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(BiomeEffects.class)
public abstract class BiomeEffectsMixin {
    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private int getSkyColor(int original) {
        if (!CONFIG.coloredSkyEnabled) return original;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return original;

        Color base = Palette.getColor(0);

        return base.getRGB();
    }


    @ModifyReturnValue(method = "getFogColor", at = @At("RETURN"))
    private int getFogColor(int original) {
        return CONFIG.coloredSkyEnabled ? Palette.getColor(0).getRGB() : original;
    }

    @ModifyReturnValue(method = "getWaterColor", at = @At("RETURN"))
    private int getWaterColor(int original) {
        return CONFIG.coloredSkyEnabled ? Palette.getColor(0).getRGB() : original;
    }
}
