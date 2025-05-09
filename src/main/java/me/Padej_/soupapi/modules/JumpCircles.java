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
        private float angularVelocity;
        private long lastUpdateTime; // Время последнего обновления
        private final boolean isFadeOut; // Флаг для эффекта затухания

        public JumpCircle(Vec3d position) {
            this.position = position;
            this.offsetY = getRandomHeightOffset(0.01, 0.02, 0.0001);
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

        public static double getRandomHeightOffset(double min, double max, double step) {
            Random random = new Random();
            int steps = (int) ((max - min) / step);
            return min + random.nextInt(steps + 1) * step;
        }

        public void render(WorldRenderContext context, double cameraX, double cameraY, double cameraZ) {
            double x = position.x - cameraX;
            double y = position.y - cameraY - 0.5 - offsetY;
            double z = position.z - cameraZ;

            updateRotation();
            renderGlowCircleBufferBuilder(context.matrixStack(), x, y, z);
        }

        private void updateRotation() {
            // Вычисляем разницу во времени
            long currentTime = System.currentTimeMillis();
            float deltaTime = (currentTime - lastUpdateTime) / 1000f; // Время в секундах
            lastUpdateTime = currentTime;

            // Ограничиваем deltaTime, чтобы избежать скачков при фризах
            deltaTime = Math.min(deltaTime, 0.1f);

            // Нормализация времени для 60 FPS
            float frameTime = 1.0f / 60.0f; // Время одного кадра при 60 FPS (~0.01667 сек)
            float normalizedDelta = deltaTime / frameTime; // Нормализация времени

            // Вычисляем долю оставшегося времени жизни на основе реального времени
            float totalLifetime = CONFIG.jumpCirclesLiveTime; // Время жизни в секундах
            float elapsedTime = (currentTime - startTime) / 1000f; // Прошедшее время в секундах
            float remainingFraction = MathHelper.clamp((totalLifetime - elapsedTime) / totalLifetime, 0, 1);

            float damping = 0.98f;

            if (remainingFraction > 0.5f) {
                angularVelocity *= damping;
            } else {
                float reverseFactor = (0.5f - remainingFraction) * 2f;
                angularVelocity = -((float) Math.toRadians(CONFIG.jumpCirclesColorSpinSpeed) * reverseFactor * damping);
            }

            // Обновляем угол вращения с учетом нормализованного времени
            rotationAngle += angularVelocity * normalizedDelta;

            if (Math.abs(angularVelocity) < 0.001f && remainingFraction > 0.5f) {
                angularVelocity = 0f;
            }
        }

        private void renderGlowCircleBufferBuilder(MatrixStack modelMatrix, double x, double y, double z) {
            int glowAlpha = ConfigUtils.intPercentToHexInt(CONFIG.jumpCirclesAlpha);
            float liveTime = CONFIG.jumpCirclesLiveTime;

            RenderSystem.enableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

            RenderSystem.setShaderTexture(0, TexturesManager.getJumpCircleUnblack());

            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            // Вычисляем долю возраста для масштабирования на основе реального времени
            float elapsedTime = (System.currentTimeMillis() - startTime) / 1000f; // Прошедшее время в секундах
            float ageFraction = elapsedTime / (liveTime / 3); // Нормализация по времени жизни
            ageFraction *= 3; // Базовый коэффициент роста
            float scaleMultiplier = CONFIG.jumpCirclesScale / 100f; // Масштаб от конфига
            ageFraction = MathHelper.clamp(ageFraction * scaleMultiplier, 0, scaleMultiplier);

            float totalLifetime = liveTime; // Время жизни в секундах
            float remainingFraction = MathHelper.clamp((totalLifetime - elapsedTime) / totalLifetime, 0, 1);

            if (remainingFraction < 0.3f) {
                float shrinkFactor = remainingFraction / 0.3f;
                ageFraction *= shrinkFactor;
            }

            float interpolatedRadius = ageFraction;
            float colorAnim = isFadeOut ? 1f - remainingFraction : 1.0f; // Учитываем флаг isFadeOut
            float scale = interpolatedRadius * 2f;

            // Получаем цвета из палитры
            Color color1 = Palette.getInterpolatedPaletteColor(0.0f);
            Color color2 = Palette.getInterpolatedPaletteColor(0.33f);
            Color color3 = Palette.getInterpolatedPaletteColor(0.66f);
            Color color4 = Palette.getInterpolatedPaletteColor(1.0f);

            modelMatrix.push();
            modelMatrix.translate(x, y + 0.1f, z);
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
            RenderSystem.disableBlend();
            RenderSystem.defaultBlendFunc();
        }
    }

    public enum JumCircleStyle {
        CIRCLE, CIRCLE_BOLD, HEXAGON, PORTAL, SOUP
    }
}
