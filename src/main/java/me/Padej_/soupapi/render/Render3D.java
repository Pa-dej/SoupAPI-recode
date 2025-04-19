package me.Padej_.soupapi.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.TargetRender;
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
import org.joml.Math;
import org.joml.*;
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
                    .color(redTip, greenTip, blueTip, alpha)
                    .normal(0, -1, 0);

            // Вторая грань (для заполнения обратной стороны)
            vertexConsumer.vertex(matrix, x2, height, z2)
                    .color(red2, green2, blue2, isHalf ? alpha2 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, x1, height, z1)
                    .color(red1, green1, blue1, isHalf ? alpha1 : alpha)
                    .normal(0, -1, 0);
            vertexConsumer.vertex(matrix, tipX, 0.0F, tipZ)
                    .color(redTip, greenTip, blueTip, alpha)
                    .normal(0, -1, 0);
        }
    }

    public static void renderTargetSelection(MatrixStack matrixStack, Camera camera, float tickDelta, Entity target, float rollAngle) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && target != null) {
            renderTarget(matrixStack, camera, tickDelta, target, rollAngle);
        }
    }

    private static void renderTarget(MatrixStack modelMatrix, Camera camera, float tickDelta, Entity targetEntity, float rollAngle) {
        VertexConsumerProvider.Immediate vertexConsumerProvider = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CustomRenderLayers.QUAD_IN_BLOCKS.apply(TexturesManager.getTargetRenderTexture()));
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
        float halfSize = (CONFIG.targetRenderLegacyScale / 50f) / 2.0F;
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
        int espLength = CONFIG.targetRenderSoulLenght; // 4 - 20
        float factor = CONFIG.targetRenderSoulFactor; // spin speed
        float shaking = 2f;

        float layerSpacing = 2;

        float amplitude = CONFIG.targetRenderSoulAmplitude;
        float radius = CONFIG.targetRenderSoulRadius / 100f;

        float startSize = CONFIG.targetRenderSoulStartSize / 100f; // 20 - 100
        float endSize = CONFIG.targetRenderSoulEndSize / 100f;

        float scaleModifier = CONFIG.targetRenderSoulScale / 100f;
        int subdivisions = CONFIG.targetRenderSoulSubdivision;
        boolean isFadeOut = true;

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
        TargetRender.TargetRenderSoulStyle.setupBlendFunc();
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (int j = 0; j < 3; j++) {
            for (int i = 0; i < espLength; i++) {
                for (int sub = 0; sub < subdivisions; sub++) {
                    float t = (float) sub / subdivisions;
                    float stepIndex = i + t;

                    double radians = Math.toRadians((((stepIndex) / 1.5f + iAge) * factor + (j * 120)) % (factor * 360));
                    double sinQuad = Math.sin(Math.toRadians(iAge * 2.5f + stepIndex * (j + 1)) * amplitude) / shaking;

                    float offset = ((stepIndex) / espLength) * layerSpacing;

                    MatrixStack particleMatrix = new MatrixStack();
                    particleMatrix.multiplyPositionMatrix(baseMatrix);

                    float x = (float) (Math.cos(radians) * radius);
                    float z = (float) (Math.sin(radians) * radius);
                    float y = (float) sinQuad;

                    particleMatrix.translate(x, y, z);

                    Matrix4f matrix = particleMatrix.peek().getPositionMatrix();

                    float animProgress = (iAge * 0.03f + stepIndex * 0.07f + j * 0.15f) % 1f;
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
        for (int i = 0; i <= 360; ++i) { // <= 360, чтобы последний угол = 360 (т.е. 0°)
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
    private static void renderSpiral(Matrix4f matrix, ArrayList<Vec3d> vecs, float progressOffset,
                                     float heightOffset, float animationSpeed, float alphaDivider) {
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

    public static void drawScanEsp(MatrixStack stack, @NotNull Entity target) {
        MinecraftClient mc = MinecraftClient.getInstance();
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        float animationSpeed = CONFIG.targetRenderTopkaSpeed / 100f; // 10 - 70
        float minHeightOffset = 0.01f;
        float maxHeightOffset = 0.5f;

        float radius = CONFIG.targetRenderTopkaRadius / 100f; // 50 - 80

        // Интерполяция позиции цели относительно камеры
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
        float speed = 1.0f - Math.abs(2.0f * triangleWave - 1.0f); // 1 в центре, 0 в крайних точках

        // Масштабируем высоту кольца в зависимости от скорости
        float heightOffset = minHeightOffset + (maxHeightOffset - minHeightOffset) * speed;
        heightOffset = Math.max(heightOffset, minHeightOffset);

        // Генерация точек для замкнутого кольца
        ArrayList<Vec3d> ring = generateRingVectors(radius, heightStep, heightOffset);

        stack.push();
        stack.translate(x, y, z);

        Matrix4f matrix = stack.peek().getPositionMatrix();

        // Рендеринг кольца с учётом направления
        renderRing(matrix, ring, heightOffset, movingUp);

        stack.translate(-x, -y, -z);
        RenderSystem.enableDepthTest();
        stack.pop();
    }

    // Метод для генерации точек кольца
    private static ArrayList<Vec3d> generateRingVectors(float radius, double heightStep, float heightOffset) {
        ArrayList<Vec3d> ring = new ArrayList<>();
        int numPoints = 360; // 360 градусов, с замыканием

        for (int i = 0; i <= numPoints; ++i) {
            double angle = Math.toRadians(i); // Угол для кольца
            double u = Math.cos(angle);
            double v = Math.sin(angle);
            ring.add(new Vec3d((float) (u * radius), (float) heightStep, (float) (v * radius)));
        }

        return ring;
    }

    private static void renderRing(Matrix4f matrix, ArrayList<Vec3d> ring, float heightOffset, boolean movingUp) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        int size = ring.size();

        // Получаем начальный и конечный цвета
        Color startColor = Palette.getColor(0.0f);
        Color endColor = Palette.getColor(1.0f);

        // Вычисляем смешанный цвет (среднее между начальным и конечным)
        int mixedRed = (startColor.getRed() + endColor.getRed()) / 2;
        int mixedGreen = (startColor.getGreen() + endColor.getGreen()) / 2;
        int mixedBlue = (startColor.getBlue() + endColor.getBlue()) / 2;
        Color mixedColor = Render2D.injectAlpha(new Color(mixedRed, mixedGreen, mixedBlue), 255);

        // Определяем зону сглаживания (например, 10% от длины кольца с каждой стороны)
        float smoothingRange = 0.1f; // 10% от длины кольца
        float smoothingEnd = 1.0f - smoothingRange;

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);

        for (int i = 0; i <= size; i++) {
            int currentIndex = i % size;

            // Прогресс для текущей точки
            float progress = (i / (float) size);

            // Определяем базовый цвет с учётом сглаживания
            Color currentColor;
            if (i == 0 || i == size) {
                currentColor = mixedColor;
            } else if (progress <= smoothingRange) {
                float t = progress / smoothingRange;
                Color targetColor = Render2D.injectAlpha(Palette.getColor(smoothingRange), 255);
                int r = (int) (mixedColor.getRed() + t * (targetColor.getRed() - mixedColor.getRed()));
                int g = (int) (mixedColor.getGreen() + t * (targetColor.getGreen() - mixedColor.getGreen()));
                int b = (int) (mixedColor.getBlue() + t * (targetColor.getBlue() - mixedColor.getBlue()));
                currentColor = Render2D.injectAlpha(new Color(r, g, b), 255);
            } else if (progress >= smoothingEnd) {
                float t = (progress - smoothingEnd) / (1.0f - smoothingEnd);
                Color targetColor = Render2D.injectAlpha(Palette.getColor(smoothingEnd), 255);
                int r = (int) (targetColor.getRed() + t * (mixedColor.getRed() - targetColor.getRed()));
                int g = (int) (targetColor.getGreen() + t * (targetColor.getGreen() - mixedColor.getGreen()));
                int b = (int) (targetColor.getBlue() + t * (targetColor.getBlue() - targetColor.getBlue()));
                currentColor = Render2D.injectAlpha(new Color(r, g, b), 255);
            } else {
                currentColor = Render2D.injectAlpha(Palette.getColor(progress), 255);
            }

            Vec3d current = ring.get(currentIndex);

            // Определяем альфа-канал с нелинейной интерполяцией
            int baseAlpha = currentColor.getAlpha(); // Базовая прозрачность цвета
            float t; // Параметр интерполяции (0 на нижней границе, 1 на верхней)
            int lowerAlpha;
            int upperAlpha;

            if (movingUp) {
                t = 1.0f; // Верхняя точка полностью непрозрачна
                upperAlpha = baseAlpha;
                t = (float) Math.sin(t * Math.PI / 2); // Нелинейная интерполяция (sinusoidal easing)
                lowerAlpha = (int) (baseAlpha * (1.0f - t)); // Нижняя точка затухает нелинейно
            } else {
                t = 1.0f; // Нижняя точка полностью непрозрачна
                lowerAlpha = baseAlpha;
                t = (float) Math.sin(t * Math.PI / 2); // Нелинейная интерполяция
                upperAlpha = (int) (baseAlpha * (1.0f - t)); // Верхняя точка затухает нелинейно
            }

            // Добавляем вершины: нижняя и верхняя для текущей точки
            bufferBuilder.vertex(matrix, (float) current.x, (float) current.y, (float) current.z)
                    .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), lowerAlpha);
            bufferBuilder.vertex(matrix, (float) current.x, (float) current.y + heightOffset, (float) current.z)
                    .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), upperAlpha);
        }

        Render2D.endBuilding(bufferBuilder);
        Render3D.endRender();
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
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
}
