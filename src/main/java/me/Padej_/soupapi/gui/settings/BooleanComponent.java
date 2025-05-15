package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class BooleanComponent extends SettingComponent<Boolean> {

    public BooleanComponent(BooleanSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean value = setting.getValue();
        context.fill(x, y, x + width, y + height, value ? 0xFF00FF00 : 0xFFFF0000);
        context.drawText(textRenderer, Text.translatable(setting.getTranslationKey()), x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 1) { // ПКМ
                setting.reset();
            } else if (button == 0) {
                setting.setValue(!setting.getValue());
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {

    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}

