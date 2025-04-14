package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AmbientParticle extends ConfigurableModule {
    private static final int POSITION_CHECK_INTERVAL = 20; // 10 секунд в тиках (20 тиков/с * 10 с)
    private static final Random RANDOM = new Random();

    private static class Particle {
        public int age;
        double posX, posY, posZ;
        double prevPosX, prevPosY, prevPosZ;
        double motionX, motionY, motionZ;
        float initialSize; // Начальный размер частицы

        Particle(double x, double y, double z) {
            this.posX = this.prevPosX = x;
            this.posY = this.prevPosY = y;
            this.posZ = this.prevPosZ = z;
            this.motionX = (RANDOM.nextFloat() - 0.5f) * 0.1f;
            this.motionY = RANDOM.nextFloat() * 0.05f;
            this.motionZ = (RANDOM.nextFloat() - 0.5f) * 0.1f;
            this.age = 0;
            this.initialSize = 0.1f + RANDOM.nextFloat() * 0.1f; // Размер от 0.1 до 0.2 с погрешностью
        }

        void update() {
            age++;
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            // Случайное изменение направления, как у мотылька
            motionX += (RANDOM.nextFloat() - 0.5f) * 0.02f;
            motionY += (RANDOM.nextFloat() - 0.5f) * 0.02f;
            motionZ += (RANDOM.nextFloat() - 0.5f) * 0.02f;

            // Ограничиваем скорость
            motionX = MathHelper.clamp(motionX, -0.1f, 0.1f);
            motionY = MathHelper.clamp(motionY, -0.05f, 0.05f);
            motionZ = MathHelper.clamp(motionZ, -0.1f, 0.1f);

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            // Затухание скорости
            motionX *= 0.95f;
            motionY *= 0.95f;
            motionZ *= 0.95f;
        }
    }

    private static final List<Particle> particles = new ArrayList<>();
    private static Vec3d lastPlayerPos = null; // Последняя записанная позиция игрока
    private static int ticksSinceLastCheck = 0; // Счетчик тиков для проверки позиции

    public static void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!CONFIG.ambientParticlesEnabled) return;

        // Увеличиваем счетчик тиков
        ticksSinceLastCheck++;

        // Проверяем позицию игрока каждые 10 секунд
        if (ticksSinceLastCheck >= POSITION_CHECK_INTERVAL) {
            lastPlayerPos = mc.player.getPos(); // Записываем текущую позицию
            ticksSinceLastCheck = 0; // Сбрасываем счетчик
        }

        // Удаляем частицы, чей возраст превысил время жизни
        particles.removeIf(particle -> particle.age >= CONFIG.ambientParticlesLiveTime);

        // Создаем новую частицу, если их меньше 10 и игрок не путешествует
        if (particles.size() < CONFIG.ambientParticlesMaxCount && !isPlayerTraveling(mc)) {
            spawnNewParticle(mc);
        }

        // Обновляем все частицы
        for (Particle particle : particles) {
            particle.update();
        }
    }

    private static boolean isPlayerTraveling(MinecraftClient client) {
        if (lastPlayerPos == null) return false; // Если позиция ещё не записана, считаем, что игрок не путешествует

        Vec3d currentPos = client.player.getPos();
        double distance = currentPos.distanceTo(lastPlayerPos); // Расстояние между текущей и последней позицией
        return distance > CONFIG.ambientParticlesSpawnRadius; // Если игрок удалился больше чем на радиус спавна, он путешествует
    }

    private static void spawnNewParticle(MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        World world = client.world;

        for (int i = 0; i < 4; i++) {
            int spawnRadius = CONFIG.ambientParticlesSpawnRadius;
            int minSpawnRadius = CONFIG.ambientParticlesIgnoreSpawnRadius;
            double angle = RANDOM.nextFloat() * 2 * Math.PI; // Случайный угол
            double distance = minSpawnRadius + RANDOM.nextFloat() * (spawnRadius - minSpawnRadius); // Расстояние от 3 до 5
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = RANDOM.nextFloat() * spawnRadius; // Высота только вверх

            BlockPos randomPos = playerPos.add((int) offsetX, (int) offsetY, (int) offsetZ);

            if (world.getBlockState(randomPos).isAir()) {
                double spawnX = randomPos.getX() + 0.5;
                double spawnY = randomPos.getY() + 0.5;
                double spawnZ = randomPos.getZ() + 0.5;
                particles.add(new Particle(spawnX, spawnY, spawnZ));
                break; // Создаем только одну частицу за вызов
            }
        }
    }

    public static void renderParticlesInWorld(WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null || particles.isEmpty()) return;
        if (!CONFIG.ambientParticlesEnabled) return;

        Camera camera = context.camera();
        MatrixStack matrices = context.matrixStack();
        float tickDelta = context.tickCounter().getTickDelta(true);

        RenderSystem.enableBlend();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        RenderSystem.setShaderTexture(0, TexturesManager.FIREFLY_ALT_GLOW);
        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        int color1 = Palette.getColor(0f).getRGB(); // Основной цвет из конфига
        int color2 = Palette.getColor(0.33f).getRGB(); // Вторичный цвет из конфига

        for (Particle particle : particles) {
            float iAge = particle.age + tickDelta;
            Vec3d currentPos = new Vec3d(
                    MathHelper.lerp(tickDelta, particle.prevPosX, particle.posX),
                    MathHelper.lerp(tickDelta, particle.prevPosY, particle.posY),
                    MathHelper.lerp(tickDelta, particle.prevPosZ, particle.posZ)
            ).subtract(camera.getPos());

            matrices.push();
            matrices.translate(currentPos.getX(), currentPos.getY(), currentPos.getZ());

            Quaternionf rotation = new Quaternionf().identity();
            rotation.rotateY(-camera.getYaw() * (float) Math.PI / 180.0f);
            rotation.rotateX(camera.getPitch() * (float) Math.PI / 180.0f);
            matrices.multiply(rotation);

            Matrix4f baseMatrix = matrices.peek().getPositionMatrix();

            float lerpFactor = (float) Math.abs(Math.sin(iAge * 0.1f));
            int interpolatedColor = interpolateColor(color1, color2, lerpFactor) + 0xFF000000; // Фиксированная альфа

            float scale;
            int liveTime = CONFIG.ambientParticlesLiveTime;
            float halfLife = liveTime / 2.0f;
            if (iAge <= halfLife) {
                scale = particle.initialSize;
            } else {
                float shrinkFactor = (liveTime - iAge) / halfLife; // От 1 до 0
                scale = particle.initialSize * shrinkFactor; // Уменьшение размера
                scale = Math.max(scale, 0.0f);
            }

            buffer.vertex(baseMatrix, -scale, scale, 0).texture(0f, 1f).color(interpolatedColor);
            buffer.vertex(baseMatrix, scale, scale, 0).texture(1f, 1f).color(interpolatedColor);
            buffer.vertex(baseMatrix, scale, -scale, 0).texture(1f, 0f).color(interpolatedColor);
            buffer.vertex(baseMatrix, -scale, -scale, 0).texture(0f, 0f).color(interpolatedColor);

            matrices.pop();
        }

        BufferRenderer.drawWithGlobalProgram(buffer.end());
        RenderSystem.setShaderTexture(0, 0);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private static int interpolateColor(int color1, int color2, float factor) {
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int r = (int) (r2 + factor * (r1 - r2));
        int g = (int) (g2 + factor * (g1 - g2));
        int b = (int) (b2 + factor * (b1 - b2));

        return (r << 16) | (g << 8) | b;
    }
}
