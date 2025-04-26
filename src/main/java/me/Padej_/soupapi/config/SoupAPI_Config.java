package me.Padej_.soupapi.config;

import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.utils.MC_Tiers;
import me.Padej_.soupapi.utils.Palette;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "soupapi")
public class SoupAPI_Config implements ConfigData {

    /**
     * MAIN
     **/
    @ConfigEntry.Gui.PrefixText // Friends
    @ConfigEntry.Category("main")
    public String[] friends = {};
    @ConfigEntry.Gui.PrefixText // MC Tiers
    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public MC_Tiers.TierGameModes mctiersGameMode = MC_Tiers.TierGameModes.SWORD;
    @ConfigEntry.Gui.PrefixText // Theme
    @ConfigEntry.Category("main")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Palette.ColorsStyle paletteStyle = Palette.ColorsStyle.DUO;
    @ConfigEntry.Category("main")
    @ConfigEntry.ColorPicker
    public int c1 = 0xf72585;
    @ConfigEntry.Category("main")
    @ConfigEntry.ColorPicker
    public int c2 = 0x7209b7;
    @ConfigEntry.Category("main")
    @ConfigEntry.ColorPicker
    public int c3 = 0x3a0ca3;
    @ConfigEntry.Category("main")
    @ConfigEntry.ColorPicker
    public int c4 = 0x4361ee;

    /**
     * TRAILS
     **/
    @ConfigEntry.Category("trails")
    public boolean trailsEnabled = false;
    @ConfigEntry.Category("trails")
    public boolean trailsRenderHalf = false;
    @ConfigEntry.Category("trails")
    public boolean trailsForGliders = false;
    @ConfigEntry.Category("trails")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
    public int trailsLenght = 5;
    @ConfigEntry.Category("trails")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int trailsHeight = 100;

    /**
     * CHINA HAT
     **/
    @ConfigEntry.Category("china_hat")
    public boolean chinaHatEnabled = false;
    @ConfigEntry.Category("china_hat")
    public boolean chinaHatRenderHalf = false;
    @ConfigEntry.Category("china_hat")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int chinaHatAlpha = 90;
    @ConfigEntry.Category("china_hat")
    @ConfigEntry.BoundedDiscrete(min = -100, max = 100)
    public int chinaHatYOffset = 0;
    @ConfigEntry.Gui.PrefixText // tip
    @ConfigEntry.Category("china_hat")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int chinaHatTipHeight = 35;
    @ConfigEntry.Gui.PrefixText // base
    @ConfigEntry.Category("china_hat")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int chinaHatBaseRadius = 65;

    /**
     * HALO
     **/
    @ConfigEntry.Category("halo")
    public boolean haloEnabled = false;

