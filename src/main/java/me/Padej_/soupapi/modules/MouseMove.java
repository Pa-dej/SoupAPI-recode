package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.MouseUtils;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.*;

public class MouseMove extends ConfigurableModule {

    public static void render(DrawContext context) {
        if (!CONFIG.mouseMoveEnabled) return;

        Color color1 = TargetHudRenderer.bottomLeft;
        Color color2 = TargetHudRenderer.bottomRight;
        Color color3 = TargetHudRenderer.topRight;
        Color color4 = TargetHudRenderer.topLeft;

        int centerX = CONFIG.mouseMoveX + 20;
        int centerY = CONFIG.mouseMoveY + 20;

        if (CONFIG.mouseMoveBlur) {
            Render2D.drawGradientBlurredShadow1(context.getMatrices(), centerX - 20, centerY - 20, 40, 40, 5, color1, color2, color3, color4);
        }
        Render2D.drawRound(context.getMatrices(), centerX - 20, centerY - 20, 40, 40, 5, Render2D.injectAlpha(new Color(0x181a29), 180));

        MatrixStack matrices = context.getMatrices();
        matrices.push();
        matrices.translate((float) MouseUtils.cursorX, (float) MouseUtils.cursorY, 0);

        int dotScale = 10;
        float scaleOffset = dotScale / 2f;

        Render2D.drawGradientBlurredShadow1(context.getMatrices(), centerX - scaleOffset, centerY - scaleOffset, dotScale, dotScale, 4, color1, color2, color3, color4);
        Render2D.renderRoundedGradientRect(context.getMatrices(), color1, color2, color3, color4, centerX - scaleOffset, centerY - scaleOffset, dotScale, dotScale, 3);

        matrices.pop();
    }
}
