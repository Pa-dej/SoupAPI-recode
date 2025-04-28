package me.Padej_.soupapi.mixin.redirect;

import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.Palette;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(InGameHud.class)
public class ScoreboardStyleMixin {

    @Shadow @Final private MinecraftClient client;
    @Unique
    private static final float colorAnimationSpeed = 0.015f;
    @Unique
    private static float scoreboardColorAnimationProgress = 0f;
    @Unique
    private static long lastUpdateTime = System.currentTimeMillis();

    @Inject(method = "render", at = @At("TAIL"))
    private void doFrame(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;
        float frameTime = 1.0f / 60.0f;
        float normalizedDelta = deltaTime / frameTime;
        scoreboardColorAnimationProgress = (scoreboardColorAnimationProgress + normalizedDelta * colorAnimationSpeed / 2f) % 1.0f;
    }

    @Redirect(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 0)
    )
    private void cancelHeaderFill(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        if (!CONFIG.hudBetterScoreboardEnabled) {
            drawContext.fill(x1, y1, x2, y2, color);
        }
    }


    @Redirect(
            method = "renderScoreboardSidebar(Lnet/minecraft/client/gui/DrawContext;Lnet/minecraft/scoreboard/ScoreboardObjective;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;fill(IIIII)V", ordinal = 1)
    )
    private void replaceBodyFill(DrawContext drawContext, int x1, int y1, int x2, int y2, int color) {
        if (CONFIG.hudBetterScoreboardEnabled) {

            float cornerRadius = 5.0f;
            float glowStrength = 10.0f;

            // Палитра
            Color c1 = Palette.getColor(0f);
            Color c2 = Palette.getColor(0.33f);
            Color c3 = Palette.getColor(0.66f);
            Color c4 = Palette.getColor(1f);
            Color[] colors = getRotatingColors(scoreboardColorAnimationProgress, c1, c2, c3, c4);

            Color topLeft = colors[0];
            Color topRight = colors[1];
            Color bottomRight = colors[2];
            Color bottomLeft = colors[3];

            // Сдвигаем фон вверх на заголовок
            y1 -= 10;
            float x = x1 - 1; // сдвигаем на 1 пиксель влево
            float y = y1 - 1; // сдвигаем на 1 пиксель вверх
            float width = (x2 - x1) + 2; // расширяем на 1 пиксель слева и справа
            float height = (y2 - y1) + 2; // расширяем на 1 пиксель сверху и снизу

            if (CONFIG.hudBetterScoreboardGlow)
                Render2D.drawGradientBlurredShadow1(drawContext.getMatrices(), x, y, width, height, (int) glowStrength, bottomLeft, bottomRight, topRight, topLeft);
            if (CONFIG.hudBetterScoreboardColor)
                Render2D.renderRoundedGradientRect(drawContext.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x, y, width, height, cornerRadius);
            if (CONFIG.hudBetterScoreboardDarker)
                Render2D.drawRound(drawContext.getMatrices(), x, y, width, height, cornerRadius, Render2D.injectAlpha(Color.BLACK, 120));
        } else {
            drawContext.fill(x1, y1, x2, y2, color);
        }
    }

    @Unique
    private static Color[] getRotatingColors(float progress, Color c1, Color c2, Color c3, Color c4) {
        progress = progress % 1.0f;
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
        return new Color[]{topLeft, topRight, bottomRight, bottomLeft};
    }

    @Unique
    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }
}
