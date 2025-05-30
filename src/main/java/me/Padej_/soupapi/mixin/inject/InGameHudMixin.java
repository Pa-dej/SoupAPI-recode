package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.font.FontRenderers;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.BetterHudStyles;
import me.Padej_.soupapi.modules.PotionsHud;
import me.Padej_.soupapi.render.Render2D;
import me.Padej_.soupapi.utils.MathUtility;
import me.Padej_.soupapi.utils.Palette;
import me.Padej_.soupapi.utils.TexturesManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.Profilers;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Unique
    private static final float healthChangeSpeed = 0.2f;
    @Unique
    private static final float colorAnimationSpeed = 0.015f;
    @Unique
    private static final float SLOT_ANIMATION_SPEED = 10;
    @Unique
    private static float displayedHealth, displayedHunger, displayedArmor = 0f;
    @Unique
    private static float hpColorAnimationProgress, colorAnimationProgress = 0f;
    @Unique
    private static float hotbarColorAnimationProgress = 0f;
    @Unique
    private static long lastUpdateTime = System.currentTimeMillis();
    @Shadow
    @Final
    private MinecraftClient client;
    @Unique
    private float selectedSlotProgress = 0f;
    @Unique
    private int targetSlot, lastSelectedSlot = 0;

    @Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
    private static void renderArmor(DrawContext context, PlayerEntity player, int i, int j, int k, int x, CallbackInfo ci) {
        boolean isLBStyle = BetterHudStyles.betterHotbarStyle.getValue().equals(BetterHudStyles.HotbarStyle.SIMPLE);

        if (!BetterHudStyles.betterHotbar.getValue()) return;
        ci.cancel();

        Color c1Base = Palette.getColor(0f);
        Color c2Base = Palette.getColor(0.33f);
        float hpProgress = (float) (Math.sin(hpColorAnimationProgress * Math.PI * 2) + 1) / 2f;
        Color c1Hp = isLBStyle ? new Color(0xbebebe) : interpolateColor(c1Base, c2Base, hpProgress);
        Color c2Hp = isLBStyle ? new Color(0xa8a8a8) : interpolateColor(c2Base, c1Base, hpProgress);

        int barWidth = 81;
        int barHeight = 10;
        int yOffset = 55;
        float cornerRadius = 2f;
        int y = context.getScaledWindowHeight() - yOffset;
        y -= 5;

        int armor = player.getArmor();
        float maxArmor = 20.0f;

        if (armor == 0) return;
        if (displayedArmor == 0f) displayedArmor = armor;
        displayedArmor = MathHelper.lerp(0.2f, displayedArmor, armor);
        Render2D.drawGradientRound(context.getMatrices(), x, y, barWidth - 1, barHeight, cornerRadius + 1,
                isLBStyle ? new Color(0xaabebebe).darker().darker() : c2Hp.darker().darker(),
                isLBStyle ? new Color(0xaabebebe).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaaa8a8a8).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaaa8a8a8).darker().darker().darker() : c2Hp.darker().darker().darker().darker());

        int filledWidth = (int) MathUtility.clamp((barWidth * (displayedArmor / maxArmor)), 0, barWidth);
        if (filledWidth != 0) {
            if (!isLBStyle) {
                Render2D.drawGradientBlurredShadow1(context.getMatrices(), x + 2, y + 2, filledWidth, barHeight - 4, 4, c1Hp, c2Hp, c2Hp, c1Hp);
            }
            Render2D.renderRoundedGradientRect(context.getMatrices(), c1Hp, c2Hp, c2Hp, c1Hp, x, y, filledWidth, barHeight, cornerRadius);
        }

        String armorText = String.valueOf(Math.round(10.0 * displayedArmor) / 10.0);
        float textX = x + barWidth / 2f;
        float textY = y + 2.5f;
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), armorText, textX, textY, Palette.getTextColor());

        Render2D.renderTexture(context.getMatrices(), TexturesManager.GUI_SHIELD, x + 2.5f, y + 2.5f, 5, 5, 0, 0, 512, 512, 512, 512);
    }

    @Unique
    private static Color[] getRotatingColors(float progress, Color c1, Color c2, Color c3, Color c4) {
        progress = progress % 1.0f; // Зацикливаем прогресс
        Color topLeft, topRight, bottomRight, bottomLeft;

        if (progress < 0.25f) {
            float phaseProgress = progress / 0.25f;
            topLeft = interpolateColor(c1, c2, phaseProgress);
            topRight = interpolateColor(c2, c3, phaseProgress);
            bottomRight = interpolateColor(c3, c4, phaseProgress);
            bottomLeft = interpolateColor(c4, c1, phaseProgress);
        } else if (progress < 0.5f) {
            float phaseProgress = (progress - 0.25f) / 0.25f;
            topLeft = interpolateColor(c2, c3, phaseProgress);
            topRight = interpolateColor(c3, c4, phaseProgress);
            bottomRight = interpolateColor(c4, c1, phaseProgress);
            bottomLeft = interpolateColor(c1, c2, phaseProgress);
        } else if (progress < 0.75f) {
            float phaseProgress = (progress - 0.5f) / 0.25f;
            topLeft = interpolateColor(c3, c4, phaseProgress);
            topRight = interpolateColor(c4, c1, phaseProgress);
            bottomRight = interpolateColor(c1, c2, phaseProgress);
            bottomLeft = interpolateColor(c2, c3, phaseProgress);
        } else {
            float phaseProgress = (progress - 0.75f) / 0.25f;
            topLeft = interpolateColor(c4, c1, phaseProgress);
            topRight = interpolateColor(c1, c2, phaseProgress);
            bottomRight = interpolateColor(c2, c3, phaseProgress);
            bottomLeft = interpolateColor(c3, c4, phaseProgress);
        }
        return new Color[]{topLeft, topRight, bottomRight, bottomLeft};
    }

    @Unique
    private static Color interpolateColor(Color start, Color end, float progress) {
        int r = MathHelper.lerp(progress, start.getRed(), end.getRed());
        int g = MathHelper.lerp(progress, start.getGreen(), end.getGreen());
        int b = MathHelper.lerp(progress, start.getBlue(), end.getBlue());
        int a = MathHelper.lerp(progress, start.getAlpha(), end.getAlpha());
        return new Color(r, g, b, a);
    }

    @Shadow
    @Nullable
    protected abstract PlayerEntity getCameraPlayer();

    @Shadow
    protected abstract void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed);

    @Shadow
    protected abstract boolean shouldRenderExperience();

    @Shadow
    @Final
    private static Identifier CROSSHAIR_TEXTURE;

    @Inject(method = "render", at = @At("TAIL"))
    private void doFrame(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!BetterHudStyles.betterHotbar.getValue()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;

        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, SoupAPI_Main.ac, 5, 5, 0x99FFFFFF);

        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastUpdateTime) / 1000f; // Время в секундах
        lastUpdateTime = currentTime;

        float tickDelta = tickCounter.getTickDelta(true);

        float targetHealth = Math.min(mc.player.getMaxHealth() + mc.player.getAbsorptionAmount(),
                mc.player.getHealth() + mc.player.getAbsorptionAmount());
        if (displayedHealth == 0f) displayedHealth = targetHealth;
        displayedHealth = MathHelper.lerp(tickDelta * healthChangeSpeed, displayedHealth, targetHealth);

        HungerManager hungerManager = mc.player.getHungerManager();
        float targetHunger = hungerManager.getFoodLevel() + hungerManager.getSaturationLevel();
        if (displayedHunger == 0f) displayedHunger = targetHunger;
        displayedHunger = MathHelper.lerp(tickDelta * healthChangeSpeed, displayedHunger, targetHunger);

        float frameTime = 1.0f / 60.0f;
        float normalizedDelta = deltaTime / frameTime;

        colorAnimationProgress = (colorAnimationProgress + normalizedDelta * colorAnimationSpeed) % 1.0f;
        hpColorAnimationProgress = (hpColorAnimationProgress + normalizedDelta * colorAnimationSpeed / 2f) % 1.0f;
        hotbarColorAnimationProgress = (hotbarColorAnimationProgress + normalizedDelta * colorAnimationSpeed / 2f) % 1.0f;

        int currentSlot = mc.player.getInventory().selectedSlot;
        if (currentSlot != targetSlot) {
            lastSelectedSlot = targetSlot;
            targetSlot = currentSlot;
            selectedSlotProgress = 0f;
        }
        selectedSlotProgress = MathHelper.lerp(SLOT_ANIMATION_SPEED * deltaTime, selectedSlotProgress, 1.0f);
        selectedSlotProgress = MathHelper.clamp(selectedSlotProgress, 0.0f, 1.0f);
    }

    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true)
    private void renderCustomFoodBar(DrawContext context, PlayerEntity player, int top, int right, CallbackInfo ci) {
        boolean isLBStyle = BetterHudStyles.betterHotbarStyle.getValue().equals(BetterHudStyles.HotbarStyle.SIMPLE);

        if (!BetterHudStyles.betterHotbar.getValue()) return;
        ci.cancel();

        float maxHunger = 40.0f;

        Color c1Base = Palette.getColor(0.33f);
        Color c2Base = Palette.getColor(0f);
        float hpProgress = (float) (Math.sin(hpColorAnimationProgress * Math.PI * 2) + 1) / 2f;
        Color c1Hp = isLBStyle ? new Color(0x856346) : interpolateColor(c1Base, c2Base, hpProgress);
        Color c2Hp = isLBStyle ? new Color(0x816046) : interpolateColor(c2Base, c1Base, hpProgress);

        int barWidth = 81;
        int barHeight = 10;
        float cornerRadius = 2f;
        int x = right - barWidth;
        top -= 5;

        int filledWidth = (int) MathUtility.clamp((barWidth * (displayedHunger / maxHunger)), 0, barWidth);
        Render2D.drawGradientRound(context.getMatrices(), x + 1, top, barWidth - 1, barHeight, cornerRadius + 1,
                isLBStyle ? new Color(0xaa816046).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa816046).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa856346).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa856346).darker().darker() : c2Hp.darker().darker());
        if (filledWidth != 0) {
            Render2D.renderRoundedGradientRect(context.getMatrices(), c1Hp, c2Hp, c2Hp, c1Hp, x + barWidth - filledWidth, top, filledWidth, barHeight, cornerRadius);
        }
        String hungerText = String.valueOf(Math.round(10.0 * displayedHunger) / 10.0);
        float textX = x + barWidth / 2f;
        float textY = top + 2.5f;
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), hungerText, textX, textY, Palette.getTextColor());

        Render2D.renderTexture(context.getMatrices(), TexturesManager.GUI_HAM, x + barWidth - 7.5f, top + 2.5f, 5, 5, 0, 0, 512, 512, 512, 512);
    }

    @Inject(method = "renderAirBubbles", at = @At("HEAD"), cancellable = true)
    private void renderAirBubbles(DrawContext context, PlayerEntity player, int heartCount, int top, int left, CallbackInfo ci) {
        boolean isLBStyle = BetterHudStyles.betterHotbarStyle.getValue().equals(BetterHudStyles.HotbarStyle.SIMPLE);

        if (!BetterHudStyles.betterHotbar.getValue()) return;
        ci.cancel();

        float maxAir = player.getMaxAir();
        float currentAir = Math.max(player.getAir(), 0);
        if (currentAir == maxAir) return;

        Color c1Base = Palette.getColor(0.33f);
        Color c2Base = Palette.getColor(0f);
        float airProgress = (float) (Math.sin(hpColorAnimationProgress * Math.PI * 2) + 1) / 2f;
        Color c1Air = isLBStyle ? new Color(0x004488) : interpolateColor(c1Base, c2Base, airProgress);
        Color c2Air = isLBStyle ? new Color(0x0088CC) : interpolateColor(c2Base, c1Base, airProgress);

        int barWidth = 81;
        int barHeight = 10;
        float cornerRadius = 2f;
        int x = left - barWidth;
        int y = top + 3;
        y -= 5;

        int filledWidth = (int) MathUtility.clamp((barWidth * (currentAir / maxAir)), 0, barWidth);
        Render2D.drawGradientRound(context.getMatrices(), x + 1, y, barWidth - 1, barHeight, cornerRadius + 1,
                isLBStyle ? new Color(0xaa004488).darker().darker().darker() : c2Air.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa004488).darker().darker().darker() : c2Air.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa0088CC).darker().darker().darker() : c2Air.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaa0088CC).darker().darker() : c2Air.darker().darker());

        if (filledWidth != 0) {
            Render2D.renderRoundedGradientRect(context.getMatrices(), c1Air, c2Air, c2Air, c1Air, x + barWidth - filledWidth, y, filledWidth, barHeight, cornerRadius);
        }
        String airText = String.valueOf(Math.round(10.0 * currentAir / 20.0) / 10.0);
        float textX = x + barWidth / 2f;
        float textY = y + 2.5f;
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), airText, textX, textY, Palette.getTextColor());

        Render2D.renderTexture(context.getMatrices(), TexturesManager.GUI_BUBBLE, x + barWidth - 7.5f, y + 2.5f, 5, 5, 0, 0, 512, 512, 512, 512);
    }

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void renderCustomHPBar(DrawContext context, PlayerEntity player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, CallbackInfo ci) {
        boolean isLBStyle = BetterHudStyles.betterHotbarStyle.getValue().equals(BetterHudStyles.HotbarStyle.SIMPLE);

        if (!BetterHudStyles.betterHotbar.getValue()) return;
        ci.cancel();

        Color c1Base = Palette.getColor(0f);
        Color c2Base = Palette.getColor(0.33f);
        float hpProgress = (float) (Math.sin(hpColorAnimationProgress * Math.PI * 2) + 1) / 2f;
        Color c1Hp = isLBStyle ? new Color(0xb23229) : interpolateColor(c1Base, c2Base, hpProgress);
        Color c2Hp = isLBStyle ? new Color(0xbc302c) : interpolateColor(c2Base, c1Base, hpProgress);
        y -= 5;

        int barWidth = 81;
        int barHeight = 10;
        float cornerRadius = 2f;


        int filledWidth = (int) MathUtility.clamp((barWidth * (displayedHealth / (maxHealth + absorption))), 8, barWidth);
        Render2D.drawGradientRound(context.getMatrices(), x, y, barWidth - 1, barHeight, cornerRadius + 1,
                isLBStyle ? new Color(0xaab23229).darker().darker() : c2Hp.darker().darker(),
                isLBStyle ? new Color(0xaab23229).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaabc302c).darker().darker().darker() : c2Hp.darker().darker().darker().darker(),
                isLBStyle ? new Color(0xaabc302c).darker().darker().darker() : c2Hp.darker().darker().darker().darker());

        if (filledWidth != 0) {
            Render2D.renderRoundedGradientRect(context.getMatrices(), c1Hp, c2Hp, c2Hp, c1Hp, x, y, filledWidth, barHeight, cornerRadius);
        }
        String healthText = String.valueOf(Math.round(10.0 * displayedHealth) / 10.0);
        float textX = x + barWidth / 2f;
        float textY = y + 2.5f;
        FontRenderers.sf_bold.drawCenteredString(context.getMatrices(), healthText, textX, textY, Palette.getTextColor());

        Render2D.renderTexture(context.getMatrices(), TexturesManager.GUI_HEART, x + 2.5, y + 2.5, 5, 5, 0, 0, 512, 512, 512, 512);
    }

    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true)
    private void cancelRenderExperienceBar(DrawContext context, int x, CallbackInfo ci) {
        if (!BetterHudStyles.betterHotbar.getValue()) return;
        Profilers.get().push("expBar");

        PlayerEntity player = this.client.player;
        if (player == null) return;

        int levelExp = player.getNextLevelExperience();
        if (levelExp > 0) {
            float progress = MathUtility.clamp(player.experienceProgress, 0f, 1f);
            float alpha = 1f; // можно сделать настраиваемым

            // Позиция полоски опыта
            float barX = x + 1;
            float barY = context.getScaledWindowHeight() - 31; // чуть выше/ниже при необходимости
            float barWidth = 182f;
            float barHeight = 3f;

            // Цвета
            Color color = new Color(0xFF7efc20);
            Color background = new Color(0, 0, 0, (int) (alpha * 80));

            // Слои отрисовки

            Render2D.drawGradientBlurredShadow1(context.getMatrices(), barX, barY, barWidth * progress, barHeight, 3, color, color, color, color);
            Render2D.drawRound(context.getMatrices(), barX, barY, barWidth, barHeight, 1, background);
            if (progress > 0)
                Render2D.renderRoundedGradientRect(context.getMatrices(), color, color, color, color, barX, barY, barWidth * progress, barHeight, 1);
        }

        Profilers.get().pop();
        ci.cancel();
    }

    @Inject(method = "renderExperienceLevel", at = @At("HEAD"), cancellable = true)
    private void cancelRenderExperienceNumber(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!BetterHudStyles.betterHotbar.getValue()) return;
        int i = client.player.experienceLevel;
        if (this.shouldRenderExperience() && i > 0) {
            Profilers.get().push("expLevel");
            String string = "" + i;
            int x = (context.getScaledWindowWidth() - (int) FontRenderers.sf_bold.getStringWidth((string))) / 2;
            int y = context.getScaledWindowHeight() - 31 - 6;
            FontRenderers.sf_bold.drawString(context.getMatrices(), string, x, y, 0xFF7efc20);
            Profilers.get().pop();
        }
        ci.cancel();
    }

    @Inject(method = "renderHotbar", at = @At("HEAD"), cancellable = true)
    private void renderHotbar(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!BetterHudStyles.betterHotbar.getValue()) return;
        boolean isLBStyle = BetterHudStyles.betterHotbarStyle.getValue().equals(BetterHudStyles.HotbarStyle.SIMPLE);
        ci.cancel();
        int yOffset = 4;
        PlayerEntity player = getCameraPlayer();
        if (player == null) return;

        ItemStack offHandStack = player.getOffHandStack();
        int halfWidth = context.getScaledWindowWidth() / 2;

        Color c1 = Palette.getColor(0f);
        Color c2 = Palette.getColor(0.33f);
        Color c3 = Palette.getColor(0.66f);
        Color c4 = Palette.getColor(1f);
        Color[] hotbarColors = getRotatingColors(hotbarColorAnimationProgress, c1, c2, c3, c4);
        Color hotbarTopLeft = hotbarColors[0];
        Color hotbarTopRight = hotbarColors[1];
        Color hotbarBottomRight = hotbarColors[2];
        Color hotbarBottomLeft = hotbarColors[3];

        MatrixStack stack = context.getMatrices();
        stack.push();
        stack.translate(0.0F, 0.0F, -90.0F);

        int cornerRadius = 2;
        int glowStrength = 8;
        int hotbarWidth = 178;
        int halfBar = hotbarWidth / 2;
        int hotbarX = halfWidth - halfBar;
        int hotbarY = context.getScaledWindowHeight() - 22 - yOffset;

        Render2D.drawGradientBlurredShadow1(stack, hotbarX - 0.5f, hotbarY + 1, hotbarWidth + 1, 19, glowStrength, hotbarBottomLeft, hotbarBottomRight, hotbarTopRight, hotbarTopLeft);
        if (isLBStyle) {
            Render2D.drawRound(stack, hotbarX - 0.5f, hotbarY + 1, hotbarWidth + 1, 19, cornerRadius, Palette.getBackColor());
        } else {
            Render2D.renderRoundedGradientRect(stack, hotbarTopLeft, hotbarTopRight, hotbarBottomRight, hotbarBottomLeft, hotbarX - 0.5f, hotbarY + 1, hotbarWidth + 1, 19, cornerRadius);
            Render2D.drawRound(stack, hotbarX - 0.5f, hotbarY + 1, hotbarWidth + 1, 19, cornerRadius, Palette.getBackColor());
        }

        float animatedX;
        if (BetterHudStyles.betterHotbarSmoothScroll.getValue()) {
            float startX = halfWidth - 90 + (lastSelectedSlot * 20);
            float targetX = halfWidth - 90 + (targetSlot * 20);
            animatedX = MathHelper.lerp(selectedSlotProgress, startX, targetX);
        } else {
            animatedX = halfWidth - 90 + (targetSlot * 20);
        }

        int selectedSlotY = context.getScaledWindowHeight() - 22 - yOffset;
        if (isLBStyle) {
            Render2D.renderRoundedGradientRect(stack, hotbarTopLeft, hotbarTopRight, hotbarBottomRight, hotbarBottomLeft, animatedX, selectedSlotY + 0.5f, 20, 20, cornerRadius);
            Render2D.drawRound(stack, animatedX + 1f, selectedSlotY + 1.5f, 18, 18, cornerRadius, Palette.getBackColor());
        } else {
            Render2D.drawRound(stack, animatedX + 1f, selectedSlotY + 1.5f, 18, 18, cornerRadius, Palette.getBackColor());
        }

        // Слот для левой руки
        if (!offHandStack.isEmpty()) {
            float offhandX = halfWidth - halfBar - 25.5f;
            int offhandY = context.getScaledWindowHeight() - 25;
            Render2D.drawGradientBlurredShadow1(stack, offhandX, offhandY, 19, 19, glowStrength, hotbarBottomLeft, hotbarBottomRight, hotbarTopRight, hotbarTopLeft);
            Render2D.renderRoundedGradientRect(stack, hotbarTopLeft, hotbarTopRight, hotbarBottomRight, hotbarBottomLeft, offhandX, offhandY, 19, 19, cornerRadius);
            Render2D.drawRound(stack, offhandX + 0.5f, offhandY + 0.5f, 18, 18, cornerRadius, Palette.getBackColor());
        }

        int dummy = 1;
        if (BetterHudStyles.betterHotbarArmor.getValue()) {
            Iterable<ItemStack> armorItems = player.getArmorItems();
            ItemStack[] armorArray = new ItemStack[4];
            int armorIndex = 0;
            int equippedArmorCount = 0;
            for (ItemStack armor : armorItems) {
                armorArray[armorIndex] = armor;
                if (armor != null && !armor.isEmpty()) {
                    equippedArmorCount++;
                }
                armorIndex++;
            }

            if (equippedArmorCount > 0) {
                int slotWidth = 20;
                int armorWidth = equippedArmorCount * slotWidth - 2;
                int armorXOffset = halfWidth + halfBar + 5;
                int armorY = context.getScaledWindowHeight() - 22 - yOffset;

                Render2D.drawGradientBlurredShadow1(stack, armorXOffset - 0.5f, armorY + 1, armorWidth + 1, 19, glowStrength, hotbarBottomLeft, hotbarBottomRight, hotbarTopRight, hotbarTopLeft);
                if (isLBStyle) {
                    Render2D.drawRound(stack, armorXOffset - 0.5f, armorY + 1, armorWidth + 1, 19, cornerRadius, Palette.getBackColor());
                } else {
                    Render2D.renderRoundedGradientRect(stack, hotbarTopLeft, hotbarTopRight, hotbarBottomRight, hotbarBottomLeft, armorXOffset - 0.5f, armorY + 1, armorWidth + 1, 19, cornerRadius);
                    Render2D.drawRound(stack, armorXOffset - 0.5f, armorY + 1, armorWidth + 1, 19, cornerRadius, Palette.getBackColor());
                }
            }

            stack.pop();

            if (equippedArmorCount > 0) {
                int armorXOffset = halfWidth + halfBar + 5;
                int armorYPos = context.getScaledWindowHeight() - 16 - 3 - yOffset;
                int currentSlot = 0;
                for (int i = 0; i < 4; i++) {
                    ItemStack armorItem = armorArray[i];
                    if (armorItem != null && !armorItem.isEmpty()) {
                        int armorX = armorXOffset + currentSlot * 20 + 2;
                        renderHotbarItem(context, armorX, armorYPos, tickCounter, player, armorItem, dummy++);
                        currentSlot++;
                    }
                }
            }
        }

        for (int slot = 0; slot < 9; ++slot) {
            int x = halfWidth - 90 + slot * 20 + 2;
            int y = context.getScaledWindowHeight() - 16 - 3 - yOffset;
            renderHotbarItem(context, x, y, tickCounter, player, player.getInventory().main.get(slot), dummy++);
        }

        // Рендерим предмет в слоте левой руки
        if (!offHandStack.isEmpty()) {
            int slotY = context.getScaledWindowHeight() - 16 - 3 - yOffset;
            this.renderHotbarItem(context, halfWidth - halfBar - 24, slotY, tickCounter, player, offHandStack, dummy++);
        }
    }

    @Inject(method = "renderHotbarItem", at = @At("HEAD"), cancellable = true)
    private void renderHotbarItem(DrawContext context, int x, int y, RenderTickCounter tickCounter, PlayerEntity player, ItemStack stack, int seed, CallbackInfo ci) {
        if (!BetterHudStyles.betterHotbar.getValue()) return;
        ci.cancel();

        int yOffset = 1;
        if (!stack.isEmpty()) {
            float bobbingDelta = (float) stack.getBobbingAnimationTime() - tickCounter.getTickDelta(false);

            y = y - yOffset;

            if (bobbingDelta > 0.0F) {
                float g = 1.0F + bobbingDelta / 5.0F;
                context.getMatrices().push();
                context.getMatrices().translate((float) (x + 8), (float) (y + 12), 0.0F);
                context.getMatrices().scale(1.0F / g, (g + 1.0F) / 2.0F, 1.0F);
                context.getMatrices().translate((float) (-(x + 8)), (float) (-(y + 12)), 0.0F);
            }

            context.drawItem(player, stack, x, y, seed);
            if (bobbingDelta > 0.0F) {
                context.getMatrices().pop();
            }

            context.drawStackOverlay(this.client.textRenderer, stack, x, y);
        }
    }

    @Inject(method = "renderStatusEffectOverlay", at = @At("HEAD"), cancellable = true)
    private void renderStatusEffectOverlay(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (!CONFIG.hudBetterPotionsHudEnabled) return;
        PotionsHud.render(context);
        ci.cancel();
    }

}
