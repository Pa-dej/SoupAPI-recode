package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.modules.CustomFog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.entity.effect.StatusEffects;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = BackgroundRenderer.class, priority = 1500)
public abstract class MixinBackgroundRenderer {

    @Inject(method = "applyFog", at = @At("RETURN"), cancellable = true)
    private static void applyFogCustom(Camera camera,
                                       BackgroundRenderer.FogType fogType,
                                       Vector4f color,
                                       float viewDistance,
                                       boolean thickenFog,
                                       float tickDelta,
                                       CallbackInfoReturnable<Fog> cir) {
        if (!CustomFog.enabled.getValue()) return;
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player.hasStatusEffect(StatusEffects.BLINDNESS) ||
                player.hasStatusEffect(StatusEffects.DARKNESS)) return;
        cir.setReturnValue(CustomFog.getCustomFog());
    }
}
