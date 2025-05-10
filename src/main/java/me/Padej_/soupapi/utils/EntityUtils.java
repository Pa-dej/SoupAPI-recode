package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.main.SoupAPI_Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static me.Padej_.soupapi.config.ConfigurableModule.mc;

public class EntityUtils {
    private static Entity targetEntity;
    private static LivingEntity lastHitEntity;

    public static void updateEntities(MinecraftClient client) {
        if (client.player == null) return;

        HitResult hitResult = client.crosshairTarget;
        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity checkedEntity = entityHitResult.getEntity();
            if (!isFriend(checkedEntity) && !checkedEntity.isSpectator()) {
                targetEntity = checkedEntity;
            } else {
                targetEntity = null;
            }
        } else {
            targetEntity = null;
        }
    }

    public static boolean isCrit() {
        PlayerEntity player = mc.player;
        if (player == null) return false;
        boolean bl = player.getAttackCooldownProgress(0.5f) > 0.9f;

        return bl &&
                player.fallDistance > 0.0F &&
                !player.isOnGround() &&
                !player.isClimbing() &&
                !player.isTouchingWater() &&
                !player.hasStatusEffect(StatusEffects.BLINDNESS) &&
                !player.hasVehicle() &&
                !player.isSprinting();
    }

    public static boolean isFriend(Entity entity) {
        String entityName = entity.getName().getString();
        for (String friend : SoupAPI_Main.configHolder.get().friends) {
            if (friend.equalsIgnoreCase(entityName)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isFriend(String name) {
        for (String friend : SoupAPI_Main.configHolder.get().friends) {
            if (friend.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static Entity getTargetEntity() {
        return targetEntity;
    }

    public static LivingEntity getLastHitEntity() {
        return lastHitEntity;
    }

    public static void clearLastHitEntity() {
        lastHitEntity = null;
    }
}


