package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.modules.TotemPopParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler {

    @Shadow
    private ClientWorld world;

    @Shadow
    private static ItemStack getActiveDeathProtector(PlayerEntity player) {
        return null;
    }

    @Inject(method = "getServerInfo", at = @At("HEAD"), cancellable = true)
    private void injectFakeServerInfo(CallbackInfoReturnable<ServerInfo> cir) {
        if (!MinecraftClient.getInstance().isInSingleplayer()) return;
        ServerInfo fakeInfo = new ServerInfo("Singleplayer", "localhost", ServerInfo.ServerType.LAN);
        cir.setReturnValue(fakeInfo);
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"), cancellable = true)
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (CONFIG == null) return;
        if (!CONFIG.hitSoundOverwriteEnabled || mc.isInSingleplayer()) return;
        if (mc.player == null || mc.player.getWorld() == null) return;

        RegistryEntry<SoundEvent> soundEntry = packet.getSound();
        if (soundEntry == null || !soundEntry.hasKeyAndValue()) return;

        SoundEvent sound;
        try {
            sound = soundEntry.value();
        } catch (IllegalStateException e) {
            return;
        }

        float volume;
        float pitch = 1.0f;

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

    @Inject(method = "onEntityStatus", at = @At("HEAD"), cancellable = true)
    private void onTotemCustomParticles(EntityStatusS2CPacket packet, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.totemPopParticlesEnabled) return;
        if (packet.getStatus() == 35) {
            Entity entity = packet.getEntity(this.world);
            if (entity != null) {
                this.world.playSound(entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.ITEM_TOTEM_USE,
                        entity.getSoundCategory(),
                        1.0F, 1.0F, false);

                if (entity == client.player) {
                    client.gameRenderer.showFloatingItem(getActiveDeathProtector(client.player));
                }

                TotemPopParticles.onTotemPop(entity);

                ci.cancel();
            }
        }
    }

}