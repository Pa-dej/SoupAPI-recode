package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.sounds.CustomSounds;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;

import java.util.Random;

public class HitSound extends ConfigurableModule {
    private static long lastHitTime = 0;

    public static void registerOnHit() {
        AttackEntityCallback.EVENT.register((playerEntity, world, hand, entity, hitResult) -> {
            if ((entity instanceof LivingEntity && !EntityUtils.isFriend(entity) && !entity.isSpectator()) && CONFIG.hitSoundEnabled) {
                long now = System.currentTimeMillis();
                if (now - lastHitTime < 150) return ActionResult.PASS;
                lastHitTime = now;

                playHitSound();
            }
            return ActionResult.PASS;
        });
    }

    public static void playHitSound() {
        if (CONFIG.hitSoundOnlyCrit) {
            if (!EntityUtils.isClientCritical()) return;
            playSound();
        } else {
            playSound();
        }
    }

    public static void playSound() {
        Random random = new Random();
        double pitch = 0.9 + (random.nextInt(51) / 100.0);

        mc.player.playSound(getSound(),
                CONFIG.hitSoundVolume / 100f,
                CONFIG.hitSoundRandomPitch ? (float) pitch : 1);
    }

    private static SoundEvent getSound() {
        return switch (CONFIG.hitSoundType) {
            case CustomSounds.SoundType.ON -> CustomSounds.ON;
            case CustomSounds.SoundType.OFF -> CustomSounds.OFF;
            case CustomSounds.SoundType.GET -> CustomSounds.GET;
            case CustomSounds.SoundType.BUBBLE -> CustomSounds.BUBBLE;
            case CustomSounds.SoundType.BELL -> CustomSounds.BELL;
            case CustomSounds.SoundType.BONK -> CustomSounds.BONK;
            case CustomSounds.SoundType.POK -> CustomSounds.POK;
            case CustomSounds.SoundType.MAGIC_POK -> CustomSounds.MAGIC_POK;
        };
    }
}
