package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.particle.ambient.DefaultAmbientParticle;
import me.Padej_.soupapi.particle.ambient.FireFly;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.TexturesManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AmbientParticle extends SoupModule {
    public static final List<Identifier> AVAILABLE_TEXTURES = new ArrayList<>();
    private static final ArrayList<DefaultAmbientParticle> fireFlies = new ArrayList<>();
    private static final ArrayList<DefaultAmbientParticle> particles = new ArrayList<>();

    public static final BooleanSetting enabled = new BooleanSetting("Enabled", "description", false);
    public static final BooleanSetting useRandomColor = new BooleanSetting("Random Color", "description", false);
    public static final BooleanSetting firefly = new BooleanSetting("Firefly", "description", false);
    public static final BooleanSetting dollar = new BooleanSetting("Dollar", "description", false);
    public static final BooleanSetting snowflake = new BooleanSetting("Snowflake", "description", false);
    public static final BooleanSetting heart = new BooleanSetting("Heart", "description", false);
    public static final BooleanSetting star = new BooleanSetting("Star", "description", false);
    public static final BooleanSetting glyphs = new BooleanSetting("Glyphs", "description", false);
    public static final EnumSetting<AmbientParticle.Style> style = new EnumSetting<>("Style", "description", Style.BOTH, AmbientParticle.Style.class);
    public static final EnumSetting<AmbientParticle.Physics> physic = new EnumSetting<>("Physic", "description", Physics.FLY, AmbientParticle.Physics.class);
    public static final SliderSetting trailLenght = new SliderSetting("Trail Lenght", "description", 10, 5, 30, 1);
    public static final SliderSetting particleScale = new SliderSetting("Particles Scale", "description", 100, 10, 200, 1);
    public static final SliderSetting particlesWithTrailScale = new SliderSetting("Particles Witch Trail Scale", "description", 100, 20, 200, 1);
    public static final SliderSetting particlesCount = new SliderSetting("Particles Count", "description", 100, 10, 400, 1);
    public static final SliderSetting particlesWitchTrailCount = new SliderSetting("Particles Witch Trail Count", "description", 30, 10, 150, 1);

    public AmbientParticle() {
        super("Ambient Particles", Category.PARTICLES);
    }

    public static void onTick() {
        if (mc.player == null || mc.world == null || !enabled.getValue()) return;
        fireFlies.removeIf(DefaultAmbientParticle::tick);
        particles.removeIf(DefaultAmbientParticle::tick);

        updateAvailableTextures();
        for (int i = fireFlies.size(); i < particlesWitchTrailCount.getValue(); i++) {
            fireFlies.add(new FireFly(
                    (float) (mc.player.getX() + MathUtility.random(-25f, 25f)),
                    (float) (mc.player.getY() + MathUtility.random(2f, 15f)),
                    (float) (mc.player.getZ() + MathUtility.random(-25f, 25f)),
                    MathUtility.random(-0.2f, 0.2f),
                    MathUtility.random(-0.1f, 0.1f),
                    MathUtility.random(-0.2f, 0.2f)));
        }

        boolean isFallPhysic = physic.getValue().equals(Physics.FALL);
        for (int j = particles.size(); j < particlesCount.getValue(); j++) {
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
        if (!enabled.getValue()) return;
        AmbientParticle.Style particleStyle = style.getValue();

        if (particleStyle.equals(Style.DEFAULT) || particleStyle.equals(Style.BOTH)) {
            particles.forEach(particle -> particle.render(context));
        }

        if (particleStyle.equals(Style.WITH_TRAIL) || particleStyle.equals(Style.BOTH)) {
            fireFlies.forEach(firefly -> firefly.render(context));
        }
    }

    private static void updateAvailableTextures() {
        AVAILABLE_TEXTURES.clear();

        if (firefly.getValue()) AVAILABLE_TEXTURES.add(TexturesManager.FIREFLY_ALT);
        if (dollar.getValue()) AVAILABLE_TEXTURES.add(TexturesManager.DOLLAR);
        if (snowflake.getValue()) AVAILABLE_TEXTURES.add(TexturesManager.SNOWFLAKE);
        if (heart.getValue()) AVAILABLE_TEXTURES.add(TexturesManager.HEART);
        if (star.getValue()) AVAILABLE_TEXTURES.add(TexturesManager.STAR);
        if (glyphs.getValue()) {
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
