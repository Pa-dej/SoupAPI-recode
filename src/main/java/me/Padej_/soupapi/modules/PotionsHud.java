package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;
import java.util.*;

import static me.Padej_.soupapi.render.WatermarkRenderer.colorAnimationProgress;

public class PotionsHud extends ConfigurableModule {
    private static final Map<StatusEffect, Float> effectAlphas = new HashMap<>();
    private static final Map<StatusEffect, Integer> maxDurations = new HashMap<>();
    private static float currentWidth = 0f;
    private static float currentHeight = 0f;

    public static void render(DrawContext context) {
        if (mc.player == null || mc.world == null) return;

        Color c1 = Palette.getColor(0.0f);
        Color c2 = Palette.getColor(0.33f);
        Color c3 = Palette.getColor(0.66f);
        Color c4 = Palette.getColor(1.0f);

        float x = CONFIG.hudBetterPotionsHudX;
        float y = CONFIG.hudBetterPotionsHudY;
        float spacing = 19;
        float width = 0;
        float height;

        List<StatusEffectInstance> rawEffects = new ArrayList<>(mc.player.getStatusEffects());
        rawEffects.sort(Comparator.comparing(a -> I18n.translate(a.getEffectType().value().getTranslationKey())));

        if (rawEffects.isEmpty()) {
            currentWidth = fast(currentWidth, 0, 15f);
            currentHeight = fast(currentHeight, 0, 15f);
            effectAlphas.clear();
            maxDurations.clear();
            if (currentWidth < 0.1f && currentHeight < 0.1f) return;
        } else {
            currentHeight -= 0.5f;
            currentWidth += 1;
        }

        float progress = colorAnimationProgress % 1.0f;
        Color topLeft, topRight, bottomRight, bottomLeft;

        if (progress < 0.25f) {
            float phase = progress / 0.25f;
            topLeft = interpolateColor(c1, c2, phase);
            topRight = interpolateColor(c2, c3, phase);
            bottomRight = interpolateColor(c3, c4, phase);
            bottomLeft = interpolateColor(c4, c1, phase);
        } else if (progress < 0.5f) {
            float phase = (progress - 0.25f) / 0.25f;
            topLeft = interpolateColor(c2, c3, phase);
            topRight = interpolateColor(c3, c4, phase);
            bottomRight = interpolateColor(c4, c1, phase);
            bottomLeft = interpolateColor(c1, c2, phase);
        } else if (progress < 0.75f) {
            float phase = (progress - 0.5f) / 0.25f;
            topLeft = interpolateColor(c3, c4, phase);
            topRight = interpolateColor(c4, c1, phase);
            bottomRight = interpolateColor(c1, c2, phase);
            bottomLeft = interpolateColor(c2, c3, phase);
        } else {
            float phase = (progress - 0.75f) / 0.25f;
            topLeft = interpolateColor(c4, c1, phase);
            topRight = interpolateColor(c1, c2, phase);
            bottomRight = interpolateColor(c2, c3, phase);
            bottomLeft = interpolateColor(c3, c4, phase);
        }

        maxDurations.keySet().removeIf(effect -> {
            RegistryEntry<StatusEffect> entry = Registries.STATUS_EFFECT.getEntry(effect);
            return mc.player.getStatusEffect(entry) == null;
        });

        List<EffectElement> effects = new ArrayList<>();
        for (StatusEffectInstance instance : rawEffects) {
            StatusEffect effect = instance.getEffectType().value();
            maxDurations.putIfAbsent(effect, instance.getDuration());
            effects.add(new EffectElement(instance));
        }

        for (EffectElement element : effects) {
            StatusEffect effect = element.getEffect();
            float currentAlpha = effectAlphas.getOrDefault(effect, 0f);
            float newAlpha = fast(currentAlpha, 255f, 10f);
            effectAlphas.put(effect, newAlpha);
        }

        for (EffectElement element : effects) {
            width = Math.max(width, element.getWidth());
        }

        height = effects.size() * spacing + 4;

        float headerHeight = 12f;
        float headerYOffset = 5f;
        int radius = 3;

        if (currentWidth > 0.1f || currentHeight > 0.1f) {
            float headerY = y - headerHeight - headerYOffset;

            Render2D.drawGradientBlurredShadow1(context.getMatrices(), x - 2.5f, headerY, currentWidth, headerHeight, 7, bottomLeft, bottomRight, topRight, topLeft);
            Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x - 3f, headerY, currentWidth, headerHeight, radius);
            Render2D.drawRound(context.getMatrices(), x - 3f, headerY, currentWidth, headerHeight, radius, Render2D.injectAlpha(Color.BLACK, 180));

            String title = "Potions";
            float textWidth = FontRenderers.sf_bold_mini.getStringWidth(title);
            FontRenderers.sf_bold.drawString(context.getMatrices(), title,
                    x - 3f + currentWidth / 2f - textWidth / 2f,
                    headerY + 3f, 0xFFFFFFFF
            );

            // Основной фон под эффекты
            Render2D.drawGradientBlurredShadow1(context.getMatrices(), x - 2.5f, y + 1, currentWidth, currentHeight, 10, bottomLeft, bottomRight, topRight, topLeft);
            Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x - 3f, y + 0.5f, currentWidth, currentHeight, radius);
            Render2D.drawRound(context.getMatrices(), x - 3f, y + 0.5f, currentWidth, currentHeight, radius, Render2D.injectAlpha(Color.BLACK, 180));

