package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;

public class KillEffect extends ConfigurableModule {

    private static final Map<Entity, Long> renderEntities = new HashMap<>();
    private static final Map<Entity, Long> lightingEntities = new HashMap<>();

    public static void onTick() {
        if (mc.player == null) return;

        // Тестовое сообщение в чат
        mc.player.sendMessage(Text.of("test"), true);

        ClientWorld world = (ClientWorld) mc.player.getWorld();
        for (Entity entity : world.getEntities()) {
            // Проверяем, что сущность - живое существо
            if (!(entity instanceof LivingEntity liv)) continue;

            // Пропускаем сущности, с которыми уже работаем
            if (entity == mc.player || renderEntities.containsKey(entity) || lightingEntities.containsKey(entity))
                continue;

            // Проверяем, что сущность мертва
            if (!entity.isAlive() || liv.getHealth() == 0) {
                // Отправляем сообщение о смерти сущности в чат
                mc.player.sendMessage(Text.of("Сущность была убита"), false);

                // Проигрываем звук
                world.playSound(mc.player, entity.getBlockPos(), SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 1.0f);

                // Добавляем сущность в список для рендеринга
                renderEntities.put(entity, System.currentTimeMillis());
            }
        }

        // Убираем старые сущности из списка
        if (!lightingEntities.isEmpty()) {
            lightingEntities.forEach((entity, time) -> {
                if (System.currentTimeMillis() - time > 5000) {
                    lightingEntities.remove(entity);
                }
            });
        }
    }

    public static void render(DrawContext context) {
    }
}


