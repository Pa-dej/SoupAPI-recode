package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.modules.HitboxDetector;
import me.Padej_.soupapi.modules.TotemPopParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityDamageS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;
import static me.Padej_.soupapi.config.ConfigurableModule.mc;

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

    @Inject(method = "onEntityDamage", at = @At("HEAD"))
    private void soupapi$onEntityDamage(EntityDamageS2CPacket packet, CallbackInfo ci) {
        ClientWorld world = mc.world;
        if (world == null || mc.player == null || !CONFIG.hitboxDetectorEnabled) return;

        Entity victim = world.getEntityById(packet.entityId());
        Entity attacker = world.getEntityById(packet.sourceCauseId());

        if (victim == null || attacker == null) return;
        if (!(victim.equals(mc.player)) || !(attacker instanceof LivingEntity attackerLiving)) return;
        if (!(attacker instanceof PlayerEntity player)) return;
        if (player.isCreative() || player.isSpectator()) return;

        Vec3d eyePos = attackerLiving.getEyePos();
        float yaw = attackerLiving.getYaw();
        float pitch = attackerLiving.getPitch();

        double xz = Math.cos(-Math.toRadians(pitch));
        Vec3d lookVec = new Vec3d(
                -Math.sin(Math.toRadians(yaw)) * xz,
                Math.sin(-Math.toRadians(pitch)),
                Math.cos(Math.toRadians(yaw)) * xz
        );

        Box hitbox = victim.getBoundingBox();
        Vec3d endVec = eyePos.add(lookVec.multiply(3.0));

        boolean hitDetected = false;
        double requiredScale = CONFIG.hitboxDetectorExpand;

        for (double scale = 1.0; scale <= 2.0; scale += 0.05) {
            double dx = (hitbox.getLengthX() * (scale - 1)) / 2;
            double dy = (hitbox.getLengthY() * (scale - 1)) / 2;
            double dz = (hitbox.getLengthZ() * (scale - 1)) / 2;
            Box scaledBox = hitbox.expand(dx, dy, dz);

            if (scaledBox.raycast(eyePos, endVec).isPresent()) {
                requiredScale = scale;
                hitDetected = (scale <= 1.0);
                break;
            }
        }

        if (!hitDetected) {
            HitboxDetector.logSuspect(player, requiredScale);
        }
    }

}

