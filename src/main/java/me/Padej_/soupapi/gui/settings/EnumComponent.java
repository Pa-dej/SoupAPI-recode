package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.EnumSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class EnumComponent<E extends Enum<E>> extends SettingComponent<E> {

    public EnumComponent(EnumSetting<E> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(x, y, x + width, y + height, 0xFF444444);
        Text text = Text.translatable(setting.getTranslationKey())
                .append(Text.literal(": "))
                .append(Text.literal(setting.getValue().toString()));
        context.drawText(textRenderer, text, x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 1) {
                setting.reset();
            } else if (button == 0) {
                E[] values = ((EnumSetting<E>) setting).getValues();
                int index = (setting.getValue().ordinal() + 1) % values.length;
                setting.setValue(values[index]);
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

