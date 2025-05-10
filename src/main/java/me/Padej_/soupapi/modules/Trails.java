package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.interfaces.TrailEntity;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.EntityUtils;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
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
        float minStep = 0.001f;

        for (PlayerEntity player : mc.world.getPlayers()) {
            // Добавляем сегмент, если есть любое движение
            Vec3d prevPos = new Vec3d(player.prevX, player.prevY, player.prevZ);
            Vec3d currentPos = player.getPos();
            if (prevPos.distanceTo(currentPos) > minStep) { // Минимальный порог для избежания дребезга
                ((TrailEntity) player).soupAPI$getTrails().add(new TrailSegment(
                        prevPos,
                        currentPos,
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

                Vec3d prevPos = new Vec3d(entity.prevX, entity.prevY, entity.prevZ);
                Vec3d currentPos = entity.getPos();
                if (prevPos.distanceTo(currentPos) > minStep) {
                    ((TrailEntity) entity).soupAPI$getTrails().add(new TrailSegment(
                            prevPos,
                            currentPos,
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

        matrixStack.push();
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();

        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        for (Entity entity : mc.world.getEntities()) {
            if ((entity instanceof PlayerEntity && !EntityUtils.isFriend(entity) && entity != mc.player) ||
                    (!CONFIG.trailsForGliders && entity != mc.player && !(entity instanceof PlayerEntity)))
                continue;

            if (entity == mc.player && mc.options.getPerspective().isFirstPerson() && !CONFIG.trailsFirstPerson)
                continue;
            if (!(entity instanceof LivingEntity) || !mc.player.canSee(entity)) continue;

            List<TrailSegment> trails = ((TrailEntity) entity).soupAPI$getTrails();
            if (trails.isEmpty()) continue;

            float height = entity.getHeight() * (CONFIG.trailsHeight / 100f);
            int baseAlpha = 255;

            for (int i = 0; i < trails.size() - 1; i++) {
                TrailSegment current = trails.get(i);
                TrailSegment next = trails.get(i + 1);

                Vec3d currentPos = current.interpolate(tickDelta);
                Vec3d nextPos = next.interpolate(tickDelta);

                float currentProgress = (float) current.animation(tickDelta);
                float nextProgress = (float) next.animation(tickDelta);
                float currentAlphaFactor = currentProgress * baseAlpha;
                float nextAlphaFactor = nextProgress * baseAlpha;

                float currentProgressColor = current.getProgress(tickDelta);
                float nextProgressColor = next.getProgress(tickDelta);
                Color currentColor = getAnimatedColor(currentProgressColor, 1 - currentProgressColor);
                Color nextColor = getAnimatedColor(nextProgressColor, 1 - nextProgressColor);

                float x1 = (float) currentPos.x;
                float y1 = (float) currentPos.y;
                float z1 = (float) currentPos.z;

                float x2 = (float) nextPos.x;
                float y2 = (float) nextPos.y;
                float z2 = (float) nextPos.z;

                int currentBottomAlpha;
                int currentMidAlpha;
                int currentTopAlpha;
                int nextBottomAlpha;
                int nextMidAlpha;
                int nextTopAlpha;

                if (CONFIG.trailsStyle == Trails.Style.FADED) {
                    currentBottomAlpha = (int) (computeFadedAlpha(0, height) * (currentAlphaFactor / 255.0f));
                    currentMidAlpha = (int) (computeFadedAlpha(height / 2.0f, height) * (currentAlphaFactor / 255.0f));
                    currentTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) (computeFadedAlpha(height, height) * (currentAlphaFactor / 255.0f));
                    nextBottomAlpha = (int) (computeFadedAlpha(0, height) * (nextAlphaFactor / 255.0f));
                    nextMidAlpha = (int) (computeFadedAlpha(height / 2.0f, height) * (nextAlphaFactor / 255.0f));
                    nextTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) (computeFadedAlpha(height, height) * (nextAlphaFactor / 255.0f));
                } else if (CONFIG.trailsStyle == Trails.Style.FADED_INVERT) {
                    currentBottomAlpha = (int) (computeFadedAlphaInvert(0, height) * (currentAlphaFactor / 255.0f));
                    currentMidAlpha = (int) (computeFadedAlphaInvert(height / 2.0f, height) * (currentAlphaFactor / 255.0f));
                    currentTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) (computeFadedAlphaInvert(height, height) * (currentAlphaFactor / 255.0f));
                    nextBottomAlpha = (int) (computeFadedAlphaInvert(0, height) * (nextAlphaFactor / 255.0f));
                    nextMidAlpha = (int) (computeFadedAlphaInvert(height / 2.0f, height) * (nextAlphaFactor / 255.0f));
                    nextTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) (computeFadedAlphaInvert(height, height) * (nextAlphaFactor / 255.0f));
                } else { // SOLID
                    currentBottomAlpha = (int) currentAlphaFactor;
                    currentMidAlpha = (int) currentAlphaFactor;
                    currentTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) currentAlphaFactor;
                    nextBottomAlpha = (int) nextAlphaFactor;
                    nextMidAlpha = (int) nextAlphaFactor;
                    nextTopAlpha = CONFIG.trailsRenderHalf ? 0 : (int) nextAlphaFactor;
                }

                // Нижний QUAD: от низа до середины
                bufferBuilder.vertex(matrix, x1, y1, z1)
                        .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentBottomAlpha);
                bufferBuilder.vertex(matrix, x2, y2, z2)
                        .color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextBottomAlpha);
                bufferBuilder.vertex(matrix, x2, y2 + height / 2.0f, z2)
                        .color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextMidAlpha);
                bufferBuilder.vertex(matrix, x1, y1 + height / 2.0f, z1)
                        .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentMidAlpha);

                // Верхний QUAD: от середины до верха (если не trailsRenderHalf)
                if (!CONFIG.trailsRenderHalf) {
                    bufferBuilder.vertex(matrix, x1, y1 + height / 2.0f, z1)
                            .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentMidAlpha);
                    bufferBuilder.vertex(matrix, x2, y2 + height / 2.0f, z2)
                            .color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextMidAlpha);
                    bufferBuilder.vertex(matrix, x2, y2 + height, z2)
                            .color(nextColor.getRed(), nextColor.getGreen(), nextColor.getBlue(), nextTopAlpha);
                    bufferBuilder.vertex(matrix, x1, y1 + height, z1)
                            .color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue(), currentTopAlpha);
                }
            }
        }

        Render2D.endBuilding(bufferBuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);

        matrixStack.pop();
    }

    public static class TrailSegment {
        private final Vec3d from;
        private final Vec3d to;
        private int ticks;
        private int prevTicks;
        private final int maxLifetime;

        public TrailSegment(Vec3d from, Vec3d to, int lifetime) {
            this.from = from;
            this.to = to;
            this.ticks = lifetime;
            this.maxLifetime = lifetime;
        }

        public Vec3d interpolate(float pt) {
            double x = from.x + ((to.x - from.x) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getX();
            double y = from.y + ((to.y - from.y) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getY();
            double z = from.z + ((to.z - from.z) * pt) - mc.getEntityRenderDispatcher().camera.getPos().getZ();
            return new Vec3d(x, y, z);
        }

        public double animation(float pt) {
            float age = (this.maxLifetime - (this.prevTicks + (this.ticks - this.prevTicks) * pt));
            return Math.max(0, 1 - age / maxLifetime);
        }

        public boolean update() {
            this.prevTicks = this.ticks;
            return this.ticks-- <= 0;
        }

        public float getProgress(float pt) {
            return (maxLifetime - (this.prevTicks + (this.ticks - this.prevTicks) * pt)) / (float) maxLifetime;
        }
    }

    private static int computeFadedAlpha(float yOffset, float height) {
        float yRelative = yOffset / height;
        if (yRelative <= 0.5f) {
            return (int) ((1.0f - yRelative / 0.5f) * 255);
        } else {
            return (int) (((yRelative - 0.5f) / 0.5f) * 255);
        }
    }

    private static int computeFadedAlphaInvert(float yOffset, float height) {
        float yRelative = yOffset / height;
        int alphaFactor = (int) (255 * (CONFIG.trailsAlphaFactor / 100.0f));
        if (yRelative <= 0.5f) {
            return (int) (alphaFactor + (yRelative / 0.5f) * (255 - alphaFactor));
        } else {
            return (int) (255 - ((yRelative - 0.5f) / 0.5f) * (255 - alphaFactor));
        }
    }

    public static Color getAnimatedColor(float x, float y) {

        Color c1 = TargetHudRenderer.topLeft;
        Color c2 = TargetHudRenderer.topRight;
        Color c3 = TargetHudRenderer.bottomRight;
        Color c4 = TargetHudRenderer.bottomLeft;

        Color top = lerpColor(c1, c2, x);
        Color bottom = lerpColor(c4, c3, x);

        return lerpColor(top, bottom, y);
    }

    public static Color lerpColor(Color a, Color b, float t) {
        int r = (int) (a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int b_ = (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, b_);
    }

    public enum Style {
        FADED, SOLID, FADED_INVERT
    }

    public enum Targets {
        PLAYERS, PROJECTILES, BOTH
    }
}
