package me.Padej_.soupapi.config;

import me.Padej_.soupapi.modules.BetterHudStyles;
import me.Padej_.soupapi.modules.JumpCircles;
import me.Padej_.soupapi.modules.TargetHud;
import me.Padej_.soupapi.modules.TargetRender;
import me.Padej_.soupapi.utils.Palette;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

import java.awt.*;

@Config(name = "soupapi")
public class SoupAPI_Config implements ConfigData {

    /** MAIN **/
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

    /** WORLD **/
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

    @ConfigEntry.Category("china_hat")
    public boolean chinaHatEnabled = false;
    @ConfigEntry.Category("china_hat")
    public boolean chinaHatRenderHalf = false;

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

    @ConfigEntry.Category("target_render")
    public boolean targetRenderEnabled = false;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetRender.TargetRenderStyle targetRenderStyle = TargetRender.TargetRenderStyle.LEGACY;
    @ConfigEntry.Category("target_render")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public TargetRender.TargetRenderLegacyTexture targetRenderLegacyTexture = TargetRender.TargetRenderLegacyTexture.LEGACY;
    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("target_render")
    @ConfigEntry.BoundedDiscrete(min = 10, max = 100)
    public int targetRenderScale = 60;

    /** HUD **/
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
    @ConfigEntry.BoundedDiscrete(min = -300, max = 300)
    public int targetHudOffsetX = 0;
    @ConfigEntry.Category("target_hud")
    @ConfigEntry.BoundedDiscrete(min = -300, max = 300)
    public int targetHudOffsetY = 0;

    @ConfigEntry.Gui.PrefixText
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarEnabled = false;
    @ConfigEntry.Category("hud")
    public boolean hudBetterHotbarShowArmor = true;
    @ConfigEntry.Category("hud")
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    public BetterHudStyles.HotbarStyle hudBetterHotbarStyle = BetterHudStyles.HotbarStyle.SIMPLE;
}
