package me.Padej_.soupapi.mixin;

import me.Padej_.soupapi.mixin.access.MouseAccessor;
import me.Padej_.soupapi.utils.MouseUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {

    @Inject(method = "updateMouse", at = @At("TAIL"))
    private void onUpdateMouse(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.isWindowFocused()) {
            double dx = ((MouseAccessor) this).getxVelocity();
            double dy = ((MouseAccessor) this).getyVelocity();

            MouseUtils.update(dx, dy);
        }
    }
}


