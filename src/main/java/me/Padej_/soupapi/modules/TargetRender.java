package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render3D;
import me.Padej_.soupapi.utils.EntityUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.joml.Math;

public class TargetRender extends ConfigurableModule {
    private static float rollAngle = 0.0f;
    private static float targetRollAngle = 0.0f;
    private static boolean increasing = true;

    public static void renderTarget(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !CONFIG.targetRenderEnabled) return;

        Entity targetEntity = EntityUtils.getTargetEntity();
        if (targetEntity == null) return;

        switch (CONFIG.targetRenderStyle) {
            case SOUL -> Render3D.renderSoulsEsp(context.tickCounter().getTickDelta(true), targetEntity);
            case SPIRAL -> Render3D.drawSpiralsEsp(context.matrixStack(), targetEntity);
        }
    }

    public static void renderTargetLegacy(WorldRenderContext context) {
        if (!CONFIG.targetRenderStyle.equals(TargetRenderStyle.LEGACY)) return;
        float rollSpeed = 1;
        float stiffness = 0.08f;
        float damping = 0.98f;
        float minSpeed = 0.3f;
        float maxSpeed = 2;
        float tickDelta = context.tickCounter().getTickDelta(true);

        float distanceToEdge = increasing ? 360 - targetRollAngle : targetRollAngle;
        float acceleration = stiffness * distanceToEdge;
        rollSpeed = Math.min(maxSpeed, Math.max(minSpeed, rollSpeed * damping + acceleration));

        if (increasing) {
            targetRollAngle += tickDelta * rollSpeed;
            if (targetRollAngle >= 360) {
                targetRollAngle = 360;
                increasing = false;
            }
        } else {
            targetRollAngle -= tickDelta * rollSpeed;
            if (targetRollAngle <= 0.0f) {
                targetRollAngle = 0.0f;
                increasing = true;
            }
        }

        rollAngle = MathHelper.lerp(0.05f, rollAngle, targetRollAngle);
        Render3D.renderTargetSelection(context.matrixStack(), context.camera(), tickDelta, rollAngle);

    }

    public enum TargetRenderStyle {
        LEGACY,
        SOUL,
        SPIRAL,
        TOPKA
        ;
    }

    public enum TargetRenderLegacyTexture {
        LEGACY,
        MARKER,
        BO,
        SIMPLE,
        SCIFI
    }
}
