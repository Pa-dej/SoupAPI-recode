package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class WatermarkRenderer {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static float colorAnimationProgress = 0f;
    private static long lastUpdateTime = System.currentTimeMillis();

    public static void render(DrawContext context) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        deltaTime = Math.min(deltaTime, 0.1f);
        lastUpdateTime = currentTime;

        float frameTime = 1.0f / 60.0f;
        float normalizedDelta = deltaTime / frameTime;

        colorAnimationProgress = (colorAnimationProgress + normalizedDelta * 0.0025f) % 1.0f;

        renderBackground(context, 4.5f, 2);
        renderName(context, 2, 2);
        renderHead(context, 2, 2);
    }

    private static void renderBackground(DrawContext context, float x, float y) {
        Color c1 = Palette.getColor(0f);    // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);    // Верхний левый

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

        float width = FontRenderers.sf_bold.getStringWidth(mc.player.getName().getString()) + 20;
        float height = 14;
        x += 13;
        if (CONFIG.mctiersEnabled) width += 18;
        y += 16;

        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 1, y + 1, width, height, 10, bottomLeft, bottomRight, topRight, topLeft);
        Render2D.renderRoundedGradientRect(context.getMatrices(), topLeft, topRight, bottomRight, bottomLeft, x + 0.5f, y + 0.5f, width, height, 4);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, width, height, 4, Render2D.injectAlpha(Color.BLACK, 180));
    }

    private static void renderName(DrawContext context, float x, float y) {
        String displayName = mc.player.getName().getString();
        y += 21.5f;
        FontRenderers.sf_bold.drawString(context.getMatrices(), displayName, x + 32, y, Colors.WHITE);

        if (!CONFIG.mctiersEnabled) return;
        float textWidth = FontRenderers.sf_bold.getStringWidth(mc.player.getName().getString());

        FontRenderers.sf_bold.drawString(context.getMatrices(), "|", x + textWidth + 36, y - 1, 0xAA525252);

        context.getMatrices().push();
        context.getMatrices().translate(x + 40 + textWidth, y - 3.7, 0);
        context.getMatrices().scale(0.3f, 0.3f, 0.3f);
        context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.getMC_TiersGameModeTexture(), 0, 0, 0, 0, 40, 40, 1268, 1153, 1268, 1153);
        context.getMatrices().pop();
    }

    private static void renderHead(DrawContext context, float x, float y) {
        Identifier texture = mc.player.getSkinTextures().texture();
        float scale = 0.3f;

        context.getMatrices().push();
        context.getMatrices().translate(x + 3.5f + 20, y + 3.5f + 20, 0);
        context.getMatrices().scale(scale, scale, scale);
        context.getMatrices().translate(-(x + 3.5f + 20), -(y + 3.5f + 20), 0);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        Render2D.drawRound(context.getMatrices(), x + 3.5f, y + 3.5f, 40, 40, 7, Render2D.injectAlpha(Color.BLACK, 20));
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), 1, 1, 1, 1, x + 3.5f, y + 3.5f, x + 3.5f + 40, y + 3.5f + 40, 7, 10);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }

    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }
}
