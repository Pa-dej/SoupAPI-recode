package me.Padej_.soupapi.gui.settings;

import me.Padej_.soupapi.settings.impl.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class StringComponent extends SettingComponent<String> {

    private final TextFieldWidget textField;
    private final TextRenderer textRenderer;

    public StringComponent(StringSetting setting, int x, int y, int width, int height) {
        super(setting, x, y, width, height);
        MinecraftClient mc = MinecraftClient.getInstance();
        this.textRenderer = mc.textRenderer;

        this.textField = new TextFieldWidget(textRenderer, x, y, width, height, Text.literal(setting.getName()));
        this.textField.setMaxLength(1024);
        this.textField.setText(setting.getValue());
        this.textField.setEditable(true);
        this.textField.setChangedListener(setting::setValue);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int labelHeight = textRenderer.fontHeight;
        context.drawText(textRenderer, Text.translatable(setting.getTranslationKey()), x, y, 0xFFFFFFFF, false);

        textField.setX(x);
        textField.setY(y + labelHeight + 2); // 2 пикселя паддинга между названием и полем
        textField.setWidth(width);
        textField.setHeight(height);

        textField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        boolean inside = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        if (button == 1 && inside) {
            textField.setText(setting.getDefaultValue());
            setting.setValue(setting.getDefaultValue());
            setFocused(false);
        } else if (inside) {
            setFocused(true);
            textField.mouseClicked(mouseX, mouseY, button);
        } else {
            setFocused(false);
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {
        textField.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        textField.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return textField.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        return textField.charTyped(chr, modifiers);
    }

    public void setFocused(boolean focused) {
        textField.setFocused(focused);
    }
}


