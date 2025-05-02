package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
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

import java.awt.*;
import java.util.List;
import java.util.*;

public class PotionsHud extends ConfigurableModule {
    private static final Map<StatusEffect, Float> effectAlphas = new HashMap<>();
    private static final Map<StatusEffect, Integer> maxDurations = new HashMap<>();
    private static float currentWidth = 0f;
    private static float currentHeight = 0f;

    public static void render(DrawContext context) {
        if (mc.player == null || mc.world == null) return;

        float x = CONFIG.hudBetterPotionsHudX;
        float y = CONFIG.hudBetterPotionsHudY;
        float spacing = 19;
        float targetWidth = 0f;
        float targetHeight;

        List<StatusEffectInstance> rawEffects = new ArrayList<>(mc.player.getStatusEffects());
        rawEffects.sort(Comparator.comparing(a -> I18n.translate(a.getEffectType().value().getTranslationKey())));

        if (rawEffects.isEmpty()) {
            currentWidth = fast(currentWidth, 0, 15f);
            currentHeight = fast(currentHeight, 0, 15f);
            effectAlphas.clear();
            maxDurations.clear();
            if (currentWidth < 0.1f && currentHeight < 0.1f) return;
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
            targetWidth = Math.max(targetWidth, element.getWidth());
        }

        targetHeight = effects.size() * spacing + 4;

        // Анимируем только если есть эффекты
        if (!effects.isEmpty()) {
            currentWidth = fast(currentWidth, targetWidth, 15f);
            currentHeight = fast(currentHeight, targetHeight, 15f);
        }

        float headerHeight = 12f;
        float headerYOffset = 5f;
        int radius = 3;

        if (currentWidth > 0.1f || currentHeight > 0.1f) {
            float headerY = y - headerHeight - headerYOffset;

            // Заголовок
            Render2D.drawGradientBlurredShadow1(context.getMatrices(), x - 2.5f, headerY, currentWidth, headerHeight, 7, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
            Render2D.renderRoundedGradientRect(context.getMatrices(), TargetHudRenderer.topLeft, TargetHudRenderer.topRight, TargetHudRenderer.bottomRight, TargetHudRenderer.bottomLeft, x - 3f, headerY, currentWidth, headerHeight, radius);
            Render2D.drawRound(context.getMatrices(), x - 3f, headerY, currentWidth, headerHeight, radius, Render2D.injectAlpha(Color.BLACK, 180));

            String title = "Potions";
            float textWidth = FontRenderers.sf_bold_mini.getStringWidth(title);
            FontRenderers.sf_bold.drawString(context.getMatrices(), title,
                    x - 3f + currentWidth / 2f - textWidth / 2f,
                    headerY + 3f, 0xFFFFFFFF
            );

            // Фон под эффекты
            Render2D.drawGradientBlurredShadow1(context.getMatrices(), x - 2.5f, y + 1, currentWidth, currentHeight, 10, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
            Render2D.renderRoundedGradientRect(context.getMatrices(), TargetHudRenderer.topLeft, TargetHudRenderer.topRight, TargetHudRenderer.bottomRight, TargetHudRenderer.bottomLeft, x - 3f, y + 0.5f, currentWidth, currentHeight, radius);
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
    }

    private static String getDuration(StatusEffectInstance statusEffect) {
        if (statusEffect.isInfinite()) return "**:**";
        int duration = statusEffect.getDuration();
        int minutes = duration / 1200;
        int seconds = (duration % 1200) / 20;
        return minutes + ":" + String.format("%02d", seconds);
    }

    private static String toRoman(int number) {
        if (number < 1 || number > 3999) return String.valueOf(number);
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
            String levelPart = instance.getAmplifier() > 0 ? " " + (CONFIG.hudBetterPotionsHudToRoman ? toRoman(instance.getAmplifier() + 1) : instance.getAmplifier() + 1) : "";
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
                Render2D.renderRoundedGradientRect(context.getMatrices(), fillStart, fillEnd, fillEnd, fillStart, barX, barY, barWidth * progress, barHeight, 1);
            }
        }

        public float getWidth() {
            return 24 + 2 + textWidth;
        }

        public StatusEffect getEffect() {
            return instance.getEffectType().value();
        }
    }
}






