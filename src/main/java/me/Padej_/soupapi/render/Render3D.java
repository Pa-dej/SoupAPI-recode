package me.Padej_.soupapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.Halo;
import me.Padej_.soupapi.modules.TargetRender;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.*;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Render3D extends ConfigurableModule {
    private static float rollAngle = 0.0f;
    private static long lastUpdateTime = System.currentTimeMillis();
    private static Camera camera = mc.gameRenderer.getCamera();

    public static void renderChinaHat(MatrixStack matrices, VertexConsumer vertexConsumer) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        float baseRadius = CONFIG.chinaHatBaseRadius / 100f;
        float height = CONFIG.chinaHatTipHeight / 100f;
        float yOffset = -CONFIG.chinaHatYOffset / 100f;
        int segments = 60;
        float time = MinecraftClient.getInstance().world.getTime() % 360;
        int alpha = (int) (1 - (CONFIG.chinaHatAlpha * 255 / 100f));
        boolean isHalf = CONFIG.chinaHatRenderHalf;

        int rotation = 360 / 5;
        float rotationOffset = (time % rotation) / rotation;

        // Массивы для хранения координат и цветов
        float[] xCoords = new float[segments + 1];
        float[] zCoords = new float[segments + 1];
        float[] reds = new float[segments + 1];
        float[] greens = new float[segments + 1];
        float[] blues = new float[segments + 1];
        float[] alphas = new float[segments + 1]; // Добавлен массив для альфа-значений

        // Вычисляем координаты и цвета для всех сегментов
        for (int i = 0; i <= segments; i++) {
            float angle = (float) (2 * Math.PI * i / segments);
            xCoords[i] = MathHelper.cos(angle) * baseRadius;
            zCoords[i] = MathHelper.sin(angle) * baseRadius;

            // Интерполяция для эффекта волны
            float interpolatedAge = RenderWithAnimatedColor.getWaveInterpolation(angle, rotationOffset);
            Color color = Palette.getColor(interpolatedAge);

            // Извлекаем RGB
            reds[i] = color.getRed() / 255f;
            greens[i] = color.getGreen() / 255f;
            blues[i] = color.getBlue() / 255f;

            // Вычисляем альфа-значение в зависимости от расстояния от центра
            if (isHalf) {
                float distanceFromCenter = Math.sqrt(xCoords[i] * xCoords[i] + zCoords[i] * zCoords[i]);
                float alphaFactor = 1.0f - (distanceFromCenter / baseRadius); // От 1 в центре до 0 на краю
                alphas[i] = Math.max(alphaFactor, 0) * alpha; // Умножаем на базовую альфу
            } else {
                alphas[i] = alpha; // Оригинальная альфа для всех точек
            }
        }

        // Вершина конуса (снизу)
        float tipX = 0.0F;
        float tipZ = 0.0F;
        float interpolatedAgeTip = RenderWithAnimatedColor.getWaveInterpolation(0, rotationOffset);
        Color colorTip = Palette.getColor(interpolatedAgeTip);
        float redTip = colorTip.getRed() / 255f;
        float greenTip = colorTip.getGreen() / 255f;
        float blueTip = colorTip.getBlue() / 255f;

        // Рисуем конус
        for (int i = 0; i < segments; i++) {
            float x1 = xCoords[i];
            float z1 = zCoords[i];
            float x2 = xCoords[i + 1];
            float z2 = zCoords[i + 1];

            float red1 = reds[i];
            float green1 = greens[i];
            float blue1 = blues[i];
            float alpha1 = alphas[i];

            float red2 = reds[i + 1];
            float green2 = greens[i + 1];
            float blue2 = blues[i + 1];
            float alpha2 = alphas[i + 1];

            // Первая грань (от основания сверху к вершине снизу)
            vertexConsumer.vertex(matrix, x1, height, z1)
                    .color(red1, green1, blue1, isHalf ? alpha1 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, x2, height, z2)
                    .color(red2, green2, blue2, isHalf ? alpha2 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, tipX, yOffset, tipZ)
                    .color(redTip, greenTip, blueTip, alpha)
                    .normal(0, -1, 0);

            // Вторая грань (для заполнения обратной стороны)
            vertexConsumer.vertex(matrix, x2, height, z2)
                    .color(red2, green2, blue2, isHalf ? alpha2 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, x1, height, z1)
                    .color(red1, green1, blue1, isHalf ? alpha1 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, tipX, yOffset, tipZ)
                    .color(redTip, greenTip, blueTip, alpha)
                    .normal(0, -1, 0);
        }
    }

    private static Vec3d calculateEntityPositionRelativeToCamera(Camera camera, float tickDelta, Entity targetEntity) {
        double interpolatedX = MathHelper.lerp(tickDelta, targetEntity.prevX, targetEntity.getX());
        double interpolatedY = MathHelper.lerp(tickDelta, targetEntity.prevY, targetEntity.getY()) + (double) (targetEntity.getHeight() / 2.0F);
        double interpolatedZ = MathHelper.lerp(tickDelta, targetEntity.prevZ, targetEntity.getZ());
        Vec3d entityPos = new Vec3d(interpolatedX, interpolatedY, interpolatedZ);
        return entityPos.subtract(camera.getPos());
    }

    public static void drawLegacy(float tickDelta, Entity targetEntity) {
        if (targetEntity == null || mc.world == null) return;

        Vec3d entityPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
        float halfSize = (CONFIG.targetRenderLegacyScale / 50f) / 2.0F;
        float alpha = CONFIG.targetRenderLegacyAlpha / 100f;

        float time = mc.world.getTime() % 360;
        int rotationAngle = 360 / 5;
        float rotationOffset = (time % rotationAngle) / (float) rotationAngle;

        Color color0 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 0.25), rotationOffset));
        Color color1 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 0.75), rotationOffset));
        Color color2 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 1.25), rotationOffset));
        Color color3 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 1.75), rotationOffset));

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f;
        lastUpdateTime = currentTime;
        rollAngle = (rollAngle + 90f * deltaTime * (CONFIG.targetRenderLegacyRollSpeed / 100f)) % 360f;

        MatrixStack matrices = new MatrixStack();
        matrices.push();

        matrices.translate(entityPos.x, entityPos.y, entityPos.z);

        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollAngle));

        Matrix4f matrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderTexture(0, TexturesManager.getTargetRenderTexture());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        TargetRender.TargetRenderSoulStyle.setupBlendFunc();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        buffer.vertex(matrix, -halfSize, halfSize, 0.0F).texture(0.0F, 0.0F).color(color0.getRed(), color0.getGreen(), color0.getBlue(), (int) (alpha * 255));
        buffer.vertex(matrix, halfSize, halfSize, 0.0F).texture(1.0F, 0.0F).color(color1.getRed(), color1.getGreen(), color1.getBlue(), (int) (alpha * 255));
        buffer.vertex(matrix, halfSize, -halfSize, 0.0F).texture(1.0F, 1.0F).color(color2.getRed(), color2.getGreen(), color2.getBlue(), (int) (alpha * 255));
        buffer.vertex(matrix, -halfSize, -halfSize, 0.0F).texture(0.0F, 1.0F).color(color3.getRed(), color3.getGreen(), color3.getBlue(), (int) (alpha * 255));

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        matrices.pop();
    }

    public static void renderSoulsEsp(float tickDelta, Entity targetEntity) {
        int espLength = CONFIG.targetRenderSoulLenght;
        float factor = CONFIG.targetRenderSoulFactor;
        float shaking = CONFIG.targetRenderSoulShaking;

        float layerSpacing = 2;
        float amplitude = CONFIG.targetRenderSoulAmplitude;
        float radius = CONFIG.targetRenderSoulRadius / 100f;
        float startSize = CONFIG.targetRenderSoulStartSize / 100f;
        float endSize = CONFIG.targetRenderSoulEndSize / 100f;
        float scaleModifier = CONFIG.targetRenderSoulScale / 100f;
        int subdivisions = CONFIG.targetRenderSoulSubdivision;

        if (targetEntity == null) return;

        float frameTime = 1.0f / 60.0f;
        float normalizedTickDelta = tickDelta / frameTime;

        Vec3d newPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
        float entityAge = targetEntity.age + tickDelta;

        MatrixStack matrices = new MatrixStack();
        MatrixStack matricesB = new MatrixStack();

        matricesB.push();
        Quaternionf rotation = new Quaternionf().identity();
        rotation.rotateY(camera.getYaw() * (float) Math.PI / 180.0f);
        rotation.rotateX(-camera.getPitch() * (float) Math.PI / 180.0f);
        matricesB.multiply(rotation);
        matricesB.pop();

        matrices.push();
        matrices.translate(newPos.getX(), newPos.getY(), newPos.getZ());

        Matrix4f baseMatrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderTexture(0, TexturesManager.getSoulTexture());
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        TargetRender.TargetRenderSoulStyle.setupBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < espLength; i++) {
                for (int sub = 0; sub < subdivisions; sub++) {
                    float t = (float) sub / subdivisions;
                    float stepIndex = i + t;

                    double radians = Math.toRadians((((stepIndex) / 1.5f + entityAge) * factor + (j * 120)) % (factor * 360));
                    double sinQuad = Math.sin(Math.toRadians(entityAge * 2.5f + stepIndex * (j + 1)) * amplitude) / shaking;

                    float offset = ((stepIndex) / espLength) * layerSpacing;
                    float x = (float) (Math.cos(radians) * radius);
                    float z = (float) (Math.sin(radians) * radius);
                    float y = (float) sinQuad;

                    MatrixStack particleMatrix = new MatrixStack();
                    particleMatrix.multiplyPositionMatrix(baseMatrix);
                    particleMatrix.translate(x, y, z);

                    Quaternionf billboardRot = new Quaternionf().identity();
                    billboardRot.rotateY(Math.toRadians(-camera.getYaw()));
                    billboardRot.rotateX(Math.toRadians(camera.getPitch()));
                    particleMatrix.multiply(billboardRot);

                    Matrix4f matrix = particleMatrix.peek().getPositionMatrix();

                    float animProgress = ((entityAge * 0.03f + stepIndex * 0.07f + j * 0.15f) * normalizedTickDelta) % 1f;
                    Color color = Palette.getInterpolatedPaletteColor(animProgress);
                    int argb = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();

                    float scale = Math.max((endSize + offset * (startSize - endSize)) * scaleModifier, 0.15f * scaleModifier);

                    buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(argb);
                    buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(argb);
                    buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0f).color(argb);
                    buffer.vertex(matrix, -scale, -scale, 0).texture(0f, 0f).color(argb);
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void renderSoulPair(float tickDelta, Entity targetEntity) {
        int espLength = CONFIG.haloSoulLenght;
        float factor = CONFIG.haloSoulFactor;
        float radius = CONFIG.haloSoulRadius / 100f;
        float startSize = CONFIG.haloSoulStartSize / 100f;
        float endSize = CONFIG.haloSoulEndSize / 100f;
        float scaleModifier = CONFIG.haloSoulScale / 100f;
        int subdivisions = CONFIG.haloSoulSubdivision;
        float layerSpacing = 2;

        if (targetEntity == null) return;
        if (targetEntity instanceof PlayerEntity playerEntity && playerEntity.isGliding()) return;
        Vec3d newPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
        float entityAge = targetEntity.age + tickDelta;

        float frameTime = 1.0f / 60.0f;
        float normalizedTickDelta = tickDelta / frameTime;

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        matrices.translate(newPos.getX(), newPos.getY() + 1, newPos.getZ());

        Matrix4f baseMatrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderTexture(0, TexturesManager.FIREFLY);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        TargetRender.TargetRenderSoulStyle.setupBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < espLength; i++) {
                for (int sub = 0; sub < subdivisions; sub++) {
                    float t = (float) sub / subdivisions;
                    float stepIndex = i + t;

                    double radians = Math.toRadians((((stepIndex) / 1.5f + entityAge) * factor + (j * 180)) % (factor * 360));
                    double sinQuad = 0;

                    float offset = ((stepIndex) / espLength) * layerSpacing;

                    float x = (float) (Math.cos(radians) * radius);
                    float z = (float) (Math.sin(radians) * radius);
                    float y = (float) sinQuad;

                    MatrixStack particleMatrix = new MatrixStack();
                    particleMatrix.multiplyPositionMatrix(baseMatrix); // только yaw, без pitch
                    particleMatrix.translate(x, y, z);

                    Quaternionf billboardRot = new Quaternionf().identity();
                    billboardRot.rotateY(Math.toRadians(-camera.getYaw()));
                    billboardRot.rotateX(Math.toRadians(camera.getPitch())); // хотим только для квадрата

                    particleMatrix.multiply(billboardRot);

                    Matrix4f matrix = particleMatrix.peek().getPositionMatrix();

                    // Нормализуем прогресс анимации для плавности
                    float animProgress = ((entityAge * 0.03f + stepIndex * 0.07f + j * 0.15f) * normalizedTickDelta) % 1f;
                    Color color = Palette.getInterpolatedPaletteColor(animProgress);
                    int argb = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();

                    float scale = Math.max((endSize + offset * (startSize - endSize)) * scaleModifier, 0.15f * scaleModifier);

                    buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(argb);
                    buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(argb);
                    buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0f).color(argb);
                    buffer.vertex(matrix, -scale, -scale, 0).texture(0f, 0f).color(argb);
                }
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void drawSpiralsEsp(MatrixStack stack, @NotNull Entity target) {

        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);

        float radius = 0.75f;
        float heightStep = 0.004f;
        float heightOffset = 0.1f;
        float animationSpeed = 6f;
        float alphaDivider = 100f;

        // Интерполяция позиции цели относительно камеры
        double x = MathHelper.lerp(tickDelta, target.prevX, target.getX()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = MathHelper.lerp(tickDelta, target.prevY, target.getY()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = MathHelper.lerp(tickDelta, target.prevZ, target.getZ()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double height = target.getHeight();

        // Генерация трёх спиралей с разными угловыми сдвигами
        var spirals = new ArrayList[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>()};
        generateSpiralVectors(spirals, radius, height, heightStep);

        stack.push();
        stack.translate(x, y - height / 2, z);
        setupRender();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();

        Matrix4f matrix = stack.peek().getPositionMatrix();

        // Рендеринг всех трёх спиралей
        renderSpiral(matrix, spirals[0], 0.0f, heightOffset, animationSpeed, alphaDivider);  // Первая спираль
        renderSpiral(matrix, spirals[1], 0.33f, heightOffset, animationSpeed, alphaDivider); // Вторая спираль
        renderSpiral(matrix, spirals[2], 0.66f, heightOffset, animationSpeed, alphaDivider); // Третья спираль

        RenderSystem.enableCull();
        stack.translate(-x, -y, -z);
        endRender();
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    private static void generateSpiralVectors(ArrayList<Vec3d>[] spirals, float radius, double initialHeight, float heightStep) {
        for (int i = 0; i <= 360; ++i) {
            double height = initialHeight - i * heightStep;
            for (int spiralIndex = 0; spiralIndex < 3; spiralIndex++) {
                double angle = Math.toRadians(i + spiralIndex * 120);
                double u = Math.cos(angle);
                double v = Math.sin(angle);
                spirals[spiralIndex].add(new Vec3d((float) (u * radius), (float) height, (float) (v * radius)));
            }
        }
    }

    // Метод для рендеринга одной спирали
    private static void renderSpiral(Matrix4f matrix, ArrayList<Vec3d> vecs, float progressOffset, float heightOffset, float animationSpeed, float alphaDivider) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int size = vecs.size();

        // Получаем начальный и конечный цвета
        Color startColor = Palette.getColor(0.0f);
        Color endColor = Palette.getColor(1.0f);

        // Вычисляем смешанный цвет (среднее между начальным и конечным)
        int mixedRed = (startColor.getRed() + endColor.getRed()) / 2;
        int mixedGreen = (startColor.getGreen() + endColor.getGreen()) / 2;
        int mixedBlue = (startColor.getBlue() + endColor.getBlue()) / 2;
        Color mixedColor = Render2D.injectAlpha(new Color(mixedRed, mixedGreen, mixedBlue), 255);

        // Определяем зону сглаживания (например, 10% от длины спирали с каждой стороны)
        float smoothingRange = 0.1f; // 10% от длины спирали
        float smoothingEnd = 1.0f - smoothingRange;

        for (int j = 0; j < size - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - SoupAPI_Main.initTime) / animationSpeed)) % 360) / alphaDivider;
            float progress = (j / (float) size + progressOffset) % 1f;

            // Определяем цвет с учётом сглаживания
            Color currentColor;
            if (j == 0 || j == size - 1) {
                // Начальная и конечная точки используют смешанный цвет
                currentColor = mixedColor;
            } else if (progress <= smoothingRange) {
                // Сглаживание в начале: интерполируем между mixedColor и цветом на smoothingStart
                float t = progress / smoothingRange;
                Color targetColor = Render2D.injectAlpha(Palette.getColor(smoothingRange), 255);
                int r = (int) (mixedColor.getRed() + t * (targetColor.getRed() - mixedColor.getRed()));
                int g = (int) (mixedColor.getGreen() + t * (targetColor.getGreen() - mixedColor.getGreen()));
                int b = (int) (mixedColor.getBlue() + t * (targetColor.getBlue() - mixedColor.getBlue()));
                currentColor = Render2D.injectAlpha(new Color(r, g, b), 255);
            } else if (progress >= smoothingEnd) {
                // Сглаживание в конце: интерполируем между цветом на smoothingEnd и mixedColor
                float t = (progress - smoothingEnd) / (1.0f - smoothingEnd);
                Color targetColor = Render2D.injectAlpha(Palette.getColor(smoothingEnd), 255);
                int r = (int) (targetColor.getRed() + t * (mixedColor.getRed() - targetColor.getRed()));
                int g = (int) (targetColor.getGreen() + t * (mixedColor.getGreen() - targetColor.getGreen()));
                int b = (int) (targetColor.getBlue() + t * (mixedColor.getBlue() - targetColor.getBlue()));
                currentColor = Render2D.injectAlpha(new Color(r, g, b), 255);
            } else {
                currentColor = Render2D.injectAlpha(Palette.getColor(progress), 255);
            }

            // Добавляем вершины: нижняя и верхняя для текущей точки
            Vec3d current = vecs.get(j);
            Color finalColor = Render2D.injectAlpha(currentColor, (int) (alpha * 255));

            bufferBuilder.vertex(matrix, (float) current.x, (float) current.y, (float) current.z)
                    .color(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());

            Vec3d next = vecs.get(j + 1);
            bufferBuilder.vertex(matrix, (float) next.x, (float) next.y + heightOffset, (float) next.z)
                    .color(finalColor.getRed(), finalColor.getGreen(), finalColor.getBlue(), finalColor.getAlpha());
        }

        Render2D.endBuilding(bufferBuilder);
    }

    public static void drawScanEsp(MatrixStack stack, Entity target) {

        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        float animationSpeed = CONFIG.targetRenderTopkaSpeed / 100f;
        float minHeightOffset = 0.01f;
        float maxHeightOffset = 0.5f;

        float radius = CONFIG.targetRenderTopkaRadius / 100f;

        double x = MathHelper.lerp(tickDelta, target.prevX, target.getX()) - mc.getEntityRenderDispatcher().camera.getPos().getX();
        double y = MathHelper.lerp(tickDelta, target.prevY, target.getY()) - mc.getEntityRenderDispatcher().camera.getPos().getY();
        double z = MathHelper.lerp(tickDelta, target.prevZ, target.getZ()) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
        double height = target.getHeight();

        float time = (System.currentTimeMillis() % 1000000) / 1000.0f;
        float t = (time * animationSpeed) % 1.0f;
        float triangleWave = t < 0.5f ? 2.0f * t : 2.0f * (1.0f - t);
        float heightStep = (float) (triangleWave * height);

        float direction = t < 0.5f ? 1.0f : -1.0f;
        boolean movingUp = direction > 0;
        float speed = 1.0f - Math.abs(2.0f * triangleWave - 1.0f);

        float heightOffset = minHeightOffset + (maxHeightOffset - minHeightOffset) * speed;
        heightOffset = Math.max(heightOffset, minHeightOffset);

        ArrayList<Vec3d> ring = generateRingVectors(radius, heightStep);

        stack.push();
        stack.translate(x, y, z);

        Matrix4f matrix = stack.peek().getPositionMatrix();

        renderRing(matrix, ring, heightOffset, movingUp);

        stack.translate(-x, -y, -z);
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    private static ArrayList<Vec3d> generateRingVectors(float radius, double heightStep) {
        ArrayList<Vec3d> ring = new ArrayList<>();
        int numPoints = 360;

        for (int i = 0; i <= numPoints; ++i) {
            double angle = Math.toRadians(i);
            double u = Math.cos(angle);
            double v = Math.sin(angle);
            ring.add(new Vec3d((float) (u * radius), (float) heightStep, (float) (v * radius)));
        }

        return ring;
    }

    private static void renderRing(Matrix4f matrix, ArrayList<Vec3d> ring, float heightOffset, boolean movingUp) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int size = ring.size();
        int colorCount = Palette.getColorsCount();

        // Угловое расстояние между цветами (в градусах)
        float angleStep = 360f / colorCount;

        // Список углов и соответствующих цветов
        List<Float> angleStops = new ArrayList<>();
        for (int i = 0; i < colorCount; i++) {
            angleStops.add(i * angleStep);
        }

        // Отрисовка кольца
        for (int i = 0; i <= size; i++) {
            int currentIndex = i % size;
            float angleDeg = (360f * currentIndex) / size;

            // Поиск двух соседних цветовых узлов
            int leftIndex = 0;
            int rightIndex;
            for (int j = 0; j < angleStops.size(); j++) {
                float stopAngle = angleStops.get(j);
                if (angleDeg >= stopAngle) {
                    leftIndex = j;
                } else {
                    break;
                }
            }
            // Если мы дошли до конца - замыкаем на 0
            rightIndex = (leftIndex + 1) % colorCount;

            float leftAngle = angleStops.get(leftIndex);
            float rightAngle = angleStops.get(rightIndex);

            float segmentSpan = (rightAngle > leftAngle) ? (rightAngle - leftAngle) : (360f - leftAngle + rightAngle);
            float t = (angleDeg - leftAngle + 360f) % 360f / segmentSpan;

            Color leftColor = Palette.getColor(leftIndex / (float) (colorCount - 1));
            Color rightColor = Palette.getColor(rightIndex / (float) (colorCount - 1));

            int r = (int) (leftColor.getRed() + t * (rightColor.getRed() - leftColor.getRed()));
            int g = (int) (leftColor.getGreen() + t * (rightColor.getGreen() - leftColor.getGreen()));
            int b = (int) (leftColor.getBlue() + t * (rightColor.getBlue() - leftColor.getBlue()));
            int a = 255;

            Color currentColor = new Color(r, g, b, a);

            Vec3d current = ring.get(currentIndex);

            float sinT = (float) Math.sin(1.0f * Math.PI / 2);
            int lowerAlpha = movingUp ? (int) (a * (1.0f - sinT)) : a;
            int upperAlpha = movingUp ? a : (int) (a * (1.0f - sinT));

            bufferBuilder.vertex(matrix, (float) current.x, (float) current.y, (float) current.z)
                    .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), lowerAlpha);
            bufferBuilder.vertex(matrix, (float) current.x, (float) current.y + heightOffset, (float) current.z)
                    .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), upperAlpha);
        }

        Render2D.endBuilding(bufferBuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }
}
