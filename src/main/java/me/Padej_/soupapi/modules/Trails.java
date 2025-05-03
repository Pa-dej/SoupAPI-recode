package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.interfaces.TrailEntity;
import me.Padej_.soupapi.render.CustomRenderLayers;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.Render3D;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.Palette;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class Trails extends ConfigurableModule {

    public static void onTick() {
        if (mc.player == null) return;
        if (!CONFIG.trailsEnabled) return;

        int trailLifetime = CONFIG.trailsLenght;

        for (PlayerEntity player : mc.world.getPlayers()) {
            Vec3d velocity = player.getVelocity();
            if (Math.abs((velocity.getY() + velocity.getY() + velocity.getX())) / 3 <= 0.001f) return;
            if (player.getPos().getZ() != player.prevZ || player.getPos().getX() != player.prevX) {
                ((TrailEntity) player).soupAPI$getTrails().add(new TrailSegment(
                        new Vec3d(player.prevX, player.prevY, player.prevZ),
                        player.getPos(),
                        trailLifetime
                ));
            }
            ((TrailEntity) player).soupAPI$getTrails().removeIf(TrailSegment::update);
        }

        if (CONFIG.trailsForGliders) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity == mc.player || !(entity instanceof LivingEntity livingEntity) || !livingEntity.isGliding()) {
                    continue;
                }

                if (entity.getPos().getZ() != entity.prevZ || entity.getPos().getX() != entity.prevX) {
                    ((TrailEntity) entity).soupAPI$getTrails().add(new TrailSegment(
                            new Vec3d(entity.prevX, entity.prevY, entity.prevZ),
                            entity.getPos(),
                            trailLifetime
                    ));
                }
                ((TrailEntity) entity).soupAPI$getTrails().removeIf(TrailSegment::update);
            }
        }
    }

    public static void renderTrail(WorldRenderContext context) {
        MatrixStack matrixStack = context.matrixStack();
        float tickDelta = context.tickCounter().getTickDelta(true);

        MinecraftClient mc = MinecraftClient.getInstance();

        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        matrixStack.push();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest(); // перед рендером
        RenderSystem.depthMask(false);  // запрет записи в Z-буфер
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (Entity entity : mc.world.getEntities()) {
            if ((entity instanceof PlayerEntity && !EntityUtils.isFriend(entity) && entity != mc.player) ||
                    (!CONFIG.trailsForGliders && entity != mc.player && !(entity instanceof PlayerEntity))) continue;

            if (entity == mc.player && mc.options.getPerspective().isFirstPerson() && !CONFIG.trailsFirstPerson) continue;
            if (!(entity instanceof LivingEntity) || !mc.player.canSee(entity)) continue;

            List<TrailSegment> trails = ((TrailEntity) entity).soupAPI$getTrails();
            if (trails.isEmpty()) continue;

            float width = entity.getHeight() * (CONFIG.trailsHeight / 100f);
            int alpha = 255;

            for (TrailSegment segment : trails) {
                Vec3d interpolated = segment.interpolate(tickDelta);
                float progress = (float) segment.animation(tickDelta);
                float alphaFactor = progress * alpha;

                Color color = Palette.getInterpolatedPaletteColor(progress);
                int bottomAlpha = (int) alphaFactor;
                int topAlpha = CONFIG.trailsRenderHalf ? 0 : (int) alphaFactor;

                float x = (float) (interpolated.x);
                float y = (float) (interpolated.y);
                float z = (float) (interpolated.z);

                bufferBuilder.vertex(matrix, x, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), bottomAlpha);
                bufferBuilder.vertex(matrix, x, y + width, z).color(color.getRed(), color.getGreen(), color.getBlue(), topAlpha);
            }
        }

        Render2D.endBuilding(bufferBuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        matrixStack.pop();
    }

    public static class TrailSegment {
        private final MinecraftClient mc = MinecraftClient.getInstance();
        private final Vec3d from;
        private final Vec3d to;
        private final int maxTicks;
        private int ticks, prevTicks;

        public TrailSegment(Vec3d from, Vec3d to, int lifetime) {
            this.from = from;
            this.to = to;
            this.ticks = lifetime;
            this.maxTicks = lifetime;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / (double) maxTicks;
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }
    }
}
