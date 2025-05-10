package me.Padej_.soupapi.mixin.modifyReturnValue;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.render.DimensionEffects;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(DimensionEffects.Overworld.class)
public class DimensionEffectMixin {

    @ModifyReturnValue(method = "adjustFogColor", at = @At("RETURN"))
    private Vec3d getSkyColor(Vec3d original) {
        Color color = Palette.getColor(0);
        Vec3d newColor = new Vec3d(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f);
        return CONFIG.customFogEnabled ? newColor : original;
    }

//    @ModifyReturnValue(method = "getSkyType", at = @At("RETURN"))
//    private DimensionEffects.SkyType getSkyType(DimensionEffects.SkyType original) {
//        return CONFIG.customFogEnabled ? DimensionEffects.SkyType.END : original;
//    }
}
