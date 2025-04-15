package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
        Render3D.renderTargetSelection(context.matrixStack(), context.camera(), context.tickCounter().getTickDelta(true), rollAngle);
    }

    public static void onTick() {
        if (!CONFIG.targetRenderStyle.equals(TargetRenderStyle.LEGACY)) return;
        float rollSpeed = CONFIG.targetRenderLegacyRollSpeed;

        float stiffness = 0.08f;
        float damping = 0.98f;
        float minSpeed = 0.3f;
        float maxSpeed = 2.0f;

        float distanceToEdge = increasing ? 360 - targetRollAngle : targetRollAngle;
        float acceleration = stiffness * distanceToEdge;
        rollSpeed = Math.min(maxSpeed, Math.max(minSpeed, rollSpeed * damping + acceleration));

        if (increasing) {
            targetRollAngle += rollSpeed;
            if (targetRollAngle >= 360) {
                targetRollAngle = 360;
                increasing = false;
            }
        } else {
            targetRollAngle -= rollSpeed;
            if (targetRollAngle <= 0.0f) {
                targetRollAngle = 0.0f;
                increasing = true;
            }
        }

        rollAngle = MathHelper.lerp(0.05f, rollAngle, targetRollAngle);
    }

    public enum TargetRenderStyle {
        LEGACY,
        SOUL,
        SPIRAL
    }

    public enum TargetRenderLegacyTexture {
        LEGACY,
        MARKER,
        BO,
        SIMPLE,
        SCIFI
    }

    public enum TargetRenderSoulStyle {
        SMOKE,
        PLASMA;

        public static void setupBlendFunc() {
            switch (CONFIG.targetRenderSoulStyle) {
                case SMOKE -> setSmoke();
                case PLASMA -> setPlasma();
            }
        }

        static void setSmoke() {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
        }

        static void setPlasma() {
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        }
    }
}
