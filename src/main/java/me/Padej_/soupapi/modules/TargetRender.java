package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render3D;
import me.Padej_.soupapi.utils.EntityUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;

public class TargetRender extends ConfigurableModule {
    private static float rollAngle = 0.0f;
    private static long lastUpdateTime = System.currentTimeMillis();

    private static Entity lastTargetEntity = null;
    private static long lastTargetUpdateTime = 0;

    private static boolean updateOrKeepTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !CONFIG.targetRenderEnabled) return false;

        long currentTime = System.currentTimeMillis();
        Entity currentTarget = EntityUtils.getTargetEntity();

        boolean visibleNow = currentTarget != null && client.player.canSee(currentTarget);

        if (visibleNow) {
            if (currentTarget != lastTargetEntity) {
                lastTargetEntity = currentTarget;
            }
            lastTargetUpdateTime = currentTime;
        }

        if (lastTargetEntity != null) {
            if (currentTime - lastTargetUpdateTime > (lastTargetEntity.isInvisible() ? 0 : CONFIG.targetRenderLiveTime * 1000L) || lastTargetEntity.isRemoved()) {
                lastTargetEntity = null;
                return false;
            }

            if (!client.player.canSee(lastTargetEntity)) {
                return false;
            }
        }

        return lastTargetEntity != null;
    }

    public static void renderTarget(WorldRenderContext context) {
        if (!updateOrKeepTarget()) return;

        float tickDelta = context.tickCounter().getTickDelta(true);

        switch (CONFIG.targetRenderStyle) {
            case SOUL -> Render3D.renderSoulsEsp(tickDelta, lastTargetEntity);
            case SPIRAL -> Render3D.drawSpiralsEsp(context.matrixStack(), lastTargetEntity);
            case TOPKA -> Render3D.drawScanEsp(context.matrixStack(), lastTargetEntity);
        }
    }

    public static void renderTargetLegacy(WorldRenderContext context) {
        if (CONFIG.targetRenderStyle != TargetRenderStyle.LEGACY) return;
        if (!updateOrKeepTarget()) return;

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;

        rollAngle = (rollAngle + 90f * deltaTime) % 360f;

        Render3D.renderTargetSelection(
                context.matrixStack(),
                context.camera(),
                context.tickCounter().getTickDelta(true),
                lastTargetEntity,
                rollAngle
        );
    }

    public enum TargetRenderStyle {
        LEGACY, SOUL, SPIRAL, TOPKA
    }

    public enum TargetRenderLegacyTexture {
        LEGACY, MARKER, BO, SIMPLE, SCIFI, AMONGUS, SKULL, JEKA, VEGAS
    }

    public enum TargetRenderSoulStyle {
        SMOKE, PLASMA;

        public static void setupBlendFunc() {
            switch (CONFIG.targetRenderSoulStyle) {
                case SMOKE -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
                case PLASMA -> RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
                case null, default -> RenderSystem.defaultBlendFunc();
            }
        }
    }
}
