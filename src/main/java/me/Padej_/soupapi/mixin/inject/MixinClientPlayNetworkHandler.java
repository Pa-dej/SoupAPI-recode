package me.Padej_.soupapi.mixin.inject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;
import static me.Padej_.soupapi.config.ConfigurableModule.mc;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "getServerInfo", at = @At("HEAD"), cancellable = true)
    private void injectFakeServerInfo(CallbackInfoReturnable<ServerInfo> cir) {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            ServerInfo fakeInfo = new ServerInfo("Singleplayer", "localhost", ServerInfo.ServerType.LAN);
            cir.setReturnValue(fakeInfo);
        }
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        SoundEvent sound = packet.getSound().value();
        if (!CONFIG.hitSoundOverwriteEnabled || mc.isInSingleplayer()) return;

        packet.getVolume();
        float volume;
        float pitch = 1;

        if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_CRIT) {
            volume = CONFIG.hitSoundOverwriteCritVolume / 100.0f;
        } else if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP) {
            volume = CONFIG.hitSoundOverwriteSweepVolume / 100.0f;
        } else if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE) {
            volume = CONFIG.hitSoundOverwriteNoDamageVolume / 100.0f;
        } else if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK) {
            volume = CONFIG.hitSoundOverwriteKnockbackVolume / 100.0f;
        } else if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_STRONG) {
            volume = CONFIG.hitSoundOverwriteStrongVolume / 100.0f;
        } else if (sound == SoundEvents.ENTITY_PLAYER_ATTACK_WEAK) {
            volume = CONFIG.hitSoundOverwriteWeakVolume / 100.0f;
        } else {
            return;
        }

        mc.player.getWorld().playSound(
                packet.getX(), packet.getY(), packet.getZ(),
                sound, packet.getCategory(), volume, pitch, false
        );
        ci.cancel();
    }
}