    @ConfigEntry.Gui.PrefixText // soul
    @ConfigEntry.Category("halo")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public Halo.SoulStyle haloSoulRenderSoulStyle = Halo.SoulStyle.SMOKE;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 4, max = 50)
    public int haloSoulLenght = 5;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 20)
    public int haloSoulFactor = 3;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 20, max = 50)
    public int haloSoulRadius = 100;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 20, max = 100)
    public int haloSoulStartSize = 50;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 20, max = 100)
    public int haloSoulEndSize = 20;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int haloSoulScale = 40;
    @ConfigEntry.Category("halo")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int haloSoulSubdivision = 3;

    /**
     * JUMP CIRCLES
     **/
    @ConfigEntry.Category("jump_circles")
    public boolean jumpCirclesEnabled = false;
    @ConfigEntry.Category("jump_circles")
    public boolean jumpCirclesFadeOut = false;
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public JumpCircles.JumCircleStyle jumpCirclesStyle = JumpCircles.JumCircleStyle.CIRCLE;
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100) // %
    public int jumpCirclesAlpha = 100;
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int jumpCirclesColorSpinSpeed = 2;
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 200)
    public int jumpCirclesScale = 100;
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.BoundedDiscrete(min = 2, max = 10)
    public int jumpCirclesLiveTime = 5;
    @ConfigEntry.Category("jump_circles")
    @ConfigEntry.BoundedDiscrete(min = -3, max = 3)
    public int jumpCirclesSpinSpeed = 1;

    /**
     * AMBIENT PARTICLES
     **/
    @ConfigEntry.Category("ambient_particles")
    public boolean ambientParticlesEnabled = false;
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("ambient_particles")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 200)
    public int ambientParticlesLiveTime = 100;
    @ConfigEntry.Category("ambient_particles")
    @ConfigEntry.BoundedDiscrete(min = 5, max = 150)
    public int ambientParticlesMaxCount = 65;
    @ConfigEntry.Category("ambient_particles")
    @ConfigEntry.BoundedDiscrete(min = 4, max = 30)
    public int ambientParticlesSpawnRadius = 20;
    @ConfigEntry.Category("ambient_particles")
    @ConfigEntry.BoundedDiscrete(min = 2, max = 15)
    public int ambientParticlesIgnoreSpawnRadius = 10;

    /**
     * TARGET RENDER
     **/
    @ConfigEntry.Category("target_render")
    public boolean targetRenderEnabled = false;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetRender.TargetRenderStyle targetRenderStyle = TargetRender.TargetRenderStyle.LEGACY;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 0, max = 10)
    public int targetRenderLiveTime = 0;

    @ConfigEntry.Gui.PrefixText // legacy
    @ConfigEntry.Category("target_render")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetRender.TargetRenderLegacyTexture targetRenderLegacyTexture = TargetRender.TargetRenderLegacyTexture.LEGACY;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = -100, max = 100)
    public int targetRenderLegacyRollSpeed = 70;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int targetRenderLegacyScale = 60;

    @ConfigEntry.Gui.PrefixText // soul
    @ConfigEntry.Category("target_render")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetRender.TargetRenderSoulStyle targetRenderSoulStyle = TargetRender.TargetRenderSoulStyle.SMOKE;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 4, max = 20)
    public int targetRenderSoulLenght = 5;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
    public int targetRenderSoulFactor = 3; // spin speed
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
    public int targetRenderSoulShaking = 3;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int targetRenderSoulAmplitude = 3;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 200)
    public int targetRenderSoulRadius = 100;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 20, max = 100)
    public int targetRenderSoulStartSize = 50;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 20, max = 100)
    public int targetRenderSoulEndSize = 20;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 100)
    public int targetRenderSoulScale = 40;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 10)
    public int targetRenderSoulSubdivision = 3;

    // spiral WIP

    @ConfigEntry.Gui.PrefixText // topka
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 30, max = 70)
    public int targetRenderTopkaRadius = 50;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 60)
    public int targetRenderTopkaSpeed = 30;

    /**
     * TARGET HUD
     **/
    @ConfigEntry.Category("target_hud")
    public boolean targetHudEnabled = false;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetHud.TargetHUD_Style targetHudStyle = TargetHud.TargetHUD_Style.MINI;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 30)
    public int targetHudRenderTime = 5;

    @ConfigEntry.Gui.PrefixText // particles
    @ConfigEntry.Category("target_hud")
    public boolean targetHudIncludeFirefly = false;
    @ConfigEntry.Category("target_hud")
    public boolean targetHudIncludeDollar = false;
    @ConfigEntry.Category("target_hud")
    public boolean targetHudIncludeSnowflake = false;
    @ConfigEntry.Category("target_hud")
    public boolean targetHudIncludeHeart = false;
    @ConfigEntry.Category("target_hud")
    public boolean targetHudIncludeStar = false;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.BoundedDiscrete(min = 50, max = 120)
    public int targetHudParticleScale = 100;

    @ConfigEntry.Gui.PrefixText // pos
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetHud.TargetHUD_ConfigPos targetHudConfigPos_BUTTON = TargetHud.TargetHUD_ConfigPos.CONFIG_POS;
    @ConfigEntry.Category("target_hud")
    public int targetHudOffsetX = 0;
    @ConfigEntry.Category("target_hud")
    public int targetHudOffsetY = 0;

    /**
     * HUD
     **/
    @ConfigEntry.Gui.PrefixText // hotbar
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarEnabled = false;
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarShowArmor = true;
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarSmooth = true;
    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public BetterHudStyles.HotbarStyle hudBetterHotbarStyle = BetterHudStyles.HotbarStyle.SIMPLE;
    @ConfigEntry.Gui.PrefixText // crosshair
    @ConfigEntry.Category("hud")
    public boolean hudDynamicCrosshairEnabled = false;
    @ConfigEntry.Category("hud")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 90)
    public int hudDynamicCrosshairFadeFactorEnabled = 80;
    @ConfigEntry.Category("hud")
    @ConfigEntry.BoundedDiscrete(min = 2, max = 15)
    public int hudDynamicCrosshairMaxOffsetEnabled = 6;

    /**
     * HIT BUBBLES
     **/
    @ConfigEntry.Category("hit_bubbles")
    public boolean hitBubblesEnabled = false;
    @ConfigEntry.Category("hit_bubbles")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public HitBubbles.Style hitBubblesStyle = HitBubbles.Style.PORTAL;
    @ConfigEntry.Category("hit_bubbles")
    @ConfigEntry.BoundedDiscrete(min = 15, max = 100)
    public int hitBubblesRenderTime = 30;

    /**
     * RPC
     **/
    @ConfigEntry.Category("rpc")
    public boolean rpcEnabled = false;
    @ConfigEntry.Category("rpc")
    public boolean rpcMctiersEnabled = false;
    @ConfigEntry.Category("rpc")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public RPC.State rpcState = RPC.State.NAME;
    @ConfigEntry.Category("rpc")
    public String rpcCustomStateText = "Have a nice day!";
}
