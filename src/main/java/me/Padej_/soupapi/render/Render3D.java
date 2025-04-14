package me.Padej_.soupapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;

public class Render3D extends ConfigurableModule {
    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

    public static void renderChinaHat(MatrixStack matrices, VertexConsumer vertexConsumer) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        // Параметры "China Hat"
        float baseRadius = 0.67F;
        float height = 0.35F;
        int segments = 60;
        float time = MinecraftClient.getInstance().world.getTime() % 360; // Для анимации вращения
        float alpha = 70 / 255f; // Альфа из исходного кода
        boolean isHalf = CONFIG.chinaHatRenderHalf;

        // Параметры для эффекта переливания
        int rotation = 360 / 5; // Скорость вращения эффекта (можно настроить)
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
                float distanceFromCenter = (float) Math.sqrt(xCoords[i] * xCoords[i] + zCoords[i] * zCoords[i]);
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
        float tipAlpha = isHalf ? alpha : alpha; // Вершина сохраняет полную прозрачность

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
            vertexConsumer.vertex(matrix, tipX, 0.0F, tipZ)
                    .color(redTip, greenTip, blueTip, tipAlpha)
                    .normal(0, -1, 0);

            // Вторая грань (для заполнения обратной стороны)
            vertexConsumer.vertex(matrix, x2, height, z2)
                    .color(red2, green2, blue2, isHalf ? alpha2 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, x1, height, z1)
                    .color(red1, green1, blue1, isHalf ? alpha1 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, tipX, 0.0F, tipZ)
                    .color(redTip, greenTip, blueTip, tipAlpha)
                    .normal(0, -1, 0);
        }
    }

    public static void renderTargetSelection(MatrixStack matrixStack, Camera camera, float tickDelta, float rollAngle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            Entity targetEntity = EntityUtils.getTargetEntity();
            if (targetEntity != null) {
                renderTarget(matrixStack, camera, tickDelta, targetEntity, rollAngle);
            }

        }
    }

    private static void renderTarget(MatrixStack modelMatrix, Camera camera, float tickDelta, Entity targetEntity, float rollAngle) {
        VertexConsumerProvider.Immediate vertexConsumerProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer((RenderLayer) CustomRenderLayers.QUAD_IN_BLOCKS.apply(TexturesManager.getTargetRenderTexture()));
        if (targetEntity != null) {
            Vec3d transformedPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
            modelMatrix.push();
            applyTranslationAndRotation(modelMatrix, transformedPos, camera);
            modelMatrix.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rollAngle));
            drawTargetQuad(vertexConsumer, modelMatrix);
            modelMatrix.pop();
        }
    }

    private static void applyTranslationAndRotation(MatrixStack modelMatrix, Vec3d transformedPos, Camera camera) {
        modelMatrix.translate(transformedPos.x, transformedPos.y, transformedPos.z);
        modelMatrix.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        modelMatrix.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
    }

    private static void drawTargetQuad(VertexConsumer vertexConsumer, MatrixStack modelMatrix) {
        Matrix4f matrix = modelMatrix.peek().getPositionMatrix();
        float halfSize = (CONFIG.targetRenderScale / 50f) / 2.0F;
        float alpha = 1.0F;

        float time = MinecraftClient.getInstance().world.getTime() % 360;
        int rotation = 360 / 5; // Можно настраивать плавность
        float rotationOffset = (time % rotation) / (float) rotation;

        // Получаем цвета палитры по углу
        Color color0 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 0.25), rotationOffset)); // Левый верх
        Color color1 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 0.75), rotationOffset)); // Правый верх
        Color color2 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 1.25), rotationOffset)); // Правый низ
        Color color3 = Palette.getColor(RenderWithAnimatedColor.getWaveInterpolation((float) (Math.PI * 1.75), rotationOffset)); // Левый низ

        // Отрисовка вершин с уникальными цветами
        vertexConsumer.vertex(matrix, -halfSize, halfSize, 0.0F)
                .color(color0.getRed() / 255f, color0.getGreen() / 255f, color0.getBlue() / 255f, alpha)
                .texture(0.0F, 0.0F)
                .light(15728880);

        vertexConsumer.vertex(matrix, halfSize, halfSize, 0.0F)
                .color(color1.getRed() / 255f, color1.getGreen() / 255f, color1.getBlue() / 255f, alpha)
                .texture(1.0F, 0.0F)
                .light(15728880);

        vertexConsumer.vertex(matrix, halfSize, -halfSize, 0.0F)
                .color(color2.getRed() / 255f, color2.getGreen() / 255f, color2.getBlue() / 255f, alpha)
                .texture(1.0F, 1.0F)
                .light(15728880);

        vertexConsumer.vertex(matrix, -halfSize, -halfSize, 0.0F)
                .color(color3.getRed() / 255f, color3.getGreen() / 255f, color3.getBlue() / 255f, alpha)
                .texture(0.0F, 1.0F)
                .light(15728880);
    }

    private static Vec3d calculateEntityPositionRelativeToCamera(Camera camera, float tickDelta, Entity targetEntity) {
        double interpolatedX = MathHelper.lerp(tickDelta, targetEntity.prevX, targetEntity.getX());
        double interpolatedY = MathHelper.lerp(tickDelta, targetEntity.prevY, targetEntity.getY()) + (double) (targetEntity.getHeight() / 2.0F);
        double interpolatedZ = MathHelper.lerp(tickDelta, targetEntity.prevZ, targetEntity.getZ());
        Vec3d entityPos = new Vec3d(interpolatedX, interpolatedY, interpolatedZ);
        return entityPos.subtract(camera.getPos());
    }

    public static void renderSoulsEsp(float tickDelta, Entity targetEntity) {
        int espLength = 10;
        float factorX = 5;
        float factorY = 5;
        float factorZ = 5;
        float amplitude = 5.5f;
        float shaking = 2f;
        float radius = 1;

        float startSize = 0.3f;
        float endSize = 0.1f;
        float layerSpacing = 2;

        MinecraftClient mc = MinecraftClient.getInstance();
        Camera camera = mc.gameRenderer.getCamera();

        if (targetEntity == null) return;
        Vec3d newPos = calculateEntityPositionRelativeToCamera(camera, tickDelta, targetEntity);
        float iAge = MathHelper.lerp(tickDelta, targetEntity.age - 1, targetEntity.age);

        MatrixStack matrices = new MatrixStack();
        matrices.push();
        matrices.translate(newPos.getX(), newPos.getY(), newPos.getZ());

        Quaternionf rotation = new Quaternionf().identity();
        rotation.rotateY(-camera.getYaw() * (float) Math.PI / 180.0f);
        rotation.rotateX(camera.getPitch() * (float) Math.PI / 180.0f);
        matrices.multiply(rotation);

        Matrix4f baseMatrix = matrices.peek().getPositionMatrix();

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR);
        RenderSystem.setShaderTexture(0, TexturesManager.FIREFLY);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i <= espLength; i++) {
                double radiansX = Math.toRadians((((float) i / 1.5f + iAge) * factorX + (j * 120)) % (factorX * 360));
                double radiansY = Math.toRadians((((float) i / 1.5f + iAge) * factorY + (j * 120)) % (factorY * 360));
                double radiansZ = Math.toRadians((((float) i / 1.5f + iAge) * factorZ + (j * 120)) % (factorZ * 360));

                double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + i * (j + 1)) * amplitude) / shaking;

                float offset = ((float) i / espLength) * layerSpacing;

                MatrixStack particleMatrix = new MatrixStack();
                particleMatrix.multiplyPositionMatrix(baseMatrix);

                Quaternionf rotationX = new Quaternionf();
                rotationX.rotateAxis(1, 0, 0, (float) radiansX);
                Quaternionf rotationY = new Quaternionf();
                rotationY.rotateAxis(0, 1, 0, (float) radiansY);
                Quaternionf rotationZ = new Quaternionf();
                rotationZ.rotateAxis(0, 0, 1, (float) radiansZ);

                particleMatrix.multiply(rotationX.invert());
                particleMatrix.multiply(rotationY);
                particleMatrix.multiply(rotationZ.invert());

                particleMatrix.translate(
                        Math.cos(radiansY) * radius,
                        sinQuad,
                        Math.sin(radiansX) * radius
                );

                Matrix4f matrix = particleMatrix.peek().getPositionMatrix();

                float animProgress = (iAge * 0.03f + i * 0.07f + j * 0.15f) % 1f;
                Color color = Palette.getInterpolatedPaletteColor(animProgress);
                int argb = 0xFF000000 | (color.getRed() << 16) | (color.getGreen() << 8) | color.getBlue();

                float scale = Math.max(endSize + offset * (startSize - endSize), 0.15f);

                buffer.vertex(matrix, -scale, scale, 0).texture(0f, 1f).color(argb);
                buffer.vertex(matrix, scale, scale, 0).texture(1f, 1f).color(argb);
                buffer.vertex(matrix, scale, -scale, 0).texture(1f, 0f).color(argb);
                buffer.vertex(matrix, -scale, -scale, 0).texture(0f, 0f).color(argb);
            }
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();

        matrices.pop();
    }

    public static void drawSpiralsEsp(MatrixStack stack, @NotNull Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);

        // Локальные настройки
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
        stack.translate(x, y, z);
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

    // Метод для генерации векторов спиралей с угловыми сдвигами
    private static void generateSpiralVectors(ArrayList<Vec3d>[] spirals, float radius, double initialHeight, float heightStep) {
        for (int i = 0; i <= 361; ++i) {
            double height = initialHeight - i * heightStep;
            for (int spiralIndex = 0; spiralIndex < 3; spiralIndex++) {
                double angle = Math.toRadians(i + spiralIndex * 120) % 360;
                double u = Math.cos(angle);
                double v = Math.sin(angle);
                spirals[spiralIndex].add(new Vec3d((float) (u * radius), (float) height, (float) (v * radius)));
            }
        }
    }

    // Метод для рендеринга одной спирали
    private static void renderSpiral(Matrix4f matrix, ArrayList<Vec3d> vecs, float progressOffset,
                                     float heightOffset, float animationSpeed, float alphaDivider) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (int j = 0; j < vecs.size() - 1; ++j) {
            float alpha = 1f - (((float) j + ((System.currentTimeMillis() - SoupAPI_Main.initTime) / animationSpeed)) % 360) / alphaDivider;
            float progress = (j / (float) vecs.size() + progressOffset) % 1f;

            Color colorA = Palette.getColor(progress);
            Color colorB = Palette.getColor((progress + 0.05f) % 1f);

            bufferBuilder.vertex(matrix, (float) vecs.get(j).x, (float) vecs.get(j).y, (float) vecs.get(j).z)
                    .color(Render2D.injectAlpha(colorA, (int) (alpha * 255)).getRGB());
            bufferBuilder.vertex(matrix, (float) vecs.get(j + 1).x, (float) vecs.get(j + 1).y + heightOffset, (float) vecs.get(j + 1).z)
                    .color(Render2D.injectAlpha(colorB, (int) (alpha * 255)).getRGB());
        }

        Render2D.endBuilding(bufferBuilder);
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }

    public static void endRender() {
        RenderSystem.disableBlend();
    }

    public static @NotNull Vec3d worldSpaceToScreenSpace(@NotNull Vec3d pos) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        int displayHeight = mc.getWindow().getHeight();
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        Vector3f target = new Vector3f();
        double scale = mc.getWindow().getScaleFactor();

        double deltaX = pos.x - camera.getPos().x;
        double deltaY = pos.y - camera.getPos().y;
        double deltaZ = pos.z - camera.getPos().z;

        Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
        Matrix4f matrixProj = new Matrix4f(lastProjMat);
        Matrix4f matrixModel = new Matrix4f(lastModMat);
        matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

        return new Vec3d(target.x / scale, (displayHeight - target.y) / scale, target.z);
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int r1 = color1 >> 16 & 255;
        int g1 = color1 >> 8 & 255;
        int b1 = color1 & 255;
        int r2 = color2 >> 16 & 255;
        int g2 = color2 >> 8 & 255;
        int b2 = color2 & 255;
        int r = (int) ((float) r1 + factor * (float) (r2 - r1));
        int g = (int) ((float) g1 + factor * (float) (g2 - g1));
        int b = (int) ((float) b1 + factor * (float) (b2 - b1));
        return r << 16 | g << 8 | b;
    }
}
