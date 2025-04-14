package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.TargetRender;
import net.minecraft.util.Identifier;

public class TexturesManager extends ConfigurableModule {
    private static final Identifier CIRCLE = Identifier.of("soupapi", "textures/circle.png");
    private static final Identifier CIRCLE_BOLD = Identifier.of("soupapi", "textures/circle_bold.png");
    private static final Identifier HEXAGON = Identifier.of("soupapi", "textures/hexagon.png");
    private static final Identifier PORTAL = Identifier.of("soupapi", "textures/portal.png"); // Thunder Hack, thx :3

    private static final Identifier CIRCLE_UNBLACK = Identifier.of("soupapi", "textures/circle_unblack.png");
    private static final Identifier CIRCLE_BOLD_UNBLACK = Identifier.of("soupapi", "textures/circle_bold_unblack.png");
    private static final Identifier HEXAGON_UNBLACK = Identifier.of("soupapi", "textures/hexagon_unblack.png");
    private static final Identifier PORTAL_UNBLACK = Identifier.of("soupapi", "textures/portal_unblack.png"); // Thunder Hack, thx :3

    private static final Identifier LEGACY = Identifier.of("soupapi", "textures/legacy.png");
    private static final Identifier SCIFI = Identifier.of("soupapi", "textures/scifi.png");
    private static final Identifier SCIFI_UNBLACK = Identifier.of("soupapi", "textures/scifi_unblack.png");
    private static final Identifier SIMPLE = Identifier.of("soupapi", "textures/simple.png");
    private static final Identifier BO = Identifier.of("soupapi", "textures/bo.png");
    private static final Identifier MARKER = Identifier.of("soupapi", "textures/marker.png");

    public static final Identifier FIREFLY = Identifier.of("soupapi", "textures/firefly.png"); // Thunder Hack, thx :3
    public static final Identifier FIREFLY_GLOW = Identifier.of("soupapi", "textures/firefly_glow.png");
    public static final Identifier FIREFLY_ALT = Identifier.of("soupapi", "textures/firefly_alt.png");
    public static final Identifier FIREFLY_ALT_GLOW = Identifier.of("soupapi", "textures/firefly_alt_glow.png");

    public static final Identifier ANON_SKIN = Identifier.of("soupapi", "textures/anon_skin.png");

    public static final Identifier GUI_BUBBLE = Identifier.of("soupapi", "textures/gui/bubble.png");
    public static final Identifier GUI_HAM = Identifier.of("soupapi", "textures/gui/ham.png");
    public static final Identifier GUI_HEART = Identifier.of("soupapi", "textures/gui/heart.png");
    public static final Identifier GUI_SHIELD = Identifier.of("soupapi", "textures/gui/shield.png");

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
        };
    }
}
