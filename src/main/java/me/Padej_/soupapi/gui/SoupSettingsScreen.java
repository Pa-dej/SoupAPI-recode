package me.Padej_.soupapi.gui;

import me.Padej_.soupapi.SoupModule;
import me.Padej_.soupapi.config.ConfigManager;
import me.Padej_.soupapi.gui.settings.*;
import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.settings.Setting;
import me.Padej_.soupapi.settings.impl.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.*;

public class SoupSettingsScreen extends Screen {

    private final Map<SoupModule.Category, List<SoupModule>> modulesByCategory;
    private final Map<Setting<?>, SettingComponent<?>> settingComponents = new HashMap<>();

    private SoupModule.Category selectedCategory = SoupModule.Category.WORLD;
    private SoupModule selectedModule = null;

    private static final List<SoupModule> ALL_MODULES = new ArrayList<>();

    static {
        ALL_MODULES.add(new AmbientParticle());
        ALL_MODULES.add(new AspectRatio());
        ALL_MODULES.add(new CustomFog());
        ALL_MODULES.add(new Capes());
        ALL_MODULES.add(new BetterHudStyles());
    }

    public SoupSettingsScreen() {
        super(Text.translatable("soupapi.screen.soup_settings.title"));

        this.modulesByCategory = new EnumMap<>(SoupModule.Category.class);
        for (SoupModule.Category cat : SoupModule.Category.values()) {
            modulesByCategory.put(cat, new ArrayList<>());
        }
        for (SoupModule module : ALL_MODULES) {
            modulesByCategory.get(module.getCategory()).add(module);
        }

        ConfigManager.loadConfig();

        // Восстановление выбранной категории и модуля
        String savedCategory = ConfigManager.getMetadata("selectedCategory");
        String savedModule = ConfigManager.getMetadata("selectedModule");

        if (savedCategory != null) {
            try {
                selectedCategory = SoupModule.Category.valueOf(savedCategory);
            } catch (IllegalArgumentException ignored) {}
        }

        if (savedModule != null) {
            for (SoupModule mod : ALL_MODULES) {
                if (mod.getDisplayName().equals(savedModule)) {
                    selectedModule = mod;
                    break;
                }
            }
        }
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
            context.drawText(MinecraftClient.getInstance().textRenderer,
                    Text.translatable(cat.getTranslationKey()), padding + 5, y + 6, 0xFFFFFFFF, false);
            y += 25;
        }

        // --- Middle: Modules ---
        y = padding;
        int modX = padding + colWidth + padding;
        List<SoupModule> mods = modulesByCategory.get(selectedCategory);
        for (SoupModule mod : mods) {
            boolean selected = mod == selectedModule;
            int color = selected ? 0xFFAAFFAA : 0xFF888888;
            context.fill(modX, y, modX + colWidth, y + 20, color);
            context.drawText(MinecraftClient.getInstance().textRenderer, mod.getTranslationKey(), modX + 5, y + 6, 0xFFFFFFFF, false);
            y += 25;
        }

        // --- Right: Settings ---
        if (selectedModule != null) {
            y = padding;
            int setX = modX + colWidth + padding;
            for (Setting<?> setting : selectedModule.getSettings()) {
                int finalY = y;
                SettingComponent<?> comp = settingComponents.computeIfAbsent(setting, s -> createComponent(setting, setX, finalY));
                comp.x = setX;
                comp.y = y;
                comp.render(context, mouseX, mouseY, delta);
                y += 25;
            }
        }

        super.render(context, mouseX, mouseY, delta);

        // --- Отрисовка description только для отображаемых настроек ---
        String hoveredDescription = null;
        if (selectedModule != null) {
            int descY = padding;
            int setX = padding + colWidth + padding + colWidth + padding;
            for (Setting<?> setting : selectedModule.getSettings()) {
                SettingComponent<?> comp = settingComponents.get(setting);
                if (comp != null) {
                    comp.x = setX;
                    comp.y = descY;
                    if (comp.isHovered(mouseX, mouseY)) {
                        hoveredDescription = comp.getSetting().getDescription();
                        break;
                    }
                    descY += 25;
                }
            }
        }

