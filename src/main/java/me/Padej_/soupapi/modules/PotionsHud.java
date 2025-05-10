package me.Padej_.soupapi.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.*;

public class PotionsHud extends ConfigurableModule {
    private static final Map<StatusEffect, Float> effectAlphas = new HashMap<>();
    private static final Map<StatusEffect, Integer> maxDurations = new HashMap<>();
    private static final Map<String, String> enUsTranslations = new HashMap<>();

    private static float currentWidth = 0f;
    private static float currentHeight = 0f;

    static {
        ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
        Identifier enUsId = Identifier.of("lang/en_us.json");

        try (InputStream input = resourceManager.getResource(enUsId).get().getInputStream()) {
            String json = new String(input.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject langJson = JsonParser.parseString(json).getAsJsonObject();

            for (Map.Entry<String, JsonElement> entry : langJson.entrySet()) {
                enUsTranslations.put(entry.getKey(), entry.getValue().getAsString());
            }
        } catch (Exception e) {
            System.err.println("[PotionsHud] Failed to load en_us.json");
            e.printStackTrace();
        }
    }

    public static void render(DrawContext context) {
        if (mc.player == null || mc.world == null) return;

        float x = CONFIG.hudBetterPotionsHudX;
        float y = CONFIG.hudBetterPotionsHudY;
        float spacing = 19;
        float targetWidth = 0f;
        float targetHeight;

        List<StatusEffectInstance> rawEffects = new ArrayList<>(mc.player.getStatusEffects());
        rawEffects.sort(Comparator.comparing(a -> {
            String key = a.getEffectType().value().getTranslationKey();
            return enUsTranslations.getOrDefault(key, key);
        }));

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
            targetWidth = Math.max(targetWidth, Math.max(element.getWidth(), 80f));
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
            MatrixStack matrices = context.getMatrices();
            Render2D.drawGradientBlurredShadow1(matrices, x - 2.5f, headerY, currentWidth, headerHeight, 5, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
            Render2D.drawRound(matrices, x - 3f, headerY, currentWidth, headerHeight, radius, Render2D.injectAlpha(new Color(0x181a29), 220));

            String title = "Potions";
            float textWidth = FontRenderers.sf_bold.getStringWidth(title);
            FontRenderers.sf_bold.drawString(matrices, title,
                    x - 3f + currentWidth / 2f - textWidth / 2f,
                    headerY + 3f, Palette.getTextColor()
            );

            // Фон под эффекты
            Render2D.drawGradientBlurredShadow1(matrices, x - 2.5f, y + 1, currentWidth, currentHeight, 5, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
            Render2D.drawRound(matrices, x - 3f, y + 0.5f, currentWidth, currentHeight, radius, Render2D.injectAlpha(new Color(0x181a29), 220));

            matrices.push();
            matrices.translate(x, headerY + 1.5f, 0);
            matrices.scale(0.5f, 0.5f, 0.5f);
            context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.GUI_POTION, 0, 0, 0, 0, 16, 16, 1024, 1024, 1024, 1024, Palette.getTextColor());
            matrices.pop();
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
            int amplifier = instance.getAmplifier();

            // Получение англоязычного имени эффекта (en_us)
            String key = instance.getEffectType().value().getTranslationKey();
            String name = PotionsHud.enUsTranslations.getOrDefault(key, key);

            String levelPart = amplifier > 0
                    ? " " + (CONFIG.hudBetterPotionsHudToRoman ? toRoman(amplifier + 1) : amplifier + 1)
                    : "";

            this.displayText = name + levelPart;
            this.textWidth = FontRenderers.sf_bold_mini.getStringWidth(displayText);
        }

        public void render(DrawContext context, float x, float y, float alpha) {
            float scale = 0.8f;

            MatrixStack matrices = context.getMatrices();

            matrices.push();
            matrices.translate(x + (1 + scale), y + (1 + scale), 0);
            matrices.scale(scale, scale, 0);
            context.drawSpriteStretched(RenderLayer::getGuiTextured, mc.getStatusEffectSpriteManager().getSprite(instance.getEffectType()), 0, 0, 18, 18);
            matrices.pop();

            FontRenderers.sf_bold_mini.drawString(matrices, displayText, x + 20, y + 2, Palette.getTextColor());
            FontRenderers.sf_bold_mini.drawString(matrices, "§7" + getDuration(instance), x + 20, y + 9, Palette.getTextColor());

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

                Render2D.drawGradientBlurredShadow1(matrices, barX, barY, barWidth * progress, barHeight, 3, fillStart, fillEnd, fillStart, fillEnd);
                Render2D.drawRound(matrices, barX, barY, barWidth, barHeight, 1, background);
                Render2D.renderRoundedGradientRect(matrices, fillStart, fillEnd, fillEnd, fillStart, barX, barY, barWidth * progress, barHeight, 1);
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