package me.Padej_.soupapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.utils.GaussianFilter;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.Texture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.RandomStringUtils;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40C;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class Render2D {
    private final static MinecraftClient mc = MinecraftClient.getInstance();
    public static HashMap<Integer, BlurredShadow> shadowCache = new HashMap<>();
    public static HashMap<Integer, BlurredShadow> shadowCache1 = new HashMap<>();


    public static void renderMiscTexture(MatrixStack matrices, Identifier textureId, float scale) {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderTexture(0, textureId);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        matrices.push();
        matrices.translate(screenWidth / 2f, screenHeight / 2f, 0);
        matrices.scale(scale, scale, 1f);
        matrices.translate(-screenWidth / 2f, -screenHeight / 2f, 0);

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float x0 = 0f;
        float y0 = 0f;
        float x1 = screenWidth;
        float y1 = screenHeight;

        Color c1 = Palette.getColor(0f);   // Нижний левый
        Color c2 = Palette.getColor(0.33f); // Нижний правый
        Color c3 = Palette.getColor(0.66f); // Верхний правый
        Color c4 = Palette.getColor(1f);   // Верхний левый

        buffer.vertex(matrix, x0, y1, 0).texture(0.0f, 1.0f).color(c1.getRed() / 255f, c1.getGreen() / 255f, c1.getBlue() / 255f, 1);
        buffer.vertex(matrix, x1, y1, 0).texture(1.0f, 1.0f).color(c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, 1);
        buffer.vertex(matrix, x1, y0, 0).texture(1.0f, 0.0f).color(c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, 1);
        buffer.vertex(matrix, x0, y0, 0).texture(0.0f, 0.0f).color(c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, 1);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
        RenderSystem.disableBlend();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static Color injectAlpha(final Color color, final int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), MathHelper.clamp(alpha, 0, 255));
    }

    public static Color applyOpacity(Color color, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity));
    }

    public static int applyOpacity(int color_int, float opacity) {
        opacity = Math.min(1, Math.max(0, opacity));
        Color color = new Color(color_int);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (color.getAlpha() * opacity)).getRGB();
    }

    public static void drawHudBase2(MatrixStack matrices, float x, float y, float width, float height, float radius, float blurStrenth, float blurOpacity, float animationFactor) {
        blurStrenth *= animationFactor;
        blurOpacity = (float) interpolate(1f, blurOpacity, animationFactor);
        Color c = interpolateColorC(Color.BLACK, Color.BLACK, animationFactor);
        drawRound(matrices, x, y, width, height, radius, c);
    }

    public static void renderGradientTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        renderGradientTextureInternal(buffer, matrices, x0, y0, width, height, u, v, regionWidth, regionHeight, textureWidth, textureHeight, c1, c2, c3, c4);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }

    public static void renderGradientTextureInternal(BufferBuilder buff, MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, Color c1, Color c2, Color c3, Color c4) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        buff.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).color(c1.getRGB());
        buff.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight).color(c2.getRGB());
        buff.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight).color(c3.getRGB());
        buff.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight).color(c4.getRGB());
    }

    public static void renderTexture(MatrixStack matrices, Identifier identifier, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShaderTexture(0, identifier);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
    }

    public static void renderTexture(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
    }

    public static void renderRoundedQuad2(MatrixStack matrices, Color c, Color c2, Color c3, Color c4, double fromX, double fromY, double toX, double toY, double radius) {
        setupRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        renderRoundedQuadInternal2(matrices.peek().getPositionMatrix(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, c2.getRed() / 255f, c2.getGreen() / 255f, c2.getBlue() / 255f, c2.getAlpha() / 255f, c3.getRed() / 255f, c3.getGreen() / 255f, c3.getBlue() / 255f, c3.getAlpha() / 255f, c4.getRed() / 255f, c4.getGreen() / 255f, c4.getBlue() / 255f, c4.getAlpha() / 255f, fromX, fromY, toX, toY, radius);
        endRender();
    }

    public static void drawGradientRound(MatrixStack ms, float x, float y, float wight, float height, float cornersRadius, Color darker, Color darker1, Color darker2, Color darker3) {
        renderRoundedQuad2(ms, darker, darker1, darker2, darker3, x, y, x + wight, y + height, cornersRadius);
    }

    public static void renderRoundedQuadInternal2(Matrix4f matrix, float cr, float cg, float cb, float ca, float cr1, float cg1, float cb1, float ca1, float cr2, float cg2, float cb2, float ca2, float cr3, float cg3, float cb3, float ca3, double fromX, double fromY, double toX, double toY, double radC1) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] map = new double[][]{new double[]{toX - radC1, toY - radC1, radC1}, new double[]{toX - radC1, fromY + radC1, radC1}, new double[]{fromX + radC1, fromY + radC1, radC1}, new double[]{fromX + radC1, toY - radC1, radC1}};

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90; r < (90 + i * 90); r += 10) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                switch (i) {
                    case 0 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr1, cg1, cb1, ca1);
                    case 1 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
                    case 2 ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr2, cg2, cb2, ca2);
                    default ->
                            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr3, cg3, cb3, ca3);
                }
            }
        }
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static boolean isHovered(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX - width <= x && mouseY >= y && mouseY - height <= y;
    }

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1, Math.max(0, amount));
        return new Color(interpolateInt(color1.getRed(), color2.getRed(), amount), interpolateInt(color1.getGreen(), color2.getGreen(), amount), interpolateInt(color1.getBlue(), color2.getBlue(), amount), interpolateInt(color1.getAlpha(), color2.getAlpha(), amount));
    }

    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }

    public static float interpolateFloat(float oldValue, float newValue, double interpolationValue) {
        return (float) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static int interpolateInt(int oldValue, int newValue, double interpolationValue) {
        return (int) interpolate(oldValue, newValue, (float) interpolationValue);
    }

    public static void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        setupRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, x, y + height, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x + width, y, 0.0F).color(c.getRGB());
        buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB());
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }

    public static void setRectPoints(BufferBuilder bufferBuilder, Matrix4f matrix, float x, float y, float x1, float y1, Color c1, Color c2, Color c3, Color c4) {
        bufferBuilder.vertex(matrix, x, y1, 0.0F).color(c1.getRGB());
        bufferBuilder.vertex(matrix, x1, y1, 0.0F).color(c2.getRGB());
        bufferBuilder.vertex(matrix, x1, y, 0.0F).color(c3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(c4.getRGB());
    }

    private static void registerTexture(Texture i, byte[] content) {
        try {
            ByteBuffer data = BufferUtils.createByteBuffer(content.length).put(content);
            data.flip();
            NativeImageBackedTexture tex = new NativeImageBackedTexture(NativeImage.read(data));
            mc.execute(() -> mc.getTextureManager().registerTexture(i.getId(), tex));
        } catch (Exception ignored) {
        }
    }

    public static void registerBufferedImageTexture(Texture i, BufferedImage bi) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "png", baos);
            byte[] bytes = baos.toByteArray();
            registerTexture(i, bytes);
        } catch (Exception ignored) {
        }
    }

    public static void renderRoundedGradientRect(MatrixStack matrices, Color color1, Color color2, Color color3, Color color4, float x, float y, float width, float height, float Radius) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT);
        RenderSystem.colorMask(true, true, true, true);

        drawRound(matrices, x, y, width, height, Radius, color1);
        setupRender();
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB());
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB());
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB());
        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
        endRender();
    }

    /*
    public static void renderRoundedTextureRect(MatrixStack matrices, double x0, double y0, double width, double height, float u, float v, double regionWidth, double regionHeight, double textureWidth, double textureHeight, float Radius) {
        double x1 = x0 + width;
        double y1 = y0 + height;
        double z = 0;

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        drawRound(matrices, (float) x0, (float) y0, (float) width, (float) height, Radius, new Color(0xFFFF00FF, true));
        setupRender();
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX);
        if (TargetHUD_N.target instanceof PlayerEntity) {
            RenderSystem.setShaderTexture(0, ((AbstractClientPlayerEntity) TargetHUD_N.target).getSkinTextures().texture());
        }
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, (float) x0, (float) y1, (float) z).texture((u) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y1, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v + (float) regionHeight) / (float) textureHeight);
        buffer.vertex(matrix, (float) x1, (float) y0, (float) z).texture((u + (float) regionWidth) / (float) textureWidth, (v) / (float) textureHeight);
        buffer.vertex(matrix, (float) x0, (float) y0, (float) z).texture((u) / (float) textureWidth, (v + 0.0F) / (float) textureHeight);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        endRender();
    }
     */

    public static void drawRound(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        renderRoundedQuad(matrices, color, x, y, width + x, height + y, radius, 4);
    }

    public static void renderRoundedQuad(MatrixStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        renderRoundedQuadInternal(matrices.peek().getPositionMatrix(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        // Центр прямоугольника
        double centerX = (fromX + toX) / 2.0;
        double centerY = (fromY + toY) / 2.0;
        bufferBuilder.vertex(matrix, (float) centerX, (float) centerY, 0.0F).color(cr, cg, cb, ca);

        // Определяем центры закруглений для каждого угла
        double[][] map = new double[][]{
                {toX - radius, toY - radius, radius},   // Верхний правый
                {toX - radius, fromY + radius, radius}, // Нижний правый
                {fromX + radius, fromY + radius, radius}, // Нижний левый
                {fromX + radius, toY - radius, radius}  // Верхний левый
        };

        // Рисуем закругленные углы
        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];
            for (double r = i * 90d; r <= (90 + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca);
            }
        }

        // Закрываем TRIANGLE_FAN, добавляя первую вершину угла снова
        double[] firstCorner = map[0];
        float rad1 = (float) Math.toRadians(0);
        float sin = (float) (Math.sin(rad1) * firstCorner[2]);
        float cos = (float) (Math.cos(rad1) * firstCorner[2]);
        bufferBuilder.vertex(matrix, (float) firstCorner[0] + sin, (float) firstCorner[1] + cos, 0.0F).color(cr, cg, cb, ca);

        BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
    }

    public static void drawBlurredShadow(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color) {
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        int identifier = (int) (width * height + width * blurRadius);
        if (shadowCache.containsKey(identifier)) {
            shadowCache.get(identifier).bind();
        } else {
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();
            GaussianFilter op = new GaussianFilter(blurRadius);
            BufferedImage blurred = op.filter(original, null);
            shadowCache.put(identifier, new BlurredShadow(blurred));
            return;
        }

        setupRender();
        RenderSystem.setShaderColor(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        renderTexture(matrices, x, y, width, height, 0, 0, width, height, width, height);
        endRender();
    }

    public static void drawGradientBlurredShadow1(MatrixStack matrices, float x, float y, float width, float height, int blurRadius, Color color1, Color color2, Color color3, Color color4) {
        if (!CONFIG.blurShadowEnabled) return;
        width = width + blurRadius * 2;
        height = height + blurRadius * 2;
        x = x - blurRadius;
        y = y - blurRadius;

        // Округление размеров для уменьшения уникальных ключей
        int roundedWidth = Math.round(width / 10) * 10;
        int roundedHeight = Math.round(height / 10) * 10;
        int identifier = roundedWidth * roundedHeight + roundedWidth * blurRadius;

        BlurredShadow shadow = shadowCache1.get(identifier);
        if (shadow == null) {
            BufferedImage original = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g = original.getGraphics();
            g.setColor(new Color(-1));
            g.fillRect(blurRadius, blurRadius, (int) (width - blurRadius * 2), (int) (height - blurRadius * 2));
            g.dispose();
            BufferedImage blurred = new GaussianFilter(blurRadius).filter(original, null);

            BufferedImage black = new BufferedImage((int) width + blurRadius * 2, (int) height + blurRadius * 2, BufferedImage.TYPE_INT_ARGB);
            Graphics g2 = black.getGraphics();
            g2.setColor(new Color(0x000000));
            g2.fillRect(0, 0, (int) width + blurRadius * 2, (int) height + blurRadius * 2);
            g2.dispose();

            BufferedImage combined = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
            Graphics g1 = combined.getGraphics();
            g1.drawImage(black, -blurRadius, -blurRadius, null);
            g1.drawImage(blurred, 0, 0, null);
            g1.dispose();

            shadowCache1.put(identifier, new BlurredShadow(combined));
            shadow = shadowCache1.get(identifier);
        }

        shadow.bind();
        setupRender();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        renderGradientTexture(matrices, x, y, width, height, 0, 0, width, height, width, height, color1, color2, color3, color4);
        endRender();
    }

    public static void drawGlyphs(MatrixStack matrices, Identifier texture, Color c, float scale) {
        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, texture);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        RenderSystem.setShaderColor(c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f);
        Render2D.renderGradientTexture(matrices, 0, 0, scale, scale, 0, 0, 128, 128, 128, 128, c, c, c, c);
        RenderSystem.setShaderColor(1, 1, 1, 1);

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void endBuilding(BufferBuilder bb) {
        BuiltBuffer builtBuffer = bb.endNullable();
        if (builtBuffer != null) {
            BufferRenderer.drawWithGlobalProgram(builtBuffer);
        }
    }

    public static class BlurredShadow {
        Texture id;

        public BlurredShadow(BufferedImage bufferedImage) {
            this.id = new Texture("texture/remote/" + RandomStringUtils.randomAlphanumeric(16));
            registerBufferedImageTexture(id, bufferedImage);
        }

        public void bind() {
            RenderSystem.setShaderTexture(0, id.getId());
        }
    }

    public record Rectangle(float x, float y, float x1, float y1) {
        public boolean contains(double x, double y) {
            return x >= this.x && x <= x1 && y >= this.y && y <= y1;
        }
    }
}
