package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.main.SoupAPI_Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

// Обновленный EntityUtils для доступа к lastHitTime
public class EntityUtils {
    private static Entity targetEntity;
    private static LivingEntity lastHitEntity;
    private static long lastHitTime;
    private static final long FORGET_DELAY = 1500; // 1.5 секунды

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

        if (lastHitEntity != null && System.currentTimeMillis() - lastHitTime > FORGET_DELAY) {
            lastHitEntity = null;
        }
    }

    public static ActionResult onHitEntity(PlayerEntity playerEntity, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
        if (entity instanceof LivingEntity && !isFriend(entity) && !entity.isSpectator()) {
            lastHitEntity = (LivingEntity) entity;
            lastHitTime = System.currentTimeMillis();
        }
        return ActionResult.PASS;
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

    public static boolean isClientCritical() {
        MinecraftClient mc = MinecraftClient.getInstance();

        return mc.player != null
                && mc.player.getAttackCooldownProgress(0.5f) > (mc.player.isOnGround() ? 1f : 0.9f)
                && mc.player.fallDistance > 0
                && !mc.player.isOnGround()
                && !mc.player.isClimbing()
                && !mc.player.isTouchingWater()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !mc.player.hasVehicle()
                && !mc.player.isSprinting();
    }

    public static Entity getTargetEntity() {
        return targetEntity;
    }

    public static LivingEntity getLastHitEntity() {
        return lastHitEntity;
    }

    public static long getLastHitTime() {
        return lastHitTime;
    }

    public static void clearLastHitEntity() {
        lastHitEntity = null;
    }
}

