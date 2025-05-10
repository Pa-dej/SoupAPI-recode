package me.Padej_.soupapi.mixin.redirect;

import me.Padej_.soupapi.render.TargetHudRenderer;
import net.minecraft.client.gui.hud.InGameOverlayRenderer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(InGameOverlayRenderer.class)
public class FireOverlayMixin {

    @Redirect(
            method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/VertexConsumer;color(FFFF)Lnet/minecraft/client/render/VertexConsumer;"
            )
    )
    private static VertexConsumer redirectColor(VertexConsumer instance, float r, float g, float b, float a) {
        if (!CONFIG.noFireOverlayEnabled) return instance.color(r, g, b, a);
        if (CONFIG.noFireOverlayCustomColorEnabled) {
            Color customColor = new Color(CONFIG.noFireOverlayCustomColor);

            float red = customColor.getRed() / 255f;
            float green = customColor.getGreen() / 255f;
            float blue = customColor.getBlue() / 255f;

            return instance.color(red, green, blue, CONFIG.noFireOverlayAlpha / 100f);
        }
        return instance.color(r, g, b, a);
    }

    @Inject(
            method = "renderFireOverlay(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/util/math/MatrixStack;translate(FFF)V",
                    ordinal = 0,
                    shift = At.Shift.AFTER
            )
    )
    private static void injectAfterTranslate(MatrixStack matrices, VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        if (!CONFIG.noFireOverlayEnabled) return;
        matrices.translate(0, -CONFIG.noFireOverlayY / 100f, 0);
    }
}


