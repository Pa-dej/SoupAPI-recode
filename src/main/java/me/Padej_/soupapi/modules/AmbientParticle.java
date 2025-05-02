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
import net.minecraft.util.Identifier;
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
    private static final int POSITION_CHECK_INTERVAL = 20;
    private static final Random RANDOM = new Random();

    private static final List<Particle> particles = new ArrayList<>();
    private static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();

    private static Vec3d lastPlayerPos = null;
    private static int ticksSinceLastCheck = 0;

    private static class Particle {
        public int age;
        double posX, posY, posZ;
        double prevPosX, prevPosY, prevPosZ;
        double motionX, motionY, motionZ;
        float initialSize;
        public Identifier texture;

        Particle(double x, double y, double z) {
            this.posX = this.prevPosX = x;
            this.posY = this.prevPosY = y;
            this.posZ = this.prevPosZ = z;
            this.motionX = (RANDOM.nextFloat() - 0.5f) * 0.1f;
            this.motionY = RANDOM.nextFloat() * 0.05f;
            this.motionZ = (RANDOM.nextFloat() - 0.5f) * 0.1f;
            this.age = 0;
            this.initialSize = 0.1f + RANDOM.nextFloat() * 0.1f;
            this.texture = getRandomTexture();
        }

        void update() {
            age++;
            prevPosX = posX;
            prevPosY = posY;
            prevPosZ = posZ;

            motionX += (RANDOM.nextFloat() - 0.5f) * 0.02f;
            motionY += (RANDOM.nextFloat() - 0.5f) * 0.02f;
            motionZ += (RANDOM.nextFloat() - 0.5f) * 0.02f;

            motionX = MathHelper.clamp(motionX, -0.1f, 0.1f);
            motionY = MathHelper.clamp(motionY, -0.05f, 0.05f);
            motionZ = MathHelper.clamp(motionZ, -0.1f, 0.1f);

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            motionX *= 0.95f;
            motionY *= 0.95f;
            motionZ *= 0.95f;
        }
    }

    public static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();
        if (CONFIG.ambientParticlesIncludeFirefly) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT_GLOW);
        if (CONFIG.ambientParticlesIncludeDollar) AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR_UNBLACK);
        if (CONFIG.ambientParticlesIncludeSnowflake) AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE_UNBLACK);
        if (CONFIG.ambientParticlesIncludeHeart) AVAILABLE_TEXTURES.add(TexturesManager.HEART_UNBLACK);
        if (CONFIG.ambientParticlesIncludeStar) AVAILABLE_TEXTURES.add(TexturesManager.STAR_UNBLACK);
        if (CONFIG.ambientParticlesIncludeGlyphs) AVAILABLE_TEXTURES.add(TexturesManager.getRandomGlyphParticle());
        if (AVAILABLE_TEXTURES.isEmpty()) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT_GLOW);
    }

    private static Identifier getRandomTexture() {
        updateAvailableTextures();
        return AVAILABLE_TEXTURES.get(RANDOM.nextInt(AVAILABLE_TEXTURES.size()));
    }

    public static void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (!CONFIG.ambientParticlesEnabled) return;

        ticksSinceLastCheck++;

        if (ticksSinceLastCheck >= POSITION_CHECK_INTERVAL) {
            lastPlayerPos = mc.player.getPos();
            ticksSinceLastCheck = 0;
        }

        particles.removeIf(p -> p.age >= CONFIG.ambientParticlesLiveTime);

        if (particles.size() < CONFIG.ambientParticlesMaxCount && !isPlayerTraveling(mc)) {
            spawnNewParticle(mc);
        }

        for (Particle p : particles) {
            p.update();
        }
    }

    private static boolean isPlayerTraveling(MinecraftClient client) {
        if (lastPlayerPos == null) return false;
        return client.player.getPos().distanceTo(lastPlayerPos) > CONFIG.ambientParticlesSpawnRadius;
    }

    private static void spawnNewParticle(MinecraftClient client) {
        BlockPos playerPos = client.player.getBlockPos();
        World world = client.world;

        for (int i = 0; i < 4; i++) {
            int spawnRadius = CONFIG.ambientParticlesSpawnRadius;
            int minSpawnRadius = CONFIG.ambientParticlesIgnoreSpawnRadius;
            double angle = RANDOM.nextFloat() * 2 * Math.PI;
            double distance = minSpawnRadius + RANDOM.nextFloat() * (spawnRadius - minSpawnRadius);
            double offsetX = Math.cos(angle) * distance;
            double offsetZ = Math.sin(angle) * distance;
            double offsetY = RANDOM.nextFloat() * spawnRadius;

            BlockPos randomPos = playerPos.add((int) offsetX, (int) offsetY, (int) offsetZ);

            if (world.getBlockState(randomPos).isAir()) {
                double spawnX = randomPos.getX() + 0.5;
                double spawnY = randomPos.getY() + 0.5;
                double spawnZ = randomPos.getZ() + 0.5;
                particles.add(new Particle(spawnX, spawnY, spawnZ));
                break;
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
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);

        int color1 = Palette.getColor(0f).getRGB();
        int color2 = Palette.getColor(0.33f).getRGB();

        for (Particle particle : particles) {
            float iAge = particle.age + tickDelta;
            Vec3d currentPos = new Vec3d(
                    MathHelper.lerp(tickDelta, particle.prevPosX, particle.posX),
                    MathHelper.lerp(tickDelta, particle.prevPosY, particle.posY),
                    MathHelper.lerp(tickDelta, particle.prevPosZ, particle.posZ)
            ).subtract(camera.getPos());

            float lerpFactor = (float) Math.abs(Math.sin(iAge * 0.1f));
            int interpolatedColor = interpolateColor(color1, color2, lerpFactor) + 0xFF000000;

            float scale;
            int liveTime = CONFIG.ambientParticlesLiveTime;
            float halfLife = liveTime / 2.0f;
            if (iAge <= halfLife) {
                scale = particle.initialSize;
            } else {
                float shrinkFactor = (liveTime - iAge) / halfLife;
                scale = particle.initialSize * shrinkFactor;
                scale = Math.max(scale, 0.0f);
            }

            matrices.push();
            matrices.translate(currentPos.getX(), currentPos.getY(), currentPos.getZ());

            Quaternionf rotation = new Quaternionf().identity();
            rotation.rotateY(-camera.getYaw() * (float) Math.PI / 180.0f);
            rotation.rotateX(camera.getPitch() * (float) Math.PI / 180.0f);
            rotation.rotateZ((float) Math.PI);
            matrices.multiply(rotation);

            RenderSystem.setShaderTexture(0, particle.texture);
            BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            Matrix4f mat = matrices.peek().getPositionMatrix();

            buffer.vertex(mat, -scale, scale, 0).texture(0f, 1f).color(interpolatedColor);
            buffer.vertex(mat, scale, scale, 0).texture(1f, 1f).color(interpolatedColor);
            buffer.vertex(mat, scale, -scale, 0).texture(1f, 0f).color(interpolatedColor);
            buffer.vertex(mat, -scale, -scale, 0).texture(0f, 0f).color(interpolatedColor);

            BufferRenderer.drawWithGlobalProgram(buffer.end());
            matrices.pop();
        }

        RenderSystem.setShaderTexture(0, 0);
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

