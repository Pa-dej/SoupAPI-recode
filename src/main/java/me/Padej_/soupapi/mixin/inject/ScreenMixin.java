package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.shedaniel.clothconfig2.api.ConfigScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Shadow
    @Final
    protected Text title;

    @Shadow
    protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Inject(method = "init()V", at = @At("TAIL"))
    private void debug(CallbackInfo ci) {
        if (!isSoupAPI_Screen()) return;

        int buttonWidth = 50;
        this.addDrawableChild(ButtonWidget.builder(Text.of("Apply"),
                        (ButtonWidget button) -> {
                            ((ConfigScreen) this).saveAll(false);
                            ConfigurableModule.saveConfig();
                        })
                .dimensions(width - 30 - buttonWidth, height - 26, buttonWidth, 20)
                .build()
        );
    }

    @Unique
    private boolean isSoupAPI_Screen() {
        return this.title.equals(Text.translatable("text.autoconfig.soupapi.title"));
    }
}
