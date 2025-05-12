package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.SliderSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public class SliderComponent extends SettingComponent<Float> {

    private final SliderSetting sliderSetting;

    public SliderComponent(SliderSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        this.sliderSetting = setting;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        float value = sliderSetting.getValue();
        float percentage = (value - sliderSetting.getMin()) / (sliderSetting.getMax() - sliderSetting.getMin());
        int sliderWidth = (int) (percentage * width);

        context.fill(x, y, x + sliderWidth, y + height, 0xFF00AAFF);
        context.drawText(MinecraftClient.getInstance().textRenderer,
                sliderSetting.getName() + ": " + String.format("%.2f", value), x + 4, y + 4, 0xFFFFFFFF, false);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            float percentage = (float) (mouseX - x) / width;
            float newValue = sliderSetting.getMin() + (sliderSetting.getMax() - sliderSetting.getMin()) * percentage;
            sliderSetting.setValue(newValue);
        }
    }
}


