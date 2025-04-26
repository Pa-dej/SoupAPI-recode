package me.Padej_.soupapi.particle;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.awt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class Particle2D {
    public double x, y, deltaX, deltaY, size, opacity;
    public Color color;
    private Identifier texture;

    private static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();

        if (CONFIG.targetHudIncludeFirefly) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        }
        if (CONFIG.targetHudIncludeDollar) {
            AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        }
        if (CONFIG.targetHudIncludeSnowflake) {
            AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        }
        if (CONFIG.targetHudIncludeHeart) {
            AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        }
        if (CONFIG.targetHudIncludeStar) {
            AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        }
        if (!CONFIG.targetHudIncludeFirefly &&
                !CONFIG.targetHudIncludeDollar &&
                !CONFIG.targetHudIncludeSnowflake &&
                !CONFIG.targetHudIncludeHeart &&
                !CONFIG.targetHudIncludeStar) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        }
    }

    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public void render2D(MatrixStack matrixStack) {
        updateAvailableTextures();
        drawOrbParticle(matrixStack, (float) x, (float) y, color);
    }

    public void drawOrbParticle(MatrixStack matrices, float x, float y, Color c) {
        matrices.push();

        float particleScale = CONFIG.targetHudParticleScale / 100f;

        matrices.translate(x + size / 2f, y + size / 2f, 0);

        matrices.scale(particleScale, particleScale, particleScale);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, (float) (opacity / 255f));

        Render2D.renderTexture(matrices, -size / 2f, -size / 2f, size, size, 0, 0, 256, 256, 256, 256);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, 0);

        matrices.pop();
    }


    public void updatePosition(float delta) {
        x += deltaX * delta;
        y += deltaY * delta;

        deltaY *= Math.pow(0.95, delta);
        deltaX *= Math.pow(0.95, delta);

        opacity -= 2f * delta;
        size /= Math.pow(1.01, delta);

        if (opacity < 1) {
            opacity = 1;
        }
    }

    public void init(final double x, final double y, final double deltaX, final double deltaY, final double size, final Color color) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
        this.opacity = 254;
        this.color = color;

        if (AVAILABLE_TEXTURES.isEmpty()) {
            this.texture = TexturesManager.FIREFLY_ALT;
        } else {
            this.texture = AVAILABLE_TEXTURES.get(RANDOM.nextInt(AVAILABLE_TEXTURES.size()));
        }
    }
}

