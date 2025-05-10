package me.Padej_.soupapi.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityTEST_Mixin {

    /*
     * Анимации ускоряются как у микро зомби.
     */
//    @Inject(method = "isBaby", at = @At("HEAD"), cancellable = true)
//    private void alwaysTrue(CallbackInfoReturnable<Boolean> cir) {
//        cir.setReturnValue(true);
//    }


}
