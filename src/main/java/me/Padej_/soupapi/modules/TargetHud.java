package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.interpolation.EaseOutCirc;
import me.Padej_.soupapi.particle.Particle2D;
import me.Padej_.soupapi.reduce.ModuleSupressor;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

public class TargetHud extends ConfigurableModule {
    private static float hudScale = 0f;
    private static final float scaleSpeed = 0.2f;
    private static float hudTimer = 0f;
    public static EaseOutCirc headAnimation = new EaseOutCirc();
    public static LivingEntity target;
    private static LivingEntity lastTarget;
    private static float displayedHealth = 0f;
    private static final float healthChangeSpeed = 0.2f;
    private static final float colorAnimationSpeed = 0.01f;
    private static long lastUpdateTime = System.currentTimeMillis();
    public static float smoothedScreenX = 0f;
    public static float smoothedScreenY = 0f;
    private static final float smoothingFactor = 0.8f;

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static void onTick() {
        if (!CONFIG.targetHudEnabled) return;
        if (mc.player == null) return;

        getTarget();

        if (lastTarget != null && !mc.player.canSee(lastTarget)) {
            hudTimer = 0;
        }

        if (target instanceof PlayerEntity) {
            if (target != lastTarget) {
                displayedHealth = Math.min(target.getMaxHealth(), getHealth());
                lastTarget = target;
            }
            hudTimer = CONFIG.targetHudRenderTime;
            target = null;
        }

        if (lastTarget instanceof PlayerEntity) {
            float targetHealth = Math.min(lastTarget.getMaxHealth(), getHealth());
            displayedHealth = MathHelper.lerp(healthChangeSpeed, displayedHealth, targetHealth);
        }

        if (ModuleSupressor.disableHPBar()) {
            displayedHealth = 20;
        }
    }

    public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        updateColors(TargetHudRenderer.colorAnimationProgress); // чтобы цвета всегда переливались, а не только при включенном таргет худе

        if (!CONFIG.targetHudEnabled) return;
        getTarget();

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        deltaTime = Math.min(deltaTime, 0.1f);
        lastUpdateTime = currentTime;

        float frameTime = 1.0f / 60.0f;
        float normalizedDelta = deltaTime / frameTime;

        TargetHudRenderer.colorAnimationProgress = (TargetHudRenderer.colorAnimationProgress + normalizedDelta * colorAnimationSpeed) % 1.0f;
        TargetHudRenderer.hpColorAnimationProgress = (TargetHudRenderer.hpColorAnimationProgress + normalizedDelta * colorAnimationSpeed / 2) % 1.0f;
        headAnimation.update(normalizedDelta); // Обновляем анимацию головы
        TargetHudRenderer.ticks += 0.1f * normalizedDelta; // Обновляем ticks для частиц

        // Плавная интерполяция масштаба HUD
        if (hudTimer > 0) {
            hudScale = MathHelper.lerp(normalizedDelta * scaleSpeed, hudScale, 1.0f);
            hudTimer -= deltaTime; // Уменьшаем таймер на основе реального времени
            if (hudTimer < 0) hudTimer = 0;
        } else {
            hudScale = MathHelper.lerp(normalizedDelta * scaleSpeed, hudScale, 0.0f);
        }

        // Обновляем частицы
        for (Particle2D p : new ArrayList<>(TargetHudRenderer.particles)) {
            p.updatePosition(normalizedDelta); // Передаем нормализованное время
            if (p.opacity < 1) TargetHudRenderer.particles.remove(p);
        }

        // Проверяем, существует ли lastTarget в мире
        if (lastTarget != null && (lastTarget.isRemoved() || !Objects.requireNonNull(mc.world).getEntitiesByClass(LivingEntity.class, lastTarget.getBoundingBox().expand(0.1), e -> e == lastTarget).contains(lastTarget))) {
            hudTimer = 0; // Сбрасываем таймер, чтобы HUD исчез
            lastTarget = null; // Очищаем lastTarget
        }

