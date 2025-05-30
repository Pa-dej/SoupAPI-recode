package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render3D;
import me.Padej_.soupapi.utils.EntityUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;

public class TargetRender extends ConfigurableModule {

    private static Entity lastTargetEntity = null;
    private static long lastTargetUpdateTime = 0;

    private static boolean updateOrKeepTarget() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !CONFIG.targetRenderEnabled) return false;

        long currentTime = System.currentTimeMillis();
        Entity currentTarget = EntityUtils.getTargetEntity();

        // Проверка: если включён флаг — только игроки
        if (CONFIG.targetRenderOnlyPlayers && !(currentTarget instanceof PlayerEntity)) {
            currentTarget = null;
        }

        boolean visibleNow = false;
        if (currentTarget != null && client.player.canSee(currentTarget)) {
            if (!currentTarget.isInvisible()) {
                visibleNow = true;
            } else if (currentTarget.isGlowing() || (currentTarget instanceof LivingEntity living && hasAnyArmor(living))) {
                visibleNow = true;
            }
        }

        if (visibleNow) {
            if (currentTarget != lastTargetEntity) {
                lastTargetEntity = currentTarget;
            }
            lastTargetUpdateTime = currentTime;
        }

        if (lastTargetEntity != null) {
            if (currentTime - lastTargetUpdateTime > CONFIG.targetRenderLiveTime * 1000L
                    || lastTargetEntity.isRemoved()) {
                lastTargetEntity = null;
                return false;
            }

            if (!client.player.canSee(lastTargetEntity)) {
                return false;
            }

            if (CONFIG.targetRenderOnlyPlayers && !(lastTargetEntity instanceof PlayerEntity)) {
                lastTargetEntity = null;
                return false;
            }
        }

        return lastTargetEntity != null;
    }

    private static boolean hasAnyArmor(LivingEntity entity) {
        return !entity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()
                || !entity.getEquippedStack(EquipmentSlot.CHEST).isEmpty()
                || !entity.getEquippedStack(EquipmentSlot.LEGS).isEmpty()
                || !entity.getEquippedStack(EquipmentSlot.FEET).isEmpty();
    }

    public static void renderTarget(WorldRenderContext context) {
        if (!updateOrKeepTarget()) return;

        float tickDelta = context.tickCounter().getTickDelta(true);

        switch (CONFIG.targetRenderStyle) {
            case SOUL -> Render3D.renderSoulsEsp(tickDelta, lastTargetEntity);
            case SPIRAL -> Render3D.drawSpiralsEsp(context.matrixStack(), lastTargetEntity);
            case TOPKA -> Render3D.drawScanEsp(context.matrixStack(), lastTargetEntity);
            case LEGACY -> Render3D.drawLegacy(tickDelta, lastTargetEntity);
        }
    }

    public enum Style {
        LEGACY, SOUL, SPIRAL, TOPKA
    }

    public enum LegacyTexture {
        LEGACY, MARKER, BO, SIMPLE, SCIFI, AMONGUS, SKULL, JEKA, VEGAS
    }

    public enum SoulTexture {
        FIREFLY, ALT
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
