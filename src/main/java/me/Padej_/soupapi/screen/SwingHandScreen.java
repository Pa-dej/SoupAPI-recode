package me.Padej_.soupapi.screen;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.modules.SwingHand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class SwingHandScreen extends Screen {

    public SwingHandScreen() {
        super(Text.of("Swing Hand Screen"));
    }

    @Override
    protected void init() {
        MinecraftClient mc = MinecraftClient.getInstance();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        int sliderWidth = 100;
        int sliderHeight = 20;
        int padding = 5;
        int rowSpacing = sliderHeight + padding;
        int colSpacing = sliderWidth + padding;

        // Теперь полностью рассчитываем начало таблицы
        int totalWidth = sliderWidth * 3 + padding * 2; // 3 слайдера + 2 промежутка
        int startX = centerX - totalWidth / 2;
        int startY = centerY - 90;

        int y = startY;

        // Первая строка: Позиция
        addSlider(startX, y, "X Pos", -2.0F, 2.0F, () -> SwingHand.xPos, v -> SwingHand.xPos = v);
        addSlider(startX + sliderWidth + padding, y, "Y Pos", -2.0F, 2.0F, () -> SwingHand.yPos, v -> SwingHand.yPos = v);
        addSlider(startX + (sliderWidth + padding) * 2, y, "Z Pos", -2.0F, 2.0F, () -> SwingHand.zPos, v -> SwingHand.zPos = v);
        y += rowSpacing;

        // Вторая строка: Обычная ротация
        addSlider(startX, y, "X Rot", -180.0F, 180.0F, () -> SwingHand.rotX, v -> SwingHand.rotX = (int) v);
        addSlider(startX + sliderWidth + padding, y, "Y Rot", -180.0F, 180.0F, () -> SwingHand.rotY, v -> SwingHand.rotY = (int) v);
        addSlider(startX + (sliderWidth + padding) * 2, y, "Z Rot", -180.0F, 180.0F, () -> SwingHand.rotZ, v -> SwingHand.rotZ = (int) v);
        y += rowSpacing;

        // Третья строка: Swing ротация
        addSlider(startX, y, "X Swing Rot", -90.0F, 90.0F, () -> SwingHand.xSwingRot, v -> SwingHand.xSwingRot = (int) v);
        addSlider(startX + sliderWidth + padding, y, "Y Swing Rot", -90.0F, 90.0F, () -> SwingHand.ySwingRot, v -> SwingHand.ySwingRot = (int) v);
        addSlider(startX + (sliderWidth + padding) * 2, y, "Z Swing Rot", -90.0F, 90.0F, () -> SwingHand.zSwingRot, v -> SwingHand.zSwingRot = (int) v);
        y += rowSpacing;

        // Scale и Speed слайдеры рядом друг с другом
        addSliderX2W((int) (centerX - sliderWidth * 1.5f) - padding, y, "Scale", 0.0F, 2.0F, () -> SwingHand.scale, v -> SwingHand.scale = v);
        addSliderX2W(centerX + padding, y, "Speed", 0.0F, 100.0F, () -> SwingHand.speed, v -> SwingHand.speed = (int) v);
        y += rowSpacing + 10; // дополнительный отступ перед кнопкой

        this.addDrawableChild(ButtonWidget.builder(Text.of("Swing Hand"), button -> {
            if (mc.player == null) return;
            mc.player.swingHand(mc.player.getActiveHand());
        }).dimensions(centerX - 50, y, 100, 20).build());
    }

    @Override
    public void close() {
        super.close();
        CONFIG.swingHand_xPos = SwingHand.xPos;
        CONFIG.swingHand_yPos = SwingHand.yPos;
        CONFIG.swingHand_zPos = SwingHand.zPos;
        CONFIG.swingHand_scale = SwingHand.scale;
        CONFIG.swingHand_rotX = SwingHand.rotX;
        CONFIG.swingHand_rotY = SwingHand.rotY;
        CONFIG.swingHand_rotZ = SwingHand.rotZ;
        CONFIG.swingHand_xSwingRot = SwingHand.xSwingRot;
        CONFIG.swingHand_ySwingRot = SwingHand.ySwingRot;
        CONFIG.swingHand_zSwingRot = SwingHand.zSwingRot;
        CONFIG.swingHand_speed = SwingHand.speed;
        ConfigurableModule.saveConfig();
    }

    private void addSlider(int x, int y, String label, float min, float max, FloatSupplier getter, FloatConsumer setter) {
        this.addDrawableChild(new SliderWidget(x, y, 100, 20, Text.of(label), (getter.get() - min) / (max - min)) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of(label + ": " + String.format("%.1f", min + this.value * (max - min))));
            }

            @Override
            protected void applyValue() {
                setter.accept(min + (float) (this.value * (max - min)));
            }
        });
    }

    private void addSliderX2W(int x, int y, String label, float min, float max, FloatSupplier getter, FloatConsumer setter) {
        this.addDrawableChild(new SliderWidget(x, y, (int) (100 * 1.5f), 20, Text.of(label), (getter.get() - min) / (max - min)) {
            @Override
            protected void updateMessage() {
                this.setMessage(Text.of(label + ": " + String.format("%.1f", min + this.value * (max - min))));
            }

            @Override
            protected void applyValue() {
                setter.accept(min + (float) (this.value * (max - min)));
            }
        });
    }

    @FunctionalInterface
    private interface FloatSupplier {
        float get();
    }

    @FunctionalInterface
    private interface FloatConsumer {
        void accept(float value);
    }
}



