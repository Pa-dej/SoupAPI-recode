package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class TotemPopParticles extends ConfigurableModule {
    public static final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static final List<Emitter> emitters = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public static void onTick() {
        emitters.removeIf(Emitter::tick);
        particles.removeIf(Particle::update);
        updateAvailableTextures();
    }

    public static void onTotemPop(Entity entity) {
        if (mc.player == null || !CONFIG.totemPopParticlesEnabled || !(entity instanceof PlayerEntity)) return;

        updateAvailableTextures();
        emitters.add(new Emitter(entity));

        Color c = Palette.getRandomColor();
        for (int i = 0; i < CONFIG.totemPopParticlesCount; i++) {
            particles.add(new Particle((float) entity.getX(),
                    (float) (entity.getY() + entity.getHeight() / 2),
                    (float) entity.getZ(),
                    c,
                    MathUtility.random(0, 180),
                    MathUtility.random(10f, 60f)
            ));
        }
    }

    public static void render(WorldRenderContext context) {
        RenderSystem.disableDepthTest();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(context.matrixStack(), context.tickCounter().getTickDelta(true));
            }
        }
        RenderSystem.enableDepthTest();
    }

    private static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();

        if (CONFIG.totemPopParticlesIncludeFirefly) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        if (CONFIG.totemPopParticlesIncludeDollar) AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        if (CONFIG.totemPopParticlesIncludeSnowflake) AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        if (CONFIG.totemPopParticlesIncludeHeart) AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        if (CONFIG.totemPopParticlesIncludeStar) AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        if (CONFIG.totemPopParticlesIncludeGlyphs) AVAILABLE_TEXTURES.add(TexturesManager.getRandomGlyphParticle());

        if (AVAILABLE_TEXTURES.isEmpty()) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY);
        }
    }

    public static class Particle {
        float x, y, z, px, py, pz;
        float motionX, motionY, motionZ;
        float rotationAngle, rotationSpeed;
        long time;
        Color color;
        Identifier glyphTexture;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed) {
            int speed = CONFIG.totemPopParticlesSpeed;
            this.x = x;
            this.y = y;
            this.z = z;
            this.px = x;
            this.py = y;
            this.pz = z;
            this.motionX = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.motionY = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.motionZ = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.time = System.currentTimeMillis();
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            if (!AVAILABLE_TEXTURES.isEmpty()) {
                this.glyphTexture = AVAILABLE_TEXTURES.get(RANDOM.nextInt(AVAILABLE_TEXTURES.size()));
            }
        }

        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            px = x;
            py = y;
            pz = z;

            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - CONFIG.totemPopParticlesScale / 10f, z)) {
                motionY = -motionY / 1.1f;
                motionX /= 1.1f;
                motionZ /= 1.1f;
            } else if (posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) ||
                    posBlock(x + sp, y, z - sp) || posBlock(x - sp, y, z + sp) ||
                    posBlock(x + sp, y, z) || posBlock(x - sp, y, z) ||
                    posBlock(x, y, z + sp) || posBlock(x, y, z - sp)) {
                motionX = -motionX;
                motionZ = -motionZ;
            }

            if (CONFIG.totemPopParticlesPhysic.equals(TotemPopParticles.Physic.BOUNCE)) {
                motionY -= 0.035f;
            }

            motionX /= 1.005f;
            motionY /= 1.005f;
            motionZ /= 1.005f;

            return System.currentTimeMillis() - time > CONFIG.totemPopParticlesRenderTime * 1000L;
        }

        public void render(MatrixStack matrixStack, float tickDelta) {
            if (!CONFIG.totemPopParticlesEnabled) return;
            float scale = 0.07f;
            float size = CONFIG.totemPopParticlesScale;

            double posX = MathHelper.lerp(tickDelta, px, x) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double posY = MathHelper.lerp(tickDelta, py, y) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double posZ = MathHelper.lerp(tickDelta, pz, z) - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);
            matrixStack.scale(scale, scale, scale);
            matrixStack.translate(size / 2f, size / 2f, size / 2f);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle += (1f / MinecraftClient.getInstance().getCurrentFps()) * rotationSpeed));
            matrixStack.translate(-size / 2f, -size / 2f, -size / 2f);

            if (glyphTexture != null) {
                Render2D.drawGlyphs(matrixStack, glyphTexture, color, size);
            }

            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    private static class Emitter {
        final Entity entity;
        final long startTime;
        final long duration = 30L * 50L; // 30 тиков * 50мс = 1.5 секунды

        public Emitter(Entity entity) {
            this.entity = entity;
            this.startTime = System.currentTimeMillis();
        }

        public boolean tick() {
            if (System.currentTimeMillis() - startTime > duration) return true;

            Color c = Palette.getRandomColor();
            int perTick = Math.max(1, CONFIG.totemPopParticlesCount / 30);
            for (int i = 0; i < perTick; i++) {
                particles.add(new Particle(
                        (float) entity.getX(),
                        (float) (entity.getY() + entity.getHeight() / 2f),
                        (float) entity.getZ(),
                        c,
                        MathUtility.random(0, 180),
                        MathUtility.random(10f, 60f)
                ));
            }
            return false;
        }
    }

    public enum Physic {
        BOUNCE, FLY
    }
}

