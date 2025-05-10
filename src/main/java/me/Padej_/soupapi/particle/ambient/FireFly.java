package me.Padej_.soupapi.particle.ambient;

import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class FireFly extends DefaultAmbientParticle {
    private final List<Trail> trails = new ArrayList<>();
    private final Color color;

    public FireFly(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
        super(posX, posY, posZ, motionX, motionY, motionZ);
        this.color = CONFIG.ambientParticlesRandomColor ? Palette.getRandomColor() : TargetHudRenderer.bottomLeft;
    }

    @Override
    public boolean tick() {
        if (mc.player == null || mc.world == null) return false;
        if (mc.player.squaredDistanceTo(posX, posY, posZ) > 100) age -= 4;
        else if (!mc.world.getBlockState(new BlockPos((int) posX, (int) posY, (int) posZ)).isAir()) age -= 8;
        else age--;

        if (age < 0)
            return true;

        trails.removeIf(Trail::update);

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        trails.add(new Trail(new Vec3d(prevPosX, prevPosY, prevPosZ), new Vec3d(posX, posY, posZ), this.color));

        motionX *= 0.99f;
        motionY *= 0.99f;
        motionZ *= 0.99f;

        return false;
    }

    @Override
    public void render() {
        float tickDelta = mc.getRenderTickCounter().getTickDelta(true);
        if (!trails.isEmpty()) {
            Camera camera = mc.gameRenderer.getCamera();
            for (Trail ctx : trails) {
                Vec3d pos = ctx.interpolate(1f);
                MatrixStack matrices = new MatrixStack();
                matrices.translate(pos.x, pos.y, pos.z);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));

                float alphaFactor = (float) ctx.animation(tickDelta);
                int alpha = (int) (255 * ((float) age / (float) maxAge) * alphaFactor);
                Color withAlpha = Render2D.injectAlpha(ctx.color(), alpha);

                Render2D.drawGlyphs(matrices, TexturesManager.FIREFLY, withAlpha, CONFIG.ambientParticlesParticleWithTrailScale / 100f);
            }
        }
    }
}

