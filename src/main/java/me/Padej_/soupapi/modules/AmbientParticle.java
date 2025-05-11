package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.particle.ambient.DefaultAmbientParticle;
import me.Padej_.soupapi.particle.ambient.FireFly;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.TexturesManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AmbientParticle extends ConfigurableModule {
    public static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static final ArrayList<DefaultAmbientParticle> fireFlies = new ArrayList<>();
    private static final ArrayList<DefaultAmbientParticle> particles = new ArrayList<>();

    public static void onTick() {
        if (mc.player == null || mc.world == null || !CONFIG.ambientParticlesEnabled) return;
        fireFlies.removeIf(DefaultAmbientParticle::tick);
        particles.removeIf(DefaultAmbientParticle::tick);

        updateAvailableTextures();
        for (int i = fireFlies.size(); i < CONFIG.ambientParticlesParticleWithTrailCount; i++) {
            fireFlies.add(new FireFly(
                    (float) (mc.player.getX() + MathUtility.random(-25f, 25f)),
                    (float) (mc.player.getY() + MathUtility.random(2f, 15f)),
                    (float) (mc.player.getZ() + MathUtility.random(-25f, 25f)),
                    MathUtility.random(-0.2f, 0.2f),
                    MathUtility.random(-0.1f, 0.1f),
                    MathUtility.random(-0.2f, 0.2f)));
        }

        boolean isFallPhysic = CONFIG.ambientParticlesPhysic.equals(Physics.FALL);
        for (int j = particles.size(); j < CONFIG.ambientParticlesDefaultParticleCount; j++) {
            particles.add(new DefaultAmbientParticle(
                    (float) (mc.player.getX() + MathUtility.random(-48f, 48f)),
                    (float) (mc.player.getY() + MathUtility.random(2, 48f)),
                    (float) (mc.player.getZ() + MathUtility.random(-48f, 48f)),
                    isFallPhysic ? MathUtility.random(-0.4f, 0.4f) : 0,
                    isFallPhysic ? MathUtility.random(-0.1f, 0.1f) : MathUtility.random(-0.2f, -0.05f),
                    isFallPhysic ? MathUtility.random(-0.4f, 0.4f) : 0));
        }
    }

    public static void render(WorldRenderContext context) {
        if (!CONFIG.ambientParticlesEnabled) return;
        AmbientParticle.Style particleStyle = CONFIG.ambientParticlesStyle;

        if (particleStyle.equals(Style.DEFAULT) || particleStyle.equals(Style.BOTH)) {
            particles.forEach(particle -> particle.render(context));
        }

        if (particleStyle.equals(Style.WITH_TRAIL) || particleStyle.equals(Style.BOTH)) {
            fireFlies.forEach(firefly -> firefly.render(context));
        }
    }

    private static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();

        if (CONFIG.ambientParticlesIncludeFirefly) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        if (CONFIG.ambientParticlesIncludeDollar) AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        if (CONFIG.ambientParticlesIncludeSnowflake) AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        if (CONFIG.ambientParticlesIncludeHeart) AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        if (CONFIG.ambientParticlesIncludeStar) AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        if (CONFIG.ambientParticlesIncludeGlyphs) {
            Collections.addAll(AVAILABLE_TEXTURES, TexturesManager.GLYPH_TEXTURES);
        }

        if (AVAILABLE_TEXTURES.isEmpty()) {
            AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY);
        }
    }

    public enum Style {
        DEFAULT, WITH_TRAIL, BOTH
    }

    public enum Physics {
        FALL, FLY
    }
}
