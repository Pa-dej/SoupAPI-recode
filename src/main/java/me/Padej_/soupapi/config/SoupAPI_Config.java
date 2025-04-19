package me.Padej_.soupapi.config;

import me.Padej_.soupapi.modules.BetterHudStyles;
import me.Padej_.soupapi.modules.JumpCircles;
import me.Padej_.soupapi.modules.TargetHud;
import me.Padej_.soupapi.modules.TargetRender;
import me.Padej_.soupapi.utils.Palette;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "soupapi")
public class SoupAPI_Config implements ConfigData {

    /**
     * MAIN
     **/
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("main")
    public String[] friends = {};
    @ConfigEntry.Gui.PrefixText
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

    /**
     * JUMP CIRCLES
     **/
    @ConfigEntry.Category("jump_circles")
    public boolean jumpCirclesEnabled = false;
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
    public int targetRenderSoulLenght = 3;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 1, max = 5)
    public int targetRenderSoulFactor = 3; // spin speed
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

    // spiral

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
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetHud.TargetHUD_ConfigPos targetHudConfigPos_BUTTON = TargetHud.TargetHUD_ConfigPos.CONFIG_POS;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.BoundedDiscrete(min = -300, max = 300)
    public int targetHudOffsetX = 0;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.BoundedDiscrete(min = -300, max = 300)
    public int targetHudOffsetY = 0;

    /**
     * HUD
     **/
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarEnabled = false;
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarShowArmor = true;
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarSmooth = true;
    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public BetterHudStyles.HotbarStyle hudBetterHotbarStyle = BetterHudStyles.HotbarStyle.SIMPLE;
}
