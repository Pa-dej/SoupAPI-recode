package me.Padej_.soupapi.screen;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.utils.CursorUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.List;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class ConfigHudPositionsScreen extends Screen {
    private final Frame targetHudFrame;
    private final Frame potionsHudFrame;
    private final Frame watermarkFrame;
    private final Frame mouseMoveFrame;

    public ConfigHudPositionsScreen() {
        super(Text.of("SoupAPI config screen"));
        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
        targetHudFrame = new Frame(
                centerX + CONFIG.targetHudOffsetX,
                centerY - CONFIG.targetHudOffsetY,
                50, 30,
                "Target HUD",
                0xAA00FF55
        );
        potionsHudFrame = new Frame(
                CONFIG.hudBetterPotionsHudX,
                CONFIG.hudBetterPotionsHudY,
                80, 50,
                "Potions HUD",
                0xAA5500FF
        );
        watermarkFrame = new Frame(
                CONFIG.waterMarkX,
                CONFIG.waterMarkY,
                100, 18,
                "Watermark",
                0xAAFF5555
        );
        mouseMoveFrame = new Frame(
                CONFIG.mouseMoveX,
                CONFIG.mouseMoveY,
                40, 40,
                "Mouse Move",
                0xAA0055FF
        );

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean hovering = targetHudFrame.isMouseOverDragArea(mouseX, mouseY)
                || potionsHudFrame.isMouseOverDragArea(mouseX, mouseY)
                || watermarkFrame.isMouseOverDragArea(mouseX, mouseY)
                || mouseMoveFrame.isMouseOverDragArea(mouseX, mouseY);

        if (hovering) {
            CursorUtils.setResizeCursor();
        } else {
            CursorUtils.setDefaultCursor();
        }

        targetHudFrame.render(context);
        potionsHudFrame.render(context);
        watermarkFrame.render(context);
        mouseMoveFrame.render(context);

        renderSnapGuides(context, targetHudFrame);
        renderSnapGuides(context, potionsHudFrame);
        renderSnapGuides(context, watermarkFrame);
        renderSnapGuides(context, mouseMoveFrame);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (targetHudFrame.isMouseOverDragArea(mouseX, mouseY)) {
                targetHudFrame.startDragging(mouseX, mouseY);
                return true;
            }
            if (potionsHudFrame.isMouseOverDragArea(mouseX, mouseY)) {
                potionsHudFrame.startDragging(mouseX, mouseY);
                return true;
            }
            if (watermarkFrame.isMouseOverDragArea(mouseX, mouseY)) {
                watermarkFrame.startDragging(mouseX, mouseY);
                return true;
            }
            if (mouseMoveFrame.isMouseOverDragArea(mouseX, mouseY)) {
                mouseMoveFrame.startDragging(mouseX, mouseY);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            if (targetHudFrame.isDragging()) {
                Point snapped = applySnappingOffsets(targetHudFrame, mouseX, mouseY);
                targetHudFrame.setPosition(snapped.x, snapped.y);
                int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
                int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
                CONFIG.targetHudOffsetX = targetHudFrame.getOffsetX(centerX);
                CONFIG.targetHudOffsetY = -targetHudFrame.getOffsetY(centerY);
                return true;
            }
            if (potionsHudFrame.isDragging()) {
                Point snapped = applySnappingOffsets(potionsHudFrame, mouseX, mouseY);
                potionsHudFrame.setPosition(snapped.x, snapped.y);
                CONFIG.hudBetterPotionsHudX = snapped.x;
                CONFIG.hudBetterPotionsHudY = snapped.y;
                return true;
            }
            if (watermarkFrame.isDragging()) {
                Point snapped = applySnappingOffsets(watermarkFrame, mouseX, mouseY);
                watermarkFrame.setPosition(snapped.x, snapped.y);
                CONFIG.waterMarkX = snapped.x;
                CONFIG.waterMarkY = snapped.y;
                return true;
            }
            if (mouseMoveFrame.isDragging()) {
                Point snapped = applySnappingOffsets(mouseMoveFrame, mouseX, mouseY);
                mouseMoveFrame.setPosition(snapped.x, snapped.y);
                CONFIG.mouseMoveX = snapped.x;
                CONFIG.mouseMoveY = snapped.y;
                return true;
            }
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            targetHudFrame.stopDragging();
            potionsHudFrame.stopDragging();
            watermarkFrame.stopDragging();
            mouseMoveFrame.stopDragging();
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        int centerX = MinecraftClient.getInstance().getWindow().getScaledWidth() / 2;
        int centerY = MinecraftClient.getInstance().getWindow().getScaledHeight() / 2;
        CONFIG.targetHudOffsetX = targetHudFrame.getOffsetX(centerX);
        CONFIG.targetHudOffsetY = -targetHudFrame.getOffsetY(centerY);
        CONFIG.hudBetterPotionsHudX = potionsHudFrame.getOffsetX(0);
        CONFIG.hudBetterPotionsHudY = potionsHudFrame.getOffsetY(0);
        CONFIG.waterMarkX = watermarkFrame.getOffsetX(0);
        CONFIG.waterMarkY = watermarkFrame.getOffsetY(0);
        CONFIG.mouseMoveX = mouseMoveFrame.getOffsetX(0);
        CONFIG.mouseMoveY = mouseMoveFrame.getOffsetY(0);
        ConfigurableModule.saveConfig();
        CursorUtils.setDefaultCursor();
        super.close();
    }

    private void renderSnapGuides(DrawContext context, Frame movingFrame) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

        int snapThreshold = 5;
        boolean drawVertical = false, drawHorizontal = false;

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // Центры рамки
        int frameCenterX = movingFrame.getX() + movingFrame.width / 2;
        int frameCenterY = movingFrame.getY() + movingFrame.height / 2;

        // Привязка к центру экрана
        if (Math.abs(frameCenterX - centerX) < snapThreshold) {
            drawVertical = true;
            movingFrame.snapX(centerX - movingFrame.width / 2);
        }
        if (Math.abs(frameCenterY - centerY) < snapThreshold) {
            drawHorizontal = true;
            movingFrame.snapY(centerY - movingFrame.height / 2);
        }

        // Привязка к другим рамкам (по центрам)
        for (Frame other : List.of(targetHudFrame, potionsHudFrame, watermarkFrame)) {
            if (other == movingFrame) continue;

            int otherCenterX = other.getX() + other.width / 2;
            int otherCenterY = other.getY() + other.height / 2;

            if (Math.abs(frameCenterX - otherCenterX) < snapThreshold) {
                drawVertical = true;
                movingFrame.snapX(otherCenterX - movingFrame.width / 2);
            }

            if (Math.abs(frameCenterY - otherCenterY) < snapThreshold) {
                drawHorizontal = true;
                movingFrame.snapY(otherCenterY - movingFrame.height / 2);
            }

            // Привязка по левому и правому краям
            if (Math.abs(movingFrame.getX() - other.getX()) < snapThreshold) {
                movingFrame.snapX(other.getX());
            } else if (Math.abs((movingFrame.getX() + movingFrame.width) - (other.getX() + other.width)) < snapThreshold) {
                movingFrame.snapX(other.getX() + other.width - movingFrame.width);
            }
        }

        // Отрисовка линий
        if (drawVertical) {
            context.fill(centerX, 0, centerX + 1, screenHeight, 0x80FFFFFF);
        }
        if (drawHorizontal) {
            context.fill(0, centerY, screenWidth, centerY + 1, 0x80FFFFFF);
        }
    }

    private Point applySnappingOffsets(Frame frame, double mouseX, double mouseY) {
        int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
        int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
        int snapThreshold = 5;

        int newX = (int)(mouseX - frame.dragOffsetX);
        int newY = (int)(mouseY - frame.dragOffsetY);

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int frameCenterX = newX + frame.width / 2;
        int frameCenterY = newY + frame.height / 2;

        // Центр экрана
        if (Math.abs(frameCenterX - centerX) < snapThreshold) {
            newX = centerX - frame.width / 2;
        }
        if (Math.abs(frameCenterY - centerY) < snapThreshold) {
            newY = centerY - frame.height / 2;
        }

        // Привязка к другим рамкам
        for (Frame other : List.of(targetHudFrame, potionsHudFrame, watermarkFrame)) {
            if (other == frame) continue;

            int otherCenterX = other.getX() + other.width / 2;
            int otherCenterY = other.getY() + other.height / 2;

            if (Math.abs(frameCenterX - otherCenterX) < snapThreshold) {
                newX = otherCenterX - frame.width / 2;
            }

            if (Math.abs(frameCenterY - otherCenterY) < snapThreshold) {
                newY = otherCenterY - frame.height / 2;
            }

            // Левые / правые края
            if (Math.abs(newX - other.getX()) < snapThreshold) {
                newX = other.getX();
            } else if (Math.abs((newX + frame.width) - (other.getX() + other.width)) < snapThreshold) {
                newX = other.getX() + other.width - frame.width;
            }

            // Верх / низ
            if (Math.abs(newY - other.getY()) < snapThreshold) {
                newY = other.getY();
            } else if (Math.abs((newY + frame.height) - (other.getY() + other.height)) < snapThreshold) {
                newY = other.getY() + other.height - frame.height;
            }
        }

        // Ограничения
        newX = MathHelper.clamp(newX, 0, screenWidth - frame.width);
        newY = MathHelper.clamp(newY, 0, screenHeight - frame.height);

        return new Point(newX, newY);
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

            TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
            int textHeight = textRenderer.fontHeight;

            int textY = y - textHeight - 2;

            if (textY < 0) {
                textY = y + height;
            }

            context.drawTextWithShadow(
                    textRenderer, name, x, textY, 0xFFFFFFFF
            );
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
                int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
                int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();

                int newX = (int)(mouseX - dragOffsetX);
                int newY = (int)(mouseY - dragOffsetY);

                newX = MathHelper.clamp(newX, 0, screenWidth - width);
                newY = MathHelper.clamp(newY, 0, screenHeight - height);

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

        public void snapX(int newX) {
            if (isDragging) this.x = newX;
        }

        public void snapY(int newY) {
            if (isDragging) this.y = newY;
        }

        public void setPosition(int newX, int newY) {
            this.x = newX;
            this.y = newY;
        }
    }
}
