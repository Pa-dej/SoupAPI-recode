package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.particle.Particle2D;
import me.Padej_.soupapi.reduce.ServerReduce;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TargetHudRender extends ConfigurableModule {
    public static final ArrayList<Particle2D> particles = new ArrayList<>();
    public static boolean sentParticles = false;
    public static float ticks = 0f;
    public static float hpColorAnimationProgress = 0f;
    public static float colorAnimationProgress = 0f;

    private static final String[] effectNames = {
            "absorption", "blindness", "fire_resistance", "haste", "health_boost",
            "invisibility", "jump_boost", "mining_fatigue", "poison", "regeneration",
            "resistance", "slow_falling", "slowness", "speed", "strength", "weakness", "wither"
    };

    public static void renderTinyHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);   // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);   // Верхний левый

        int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
        int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

        // Определяем текущий этап вращения фона
        float progress = colorAnimationProgress % 1.0f;
        Color topLeft, topRight, bottomRight, bottomLeft;

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
        int w = 55;
        int h = 5;
        int r = 3;
        int xOffset = 24;
        int yOffset = 3;
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2 + xOffset, y + 2 + yOffset, w + 1, h + 1, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x + xOffset, y + yOffset, w + 5, h + 5, r);
        Render2D.drawRound(context.getMatrices(), x + 0.5f + xOffset, y + 0.5f + yOffset, w + 4, h + 5, r, Render2D.injectAlpha(Color.BLACK, 180));

        // Голова игрока
        Identifier texture = mc.player.getSkinTextures().texture();
        int headScale = 25;
        if (target.isInvisible()) {
            texture = TexturesManager.ANON_SKIN;
        } else if (target instanceof PlayerEntity) {
            texture = ((AbstractClientPlayerEntity) target).getSkinTextures().texture();
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
        Render2D.drawRound(context.getMatrices(), x - 2, y + 2.5f, headScale, headScale, r, Render2D.injectAlpha(Color.BLACK, 20));
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x - 2, y + 2.5, x - 2 + headScale, y + 2.5 + headScale, r, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                p.render2D(context.getMatrices());
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x, y, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        // Полоска HP с анимацией переключения цветов
        float hpProgress = hpColorAnimationProgress % 1.0f;
        Color hpLeft, hpRight;

        if (hpProgress < 0.5f) {
            float phaseProgress = hpProgress / 0.5f;
            hpLeft = interpolateColor(c1, c3, phaseProgress);
            hpRight = interpolateColor(c3, c1, phaseProgress);
        } else {
            float phaseProgress = (hpProgress - 0.5f) / 0.5f;
            hpLeft = interpolateColor(c3, c1, phaseProgress);
            hpRight = interpolateColor(c1, c3, phaseProgress);
        }

        // Отрисовка полоски HP
        Render2D.drawGradientRound(context.getMatrices(), x + 25, y + 14.5f, 59, 2, 1, c3.darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker());
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, x + 25, y + 14.5f, (int) MathUtility.clamp((60 * (health / target.getMaxHealth())), 2, 59), 2, 1);

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        java.util.List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 25;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 4, 0);
            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
            if (ServerReduce.dontShowTargetHudItemsOverlay()) {
                context.drawItem(itemStack, 0, 0);
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += 10;
        }

        float effectXOffset = x + 25;
        float effectYOffset = y + 18;
        for (String effectName : effectNames) {
            Identifier effectId = Identifier.ofVanilla(effectName);
            StatusEffect effect = Registries.STATUS_EFFECT.get(effectId);
            if (effect != null && target.hasStatusEffect(Registries.STATUS_EFFECT.getEntry(effectId).orElse(null))) {
                context.getMatrices().push();
                context.getMatrices().translate(effectXOffset, effectYOffset, 0);
                context.getMatrices().scale(0.5f, 0.5f, 0.5f);
                context.drawTexture(RenderLayer::getGuiTextured, Identifier.ofVanilla("textures/mob_effect/" + effectName + ".png"), 0, 0, 0, 0, 18, 18, 18, 18);
                context.getMatrices().pop();
                effectXOffset += 10;
            }
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderMiniHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);
        Color c2 = Palette.getColor(0.33f);
        Color c3 = Palette.getColor(0.66f);
        Color c4 = Palette.getColor(1f);

        int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
        int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

        // Определяем текущий этап вращения фона
        float progress = colorAnimationProgress % 1.0f;
        Color topLeft, topRight, bottomRight, bottomLeft;

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

        // Градиентный фон
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, 91, 31, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, 95, 35, 7);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, 94, 34, 7, Render2D.injectAlpha(Color.BLACK, 180));

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

        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                p.render2D(context.getMatrices());
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x, y, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        // Полоска HP
        float hpProgress = hpColorAnimationProgress % 1.0f;
        Color hpLeft, hpRight;

        if (hpProgress < 0.5f) {
            float phaseProgress = hpProgress / 0.5f;
            hpLeft = interpolateColor(c1, c3, phaseProgress);
            hpRight = interpolateColor(c3, c1, phaseProgress);
        } else {
            float phaseProgress = (hpProgress - 0.5f) / 0.5f;
            hpLeft = interpolateColor(c3, c1, phaseProgress);
            hpRight = interpolateColor(c1, c3, phaseProgress);
        }

        Render2D.drawGradientRound(context.getMatrices(), x + 38, y + 25, 52, 7, 2f, c3.darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker());
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, x + 38, y + 25, (int) MathUtility.clamp((52 * (health / target.getMaxHealth())), 8, 52), 7, 2f);

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

    public static void renderNormalHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);
        Color c2 = Palette.getColor(0.33f);
        Color c3 = Palette.getColor(0.66f);
        Color c4 = Palette.getColor(1f);

        int x = context.getScaledWindowWidth() / 2 + CONFIG.targetHudOffsetX;
        int y = context.getScaledWindowHeight() / 2 - CONFIG.targetHudOffsetY;

        // Определяем текущий этап вращения фона
        float progress = colorAnimationProgress % 1.0f;
        Color topLeft, topRight, bottomRight, bottomLeft;

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

        // Градиентный фон
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
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                p.render2D(context.getMatrices());
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x, y, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        // Полоска HP
        float hpProgress = hpColorAnimationProgress % 1.0f;
        Color hpLeft, hpRight;

        if (hpProgress < 0.5f) {
            float phaseProgress = hpProgress / 0.5f;
            hpLeft = interpolateColor(c1, c3, phaseProgress);
            hpRight = interpolateColor(c3, c1, phaseProgress);
        } else {
            float phaseProgress = (hpProgress - 0.5f) / 0.5f;
            hpLeft = interpolateColor(c3, c1, phaseProgress);
            hpRight = interpolateColor(c1, c3, phaseProgress);
        }

        Render2D.drawGradientRound(context.getMatrices(), x + 48, y + 32, 85, 11, 4f, c3.darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker(), c3.darker().darker().darker().darker());
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, x + 48, y + 32, (int) MathUtility.clamp((85 * (health / target.getMaxHealth())), 8, 85), 11, 4f);

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), String.valueOf(Math.round(10.0 * health) / 10.0), x + 92f, y + 35f, Render2D.applyOpacity(Colors.WHITE, animationFactor));
        FontRenderers.sf_bold.drawString(context.getMatrices(), displayName, x + 48, y + 7, Render2D.applyOpacity(Colors.WHITE, animationFactor));

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

    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }
}
