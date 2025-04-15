package me.Padej_.soupapi.screen;

import me.Padej_.soupapi.config.ConfigurableModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class ConfigScreen extends Screen {
    private Frame targetHudFrame;

    public ConfigScreen() {
        super(Text.of("SoupAPI config screen"));
        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
        targetHudFrame = new Frame(
                centerX + CONFIG.targetHudOffsetX,
                centerY - CONFIG.targetHudOffsetY,
                15, 15,
                "Target HUD",
                0xAA00FF55
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        targetHudFrame.render(context);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && targetHudFrame.isMouseOverDragArea(mouseX, mouseY)) {
            targetHudFrame.startDragging(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0 && targetHudFrame.isDragging()) {
            targetHudFrame.updatePosition(mouseX, mouseY);
            int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
            int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
            CONFIG.targetHudOffsetX = targetHudFrame.getOffsetX(centerX);
            CONFIG.targetHudOffsetY = -targetHudFrame.getOffsetY(centerY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            targetHudFrame.stopDragging();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
        System.out.println("Target HUD offsets: X=" + targetHudFrame.getOffsetX(centerX) +
                ", Y=" + targetHudFrame.getOffsetY(centerY));
        CONFIG.targetHudOffsetX = targetHudFrame.getOffsetX(centerX);
        CONFIG.targetHudOffsetY = -targetHudFrame.getOffsetY(centerY);
        ConfigurableModule.saveConfig();
        super.close();
    }

    private static class Frame {
        private int x, y;
        private final int width, height;
        private final String name;
        private final int color;
        private boolean isDragging;
        private double dragOffsetX, dragOffsetY;

        public Frame(int x, int y, int width, int height, String name, int color) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.name = name;
            this.color = color;
            this.isDragging = false;
        }

        public void render(DrawContext context) {
            context.fill(x, y, x + width, y + height, color);
            context.drawBorder(x, y, width, height, color);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer,
                    name, x, y - 12, 0xFFFFFFFF);
        }

        public boolean isMouseOver(double mouseX, double mouseY) {
            int hitboxWidth = Math.max(width, MinecraftClient.getInstance().textRenderer.getWidth(name));
            return mouseX >= x && mouseX <= x + hitboxWidth &&
                    mouseY >= y - 12 && mouseY <= y + height;
        }

        public boolean isMouseOverDragArea(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width &&
                    mouseY >= y && mouseY <= y + height;
        }

        public void startDragging(double mouseX, double mouseY) {
            isDragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
        }

        public void updatePosition(double mouseX, double mouseY) {
            if (isDragging) {
                int hitboxWidth = Math.max(width, MinecraftClient.getInstance().textRenderer.getWidth(name));
                int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
                int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

                int newX = (int)(mouseX - dragOffsetX);
                int newY = (int)(mouseY - dragOffsetY);

                newX = MathHelper.clamp(newX, 0, screenWidth - hitboxWidth);
                newY = MathHelper.clamp(newY, 12, screenHeight - height);

                x = newX;
                y = newY;
            }
        }

        public void stopDragging() {
            isDragging = false;
        }

        public boolean isDragging() {
            return isDragging;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getOffsetX(int centerX) {
            return x - centerX;
        }

        public int getOffsetY(int centerY) {
            return y - centerY;
        }
    }
}
