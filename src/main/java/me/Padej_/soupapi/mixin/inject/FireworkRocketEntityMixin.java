package me.Padej_.soupapi.mixin.inject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireworkRocketEntity.class)
public class FireworkRocketEntityMixin { // Firework Timer

    @Shadow
    private int lifeTime;

    @Shadow
    @Nullable
    private LivingEntity shooter;

    @Shadow
    private int life;

    @Inject(method = "tick", at = @At("TAIL"))
    private void printFlightDuration(CallbackInfo ci) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || shooter == null) return;
        if (shooter.equals(player)) {
            player.sendMessage(Text.of(lifeTime + " " + life), true);
        }
    }
}
