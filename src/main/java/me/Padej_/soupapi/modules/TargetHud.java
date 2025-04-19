package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.interpolation.EaseOutCirc;
import me.Padej_.soupapi.particle.Particle2D;
import me.Padej_.soupapi.reduce.ServerReduce;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHud extends ConfigurableModule {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static float hudScale = 0f; // Текущий масштаб HUD
    private static final float scaleSpeed = 0.2f; // Скорость анимации масштабирования
    private static int hudTimer = 0; // Таймер для задержки рендера
    public static EaseOutCirc headAnimation = new EaseOutCirc();
    public static LivingEntity target;
    private static LivingEntity lastTarget; // Сохраняем последнюю цель для рендера
    private static float displayedHealth = 0f; // Текущее отображаемое значение здоровья
    private static final float healthChangeSpeed = 0.2f; // Скорость изменения здоровья
    private static float colorAnimationProgress = 0f; // Прогресс анимации цветов фона
    private static float hpColorAnimationProgress = 0f; // Прогресс анимации цветов полоски HP
    private static final float colorAnimationSpeed = 0.015f; // Скорость вращения цветов
    private static final ArrayList<Particle2D> particles = new ArrayList<>();
    private static boolean sentParticles = false;
    static float ticks;

    private static final Timer timer = new Timer();

    public static void onTick() {
        if (!CONFIG.targetHudEnabled) return;
        headAnimation.update();

        // Обновляем состояние цели и таймер
        getTarget();
        if (target instanceof PlayerEntity) {
            if (target != lastTarget) { // Если цель сменилась
                displayedHealth = Math.min(target.getMaxHealth(), getHealth()); // Сбрасываем displayedHealth
                lastTarget = target;
            }
            hudTimer = CONFIG.targetHudRenderTime * 20; // Сбрасываем таймер при наличии цели
            target = null;
        } else if (hudTimer > 0) {
            hudTimer--; // Уменьшаем таймер, если цели нет
        }

        // Обновляем прогресс анимации цветов
        colorAnimationProgress = (colorAnimationProgress + colorAnimationSpeed) % 1.0f;
        hpColorAnimationProgress = (hpColorAnimationProgress + colorAnimationSpeed / 2) % 1.0f;

        // Плавная интерполяция здоровья
        if (lastTarget instanceof PlayerEntity) {
            float targetHealth = Math.min(lastTarget.getMaxHealth(), getHealth());
            displayedHealth = MathHelper.lerp(healthChangeSpeed, displayedHealth, targetHealth);
        }
    }

    public static void render(DrawContext context, RenderTickCounter renderTickCounter) {
        if (!CONFIG.targetHudEnabled) return;
        getTarget();
        float tickDelta = renderTickCounter.getTickDelta(true);

        // Плавная интерполяция масштаба HUD
        if (hudTimer > 0) {
            hudScale = MathHelper.lerp(tickDelta * scaleSpeed, hudScale, 1.0f);
        } else {
            hudScale = MathHelper.lerp(tickDelta * scaleSpeed, hudScale, 0.0f);
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
            } else { // Заглушка
                centerX = x;
                centerY = y;
            }

            context.getMatrices().translate(centerX, centerY, 0);
            context.getMatrices().scale(hudScale, hudScale, 1f);
            context.getMatrices().translate(-centerX, -centerY, 0);

            float animationFactor = MathUtility.clamp(hudScale, 0, 1f);
            if (CONFIG.targetHudStyle.equals(TargetHUD_Style.MINI)) {
                renderMiniHUD(context, tickDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
            } else if (CONFIG.targetHudStyle.equals(TargetHUD_Style.NORMAL)) {
                renderNormalHUD(context, tickDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
            } else {
                renderNormalHUD(context, tickDelta, displayedHealth, animationFactor, (PlayerEntity) lastTarget);
            }

            context.getMatrices().pop();
        }
    }

    private static void renderMiniHUD(DrawContext context, float tickDelta, float health, float animationFactor, PlayerEntity target) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, tickDelta)) / 8f;

        Color c1 = Palette.getColor(0f);   // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);   // Верхний левый

        int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
        int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

        // Определяем текущий этап вращения фона на основе colorAnimationProgress (0.0 - 1.0)
        float progress = colorAnimationProgress % 1.0f; // Зацикливаем прогресс
        Color topLeft, topRight, bottomRight, bottomLeft;

        // Разделяем анимацию фона на 4 фазы (вращение по часовой стрелке)
        if (progress < 0.25f) {
            float phaseProgress = progress / 0.25f;
            topLeft = interpolateColor(c1, c2, phaseProgress);
            topRight = interpolateColor(c2, c3, phaseProgress);
            bottomRight = interpolateColor(c3, c4, phaseProgress);
            bottomLeft = interpolateColor(c4, c1, phaseProgress);
        } else if (progress < 0.5f) {
            float phaseProgress = (progress - 0.25f) / 0.25f;
            topLeft = interpolateColor(c2, c3, phaseProgress);
            topRight = interpolateColor(c3, c4, phaseProgress);
            bottomRight = interpolateColor(c4, c1, phaseProgress);
            bottomLeft = interpolateColor(c1, c2, phaseProgress);
        } else if (progress < 0.75f) {
            float phaseProgress = (progress - 0.5f) / 0.25f;
            topLeft = interpolateColor(c3, c4, phaseProgress);
            topRight = interpolateColor(c4, c1, phaseProgress);
            bottomRight = interpolateColor(c1, c2, phaseProgress);
            bottomLeft = interpolateColor(c2, c3, phaseProgress);
        } else {
            float phaseProgress = (progress - 0.75f) / 0.25f;
            topLeft = interpolateColor(c4, c1, phaseProgress);
            topRight = interpolateColor(c1, c2, phaseProgress);
            bottomRight = interpolateColor(c2, c3, phaseProgress);
            bottomLeft = interpolateColor(c3, c4, phaseProgress);
        }

        // Градиентный фон с вращением цветов
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, 91, 31, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, 95, 35, 7);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, 94, 34, 7, Render2D.injectAlpha(Color.BLACK, 180));

        // Остальной код для головы и текстур остается без изменений
        Identifier texture = mc.player.getSkinTextures().texture();
        String displayName = "Invisible";
        if (target.isInvisible()) {
            texture = TexturesManager.ANON_SKIN;
        } else if (target instanceof PlayerEntity) {
            texture = ((AbstractClientPlayerEntity) target).getSkinTextures().texture();
            displayName = target.getName().getString();
        }
        context.getMatrices().push();
        context.getMatrices().translate(x + 2.5 + 15, y + 2.5 + 15, 0);
        context.getMatrices().scale(1 - hurtPercent / 20f, 1 - hurtPercent / 20f, 1f);
        context.getMatrices().translate(-(x + 2.5 + 15), -(y + 2.5 + 15), 0);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        Render2D.drawRound(context.getMatrices(), x + 2.5f, y + 2.5f, 30, 30, 5, Render2D.injectAlpha(Color.BLACK, 20));
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x + 2.5, y + 2.5, x + 2.5 + 30, y + 2.5 + 30, 5, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 2.5, y + 2.5, 30, 30, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 2.5, y + 2.5, 30, 30, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles)
            if (p.opacity > 4)
                p.render2D(context.getMatrices());

        if (timer.passedMs(1000 / 60)) {
            ticks += 0.1f;
            for (final Particle2D p : particles) {
                p.updatePosition();
                if (p.opacity < 1) particles.remove(p);
            }
            timer.reset();
        }

        final ArrayList<Particle2D> removeList = new ArrayList<>();
        for (final Particle2D p : particles) {
            if (p.opacity <= 1) {
                removeList.add(p);
            }
        }

        for (final Particle2D p : removeList) {
            particles.remove(p);
        }

        if ((target.hurtTime == 9 && !sentParticles)) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x, y, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        // Полоска HP с анимацией переключения цветов (инверсия)
        float hpProgress = hpColorAnimationProgress % 1.0f; // Зацикливаем прогресс для HP
        Color hpLeft, hpRight;

        // Инверсия цветов между c1 и c3
        if (hpProgress < 0.5f) {
            // Первая половина цикла: c1 слева, c3 справа
            float phaseProgress = hpProgress / 0.5f;
            hpLeft = interpolateColor(c1, c3, phaseProgress);
            hpRight = interpolateColor(c3, c1, phaseProgress);
        } else {
            // Вторая половина цикла: c3 слева, c1 справа
            float phaseProgress = (hpProgress - 0.5f) / 0.5f;
            hpLeft = interpolateColor(c3, c1, phaseProgress);
            hpRight = interpolateColor(c1, c3, phaseProgress);
        }

        // Отрисовка полоски HP
        Render2D.drawGradientRound(context.getMatrices(), x + 38, y + 25, 52, 7, 2f, c3.darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker());
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, x + 38, y + 25, (int) MathUtility.clamp((52 * (health / target.getMaxHealth())), 8, 52), 7, 2f);

        // Текст и предметы остаются без изменений
        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), String.valueOf(Math.round(10.0 * health) / 10.0), x + 65, y + 27f, Render2D.applyOpacity(Colors.WHITE, animationFactor));
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), displayName, x + 38, y + 5, Render2D.applyOpacity(Colors.WHITE, animationFactor));

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        java.util.List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 38;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 13, 0);
            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
            if (ServerReduce.dontShowTargetHudItemsOverlay()) {
                context.drawItem(itemStack, 0, 0);
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += 9;
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    private static void renderNormalHUD(DrawContext context, float tickDelta, float health, float animationFactor, PlayerEntity target) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, tickDelta)) / 8f;

        Color c1 = Palette.getColor(0f);   // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);   // Верхний левый

        int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
        int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

        // Определяем текущий этап вращения фона на основе colorAnimationProgress (0.0 - 1.0)
        float progress = colorAnimationProgress % 1.0f; // Зацикливаем прогресс
        Color topLeft, topRight, bottomRight, bottomLeft;

        // Разделяем анимацию фона на 4 фазы (вращение по часовой стрелке)
        if (progress < 0.25f) {
            float phaseProgress = progress / 0.25f;
            topLeft = interpolateColor(c1, c2, phaseProgress);
            topRight = interpolateColor(c2, c3, phaseProgress);
            bottomRight = interpolateColor(c3, c4, phaseProgress);
            bottomLeft = interpolateColor(c4, c1, phaseProgress);
        } else if (progress < 0.5f) {
            float phaseProgress = (progress - 0.25f) / 0.25f;
            topLeft = interpolateColor(c2, c3, phaseProgress);
            topRight = interpolateColor(c3, c4, phaseProgress);
            bottomRight = interpolateColor(c4, c1, phaseProgress);
            bottomLeft = interpolateColor(c1, c2, phaseProgress);
        } else if (progress < 0.75f) {
            float phaseProgress = (progress - 0.5f) / 0.25f;
            topLeft = interpolateColor(c3, c4, phaseProgress);
            topRight = interpolateColor(c4, c1, phaseProgress);
            bottomRight = interpolateColor(c1, c2, phaseProgress);
            bottomLeft = interpolateColor(c2, c3, phaseProgress);
        } else {
            float phaseProgress = (progress - 0.75f) / 0.25f;
            topLeft = interpolateColor(c4, c1, phaseProgress);
            topRight = interpolateColor(c1, c2, phaseProgress);
            bottomRight = interpolateColor(c2, c3, phaseProgress);
            bottomLeft = interpolateColor(c3, c4, phaseProgress);
        }

        // Градиентный фон с вращением цветов
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, 133, 43, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, 137, 47.5f, 9);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, 136, 46, 9, Render2D.injectAlpha(Color.BLACK, 220));

        // Голова игрока
        Identifier texture = mc.player.getSkinTextures().texture();
        String displayName = "Invisible";
        if (target.isInvisible()) {
            texture = TexturesManager.ANON_SKIN;
        } else if (target instanceof PlayerEntity) {
            texture = ((AbstractClientPlayerEntity) target).getSkinTextures().texture();
            displayName = target.getName().getString();
        }
        context.getMatrices().push();
        context.getMatrices().translate(x + 3.5f + 20, y + 3.5f + 20, 0);
        context.getMatrices().scale(1 - hurtPercent / 15f, 1 - hurtPercent / 15f, 1f);
        context.getMatrices().translate(-(x + 3.5f + 20), -(y + 3.5f + 20), 0);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        Render2D.drawRound(context.getMatrices(), x + 3.5f, y + 3.5f, 40, 40, 7, Render2D.injectAlpha(Color.BLACK, 20));
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x + 3.5f, y + 3.5f, x + 3.5f + 40, y + 3.5f + 40, 7, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles)
            if (p.opacity > 4)
                p.render2D(context.getMatrices());

        if (timer.passedMs(1000 / 60)) {
            ticks += 0.1f;
            for (final Particle2D p : particles) {
                p.updatePosition();
                if (p.opacity < 1) particles.remove(p);
            }
            timer.reset();
        }

        final ArrayList<Particle2D> removeList = new ArrayList<>();
        for (final Particle2D p : particles) {
            if (p.opacity <= 1) {
                removeList.add(p);
            }
        }

        for (final Particle2D p : removeList) {
            particles.remove(p);
        }

        if ((target.hurtTime == 9 && !sentParticles)) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x, y, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        // Полоска HP с анимацией переключения цветов (инверсия)
        float hpProgress = hpColorAnimationProgress % 1.0f; // Зацикливаем прогресс для HP
        Color hpLeft, hpRight;

        // Инверсия цветов между c1 и c3
        if (hpProgress < 0.5f) {
            // Первая половина цикла: c1 слева, c3 справа
            float phaseProgress = hpProgress / 0.5f;
            hpLeft = interpolateColor(c1, c3, phaseProgress);
            hpRight = interpolateColor(c3, c1, phaseProgress);
        } else {
            // Вторая половина цикла: c3 слева, c1 справа
            float phaseProgress = (hpProgress - 0.5f) / 0.5f;
            hpLeft = interpolateColor(c3, c1, phaseProgress);
            hpRight = interpolateColor(c1, c3, phaseProgress);
        }

        // Отрисовка полоски HP
        Render2D.drawGradientRound(context.getMatrices(), x + 48, y + 32, 85, 11, 4f, c3.darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker());
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, x + 48, y + 32, (int) MathUtility.clamp((85 * (health / target.getMaxHealth())), 8, 85), 11, 4f);

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), String.valueOf(Math.round(10.0 * health) / 10.0), x + 92f, y + 35f, Render2D.applyOpacity(Colors.WHITE, animationFactor));
        FontRenderers.sf_bold.drawString(context.getMatrices(), displayName, x + 48, y + 7, Render2D.applyOpacity(Colors.WHITE, animationFactor));

        // Предметы инвентаря
        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 48;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 15, 0);
            context.getMatrices().scale(0.75f, 0.75f, 0.75f);
            if (ServerReduce.dontShowTargetHudItemsOverlay()) {
                context.drawItem(itemStack, 0, 0);
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += 12;
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
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

    // Вспомогательная функция для интерполяции цветов
    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }

    public enum TargetHUD_Style {
        MINI, NORMAL
    }

    public enum TargetHUD_ConfigPos {
        CONFIG_POS
    }
}
