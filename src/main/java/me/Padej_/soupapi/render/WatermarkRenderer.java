package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL40C;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class WatermarkRenderer {
    private static MinecraftClient mc = MinecraftClient.getInstance();

    public static void render(DrawContext context) {
        if (!CONFIG.waterMarkEnabled) return;
        float x = CONFIG.waterMarkX;
        float y = CONFIG.waterMarkY;
        float xOffset = -14f;
        float yOffset = -14f;

        MatrixStack matrixStack = context.getMatrices();
        matrixStack.push();
        matrixStack.translate(0, 0, 10);
        renderBackground(context, x + 2.5f + xOffset, y + yOffset);
        renderName(context, x + xOffset, y + yOffset);
        renderHead(context, x + xOffset, y + yOffset);
        matrixStack.pop();
    }

    private static void renderBackground(DrawContext context, float x, float y) {
        float width = FontRenderers.sf_bold.getStringWidth(mc.player.getName().getString()) + 20;
        float height = 14;
        x += 13;
        if (CONFIG.mctiersEnabled) width += 18;
        y += 16;

        Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 1, y + 1, width, height, 5, TargetHudRenderer.bottomLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.topRight, TargetHudRenderer.topLeft);
//        Render2D.renderRoundedGradientRect(context.getMatrices(), TargetHudRenderer.topLeft, TargetHudRenderer.topLeft, TargetHudRenderer.bottomRight, TargetHudRenderer.bottomLeft, x + 0.5f, y + 0.5f, width, height, 4);
        Render2D.drawRound(context.getMatrices(), x + 0.5f, y + 0.5f, width, height, 4, Render2D.injectAlpha(new Color(0x181a29), 180));
    }

    private static void renderName(DrawContext context, float x, float y) {
        String displayName = mc.player.getName().getString();
        y += 21.5f;
        FontRenderers.sf_bold.drawString(context.getMatrices(), displayName, x + 32, y, Palette.getTextColor());

        if (!CONFIG.mctiersEnabled) return;
        float textWidth = FontRenderers.sf_bold.getStringWidth(mc.player.getName().getString());

        FontRenderers.sf_bold.drawString(context.getMatrices(), "|", x + textWidth + 36, y - 1, 0xAA525252);

        context.getMatrices().push();
        context.getMatrices().translate(x + 40 + textWidth, y - 3.7, 0);
        context.getMatrices().scale(0.3f, 0.3f, 0.3f);
        context.drawTexture(RenderLayer::getGuiTextured, TexturesManager.getMC_TiersGameModeTexture(), 0, 0, 0, 0, 40, 40, 1268, 1153, 1268, 1153, Palette.getTextColor());
        context.getMatrices().pop();
    }

    private static void renderHead(DrawContext context, float x, float y) {
        Identifier texture = mc.player.getSkinTextures().texture();
        float scale = 0.3f;
        float r = 9;

        context.getMatrices().push();
        context.getMatrices().translate(x + 3.5f + 20, y + 3.5f + 20, 0);
        context.getMatrices().scale(scale, scale, scale);
        context.getMatrices().translate(-(x + 3.5f + 20), -(y + 3.5f + 20), 0);
        RenderSystem.enableBlend();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);
        Render2D.drawRound(context.getMatrices(), x + 3.5f, y + 3.5f, 40, 40, r, Render2D.injectAlpha(Color.BLACK, 20));
        Render2D.setupRender();
        Render2D.renderRoundedQuadInternal(context.getMatrices().peek().getPositionMatrix(), 1, 1, 1, 1, x + 3.5f, y + 3.5f, x + 3.5f + 40, y + 3.5f + 40, r, 3);
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 8, 8, 8, 8, 64, 64);
        Render2D.renderTexture(context.getMatrices(), texture, x + 3.5f, y + 3.5f, 40, 40, 40, 8, 8, 8, 64, 64);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        context.getMatrices().pop();
    }
}

