package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.ConfigUtils;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class JumpCircles extends ConfigurableModule {

    private static final List<JumpCircle> CIRCLES = new ArrayList<>();
    private static boolean wasJumping = false;

    public static void onTick() {
        if (mc.player instanceof PlayerEntity player) {
            boolean isJumping = !player.isOnGround();

            if (isJumping && !wasJumping && mc.options.jumpKey.isPressed()) {
                CIRCLES.add(new JumpCircle(player.getPos()));
            }

            wasJumping = isJumping;
        }
    }

    public static void renderCircles(WorldRenderContext context) {
        if (!CONFIG.jumpCirclesEnabled) {
            CIRCLES.clear();
            return;
        }

        double cameraX = context.camera().getPos().x;
        double cameraY = context.camera().getPos().y;
        double cameraZ = context.camera().getPos().z;

        CIRCLES.removeIf(JumpCircle::isExpired);
        for (JumpCircle circle : CIRCLES) {
            circle.render(context, cameraX, cameraY, cameraZ);
        }
    }

    private static class JumpCircle {
        private final Vec3d position;
        private final double offsetY;
        private final long startTime; // Время создания круга
        private float rotationAngle;
        private final float angularVelocity;
        private long lastUpdateTime; // Время последнего обновления
        private final boolean isFadeOut; // Флаг для эффекта затухания

        public JumpCircle(Vec3d position) {
            this.position = position;
            this.offsetY = mc.player.getVelocity().getY();
            this.rotationAngle = 0f;
            this.angularVelocity = (float) Math.toRadians(CONFIG.jumpCirclesSpinSpeed);
            this.startTime = System.currentTimeMillis(); // Запоминаем время создания
            this.lastUpdateTime = startTime; // Инициализация времени обновления
            this.isFadeOut = CONFIG.jumpCirclesFadeOut; // Инициализация флага из конфигурации
        }

        public boolean isExpired() {
            long currentTime = System.currentTimeMillis();
            float elapsedTime = (currentTime - startTime) / 1000f; // Прошедшее время в секундах
            return elapsedTime > CONFIG.jumpCirclesLiveTime; // Сравниваем с временем жизни в секундах
        }

        public void render(WorldRenderContext context, double cameraX, double cameraY, double cameraZ) {
            double x = position.x - cameraX;
            double y = position.y - cameraY - offsetY;
            double z = position.z - cameraZ;

            updateRotation();
            renderGlowCircleBufferBuilder(context.matrixStack(), x, y, z);
        }

        private void updateRotation() {
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastUpdateTime) / 1000f;
            lastUpdateTime = currentTime;

            deltaTime = Math.min(deltaTime, 0.1f);

            float totalLifetime = CONFIG.jumpCirclesLiveTime;
            float elapsedTime = (currentTime - startTime) / 1000f;
            float remainingFraction = MathHelper.clamp((totalLifetime - elapsedTime) / totalLifetime, 0, 1);

            // Вращение до начала исчезания
            if (remainingFraction > 0.3f) {
                float frameTime = 1.0f / 60.0f;
                float normalizedDelta = deltaTime / frameTime;
                rotationAngle -= angularVelocity * normalizedDelta;
            } else {
                float fadeFactor = remainingFraction / 0.3f; // от 1 до 0
                float frameTime = 1.0f / 60.0f;
                float normalizedDelta = deltaTime / frameTime;
                rotationAngle += angularVelocity * normalizedDelta * fadeFactor;
            }
        }

        private void renderGlowCircleBufferBuilder(MatrixStack modelMatrix, double x, double y, double z) {
            int glowAlpha = ConfigUtils.intPercentToHexInt(CONFIG.jumpCirclesAlpha);
            float liveTime = CONFIG.jumpCirclesLiveTime;

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

            RenderSystem.setShaderTexture(0, TexturesManager.getJumpCircle());
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f;
            float ageFraction = elapsedTime / (liveTime / 3);
            ageFraction *= 3;
            float scaleMultiplier = CONFIG.jumpCirclesScale / 100f;
            ageFraction = MathHelper.clamp(ageFraction * scaleMultiplier, 0, scaleMultiplier);

            float remainingFraction = MathHelper.clamp((liveTime - elapsedTime) / liveTime, 0, 1);

            if (remainingFraction < 0.3f) {
                float shrinkFactor = remainingFraction / 0.3f;
                ageFraction *= shrinkFactor;
            }

            float interpolatedRadius = ageFraction;
            float colorAnim = isFadeOut ? 1f - remainingFraction : 1.0f;
            float scale = interpolatedRadius * 2f;

            Color color1 = Palette.getInterpolatedPaletteColor(0.0f);
            Color color2 = Palette.getInterpolatedPaletteColor(0.33f);
            Color color3 = Palette.getInterpolatedPaletteColor(0.66f);
            Color color4 = Palette.getInterpolatedPaletteColor(1.0f);

            modelMatrix.push();
            modelMatrix.translate(x, y, z);
            modelMatrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            modelMatrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees((float) Math.toDegrees(rotationAngle)));
            Matrix4f matrix = modelMatrix.peek().getPositionMatrix();

            buffer.vertex(matrix, -interpolatedRadius, -interpolatedRadius + scale, 0)
                    .texture(0, 1)
                    .color(color1.getRed(), color1.getGreen(), color1.getBlue(), (int) (glowAlpha * colorAnim));
            buffer.vertex(matrix, -interpolatedRadius + scale, -interpolatedRadius + scale, 0)
                    .texture(1, 1)
                    .color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int) (glowAlpha * colorAnim));
            buffer.vertex(matrix, -interpolatedRadius + scale, -interpolatedRadius, 0)
                    .texture(1, 0)
                    .color(color3.getRed(), color3.getGreen(), color3.getBlue(), (int) (glowAlpha * colorAnim));
            buffer.vertex(matrix, -interpolatedRadius, -interpolatedRadius, 0)
                    .texture(0, 0)
                    .color(color4.getRed(), color4.getGreen(), color4.getBlue(), (int) (glowAlpha * colorAnim));

            modelMatrix.pop();

            Render2D.endBuilding(buffer);
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
        }
    }

    public enum JumCircleStyle {
        CIRCLE, CIRCLE_BOLD, HEXAGON, PORTAL, SOUP
    }
}
