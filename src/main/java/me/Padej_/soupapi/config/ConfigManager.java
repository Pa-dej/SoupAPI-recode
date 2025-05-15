package me.Padej_.soupapi.config;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.gui.SoupSettingsScreen;
import me.Padej_.soupapi.settings.Setting;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.MinMaxSliderSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ConfigManager {
    private static final Path CONFIG_PATH = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "config", "soupapi_config.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static Map<String, Map<String, Object>> config = new HashMap<>();
    private static Map<String, String> metadata = new HashMap<>();

    public static void saveConfig() {
        Map<String, Object> fullData = new HashMap<>();

        // Собираем настройки модулей
        Map<String, Map<String, Object>> modulesConfig = new HashMap<>();
        for (SoupModule module : SoupSettingsScreen.getAllModules()) {
            modulesConfig.put(module.getDisplayName().getString(), getStringObjectMap(module));
        }

        fullData.put("modules", modulesConfig);
        fullData.put("meta", metadata);

        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            Files.write(CONFIG_PATH, GSON.toJson(fullData).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static @NotNull Map<String, Object> getStringObjectMap(SoupModule module) {
        Map<String, Object> moduleSettings = new HashMap<>();
        for (Setting<?> setting : module.getSettings()) {
            Object value = setting.getValue();
            if (setting instanceof EnumSetting<?> enumSetting) {
                value = value.toString();
            } else if (setting instanceof MinMaxSliderSetting minMaxSetting) {
                value = new float[]{minMaxSetting.getMinValue(), minMaxSetting.getMaxValue()};
            }
            moduleSettings.put(setting.getName(), value);
        }
        return moduleSettings;
    }

    public static void loadConfig() {
        if (!Files.exists(CONFIG_PATH)) return;

        try {
            String json = new String(Files.readAllBytes(CONFIG_PATH));
            if (json.trim().isEmpty()) return;

            JsonObject fullData = JsonParser.parseString(json).getAsJsonObject();

            JsonObject modulesJson = fullData.getAsJsonObject("modules");
            JsonObject metaJson = fullData.getAsJsonObject("meta");

            Type modulesType = new TypeToken<Map<String, Map<String, Object>>>(){}.getType();
            Map<String, Map<String, Object>> modulesConfig = GSON.fromJson(modulesJson, modulesType);
            config = Objects.requireNonNullElseGet(modulesConfig, HashMap::new);

            if (metaJson != null) {
                Type metaType = new TypeToken<Map<String, String>>(){}.getType();
                metadata = GSON.fromJson(metaJson, metaType);
            }

            for (SoupModule module : SoupSettingsScreen.getAllModules()) {
                Map<String, Object> moduleSettings = config.get(module.getDisplayName());
                if (moduleSettings == null) continue;

                for (Setting<?> setting : module.getSettings()) {
                    Object value = moduleSettings.get(setting.getName());
                    if (value != null) {
                        try {
                            switch (setting) {
                                case BooleanSetting booleanSetting -> booleanSetting.setValue((Boolean) value);
                                case SliderSetting sliderSetting ->
                                        sliderSetting.setValue(((Double) value).floatValue());
                                case EnumSetting<?> enumSetting -> {
                                    Enum<?>[] values = enumSetting.getValues();
                                    for (Enum<?> enumValue : values) {
                                        if (enumValue.toString().equals(value)) {
                                            ((EnumSetting) enumSetting).setValue(enumValue);
                                            break;
                                        }
                                    }
                                }
                                case MinMaxSliderSetting minMaxSetting -> {
                                    double[] array = (double[]) value;
                                    minMaxSetting.setMinValue((float) array[0]);
                                    minMaxSetting.setMaxValue((float) array[1]);
                                }
                                default -> {
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (IOException | JsonParseException e) {
            e.printStackTrace();
        }
    }

    public static void setMetadata(String key, String value) {
        if (value == null) metadata.remove(key);
        else metadata.put(key, value);
    }

    public static String getMetadata(String key) {
        return metadata.get(key);
    }
}
