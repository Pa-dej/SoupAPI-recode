package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HitboxDetector extends ConfigurableModule {
    private static final Map<UUID, SusData> suspiciousHits = new HashMap<>();
    private static UUID lastAttackerUUID = null;

    // Логируем подозрительный удар
    public static void logSuspect(PlayerEntity attacker, double requiredScale) {
        UUID uuid = attacker.getUuid();
        SusData data = suspiciousHits.computeIfAbsent(uuid, k -> new SusData(attacker.getName().getString()));
        data.totalScale += requiredScale;
        data.susCount++;

        // Сохраняем UUID последнего атакующего
        lastAttackerUUID = uuid;
    }

    // Рендеринг на HUD
    public static void render(DrawContext context) {
        if (lastAttackerUUID == null || !CONFIG.hitboxDetectorEnabled) return;

        SusData data = suspiciousHits.get(lastAttackerUUID);
        if (data == null) return;

        float x = CONFIG.hitboxDetectorOffsetX;
        float y = CONFIG.hitboxDetectorOffsetY;
        float height = 30;  // Фиксированная высота

        // Получаем ширину текста
        float width = Math.max(
                Math.max(
                        FontRenderers.sf_bold.getStringWidth(data.name) + 5,
                        FontRenderers.sf_bold.getStringWidth("Factor: " + String.format("%.2f", data.getAverageSusFactor()))) + 5,
                        FontRenderers.sf_bold.getStringWidth("Hits: " + data.susCount) + 5
        );

        // Если ширина меньше 80, устанавливаем 80
        width = Math.max(width, 70);

        // Рендерим фоновую часть
        renderBackground(context, x, y, width, height);

        // Заголовок
        float headerHeight = 12f;
        float headerYOffset = 5f;
        MatrixStack matrices = context.getMatrices();
        Render2D.drawGradientBlurredShadow1(matrices, x - 2.5f, y - headerHeight - headerYOffset, width, headerHeight, 7, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
        Render2D.renderRoundedGradientRect(matrices, TargetHudRenderer.topLeft, TargetHudRenderer.topRight, TargetHudRenderer.bottomRight, TargetHudRenderer.bottomLeft, x - 3f, y - headerHeight - headerYOffset, width, headerHeight, 3);
        Render2D.drawRound(matrices, x - 3f, y - headerHeight - headerYOffset, width, headerHeight, 3, Render2D.injectAlpha(Color.BLACK, 180));

        String title = "Sus Hits";
        float textWidth = FontRenderers.sf_bold.getStringWidth(title);
        FontRenderers.sf_bold.drawString(matrices, title,
                x - 3f + width / 2f - textWidth / 2f,
                y - headerHeight - headerYOffset + 3f, Palette.getTextColor());

        // Рендерим данные о последнем атакующем
        renderText(context, x, y - 15, data, width);

        // Рендерим иконку (можно заменить на иконку, соответствующую подозрительному игроку)
        renderIcons(context, x, y - 16);
    }

    private static void renderBackground(DrawContext context, float x, float y, float width, float height) {
        MatrixStack matrices = context.getMatrices();
        Render2D.drawGradientBlurredShadow1(matrices, x - 2.5f, y - 2.5f, width, height, 10, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
        Render2D.renderRoundedGradientRect(matrices, TargetHudRenderer.topLeft, TargetHudRenderer.topRight, TargetHudRenderer.bottomRight, TargetHudRenderer.bottomLeft, x - 3f, y - 3f, width, height, 4);
        Render2D.drawRound(matrices, x - 3f, y - 3f, width, height, 4, Render2D.injectAlpha(Color.BLACK, 180));
    }

    private static void renderText(DrawContext context, float x, float y, SusData data, float width) {
        float textYOffset = 15f;
        FontRenderers.sf_bold.drawString(context.getMatrices(), data.name, x + 5, y + textYOffset, Palette.getTextColor());
        FontRenderers.sf_bold.drawString(context.getMatrices(), "Factor: " + String.format("%.2f", data.getAverageSusFactor()), x + 5, y + textYOffset + 10, Palette.getTextColor());
        FontRenderers.sf_bold.drawString(context.getMatrices(), "Hits: " + data.susCount / 2, x + 5, y + textYOffset + 20, Palette.getTextColor());
    }

    private static void renderIcons(DrawContext context, float x, float y) {
        context.getMatrices().push();
        context.getMatrices().translate(x, y, 0);
        context.getMatrices().scale(0.3f, 0.3f, 0.3f);
        context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.GUI_HITBOX, 0, 0, 0, 0, 32, 32, 512, 512, 512, 512, Palette.getTextColor());
        context.getMatrices().pop();
    }

    // Структура для хранения данных о подозрительном игроке
    private static class SusData {
        String name;
        double totalScale = 0;
        int susCount = 0;

        SusData(String name) {
            this.name = name;
        }

        double getAverageSusFactor() {
            return susCount == 0 ? 0 : totalScale / susCount;
        }
    }
}



