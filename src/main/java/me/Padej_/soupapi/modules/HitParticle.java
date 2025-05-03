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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.List;

public class HitParticle extends ConfigurableModule {
    private static final HashMap<Integer, Float> healthMap = new HashMap<>();
    public static final CopyOnWriteArrayList<Particle> particles = new CopyOnWriteArrayList<>();
    private static VertexConsumerProvider vertexConsumerProvider;
    private static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static final Random RANDOM = new Random();

    public static void onTick() {
        particles.removeIf(Particle::update);
        if (mc.player == null || !CONFIG.hitParticlesEnabled) return;

        updateAvailableTextures();

        if (!CONFIG.hitParticlesTextMode.equals(HitTextMode.DISABLED)) {
            if (CONFIG.hitParticlesTextMode == HitTextMode.ALL_ENTITIES) {
                for (Entity entity : mc.world.getEntities()) {
                    if (entity instanceof LivingEntity lent && mc.player.squaredDistanceTo(entity) <= 256f && lent.isAlive()) {
                        float health = lent.getHealth() + lent.getAbsorptionAmount();
                        float lastHealth = healthMap.getOrDefault(entity.getId(), health);
                        healthMap.put(entity.getId(), health);
                        if (lastHealth == health) continue;

                        float delta = health - lastHealth;
                        if (!CONFIG.hitParticlesTextShowHeal && delta > 0) continue;

                        Color c = Palette.getRandomColor();
                        particles.add(new Particle((float) lent.getX(),
                                MathUtility.random((float) (lent.getY() + lent.getHeight()), (float) lent.getY()),
                                (float) lent.getZ(),
                                c,
                                MathUtility.random(0, 180),
                                MathUtility.random(10f, 60f),
                                delta,
                                true));
                    }
                }
            } else if (CONFIG.hitParticlesTextMode == HitTextMode.ONLY_SELF_DAMAGE && mc.player.hurtTime > 0) {
                Color c = Palette.getRandomColor();
                float delta = -1.0f;
                particles.add(new Particle((float) mc.player.getX(),
                        MathUtility.random((float) (mc.player.getY() + mc.player.getHeight()), (float) mc.player.getY()),
                        (float) mc.player.getZ(),
                        c,
                        MathUtility.random(0, 180),
                        MathUtility.random(10f, 60f),
                        delta,
                        true));
            }
        }

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (player != mc.player) continue;
            if (player.hurtTime > 0) {
                Color c = Palette.getRandomColor();
                for (int i = 0; i < CONFIG.hitParticlesCount; i++) {
                    particles.add(new Particle((float) player.getX(),
                            MathUtility.random((float) (player.getY() + player.getHeight()), (float) player.getY()),
                            (float) player.getZ(),
                            c,
                            MathUtility.random(0, 180),
                            MathUtility.random(10f, 60f),
                            0,
                            false));
                }
            }
        }
    }

    public static void render(WorldRenderContext context) {
        RenderSystem.disableDepthTest();
        vertexConsumerProvider = context.consumers();
        if (mc.player != null && mc.world != null) {
            for (Particle particle : particles) {
                particle.render(context.matrixStack(), context.tickCounter().getTickDelta(true));
            }
        }
        RenderSystem.enableDepthTest();
    }

    public static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();

        if (CONFIG.hitParticleIncludeFirefly) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        }
        if (CONFIG.hitParticleIncludeDollar) {
            AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        }
        if (CONFIG.hitParticleIncludeSnowflake) {
            AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        }
        if (CONFIG.hitParticleIncludeHeart) {
            AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        }
        if (CONFIG.hitParticleIncludeStar) {
            AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        }
        if (CONFIG.hitParticleIncludeGlyphs) {
            AVAILABLE_TEXTURES.add(TexturesManager.getRandomGlyphParticle());
        }

        if (AVAILABLE_TEXTURES.isEmpty()) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY); // дефолт
        }
    }

    public static class Particle {
        boolean isText;
        float x, y, z, px, py, pz;
        float motionX, motionY, motionZ;
        float rotationAngle, rotationSpeed, health;
        long time;
        Color color;
        Identifier glyphTexture;

        public Particle(float x, float y, float z, Color color, float rotationAngle, float rotationSpeed, float health, boolean isText) {
            int speed = CONFIG.hitParticlesSpeed;
            this.x = x; this.y = y; this.z = z;
            this.px = x; this.py = y; this.pz = z;
            this.motionX = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.motionY = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.motionZ = MathUtility.random(-(float) speed / 50f, (float) speed / 50f);
            this.time = System.currentTimeMillis();
            this.color = color;
            this.rotationAngle = rotationAngle;
            this.rotationSpeed = rotationSpeed;
            this.health = health;
            this.isText = isText;
            if (!isText && !AVAILABLE_TEXTURES.isEmpty()) {
                this.glyphTexture = AVAILABLE_TEXTURES.get(RANDOM.nextInt(AVAILABLE_TEXTURES.size()));
            }
        }

        public long getTime() {
            return time;
        }

        public boolean update() {
            double sp = Math.sqrt(motionX * motionX + motionZ * motionZ);
            px = x; py = y; pz = z;

            x += motionX;
            y += motionY;
            z += motionZ;

            if (posBlock(x, y - CONFIG.hitParticlesScale / 10f, z)) {
                motionY = -motionY / 1.1f;
                motionX /= 1.1f;
                motionZ /= 1.1f;
            } else if (posBlock(x - sp, y, z - sp) || posBlock(x + sp, y, z + sp) ||
                    posBlock(x + sp, y, z - sp) || posBlock(x - sp, y, z + sp) ||
                    posBlock(x + sp, y, z)     || posBlock(x - sp, y, z)     ||
                    posBlock(x, y, z + sp)     || posBlock(x, y, z - sp)) {
                motionX = -motionX;
                motionZ = -motionZ;
            }

            if (CONFIG.hitParticlesPhysic.equals(Physic.BOUNCE))
                motionY -= 0.035f;

            motionX /= 1.005f;
            motionY /= 1.005f;
            motionZ /= 1.005f;

            return System.currentTimeMillis() - time > CONFIG.hitParticlesRenderTime * 1000L;
        }

        public void render(MatrixStack matrixStack, float tickDelta) {
            if (!CONFIG.hitParticlesEnabled) return;
            float scale = isText ? CONFIG.hitParticlesTextScale * 0.025f : 0.07f;
            float size = CONFIG.hitParticlesScale;
            Color healColor = new Color(0x76cf41);
            Color damageColor = new Color(0xd42d2d);

            double posX = MathHelper.lerp(tickDelta, px, x) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double posY = MathHelper.lerp(tickDelta, py, y) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double posZ = MathHelper.lerp(tickDelta, pz, z) - mc.getEntityRenderDispatcher().camera.getPos().getZ();

            matrixStack.push();
            matrixStack.translate(posX, posY, posZ);
            matrixStack.scale(scale, scale, scale);

            matrixStack.translate(size / 2f, size / 2f, size / 2f);
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-mc.gameRenderer.getCamera().getYaw()));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(mc.gameRenderer.getCamera().getPitch()));

            if (isText) {
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));
            } else {
                matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rotationAngle += (1f / MinecraftClient.getInstance().getCurrentFps()) * rotationSpeed));
            }

            matrixStack.translate(-size / 2f, -size / 2f, -size / 2f);

            if (isText) {
                String hpString = MathUtility.round2(health) + " ";
                int textColor = (health > 0 ? healColor : damageColor).getRGB();
                MinecraftClient.getInstance().textRenderer.draw(
                        Text.of(hpString),
                        0,
                        0,
                        textColor,
                        false,
                        matrixStack.peek().getPositionMatrix(),
                        vertexConsumerProvider,
                        TextRenderer.TextLayerType.NORMAL,
                        0,
                        LightmapTextureManager.pack(15, 15)
                );
            } else {
                if (glyphTexture != null) {
                    Render2D.drawGlyphs(matrixStack, glyphTexture, color, size);
                }
            }

            matrixStack.pop();
        }

        private boolean posBlock(double x, double y, double z) {
            Block b = mc.world.getBlockState(BlockPos.ofFloored(x, y, z)).getBlock();
            return (!(b instanceof AirBlock) && b != Blocks.WATER && b != Blocks.LAVA);
        }
    }

    public enum Physic {
        BOUNCE, FLY
    }

    public enum HitTextMode {
        DISABLED, ALL_ENTITIES, ONLY_SELF_DAMAGE
    }
}