        if (hoveredDescription != null && !hoveredDescription.isEmpty()) {
            int screenWidth = this.width;
            int screenHeight = this.height;
            int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(hoveredDescription);
            int textX = (screenWidth - textWidth) / 2;
            int textY = screenHeight - 80;

            context.fill(textX - 4, textY - 2, textX + textWidth + 4, textY + 12, 0x88000000);
            context.drawText(MinecraftClient.getInstance().textRenderer, hoveredDescription, textX, textY, 0xFFFFFFFF, false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int padding = 10;
        int colWidth = 100;
        int y = padding;

        for (SoupModule.Category cat : SoupModule.Category.values()) {
            if (mouseX >= padding && mouseX <= padding + colWidth &&
                    mouseY >= y && mouseY <= y + 20) {
                selectedCategory = cat;
                selectedModule = null;
                return true;
            }
            y += 25;
        }

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

        boolean clickedOnComponent = false;
        if (selectedModule != null) {
            int i = 10;
            int setX = 10 + 100 + 10 + 100 + 10;
            for (Setting<?> setting : selectedModule.getSettings()) {
                SettingComponent<?> comp = settingComponents.get(setting);
                if (comp != null) {
                    comp.x = setX;
                    comp.y = i;
                    if (comp.isHovered(mouseX, mouseY)) {
                        comp.mouseClicked(mouseX, mouseY, button);
                        if (comp instanceof StringComponent sc) {
                            sc.setFocused(true);  // Вот здесь ставим фокус при клике по полю
                        }
                        clickedOnComponent = true;
                    } else if (comp instanceof StringComponent sc) {
                        sc.setFocused(false);
                    }
                }
                i += 25;
            }
        }

        if (clickedOnComponent) {
            return true;
        }

        ConfigManager.saveConfig();
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (selectedModule != null) {
            int y = 10;
            int setX = 10 + 100 + 10 + 100 + 10;
            for (Setting<?> setting : selectedModule.getSettings()) {
                SettingComponent<?> comp = settingComponents.get(setting);
                if (comp != null) {
                    comp.x = setX;
                    comp.y = y;
                    comp.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
                }
                y += 25;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (selectedModule != null) {
            int y = 10;
            int setX = 10 + 100 + 10 + 100 + 10;
            for (Setting<?> setting : selectedModule.getSettings()) {
                SettingComponent<?> comp = settingComponents.get(setting);
                if (comp != null) {
                    comp.x = setX;
                    comp.y = y;
                    comp.mouseReleased(mouseX, mouseY, button);
                }
                y += 25;
            }
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private SettingComponent<?> createComponent(Setting<?> setting, int x, int y) {
        return switch (setting) {
            case BooleanSetting bs -> new BooleanComponent(bs, x, y, 150, 20);
            case SliderSetting ss -> new SliderComponent(ss, x, y, 150, 20);
            case EnumSetting<?> es -> new EnumComponent<>(es, x, y, 150, 20);
            case ButtonSetting bs -> new ButtonComponent(bs, x, y, 150, 20);
            case StringSetting ss -> new StringComponent(ss, x, y, 150, 20);
            case null, default -> null;
        };
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (SettingComponent<?> comp : settingComponents.values()) {
            if (comp instanceof StringComponent sc && sc.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (SettingComponent<?> comp : settingComponents.values()) {
            if (comp instanceof StringComponent sc && sc.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        ConfigManager.setMetadata("selectedCategory", selectedCategory.name());
        ConfigManager.setMetadata("selectedModule", selectedModule != null ? selectedModule.getDisplayName().getString() : null);
        ConfigManager.saveConfig();
        super.close();
    }
}
