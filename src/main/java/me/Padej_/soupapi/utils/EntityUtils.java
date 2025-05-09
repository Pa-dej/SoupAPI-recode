package me.Padej_.soupapi.utils;

import me.Padej_.soupapi.main.SoupAPI_Main;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class EntityUtils {
    private static Entity targetEntity;
    private static LivingEntity lastHitEntity;
    public static boolean particleCrit = false;
    private static int lastDamagedEntityId = -1;
    private static long lastDamageTime = 0;
    private static final long CRIT_VALIDITY_MS = 30;

    public static void registerClientDamage(int entityId) {
        lastDamagedEntityId = entityId;
        lastDamageTime = System.currentTimeMillis();
    }

    public static boolean checkAndClearCritTarget(int animEntityId) {
        if (lastDamagedEntityId == animEntityId &&
                System.currentTimeMillis() - lastDamageTime < CRIT_VALIDITY_MS) {
            lastDamagedEntityId = -1;
            return true;
        }
        return false;
    }

    public static void onCrit() {
        particleCrit = true;
        if (CONFIG.hitSoundOnlyCrit) {
            HitSound.playSound();
        }
    }

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


