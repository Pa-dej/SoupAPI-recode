package me.Padej_.soupapi.particle.ambient;

import me.Padej_.soupapi.modules.AmbientParticle;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Random;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;
import static me.Padej_.soupapi.modules.AmbientParticle.AVAILABLE_TEXTURES;

public class DefaultAmbientParticle {
    public static MinecraftClient mc = MinecraftClient.getInstance();

    protected Identifier texture;
    protected final Color color;
    protected float prevPosX, prevPosY, prevPosZ, posX, posY, posZ, motionX, motionY, motionZ;
    protected int age;
    protected final int maxAge;

    public DefaultAmbientParticle(float posX, float posY, float posZ, float motionX, float motionY, float motionZ) {
        this.texture = AVAILABLE_TEXTURES.get(new Random().nextInt(AVAILABLE_TEXTURES.size()));
        this.color = AmbientParticle.useRandomColor.getValue() ? Palette.getRandomColor() : TargetHudRenderer.topLeft;
        this.posX = posX;
        this.posY = posY;
        this.posZ = posZ;
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        age = (int) MathUtility.random(100, 300);
        maxAge = age;
    }

    public boolean tick() {
        if (mc.player.squaredDistanceTo(posX, posY, posZ) > 4096) age -= 8;
        else age--;

        if (age < 0)
            return true;

        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;

        posX += motionX;
        posY += motionY;
        posZ += motionZ;

        motionX *= 0.9f;
        if (AmbientParticle.physic.getValue().equals(AmbientParticle.Physics.FALL)) {
            motionY *= 0.9f;
        }
        motionZ *= 0.9f;

        motionY -= 0.001f;

        return false;
    }

    public void render(WorldRenderContext context) {
        Camera camera = context.camera();
        Color color = TargetHudRenderer.topLeft;
        Vec3d pos = interpolatePos(context, prevPosX, prevPosY, prevPosZ, posX, posY, posZ);

        MatrixStack matrices = new MatrixStack();
        matrices.translate(pos.x, pos.y, pos.z);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-camera.getYaw()));
        matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180));

        int alpha = (int) (255 * ((float) age / (float) maxAge));
        Color withAlpha = Render2D.injectAlpha(color, alpha);

        Render2D.drawGlyphs(matrices, texture, withAlpha, AmbientParticle.particleScale.getValue() / 100f);
    }

    private static Vec3d interpolatePos(WorldRenderContext context, float prevPosX, float prevPosY, float prevPosZ, float posX, float posY, float posZ) {
        float tickDelta = context.tickCounter().getTickDelta(true);
        Vec3d cameraPos = context.camera().getPos();
        double x = prevPosX + ((posX - prevPosX) * tickDelta) - cameraPos.getX();
        double y = prevPosY + ((posY - prevPosY) * tickDelta) - cameraPos.getY();
        double z = prevPosZ + ((posZ - prevPosZ) * tickDelta) - cameraPos.getZ();
        return new Vec3d(x, y, z);
    }
}
