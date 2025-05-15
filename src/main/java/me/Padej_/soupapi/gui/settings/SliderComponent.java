package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.SliderSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class SliderComponent extends SettingComponent<Float> {
    private final SliderSetting sliderSetting;
    private boolean dragging = false;

    public SliderComponent(SliderSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        this.sliderSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float percentage = (setting.getValue() - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin());
        int fillWidth = (int)(percentage * width);

        context.fill(x, y, x + fillWidth, y + height, 0xFF00FF00);
        context.fill(x + fillWidth, y, x + width, y + height, 0xFF555555);

        Text text = Text.translatable(setting.getTranslationKey())
                .append(Text.literal(": "))
                .append(Text.literal(String.valueOf(setting.getValue())));

        context.drawText(textRenderer, text, x + 5, y + 5, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 1) {
                setting.reset();
            } else if (button == 0) {
                dragging = true;
                updateValue(mouseX);
            }
        }
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateValue(mouseX);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        dragging = false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    private void updateValue(double mouseX) {
        float percentage = (float)Math.max(0, Math.min(1, (mouseX - x) / width));
        float rawValue = sliderSetting.getMin() + percentage * (sliderSetting.getMax() - sliderSetting.getMin());

        // округление по шагу
        float step = sliderSetting.getStep();
        float steppedValue = Math.round(rawValue / step) * step;

        setting.setValue(steppedValue);
    }
}