package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.BooleanSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class BooleanComponent extends SettingComponent<Boolean> {

    public BooleanComponent(BooleanSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean value = setting.getValue();
        context.fill(x, y, x + width, y + height, value ? 0xFF00FF00 : 0xFFFF0000);
        context.drawText(MinecraftClient.getInstance().textRenderer, setting.getName(), x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            setting.setValue(!setting.getValue());
        }
    }
}

