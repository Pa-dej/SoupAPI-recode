package me.Padej_.soupapi.mixin.inject;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextFieldWidget.class)
public class TextFieldWidgetMixin {

    @Shadow private int maxLength;

    @Inject(method = "<init>(Lnet/minecraft/client/font/TextRenderer;IILnet/minecraft/text/Text;)V", at = @At("TAIL"))
    private void init(TextRenderer textRenderer, int width, int height, Text text, CallbackInfo ci) {
        maxLength = 1025;
    }
}
