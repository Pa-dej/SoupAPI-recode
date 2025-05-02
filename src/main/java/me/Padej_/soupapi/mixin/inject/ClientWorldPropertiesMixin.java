package me.Padej_.soupapi.mixin.inject;

import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(ClientWorld.Properties.class)
public abstract class ClientWorldPropertiesMixin {
    @Inject(method = "getTimeOfDay", at = @At("HEAD"), cancellable = true)
    private void onGetTimeOfDay(CallbackInfoReturnable<Long> cir) {
        if (!CONFIG.timeChangerEnabled) return;
        long time = (long) ((CONFIG.timeChangerTime - 1) / 99.0 * 24000);
        cir.setReturnValue(time);
    }
}

