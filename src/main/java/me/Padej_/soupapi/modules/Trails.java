package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.interfaces.TrailEntity;
import me.Padej_.soupapi.render.CustomRenderLayers;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.Palette;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

import java.awt.*;
import java.util.List;

public class Trails extends ConfigurableModule {

    public static void onTick() {
        if (mc.player == null) return;
        if (!CONFIG.trailsEnabled) return;

        int trailLifetime = CONFIG.trailsLenght;

        for (PlayerEntity player : mc.world.getPlayers()) {
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
        VertexConsumerProvider.Immediate vertexConsumerProvider = mc.getBufferBuilders().getEntityVertexConsumers();

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player && mc.options.getPerspective().isFirstPerson() && !CONFIG.trailsFirstPerson) continue;
            if (!EntityUtils.isFriend(entity) && entity != mc.player) continue;

            List<Trails.TrailSegment> trails = ((TrailEntity) entity).soupAPI$getTrails();
            MatrixStack matrixStack = context.matrixStack();
            float tickDelta = context.tickCounter().getTickDelta(true);
            if (trails.isEmpty()) continue;

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CustomRenderLayers.CHINA_HAT_LAYER.apply(1.0));

            matrixStack.push();
            RenderSystem.disableCull();
            RenderSystem.disableDepthTest();

            Matrix4f matrix = matrixStack.peek().getPositionMatrix();
            float width = entity.getHeight() * (CONFIG.trailsHeight / 100f);
            int alpha = 255;

            for (Trails.TrailSegment trailSegment : trails) {
                Vec3d pos = trailSegment.interpolate(tickDelta);
                float progress = (float) trailSegment.animation(tickDelta);
                float alphaFactor = progress * alpha;

                Color color = Palette.getInterpolatedPaletteColor(progress);
                int bottomAlpha = (int) alphaFactor;
                int topAlpha = CONFIG.trailsRenderHalf ? 0 : (int) alphaFactor;

                vertexConsumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                        .color(color.getRed(), color.getGreen(), color.getBlue(), bottomAlpha);
                vertexConsumer.vertex(matrix, (float) pos.x, (float) pos.y + width, (float) pos.z)
                        .color(color.getRed(), color.getGreen(), color.getBlue(), topAlpha);
            }

            matrixStack.pop();
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            vertexConsumerProvider.draw();
        }

        if (CONFIG.trailsForGliders) {
            for (Entity entity : mc.world.getEntities()) {
                if (entity == mc.player || !(entity instanceof LivingEntity livingEntity) || !livingEntity.isGliding()) {
                    continue;
                }

                List<Trails.TrailSegment> trails = ((TrailEntity) entity).soupAPI$getTrails();
                MatrixStack matrixStack = context.matrixStack();
                float tickDelta = context.tickCounter().getTickDelta(true);
                if (trails.isEmpty()) continue;

                VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CustomRenderLayers.CHINA_HAT_LAYER.apply(1.0));

                matrixStack.push();
                RenderSystem.disableCull();
                RenderSystem.disableDepthTest();

                Matrix4f matrix = matrixStack.peek().getPositionMatrix();
                float width = entity.getHeight() * (CONFIG.trailsHeight / 100f);
                int alpha = 255;

                for (Trails.TrailSegment trailSegment : trails) {
                    Vec3d pos = trailSegment.interpolate(tickDelta);
                    float progress = (float) trailSegment.animation(tickDelta);
                    float alphaFactor = progress * alpha;

                    Color color = Palette.getInterpolatedPaletteColor(progress);
                    int bottomAlpha = (int) alphaFactor;
                    int topAlpha = CONFIG.trailsRenderHalf ? 0 : (int) alphaFactor;

                    vertexConsumer.vertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                            .color(color.getRed(), color.getGreen(), color.getBlue(), bottomAlpha);
                    vertexConsumer.vertex(matrix, (float) pos.x, (float) pos.y + width, (float) pos.z)
                            .color(color.getRed(), color.getGreen(), color.getBlue(), topAlpha);
                }

                matrixStack.pop();
                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
                vertexConsumerProvider.draw();
            }
        }
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
