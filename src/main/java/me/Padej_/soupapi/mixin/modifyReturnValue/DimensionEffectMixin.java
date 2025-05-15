package me.Padej_.soupapi.mixin.modifyReturnValue;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.Padej_.soupapi.modules.CustomFog;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(DimensionEffects.Overworld.class)
public class DimensionEffectMixin {

    @ModifyReturnValue(method = "isSunRisingOrSetting", at = @At("RETURN"))
    private boolean isSunRisingOrSetting(boolean original) {
        return !CustomFog.enabled.getValue() && original;
    }

    @ModifyReturnValue(method = "adjustFogColor", at = @At("RETURN"))
    private Vec3d adjustFogColor(Vec3d original) {
        Color color = Palette.getColor(0);
        Vec3d newColor = new Vec3d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        return CustomFog.enabled.getValue() ? newColor : original;
    }

    @ModifyReturnValue(method = "useThickFog", at = @At("RETURN"))
    private boolean adjustFogColor(boolean original) {
        if (CustomFog.thick.getValue() && CustomFog.enabled.getValue()) {
            return true;
        } else {
            return original;
        }
    }

    @ModifyReturnValue(method = "getSkyColor", at = @At("RETURN"))
    private int getSkyColor(int original) {

        if (!CONFIG.coloredSkyEnabled) return original;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return original;

        Color base = Palette.getColor(0);

        return base.getRGB();
    }

//    @ModifyReturnValue(method = "getSkyType", at = @At("RETURN"))
//    private DimensionEffects.SkyType getSkyType(DimensionEffects.SkyType original) {
//        return CustomFog.enabled.getValue() ? DimensionEffects.SkyType.END : original;
//    }
}
