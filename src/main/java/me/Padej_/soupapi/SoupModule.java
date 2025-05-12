package me.Padej_.soupapi;

import me.Padej_.soupapi.config.ConfigManager;
import me.Padej_.soupapi.settings.Setting;
import net.minecraft.client.MinecraftClient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class SoupModule {

    protected final String name;
    protected final Category category;

    public static MinecraftClient mc = MinecraftClient.getInstance();

    public SoupModule(String name, Category category) {
        this.name = name;
        this.category = category;
    }

    public List<Setting<?>> getSettings() {
        List<Setting<?>> result = new ArrayList<>();
        for (Field field : this.getClass().getDeclaredFields()) {
            if (!Setting.class.isAssignableFrom(field.getType())) continue;

            field.setAccessible(true);
            try {
                Setting<?> setting = (Setting<?>) field.get(this);
                if (setting != null) {
                    result.add(setting);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public Category getCategory() {
        return category;
    }

    public enum Category {
        VISUALS, HUD, PARTICLES, WORLD, OTHER
    }
}