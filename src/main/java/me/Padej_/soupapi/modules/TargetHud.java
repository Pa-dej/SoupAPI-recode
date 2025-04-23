package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.interpolation.EaseOutCirc;
import me.Padej_.soupapi.particle.Particle2D;
import me.Padej_.soupapi.render.TargetHudRender;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.MathUtility;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;

public class TargetHud extends ConfigurableModule {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float hudScale = 0f; // Текущий масштаб HUD
    private static final float scaleSpeed = 0.2f; // Скорость анимации масштабирования
    private static float hudTimer = 0f; // Таймер для задержки рендера (в секундах)
    public static EaseOutCirc headAnimation = new EaseOutCirc();
    public static LivingEntity target;
    private static LivingEntity lastTarget; // Сохраняем последнюю цель для рендера
    private static float displayedHealth = 0f; // Текущее отображаемое значение здоровья
    private static final float healthChangeSpeed = 0.2f; // Скорость изменения здоровья
    private static final float colorAnimationSpeed = 0.01f; // Скорость вращения цветов
    private static long lastUpdateTime = System.currentTimeMillis(); // Время последнего обновления

    public static void onTick() {
        if (!CONFIG.targetHudEnabled) return;

        // Обновляем состояние цели
        getTarget();
        if (target instanceof PlayerEntity) {
            if (target != lastTarget) { // Если цель сменилась
                displayedHealth = Math.min(target.getMaxHealth(), getHealth()); // Сбрасываем displayedHealth
                lastTarget = target;
            }
            hudTimer = CONFIG.targetHudRenderTime; // Сбрасываем таймер (в секундах)
            target = null;
        }

        // Плавная интерполяция здоровья
        if (lastTarget instanceof PlayerEntity) {
            float targetHealth = Math.min(lastTarget.getMaxHealth(), getHealth());
            displayedHealth = MathHelper.lerp(healthChangeSpeed, displayedHealth, targetHealth);
        }
    }

    public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        if (!CONFIG.targetHudEnabled) return;
        getTarget();

        // Вычисляем разницу во времени
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f; // Время в секундах
        deltaTime = Math.min(deltaTime, 0.1f); // Ограничение на 100 мс
        lastUpdateTime = currentTime;

        // Нормализация времени для 60 FPS
        float frameTime = 1.0f / 60.0f; // Время одного кадра при 60 FPS (~0.01667 сек)
        float normalizedDelta = deltaTime / frameTime;

        // Обновляем анимации
        TargetHudRender.colorAnimationProgress = (TargetHudRender.colorAnimationProgress + normalizedDelta * colorAnimationSpeed) % 1.0f;
        TargetHudRender.hpColorAnimationProgress = (TargetHudRender.hpColorAnimationProgress + normalizedDelta * colorAnimationSpeed / 2) % 1.0f;
        headAnimation.update(normalizedDelta); // Обновляем анимацию головы
        TargetHudRender.ticks += 0.1f * normalizedDelta; // Обновляем ticks для частиц

        // Плавная интерполяция масштаба HUD
        if (hudTimer > 0) {
            hudScale = MathHelper.lerp(normalizedDelta * scaleSpeed, hudScale, 1.0f);
            hudTimer -= deltaTime; // Уменьшаем таймер на основе реального времени
            if (hudTimer < 0) hudTimer = 0;
        } else {
            hudScale = MathHelper.lerp(normalizedDelta * scaleSpeed, hudScale, 0.0f);
        }

        // Обновляем частицы
        for (Particle2D p : new ArrayList<>(TargetHudRender.particles)) {
            p.updatePosition(normalizedDelta); // Передаем нормализованное время
            if (p.opacity < 1) TargetHudRender.particles.remove(p);
        }

        // Рендерим HUD, если масштаб > 0 и есть последняя цель
        if (hudScale > 0 && lastTarget instanceof PlayerEntity) {
            int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
            int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

            context.getMatrices().push();

            // Выбор центра масштабирования в зависимости от стиля HUD
            float centerX, centerY;
            if (CONFIG.targetHudStyle.equals(TargetHUD_Style.MINI)) {
                centerX = x + 47.5f; // Центр для MiniHUD (95/2)
                centerY = y + 17.5f; // (35/2)
            } else if (CONFIG.targetHudStyle.equals(TargetHUD_Style.NORMAL)) {
                centerX = x + 68.5f; // Центр для NormalHUD (137/2)
                centerY = y + 23.75f; // (47.5/2)
            } else { // TINY
                centerX = x;
                centerY = y;
            }

            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(hudScale, hudScale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            float animationFactor = MathUtility.clamp(hudScale, 0, 1f);
            switch (CONFIG.targetHudStyle) {
                case MINI ->
                        TargetHudRender.renderMiniHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
                case TINY ->
                        TargetHudRender.renderTinyHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
                default ->
                        TargetHudRender.renderNormalHUD(context, normalizedDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
            }

            context.getMatrices().pop();
        }
    }

    public static void getTarget() {
        if (mc.currentScreen == null) {
            if (!(EntityUtils.getTargetEntity() instanceof LivingEntity)) return;
            target = (LivingEntity) EntityUtils.getTargetEntity();
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

    public enum TargetHUD_Style {
        MINI, TINY, NORMAL
    }

    public enum TargetHUD_ConfigPos {
        CONFIG_POS
    }
}
