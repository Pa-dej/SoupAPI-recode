package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public abstract class SettingComponent<T> {
    protected final Setting<T> setting;
    public int x;
    public int y;
    public int width;
    public int height;

    public static TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    public SettingComponent(Setting<T> setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            if (button == 1) { // ПКМ — сброс на дефолт
                setting.reset();
            }
        }
    }

    public abstract void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY);

    public abstract void mouseReleased(double mouseX, double mouseY, int button);

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public Setting<T> getSetting() {
        return setting;
    }

    public abstract boolean keyPressed(int keyCode, int scanCode, int modifiers);

    public abstract boolean charTyped(char chr, int modifiers);
}
