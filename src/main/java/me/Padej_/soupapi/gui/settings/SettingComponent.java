package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.Setting;
import net.minecraft.client.gui.DrawContext;

public abstract class SettingComponent<T> {
    protected final Setting<T> setting;
    protected int x, y, width, height;

    public SettingComponent(Setting<T> setting, int x, int y, int width, int height) {
        this.setting = setting;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    public abstract void mouseClicked(double mouseX, double mouseY, int button);
}

