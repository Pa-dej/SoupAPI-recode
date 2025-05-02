package me.Padej_.soupapi.modules;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import me.Padej_.soupapi.utils.Timer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;

public class HitBubbles extends ConfigurableModule {

    private static final ArrayList<HitBubble> bubbles = new ArrayList<>();
    private static long lastHitTime = 0;

    public static void registerOnHit() {
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, entity, hitResult) -> {
            if ((entity instanceof LivingEntity && !EntityUtils.isFriend(entity) && !entity.isSpectator()) && CONFIG.hitBubblesEnabled) {
                long now = System.currentTimeMillis();
                if (now - lastHitTime < 150) return ActionResult.PASS; // защита от спама (150мс)
                lastHitTime = now;

                float yaw = playerEntity.getYaw();
                float pitch = playerEntity.getPitch();
                double distance = 5.0;
                Vec3d point = getRtxPoint(yaw, pitch, distance);

                if (point != null) {
                    bubbles.add(new HitBubble(
                            (float) point.x, (float) point.y, (float) point.z,
                            -yaw, pitch, new Timer()
                    ));
                }
            }
            return ActionResult.PASS;
        });
    }

    public static void render(WorldRenderContext context) {
        RenderSystem.disableDepthTest();
        ArrayList<HitBubble> copy = new ArrayList<>(bubbles);
        MatrixStack matrixStack = context.matrixStack();

        for (HitBubble b : copy) {
            matrixStack.push();
            matrixStack.translate(
                    b.x - mc.gameRenderer.getCamera().getPos().getX(),
                    b.y - mc.gameRenderer.getCamera().getPos().getY(),
                    b.z - mc.gameRenderer.getCamera().getPos().getZ()
            );
            matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(b.yaw));
            matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(b.pitch));

            float angle = -b.life.getPassedTimeMs() / 4f;
            float factor = b.life.getPassedTimeMs() / (CONFIG.hitBubblesRenderTime * 50f);
            drawBubble(matrixStack, angle, factor);

            matrixStack.pop();
        }

        RenderSystem.enableDepthTest();
        bubbles.removeIf(b -> b.life.passedMs(CONFIG.hitBubblesRenderTime * 50L));
    }

    private static void drawBubble(MatrixStack matrices, float angle, float factor) {
        Render2D.setupRender();
        RenderSystem.disableCull();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShaderTexture(0, TexturesManager.getHitBubbleTexture());

        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
        float scale = factor * 2f;
        Render2D.renderGradientTexture(matrices,
                -scale / 2, -scale / 2, scale, scale,
                0, 0, 128, 128, 128, 128,
                Render2D.applyOpacity(Palette.getColor(0), 1f - factor),
                Render2D.applyOpacity(Palette.getColor(0.33f), 1f - factor),
                Render2D.applyOpacity(Palette.getColor(0.66f), 1f - factor),
                Render2D.applyOpacity(Palette.getColor(1), 1f - factor));
        Render2D.endRender();
        RenderSystem.enableCull();
    }

    private static Vec3d getRtxPoint(float yaw, float pitch, double distance) {
        Vec3d from = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0);
        Vec3d direction = getRotationVector(pitch, yaw);
        Vec3d to = from.add(direction.multiply(distance));
        double distanceSq = distance * distance;

        Box box = mc.player.getBoundingBox().stretch(direction.multiply(distance)).expand(1.0, 1.0, 1.0);
        EntityHitResult result = ProjectileUtil.raycast(
                mc.player,
                from,
                to,
                box,
                (entity) -> !entity.isSpectator() && entity.canHit(),
                distanceSq
        );

        if (result != null && result.getEntity() instanceof LivingEntity) {
            return result.getPos();
        }
        return null;
    }

    private static Vec3d getRotationVector(float pitch, float yaw) {
        float f = (float) Math.cos(-yaw * 0.017453292F - Math.PI);
        float g = (float) Math.sin(-yaw * 0.017453292F - Math.PI);
        float h = (float) -Math.cos(-pitch * 0.017453292F);
        float i = (float) Math.sin(-pitch * 0.017453292F);
        return new Vec3d(g * h, i, f * h);
    }

    public enum Style {
        CIRCLE, CIRCLE_BOLD, HEXAGON, PORTAL, PORTAL_2
    }

    public record HitBubble(float x, float y, float z, float yaw, float pitch, Timer life) {
    }
}

