package me.Padej_.soupapi.particle.ambient;

import me.Padej_.soupapi.modules.AmbientParticle;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.Vec3d;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class Trail {
    private static MinecraftClient mc = MinecraftClient.getInstance();
    private final Vec3d from;
    private final Vec3d to;
    private final Color color;
    private int ticks, prevTicks;

    public Trail(Vec3d from, Vec3d to, Color color) {
        this.from = from;
        this.to = to;
        this.ticks = AmbientParticle.trailLenght.getValue().intValue();
        this.color = color;
    }

    public Vec3d interpolate(float pt) {
        Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getPos();
        double x = from.x + ((to.x - from.x) * pt) - cameraPos.getX();
        double y = from.y + ((to.y - from.y) * pt) - cameraPos.getY();
        double z = from.z + ((to.z - from.z) * pt) - cameraPos.getZ();
        return new Vec3d(x, y, z);
    }

    public double animation(float pt) {
        return (this.prevTicks + (this.ticks - this.prevTicks) * pt) / 10.;
    }

    public boolean update() {
        this.prevTicks = this.ticks;
        return this.ticks-- <= 0;
    }

    public Color color() {
        return color;
    }
}
