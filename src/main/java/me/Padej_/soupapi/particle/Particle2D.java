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
    public double hudOffsetX, hudOffsetY; // Смещение для слежения за HUD
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
        if (CONFIG.targetHudIncludeGlyphs) {
            AVAILABLE_TEXTURES.add(TexturesManager.getRandomGlyphParticle());
        }
        if (!CONFIG.targetHudIncludeFirefly &&
                !CONFIG.targetHudIncludeDollar &&
                !CONFIG.targetHudIncludeSnowflake &&
                !CONFIG.targetHudIncludeHeart &&
                !CONFIG.targetHudIncludeStar &&
                !CONFIG.targetHudIncludeGlyphs
        ) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY);
        }
    }

    public static Color mixColors(final Color color1, final Color color2, final double percent) {
        final double inverse_percent = 1.0 - percent;
        final int redPart = (int) (color1.getRed() * percent + color2.getRed() * inverse_percent);
        final int greenPart = (int) (color1.getGreen() * percent + color2.getGreen() * inverse_percent);
        final int bluePart = (int) (color1.getBlue() * percent + color2.getBlue() * inverse_percent);
        return new Color(redPart, greenPart, bluePart);
    }

    public void render2D(MatrixStack matrixStack, float hudX, float hudY, float depthFactor) {
        if (!CONFIG.targetHudParticles) return;
        matrixStack.push();

        float particleScale = CONFIG.targetHudParticleScale / 100f;

        // Применяем смещение HUD и масштаб глубины
        float renderX = (float) (x + hudX);
        float renderY = (float) (y + hudY);
        matrixStack.translate(renderX + size / 2f, renderY + size / 2f, 0);
        matrixStack.scale(particleScale * depthFactor, particleScale * depthFactor, particleScale);

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, (float) (opacity / 255f));

        Render2D.renderTexture(matrixStack, -size / 2f, -size / 2f, size, size, 0, 0, 256, 256, 256, 256);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, 0);

        matrixStack.pop();
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

    public void init(final double x, final double y, final double deltaX, final double deltaY, final double size, final Color color, boolean followHud) {
        this.x = x;
        this.y = y;
        this.deltaX = deltaX;
        this.deltaY = deltaY;
        this.size = size;
        this.opacity = 254;
        this.color = color;

        // Сохраняем начальное смещение HUD, если followHud включен
        if (followHud) {
            this.hudOffsetX = x;
            this.hudOffsetY = y;
        } else {
            this.hudOffsetX = 0;
            this.hudOffsetY = 0;
        }

        // Гарантируем индивидуальную текстуру для каждой частицы
        updateAvailableTextures();

        if (AVAILABLE_TEXTURES.isEmpty()) {
            this.texture = TexturesManager.getRandomGlyphParticle();
        } else {
            this.texture = AVAILABLE_TEXTURES.get(RANDOM.nextInt(AVAILABLE_TEXTURES.size()));
        }
    }
}


