package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.font.FontRenderers;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class FontRegistrier {
    @Inject(method = "<init>", at = @At("TAIL"))
    void postWindowInit(RunArgs args, CallbackInfo ci) {
        try {
            FontRenderers.sf_bold = FontRenderers.create(16f, "sf_bold");
            FontRenderers.sf_bold_mini = FontRenderers.create(14f, "sf_bold");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
