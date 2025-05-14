package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.particle.Particle2D;
import me.Padej_.soupapi.reduce.ModuleSupressor;
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
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL40C;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.Padej_.soupapi.modules.TargetHud.smoothedScreenX;
import static me.Padej_.soupapi.modules.TargetHud.smoothedScreenY;

public class TargetHudRenderer extends ConfigurableModule {
    public static final ArrayList<Particle2D> particles = new ArrayList<>();
    public static boolean sentParticles = false;
    public static float ticks = 0f;
    public static float hpColorAnimationProgress = 0f;
    public static float colorAnimationProgress = 0f;
    public static Color topLeft = new Color(0xFFFFFFFF);
    public static Color topRight = new Color(0xFFFFFFFF);
    public static Color bottomLeft = new Color(0xFFFFFFFF);
    public static Color bottomRight = new Color(0xFFFFFFFF);

    private static final String[] effectNames = {
            "absorption", "blindness", "fire_resistance", "haste", "health_boost",
            "invisibility", "jump_boost", "mining_fatigue", "poison", "regeneration",
            "resistance", "slow_falling", "slowness", "speed", "strength", "weakness", "wither"
    };

    public static void renderTinyHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target, int x, int y, Vec3d screenPos) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = TargetHudRenderer.topLeft;
        Color c3 = TargetHudRenderer.bottomRight;
        Color c4 = TargetHudRenderer.bottomLeft;

        // Градиентный фон с вращением цветов
        int w = 90;
        int h = 29;
        int r = 3;
        int xOffset = -4;
        int yOffset = 0;
        Render2D.drawRound(context.getMatrices(), x + xOffset, y + 0.5f + yOffset, w, h, r, Palette.getBackColor());

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
        Render2D.drawRound(context.getMatrices(), x - 2, y + 2.5f, headScale, headScale, r, Palette.getBackColor());
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x - 2, y + 2.5, x - 2 + headScale, y + 2.5 + headScale, r, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                float depthFactor = CONFIG.targetHudFollow ? (float) screenPos.z : 1.0f;
                p.render2D(context.getMatrices(), smoothedScreenX, smoothedScreenY, depthFactor);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x - smoothedScreenX, y - smoothedScreenY, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c, CONFIG.targetHudFollow);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        int barX = x + 25;
        int barY = y + 15;
        int barHeight = 2;
        float healthBarWidth = 59;
        int innerBarWidth = (int) MathUtility.clamp((healthBarWidth * (health / target.getMaxHealth())), 8, healthBarWidth);
        Color background = new Color(0x424242);

        Render2D.drawGradientBlurredShadow1(context.getMatrices(), barX, barY, innerBarWidth, barHeight, 4, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.drawGradientRound(context.getMatrices(), barX, barY, healthBarWidth - 1, barHeight, 2, background, background, background, background);
        Render2D.renderRoundedGradientRect(context.getMatrices(), c4, c3, c3, c4, barX, barY, innerBarWidth, barHeight, 2);

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        java.util.List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 25;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 4, 0);
            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
            context.drawItem(itemStack, 0, 0);
            if (!ModuleSupressor.disableItemOverlay()) {
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

    public static void renderMiniHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target, int x, int y, Vec3d screenPos) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        // Градиентный фон
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, 91, 31, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, 95, 35, 7);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, 94, 34, 7, Palette.getBackColor());

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
        Render2D.drawRound(context.getMatrices(), x + 2.5f, y + 2.5f, 30, 30, 5, Palette.getBackColor());
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x + 2.5, y + 2.5, x + 2.5 + 30, y + 2.5 + 30, 5, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 2.5, y + 2.5, 30, 30, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 2.5, y + 2.5, 30, 30, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();

        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                float depthFactor = CONFIG.targetHudFollow ? MathHelper.clamp(1.0f - (float) screenPos.z, 0.1f, 1.0f) : 1.0f;
                p.render2D(context.getMatrices(), smoothedScreenX, smoothedScreenY, depthFactor);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x - smoothedScreenX, y - smoothedScreenY, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c, CONFIG.targetHudFollow);
                ;
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

        FontRenderers.sf_bold_mini.drawCenteredString(context.getMatrices(), String.valueOf(Math.round(10.0 * health) / 10.0), x + 65, y + 27f, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));
        FontRenderers.sf_bold_mini.drawString(context.getMatrices(), displayName, x + 38, y + 5, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 38;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 13, 0);
            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
            context.drawItem(itemStack, 0, 0);
            if (!ModuleSupressor.disableItemOverlay()) {
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += 9;
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderNormalHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target, int x, int y, Vec3d screenPos) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        // Градиентный фон
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, 133, 43, 20, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, 137, 47.5f, 9);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, 136, 46, 9, Palette.getBackColor());

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
        Render2D.drawRound(context.getMatrices(), x + 3.5f, y + 3.5f, 40, 40, 7, Palette.getBackColor());
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x + 3.5f, y + 3.5f, x + 3.5f + 40, y + 3.5f + 40, 7, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                float depthFactor = CONFIG.targetHudFollow ? MathHelper.clamp(1.0f - (float) screenPos.z, 0.1f, 1.0f) : 1.0f;
                p.render2D(context.getMatrices(), smoothedScreenX, smoothedScreenY, depthFactor);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x - smoothedScreenX, y - smoothedScreenY, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c, CONFIG.targetHudFollow);
                ;
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

        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), String.valueOf(Math.round(10.0 * health) / 10.0), x + 92f, y + 35f, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));
        FontRenderers.sf_bold.drawString(context.getMatrices(), displayName, x + 48, y + 7, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 48;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, y + 15, 0);
            context.getMatrices().scale(0.75f, 0.75f, 0.75f);
            context.drawItem(itemStack, 0, 0);
            if (!ModuleSupressor.disableItemOverlay()) {
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += 12;
        }
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void renderAresHUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target, int x, int y, Vec3d screenPos) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = Palette.getColor(0f);
        Color c3 = Palette.getColor(0.66f);

        Identifier texture = mc.player.getSkinTextures().texture();
        String displayName = "Invisible";
        if (target.isInvisible()) {
            texture = TexturesManager.ANON_SKIN;
        } else if (target instanceof PlayerEntity) {
            texture = ((AbstractClientPlayerEntity) target).getSkinTextures().texture();
            displayName = target.getName().getString();
        }

        int textWidth = (int) FontRenderers.sf_bold_17.getStringWidth(displayName);
        int minWidth = 120;
        int padding = 50;
        int totalWidthBackground = Math.max(minWidth, textWidth + padding);
        int healthBarWidth = totalWidthBackground - 52;

        // Градиентный фон
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, totalWidthBackground, 46, 7, Palette.getBackColor());

        float headScale = 20;
        context.getMatrices().push();
        context.getMatrices().translate(x + 3.5f + headScale, y + 3.5f + headScale, 0);
        context.getMatrices().scale(1 - hurtPercent / 15f, 1 - hurtPercent / 15f, 1f);
        context.getMatrices().translate(-(x + 3.5f + headScale), -(y + 3.5f + headScale), 0);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        Render2D.drawRound(context.getMatrices(), x + 3.5f, y + 3.5f, 40, 40, 5, Palette.getBackColor());
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x + 3.5f, y + 3.5f, x + 3.5f + headScale * 2, y + 3.5f + headScale * 2, 5, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, headScale * 2, headScale * 2, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, headScale * 2, headScale * 2, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                float depthFactor = CONFIG.targetHudFollow ? MathHelper.clamp(1.0f - (float) screenPos.z, 0.1f, 1.0f) : 1.0f;
                p.render2D(context.getMatrices(), smoothedScreenX, smoothedScreenY, depthFactor);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x - smoothedScreenX, y - smoothedScreenY, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c, CONFIG.targetHudFollow);
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

        int barX = x + 48;
        int barY = y + 32;
        int barHeight = 9;
        int innerBarWidth = (int) MathUtility.clamp((healthBarWidth * (health / target.getMaxHealth())), 8, healthBarWidth);
        Color background = Palette.getBackColor().darker().darker().darker();

        Render2D.drawGradientRound(context.getMatrices(), barX, barY, healthBarWidth - 1, barHeight, 2, background, background, background, background);
        Render2D.drawGradientBlurredShadow1(context.getMatrices(), barX, barY, innerBarWidth, barHeight, 4, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), hpLeft, hpRight, hpRight, hpLeft, barX, barY, innerBarWidth, barHeight, 2);

        FontRenderers.sf_bold_17.drawString(context.getMatrices(), displayName, x + 48, y + 7, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));
        FontRenderers.sf_bold.drawString(context.getMatrices(), "HP: " + Math.round(10.0 * health) / 10.0, x + 48, y + 20, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }


    public static void renderAlt_1_HUD(DrawContext context, float normalizedDelta, float health, float animationFactor, PlayerEntity target, int x, int y, Vec3d screenPos) {
        float hurtPercent = (Render2D.interpolateFloat(MathUtility.clamp(target.hurtTime == 0 ? 0 : target.hurtTime + 1, 0, 10), target.hurtTime, normalizedDelta)) / 8f;

        Color c1 = TargetHudRenderer.topLeft;
        Color c3 = TargetHudRenderer.bottomRight;
        Color c4 = TargetHudRenderer.bottomLeft;

        // Градиентный фон
        int w = 90;
        int h = 33;
        int r = 3;
        int xOffset = -5;
        int yOffset = 0;
        Render2D.drawRound(context.getMatrices(), x + xOffset, y + 0.5f + yOffset, w, h, r, Palette.getBackColor());

        // Голова игрока
        Identifier texture = mc.player.getSkinTextures().texture();
        String displayName = "Invisible";
        int headScale = 20;
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
        Render2D.drawRound(context.getMatrices(), x - 2, y + 2.5f, headScale, headScale, r, Palette.getBackColor());
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), animationFactor, animationFactor, animationFactor, animationFactor, x - 2, y + 2.5, x - 2 + headScale, y + 2.5 + headScale, r, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f - hurtPercent / 2, 1f - hurtPercent / 2, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x - 2, y + 2.5, headScale, headScale, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();

        // Партиклы
        for (final Particle2D p : particles) {
            if (p.opacity > 4) {
                float depthFactor = CONFIG.targetHudFollow ? (float) screenPos.z : 1.0f;
                p.render2D(context.getMatrices(), smoothedScreenX, smoothedScreenY, depthFactor);
            }
        }

        if (target.hurtTime == 9 && !sentParticles) {
            for (int i = 0; i <= 6; i++) {
                final Particle2D p = new Particle2D();
                final Color c = Particle2D.mixColors(c1, c3, (Math.sin(ticks + x * 0.4f + i) + 1) * 0.5f);
                p.init(x - smoothedScreenX, y - smoothedScreenY, MathUtility.random(-3f, 3f), MathUtility.random(-3f, 3f), 20, c, CONFIG.targetHudFollow);
                particles.add(p);
            }
            sentParticles = true;
        }

        if (target.hurtTime == 8) sentParticles = false;

        int barX = x - 2;
        int barY = y + 27;
        int barHeight = 2;
        float healthBarWidth = 84;
        int innerBarWidth = (int) MathUtility.clamp((healthBarWidth * (health / target.getMaxHealth())), 8, healthBarWidth);
        Color background = new Color(0x424242);

        FontRenderers.sf_bold_12.drawString(context.getMatrices(), displayName, x + 20, y + 6, Render2D.applyOpacity(Palette.getTextColor(), animationFactor));

        Render2D.drawGradientBlurredShadow1(context.getMatrices(), barX, barY, innerBarWidth, barHeight, 4, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.drawGradientRound(context.getMatrices(), barX, barY, healthBarWidth - 1, barHeight, 1, background, background, background, background);
        Render2D.renderRoundedGradientRect(context.getMatrices(), c4, c3, c3, c4, barX, barY, innerBarWidth, barHeight, 1);

        RenderSystem.setShaderColor(1f, 1f, 1f, animationFactor);
        java.util.List<ItemStack> armor = target.getInventory().armor;
        ItemStack[] items = new ItemStack[]{target.getMainHandStack(), armor.get(3), armor.get(2), armor.get(1), armor.get(0), target.getOffHandStack()};

        float xItemOffset = x + 20;
        float yItemOffset = y + 12;
        float itemGap = 10;
        float itemScale = 0.5f;
        for (ItemStack itemStack : items) {
            context.getMatrices().push();
            context.getMatrices().translate(xItemOffset, yItemOffset, 0);
            context.getMatrices().scale(itemScale, itemScale, itemScale);
            context.drawItem(itemStack, 0, 0);
            if (!ModuleSupressor.disableItemOverlay()) {
                context.drawStackOverlay(mc.textRenderer, itemStack, 0, 0);
            }
            context.getMatrices().pop();
            xItemOffset += itemGap;
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
