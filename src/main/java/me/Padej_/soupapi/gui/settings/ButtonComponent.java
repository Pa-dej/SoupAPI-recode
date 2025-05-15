package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.ButtonSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class ButtonComponent extends SettingComponent<Void> {

    public ButtonComponent(ButtonSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(x, y, x + width, y + height, 0xFF5555FF);
        context.drawText(textRenderer, Text.translatable(setting.getTranslationKey()), x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY) && button == 0) { // ЛКМ
            ((ButtonSetting) setting).click();
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {}

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {}

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }
}

