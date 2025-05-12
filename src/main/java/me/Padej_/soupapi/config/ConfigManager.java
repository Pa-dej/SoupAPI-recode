package me.Padej_.soupapi.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.gui.SoupSettingsScreen;
import me.Padej_.soupapi.settings.Setting;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.MinMaxSliderSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "config", "soupapi_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void saveConfig() {
        Map<String, Map<String, Object>> config = new HashMap<>();

        // Collect settings from all modules
        for (SoupModule module : SoupSettingsScreen.getAllModules()) {
            Map<String, Object> moduleSettings = new HashMap<>();
            for (Setting<?> setting : module.getSettings()) {
                Object value = setting.getValue();
                if (setting instanceof EnumSetting<?> enumSetting) {
                    value = value.toString(); // Store enum as string
                } else if (setting instanceof MinMaxSliderSetting minMaxSetting) {
                    value = new float[]{minMaxSetting.getMinValue(), minMaxSetting.getMaxValue()};
                }
                moduleSettings.put(setting.getName(), value);
            }
            config.put(module.getName(), moduleSettings);
        }

        // Write to file
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, GSON.toJson(config).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) {
            return; // No config file exists
        }

        try {
            String json = new String(Files.readAllBytes(CONFIG_PATH));
            if (json.trim().isEmpty()) {
                System.err.println("Config file is empty: " + CONFIG_PATH);
                return;
            }

            Map<String, Map<String, Object>> config = GSON.fromJson(json, new TypeToken<Map<String, Map<String, Object>>>(){}.getType());
            if (config == null) {
                System.err.println("Failed to parse config file: " + CONFIG_PATH);
                return;
            }

            // Apply settings to modules
            for (SoupModule module : SoupSettingsScreen.getAllModules()) {
                Map<String, Object> moduleSettings = config.get(module.getName());
                if (moduleSettings == null) {
                    continue;
                }
                for (Setting<?> setting : module.getSettings()) {
                    Object value = moduleSettings.get(setting.getName());
                    if (value != null) {
                        try {
                            if (setting instanceof BooleanSetting booleanSetting) {
                                booleanSetting.setValue((Boolean) value);
                            } else if (setting instanceof SliderSetting sliderSetting) {
                                sliderSetting.setValue(((Double) value).floatValue());
                            } else if (setting instanceof EnumSetting<?> enumSetting) {
                                Enum<?>[] values = enumSetting.getValues();
                                for (Enum<?> enumValue : values) {
                                    if (enumValue.toString().equals(value)) {
                                        ((EnumSetting) enumSetting).setValue(enumValue);
                                        break;
                                    }
                                }
                            } else if (setting instanceof MinMaxSliderSetting minMaxSetting) {
                                double[] array = (double[]) value;
                                minMaxSetting.setMinValue((float) array[0]);
                                minMaxSetting.setMaxValue((float) array[1]);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (JsonParseException e) {
            System.err.println("Error parsing config file: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading config file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
