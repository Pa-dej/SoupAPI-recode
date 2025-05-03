package me.Padej_.soupapi.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class CustomPhysicParticleFactory implements ParticleFactory<SimpleParticleType> {
    private final SpriteProvider spriteProvider;

    public CustomPhysicParticleFactory(SpriteProvider spriteProvider) {
        this.spriteProvider = spriteProvider;
    }

    @Override
    public Particle createParticle(SimpleParticleType type, ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        CustomPhysicParticle particle = new CustomPhysicParticle(world, x, y, z, this.spriteProvider);
        // Устанавливаем начальную скорость (в основном горизонтальную)
        particle.setVelocity(velocityX, Math.min(velocityY, 0.2), velocityZ); // Ограничиваем вертикальную скорость
        particle.setSprite(this.spriteProvider);
        return particle;
    }
}

