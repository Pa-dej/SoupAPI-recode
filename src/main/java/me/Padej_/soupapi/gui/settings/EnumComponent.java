package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.EnumSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class EnumComponent<E extends Enum<E>> extends SettingComponent<E> {

    public EnumComponent(EnumSetting<E> setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(x, y, x + width, y + height, 0xFF444444);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                setting.getName() + ": " + setting.getValue().name(), x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            E[] values = ((EnumSetting<E>) setting).getValues();
            int index = (setting.getValue().ordinal() + 1) % values.length;
            setting.setValue(values[index]);
        }
    }
}

