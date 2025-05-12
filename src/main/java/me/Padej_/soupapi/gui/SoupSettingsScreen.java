package me.Padej_.soupapi.gui;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.config.ConfigManager;
import me.Padej_.soupapi.gui.settings.BooleanComponent;
import me.Padej_.soupapi.gui.settings.EnumComponent;
import me.Padej_.soupapi.gui.settings.SettingComponent;
import me.Padej_.soupapi.gui.settings.SliderComponent;
import me.Padej_.soupapi.modules.CustomFog;
import me.Padej_.soupapi.settings.Setting;
import me.Padej_.soupapi.settings.impl.BooleanSetting;
import me.Padej_.soupapi.settings.impl.EnumSetting;
import me.Padej_.soupapi.settings.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SoupSettingsScreen extends Screen {

    private final Map<SoupModule.Category, List<SoupModule>> modulesByCategory;
    private SoupModule.Category selectedCategory = SoupModule.Category.WORLD;
    private SoupModule selectedModule = null;

    private static final List<SoupModule> ALL_MODULES = new ArrayList<>();

    static {
//        ALL_MODULES.add(new CustomFogNewGUI());
    }

    public SoupSettingsScreen() {
        super(Text.literal("Soup Settings"));

        this.modulesByCategory = new EnumMap<>(SoupModule.Category.class);
        for (SoupModule.Category cat : SoupModule.Category.values()) {
            modulesByCategory.put(cat, new ArrayList<>());
        }
        for (SoupModule module : ALL_MODULES) {
            modulesByCategory.get(module.getCategory()).add(module);
        }

        // Load config when screen is opened
        ConfigManager.loadConfig();
    }

    public static List<SoupModule> getAllModules() {
        return ALL_MODULES;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int padding = 10;
        int colWidth = 100;

        // --- Left: Categories ---
        int y = padding;
        for (SoupModule.Category cat : SoupModule.Category.values()) {
            boolean selected = cat == selectedCategory;
            int color = selected ? 0xFFAAAAFF : 0xFF888888;
            context.fill(padding, y, padding + colWidth, y + 20, color);
            context.drawText(MinecraftClient.getInstance().textRenderer, cat.name(), padding + 5, y + 6, 0xFFFFFFFF, false);
            y += 25;
        }

        // --- Middle: Modules in selected category ---
        y = padding;
        int modX = padding + colWidth + padding;
        List<SoupModule> mods = modulesByCategory.get(selectedCategory);
        for (SoupModule mod : mods) {
            boolean selected = mod == selectedModule;
            int color = selected ? 0xFFAAFFAA : 0xFF888888;
            context.fill(modX, y, modX + colWidth, y + 20, color);
            context.drawText(MinecraftClient.getInstance().textRenderer, mod.getName(), modX + 5, y + 6, 0xFFFFFFFF, false);
            y += 25;
        }

        // --- Right: Settings of selected module ---
        if (selectedModule != null) {
            y = padding;
            int setX = modX + colWidth + padding;
            List<Setting<?>> settings = selectedModule.getSettings();
            for (Setting<?> setting : settings) {
                SettingComponent<?> comp = null;
                if (setting instanceof BooleanSetting bs) {
                    comp = new BooleanComponent(bs, setX, y, 150, 20);
                } else if (setting instanceof SliderSetting ss) {
                    comp = new SliderComponent(ss, setX, y, 150, 20);
                } else if (setting instanceof EnumSetting<?> es) {
                    comp = new EnumComponent<>(es, setX, y, 150, 20);
                }
                if (comp != null) {
                    comp.render(context, mouseX, mouseY, delta);
                    y += 25;
                }
            }
        }

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int padding = 10;
        int colWidth = 100;
        int y = padding;

        // --- Click: Category column ---
        for (SoupModule.Category cat : SoupModule.Category.values()) {
            if (mouseX >= padding && mouseX <= padding + colWidth &&
                    mouseY >= y && mouseY <= y + 20) {
                selectedCategory = cat;
                selectedModule = null;
                return true;
            }
            y += 25;
        }

        // --- Click: Module column ---
        y = padding;
        int modX = padding + colWidth + padding;
        List<SoupModule> mods = modulesByCategory.get(selectedCategory);
        for (SoupModule mod : mods) {
            if (mouseX >= modX && mouseX <= modX + colWidth &&
                    mouseY >= y && mouseY <= y + 20) {
                selectedModule = mod;
                return true;
            }
            y += 25;
        }

        // --- Click: Setting widgets ---
        boolean settingChanged = false;
        if (selectedModule != null) {
            y = padding;
            int setX = modX + colWidth + padding;
            for (Setting<?> setting : selectedModule.getSettings()) {
                SettingComponent<?> comp = null;
                if (setting instanceof BooleanSetting bs) {
                    comp = new BooleanComponent(bs, setX, y, 150, 20);
                } else if (setting instanceof SliderSetting ss) {
                    comp = new SliderComponent(ss, setX, y, 150, 20);
                } else if (setting instanceof EnumSetting<?> es) {
                    comp = new EnumComponent<>(es, setX, y, 150, 20);
                }
                if (comp != null && mouseX >= setX && mouseX <= setX + 150 &&
                        mouseY >= y && mouseY <= y + 20) {
                    comp.mouseClicked(mouseX, mouseY, button);
                    settingChanged = true;
                }
                y += 25;
            }
        }

        // Save config if a setting was changed
        if (settingChanged) {
            ConfigManager.saveConfig();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        // Save config when closing the screen
        ConfigManager.saveConfig();
        super.close();
    }
}