        if (hudScale > 0 && lastTarget instanceof PlayerEntity) {
            int targetScreenX, targetScreenY;
            Vec3d screenPos = Vec3d.ZERO; // Инициализируем по умолчанию

            // Определяем координаты HUD в зависимости от флага isFollow
            if (CONFIG.targetHudFollow) {
                // Получаем экранные координаты сущности
                float tickDelta = renderTickCounter.getTickDelta(true);
                double x = lastTarget.prevX + (lastTarget.getX() - lastTarget.prevX) * tickDelta;
                double y = lastTarget.prevY + (lastTarget.getY() - lastTarget.prevY) * tickDelta;
                double z = lastTarget.prevZ + (lastTarget.getZ() - lastTarget.prevZ) * tickDelta;
                Vec3d entityPos = new Vec3d(x, y + 1.1, z);
                screenPos = worldSpaceToScreenSpace(entityPos);

                // Проверяем, находится ли сущность в видимой области
                if (screenPos.z > 0 && screenPos.z < 1) {
                    targetScreenX = (int) screenPos.x;
                    targetScreenY = (int) screenPos.y;
                } else {
                    return;
                }
            } else {
                // Используем фиксированные координаты из конфигурации
                targetScreenX = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
                targetScreenY = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;
            }

            // Применяем сглаживание к экранным координатам
            smoothedScreenX = MathHelper.lerp(normalizedDelta * smoothingFactor, smoothedScreenX, targetScreenX);
            smoothedScreenY = MathHelper.lerp(normalizedDelta * smoothingFactor, smoothedScreenY, targetScreenY);

            context.getMatrices().push();

            // Выбор центра масштабирования в зависимости от стиля HUD
            float centerX, centerY;
            if (CONFIG.targetHudStyle.equals(Style.MINI)) {
                centerX = smoothedScreenX + 47.5f; // Центр для MiniHUD (95/2)
                centerY = smoothedScreenY + 17.5f; // (35/2)
            } else if (CONFIG.targetHudStyle.equals(Style.NORMAL)) {
                centerX = smoothedScreenX + 68.5f; // Центр для NormalHUD (137/2)
                centerY = smoothedScreenY + 23.75f; // (47.5/2)
            } else { // TINY
                centerX = smoothedScreenX;
                centerY = smoothedScreenY;
            }

            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(hudScale, hudScale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            if (CONFIG.targetHudFollow) {
                float depthFactor = MathHelper.clamp(1.0f - (float) screenPos.z, 0.1f, 1.0f);
                float adjustedOffsetX = CONFIG.targetHudEntityOffsetX * 10 * depthFactor;
                float adjustedOffsetY = CONFIG.targetHudEntityOffsetY * 10 * depthFactor;
                context.getMatrices().translate(adjustedOffsetX, -adjustedOffsetY, 0);
            }

            float animationFactor = MathUtility.clamp(hudScale, 0, 1f);
            switch (CONFIG.targetHudStyle) {
                case MINI ->
                        TargetHudRenderer.renderMiniHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget, (int) smoothedScreenX, (int) smoothedScreenY, screenPos);
                case TINY ->
                        TargetHudRenderer.renderTinyHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget, (int) smoothedScreenX, (int) smoothedScreenY, screenPos);
                case ARES ->
                        TargetHudRenderer.renderAresHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget, (int) smoothedScreenX, (int) smoothedScreenY, screenPos);
                case ALT_1 ->
                        TargetHudRenderer.renderAlt_1_HUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget, (int) smoothedScreenX, (int) smoothedScreenY, screenPos);
                default ->
                        TargetHudRenderer.renderNormalHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget, (int) smoothedScreenX, (int) smoothedScreenY, screenPos);
            }

            context.getMatrices().pop();
        }
    }

    private static void updateColors(float colorAnimationProgress) {
        Color c1 = Palette.getColor(0f);   // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);   // Верхний левый

        float progress = colorAnimationProgress % 1.0f;

        if (progress < 0.25f) {
            float phaseProgress = progress / 0.25f;
            TargetHudRenderer.topLeft = interpolateColor(c1, c2, phaseProgress);
            TargetHudRenderer.topRight = interpolateColor(c2, c3, phaseProgress);
            TargetHudRenderer.bottomRight = interpolateColor(c3, c4, phaseProgress);
            TargetHudRenderer.bottomLeft = interpolateColor(c4, c1, phaseProgress);
        } else if (progress < 0.5f) {
            float phaseProgress = (progress - 0.25f) / 0.25f;
            TargetHudRenderer.topLeft = interpolateColor(c2, c3, phaseProgress);
            TargetHudRenderer.topRight = interpolateColor(c3, c4, phaseProgress);
            TargetHudRenderer.bottomRight = interpolateColor(c4, c1, phaseProgress);
            TargetHudRenderer.bottomLeft = interpolateColor(c1, c2, phaseProgress);
        } else if (progress < 0.75f) {
            float phaseProgress = (progress - 0.5f) / 0.25f;
            TargetHudRenderer.topLeft = interpolateColor(c3, c4, phaseProgress);
            TargetHudRenderer.topRight = interpolateColor(c4, c1, phaseProgress);
            TargetHudRenderer.bottomRight = interpolateColor(c1, c2, phaseProgress);
            TargetHudRenderer.bottomLeft = interpolateColor(c2, c3, phaseProgress);
        } else {
            float phaseProgress = (progress - 0.75f) / 0.25f;
            TargetHudRenderer.topLeft = interpolateColor(c4, c1, phaseProgress);
            TargetHudRenderer.topRight = interpolateColor(c1, c2, phaseProgress);
            TargetHudRenderer.bottomRight = interpolateColor(c2, c3, phaseProgress);
            TargetHudRenderer.bottomLeft = interpolateColor(c3, c4, phaseProgress);
        }
    }

    public static void getTarget() {
        if (mc.currentScreen == null) {
            if (!(EntityUtils.getTargetEntity() instanceof LivingEntity)) return;
            target = (LivingEntity) EntityUtils.getTargetEntity();
            if (target.isInvisible()) hudTimer = 0;
            if (target != null && !mc.player.canSee(target)) {
                target = null;
            }
        } else {
            if (mc.currentScreen instanceof ChatScreen) {
                target = mc.player;
            } else {
                target = null;
            }
        }
    }

    public static float getHealth() {
        if (lastTarget == null) return 0f;
        if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().getServerInfo();
        }
        return lastTarget.getHealth() + lastTarget.getAbsorptionAmount();
    }

    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }

    public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();
        double scale = mc.getWindow().getScaleFactor();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3d(target.x / scale, (displayHeight - target.y) / scale, target.z);
    }

    public enum Style {
        MINI, TINY, NORMAL, ARES, ALT_1
    }

    public enum Config {
        CONFIG_POS
    }
}
