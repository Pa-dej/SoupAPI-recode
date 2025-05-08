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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class JumpParticles extends ConfigurableModule {
    public static final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static boolean wasJumping = false;

    public static void onTick() {
        if (mc.player == null || !CONFIG.jumpParticlesEnabled) return;
        updateAvailableTextures();

        boolean isJumping = !mc.player.isOnGround();
        if (isJumping && !wasJumping && mc.options.jumpKey.isPressed()) {
            spawnParticles(mc.player);
        }
        wasJumping = isJumping;

        particles.removeIf(Particle::update);
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

        if (CONFIG.jumpParticlesIncludeFirefly) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        if (CONFIG.jumpParticlesIncludeDollar) AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        if (CONFIG.jumpParticlesIncludeSnowflake) AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        if (CONFIG.jumpParticlesIncludeHeart) AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        if (CONFIG.jumpParticlesIncludeStar) AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        if (CONFIG.jumpParticlesIncludeGlyphs) {
            Collections.addAll(AVAILABLE_TEXTURES, TexturesManager.GLYPH_TEXTURES);
        }

        if (AVAILABLE_TEXTURES.isEmpty()) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY);
        }
    }

    private static void spawnParticles(PlayerEntity player) {
        Color c = Palette.getRandomColor();
        for (int i = 0; i < CONFIG.jumpParticlesCount; i++) {
            particles.add(new Particle(
                    (float) player.getX(),
                    (float) (player.getY()),
                    (float) player.getZ(),
                    c,
                    MathUtility.random(0, 180),
                    MathUtility.random(10f, 60f)
            ));
        }
    }

    public static class Particle extends TotemPopParticles.Particle {
        private final long lifeTimeMs;
        private float alpha = 1.0f;
        private float scale = CONFIG.jumpParticlesScale;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed) {
            super(x, y, z, color, rotationAngle, rotationSpeed);
            int speed = CONFIG.jumpParticlesSpeed;
            this.motionX = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.motionY = 0;
            this.motionZ = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.time = System.currentTimeMillis();
            this.lifeTimeMs = CONFIG.jumpParticlesLiveTime * 1000L;

            if (!AVAILABLE_TEXTURES.isEmpty()) {
                this.glyphTexture = AVAILABLE_TEXTURES.get(new Random().nextInt(AVAILABLE_TEXTURES.size()));
            }
        }

        @Override
        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);

            px = x; py = y; pz = z;
            x += motionX;
            y += motionY;
            z += motionZ;

            boolean bounce = CONFIG.jumpParticlesPhysic == Physic.BOUNCE;

            if (posBlock(x, y - CONFIG.jumpParticlesScale / 10f, z)) {
                if (bounce) {
                    motionY = 0.18f + MathUtility.random(0.0f, 0.05f);
                    motionX += MathUtility.random(-0.005f, 0.005f);
                    motionZ += MathUtility.random(-0.005f, 0.005f);
                } else {
                    motionY = -motionY * 0.8f;
                }

                motionX *= 0.98f;
                motionZ *= 0.98f;
            }

            else if (posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) ||
                    posBlock(x + sp, y, z - sp) || posBlock(x - sp, y, z + sp) ||
                    posBlock(x + sp, y, z)     || posBlock(x - sp, y, z)     ||
                    posBlock(x, y, z + sp)     || posBlock(x, y, z - sp)) {
                motionX = -motionX * 0.9f;
                motionZ = -motionZ * 0.9f;
            }

            if (bounce) motionY -= 0.035f;
            motionY *= 0.99f;

            long elapsed = System.currentTimeMillis() - time;
            float lifeFraction = Math.min(1.0f, (float) elapsed / lifeTimeMs);

            if (lifeFraction >= 0.8f) {
                float fade = 1.0f - (lifeFraction - 0.8f) / 0.2f;
                if (CONFIG.jumpParticlesDisappear == Disappear.ALPHA) {
                    alpha = fade;
                } else if (CONFIG.jumpParticlesDisappear == Disappear.SCALE) {
                    scale = CONFIG.jumpParticlesScale * fade;
                }
            }

            return elapsed > lifeTimeMs;
        }

        @Override
        public void render(MatrixStack matrixStack, float tickDelta) {
            if (!CONFIG.jumpParticlesEnabled) return;

            float baseScale = 0.07f;
            float renderScale = baseScale * scale;

            double posX = MathHelper.lerp(tickDelta, px, x) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double posY = MathHelper.lerp(tickDelta, py, y) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double posZ = MathHelper.lerp(tickDelta, pz, z) - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);
            matrixStack.scale(renderScale, renderScale, renderScale);
            matrixStack.translate(0.5f, 0.5f, 0.5f);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));
            matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle += (1f / mc.getCurrentFps()) * rotationSpeed));
            matrixStack.translate(-0.5f, -0.5f, -0.5f);

            if (glyphTexture != null) {
                Render2D.drawGlyphs(matrixStack, glyphTexture, new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (alpha * 255)), 1f);
            }

            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            if (mc.player == null || mc.world == null) return false;
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    public enum Physic {
        BOUNCE, FLY
    }

    public enum Disappear {
        ALPHA, SCALE
    }
}

