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
        float time = mc.world.getTimeOfDay(); // от 0.0 до 1.0
        float brightness = 1.0f - Math.abs(time - 0.5f) * 2.0f;
        brightness = Math.max(0f, Math.min(1f, brightness)); // clamp [0, 1]

        Color base = Palette.getColor(0);
        Color darkened = new Color(
                (int)(base.getRed() * brightness),
                (int)(base.getGreen() * brightness),
                (int)(base.getBlue() * brightness)
        );

        return darkened.getRGB();
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
