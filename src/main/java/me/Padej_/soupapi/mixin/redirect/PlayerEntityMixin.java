package me.Padej_.soupapi.mixin.redirect;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Redirect(
            method = "attack(Lnet/minecraft/entity/Entity;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
            )
    )
    private void redirectAttackSound(World world, PlayerEntity source, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        if (CONFIG.hitSoundOverwriteEnabled) {
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
            }
        }

        world.playSound(source, x, y, z, sound, category, volume, pitch);
    }
}

