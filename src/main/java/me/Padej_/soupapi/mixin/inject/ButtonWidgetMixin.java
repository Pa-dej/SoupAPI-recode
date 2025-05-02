package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.screen.ConfigHudPositionsScreen;
import me.Padej_.soupapi.screen.SwingHandScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends PressableWidget {

    public ButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onPress(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        switch (this.getMessage().getString()) {
            case "CONFIG_POS" -> {
                mc.setScreen(new ConfigHudPositionsScreen());
                ci.cancel();
            }
            case "CONFIG_HANDS" -> {
                mc.setScreen(new SwingHandScreen());
                ci.cancel();
            }
        }
    }
}
