package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.TargetRender;
import net.minecraft.util.Identifier;

public class TexturesManager extends ConfigurableModule {
    private static final Identifier CIRCLE = Identifier.of("soupapi", "textures/jump_circles/circle.png");
    private static final Identifier CIRCLE_BOLD = Identifier.of("soupapi", "textures/jump_circles/circle_bold.png");
    private static final Identifier HEXAGON = Identifier.of("soupapi", "textures/jump_circles/hexagon.png");
    public static final Identifier PORTAL = Identifier.of("soupapi", "textures/jump_circles/portal.png");
    public static final Identifier PORTAL_2 = Identifier.of("soupapi", "textures/jump_circles/portal_2.png");

    private static final Identifier CIRCLE_UNBLACK = Identifier.of("soupapi", "textures/jump_circles/circle_unblack.png");
    private static final Identifier CIRCLE_BOLD_UNBLACK = Identifier.of("soupapi", "textures/jump_circles/circle_bold_unblack.png");
    private static final Identifier HEXAGON_UNBLACK = Identifier.of("soupapi", "textures/jump_circles/hexagon_unblack.png");
    private static final Identifier PORTAL_UNBLACK = Identifier.of("soupapi", "textures/jump_circles/portal_unblack.png");

    private static final Identifier LEGACY = Identifier.of("soupapi", "textures/target_render/legacy.png");
    private static final Identifier SCIFI = Identifier.of("soupapi", "textures/target_render/scifi.png");
    private static final Identifier SCIFI_UNBLACK = Identifier.of("soupapi", "textures/target_render/scifi_unblack.png");
    private static final Identifier SIMPLE = Identifier.of("soupapi", "textures/target_render/simple.png");
    private static final Identifier BO = Identifier.of("soupapi", "textures/target_render/bo.png");
    private static final Identifier MARKER = Identifier.of("soupapi", "textures/target_render/marker.png");
    private static final Identifier SKULL = Identifier.of("soupapi", "textures/target_render/skull.png");
    private static final Identifier AMOGUS = Identifier.of("soupapi", "textures/target_render/amongus.png");
    private static final Identifier VEGAS = Identifier.of("soupapi", "textures/target_render/vegas.png");
    private static final Identifier JEKA = Identifier.of("soupapi", "textures/target_render/jeka.png");

    public static final Identifier FIREFLY = Identifier.of("soupapi", "textures/particles/firefly.png");
    public static final Identifier FIREFLY_GLOW = Identifier.of("soupapi", "textures/particles/firefly_glow.png");
    public static final Identifier FIREFLY_ALT = Identifier.of("soupapi", "textures/particles/firefly_alt.png");
    public static final Identifier FIREFLY_ALT_GLOW = Identifier.of("soupapi", "textures/particles/firefly_alt_glow.png");
    public static final Identifier DOLLAR = Identifier.of("soupapi", "textures/particles/dollar.png");
    public static final Identifier SNOWFLAKE = Identifier.of("soupapi", "textures/particles/snowflake.png");
    public static final Identifier HEART = Identifier.of("soupapi", "textures/particles/heart.png");
    public static final Identifier STAR = Identifier.of("soupapi", "textures/particles/star.png");

    public static final Identifier ANON_SKIN = Identifier.of("soupapi", "textures/skin/anon_skin.png");

    public static final Identifier GUI_BUBBLE = Identifier.of("soupapi", "textures/gui/bubble.png");
    public static final Identifier GUI_HAM = Identifier.of("soupapi", "textures/gui/ham.png");
    public static final Identifier GUI_HEART = Identifier.of("soupapi", "textures/gui/heart.png");
    public static final Identifier GUI_SHIELD = Identifier.of("soupapi", "textures/gui/shield.png");
    public static final Identifier GUI_POTION = Identifier.of("soupapi", "textures/gui/potion.png");

    public static final Identifier MC_TIERS_LOGO = Identifier.of("soupapi", "textures/gui/mctiers/mctiers.png");
    public static final Identifier ROOKIE = Identifier.of("soupapi", "textures/gui/mctiers/rookie.png");
    public static final Identifier LTMS = Identifier.of("soupapi", "textures/gui/mctiers/2v2.png");
    public static final Identifier AXE = Identifier.of("soupapi", "textures/gui/mctiers/axe.png");
    public static final Identifier MACE = Identifier.of("soupapi", "textures/gui/mctiers/mace.png");
    public static final Identifier NETHOP = Identifier.of("soupapi", "textures/gui/mctiers/nethop.png");
    public static final Identifier POT = Identifier.of("soupapi", "textures/gui/mctiers/pot.png");
    public static final Identifier SMP = Identifier.of("soupapi", "textures/gui/mctiers/smp.png");
    public static final Identifier SWORD = Identifier.of("soupapi", "textures/gui/mctiers/sword.png");
    public static final Identifier UHC = Identifier.of("soupapi", "textures/gui/mctiers/uhc.png");
    public static final Identifier VANILLA = Identifier.of("soupapi", "textures/gui/mctiers/vanilla.png");

    public static Identifier getJumpCircleUnblack() {
        return switch (CONFIG.jumpCirclesStyle) {
            case CIRCLE -> CIRCLE_UNBLACK;
            case PORTAL -> PORTAL_UNBLACK;
            case HEXAGON -> HEXAGON_UNBLACK;
            case CIRCLE_BOLD -> CIRCLE_BOLD_UNBLACK;
        };
    }

    public static Identifier getTargetRenderTexture() {
        return switch (CONFIG.targetRenderLegacyTexture) {
            case LEGACY -> LEGACY;
            case MARKER -> MARKER;
            case BO -> BO;
            case SIMPLE -> SIMPLE;
            case SCIFI -> SCIFI_UNBLACK;
            case JEKA -> JEKA;
            case AMONGUS -> AMOGUS;
            case SKULL -> SKULL;
            case VEGAS -> VEGAS;
        };
    }

    public static Identifier getHitBubbleTexture() {
        return switch (CONFIG.hitBubblesStyle) {
            case CIRCLE -> CIRCLE;
            case CIRCLE_BOLD -> CIRCLE_BOLD;
            case HEXAGON -> HEXAGON;
            case PORTAL -> PORTAL;
            case PORTAL_2 -> PORTAL_2;
        };
    }

    public static Identifier getMC_TiersGameModeTexture() {
        return switch (CONFIG.mctiersGameMode) {
            case LTMs -> LTMS;
            case AXE -> AXE;
            case MACE -> MACE;
            case NETHER_OP -> NETHOP;
            case POT -> POT;
            case SMP -> SMP;
            case SWORD -> SWORD;
            case UHC -> UHC;
            case VANILLA -> VANILLA;
        };
    }
}