            context.getMatrices().push();
            context.getMatrices().translate(x, headerY + 1.5f, 0);
            context.getMatrices().scale(0.5f, 0.5f, 0.5f);
            context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.GUI_POTION, 0, 0, 0, 0, 16, 16, 1024, 1024, 1024, 1024);
            context.getMatrices().pop();
        }

        for (int i = 0; i < effects.size(); i++) {
            EffectElement e = effects.get(i);
            float alpha = effectAlphas.getOrDefault(e.getEffect(), 255f);
            e.render(context, x - 2, y + 2 + i * spacing, alpha);
        }

        if (!effects.isEmpty()) {
            currentWidth = fast(currentWidth, width, 15f);
            currentHeight = fast(currentHeight, height, 15f);
        }
    }

    private static String getDuration(StatusEffectInstance statusEffect) {
        if (statusEffect.isInfinite()) return "**:**";
        int duration = statusEffect.getDuration();
        int minutes = duration / 1200;
        int seconds = (duration % 1200) / 20;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private static String toRoman(int number) {
        if (number < 1 || number > 3999) {
            return String.valueOf(number);
        }

        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (number >= values[i]) {
                result.append(symbols[i]);
                number -= values[i];
            }
        }
        return result.toString();
    }

    private static float fast(float end, float start, float multiple) {
        float clampedDelta = MathUtility.clamp((1f / mc.getCurrentFps()) * multiple, 0f, 1f);
        return (1f - clampedDelta) * end + clampedDelta * start;
    }

    private static class EffectElement {
        private final StatusEffectInstance instance;
        private final String displayText;
        private final float textWidth;

        public EffectElement(StatusEffectInstance instance) {
            this.instance = instance;
            StatusEffect effect = instance.getEffectType().value();
            String name = I18n.translate(effect.getTranslationKey());

            String levelPart = "";
            if (instance.getAmplifier() > 0) {
                int level = instance.getAmplifier() + 1;
                levelPart = " " + (CONFIG.hudBetterPotionsHudToRoman ? toRoman(level) : level);
            }

            this.displayText = name + levelPart;
            this.textWidth = FontRenderers.sf_bold_mini.getStringWidth(displayText);
        }

        public void render(DrawContext context, float x, float y, float alpha) {
            int textColor = new Color(255, 255, 255, (int) alpha).getRGB();
            float scale = 0.8f;

            context.getMatrices().push();
            context.getMatrices().translate(x + (1 + scale), y + (1 + scale), 0);
            context.getMatrices().scale(scale, scale, 0);
            context.drawSpriteStretched(RenderLayer::getGuiTextured, mc.getStatusEffectSpriteManager().getSprite(instance.getEffectType()), 0, 0, 18, 18);
            context.getMatrices().pop();

            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), displayText, x + 20, y + 2, textColor);
            FontRenderers.sf_bold_mini.drawString(context.getMatrices(), "§7" + getDuration(instance), x + 20, y + 9, textColor);

            if (!instance.isInfinite()) {
                StatusEffect effect = instance.getEffectType().value();
                int maxDuration = maxDurations.getOrDefault(effect, instance.getDuration());
                float progress = MathUtility.clamp(instance.getDuration() / (float) maxDuration, 0f, 1f);

                float barX = x + 20;
                float barY = y + 15;
                float barWidth = textWidth;
                float barHeight = 1.5f;

                Color fillStart = Palette.getColor(0f).darker();
                Color fillEnd = Palette.getColor(0.33f).darker();
                Color background = new Color(0, 0, 0, (int) (alpha * 0.3f));


                Render2D.drawGradientBlurredShadow1(context.getMatrices(), barX, barY, barWidth * progress, barHeight, 3, fillStart, fillEnd, fillStart, fillEnd);
                Render2D.drawRound(context.getMatrices(), barX, barY, barWidth, barHeight, 1, background);
                Render2D.renderRoundedGradientRect(context.getMatrices(), fillStart, fillEnd, fillEnd, fillStart,
                        barX, barY, barWidth * progress, barHeight, 1);
            }
        }

        public float getWidth() {
            return 18 + 2 + textWidth;
        }

        public StatusEffect getEffect() {
            return instance.getEffectType().value();
        }
    }

    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }
}




