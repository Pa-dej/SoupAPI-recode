package me.Padej_.soupapi.screen;

import me.Padej_.soupapi.config.SoupAPI_Config;
import me.Padej_.soupapi.modules.Capes;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;
import static me.Padej_.soupapi.config.ConfigurableModule.saveConfig;

public class CapeSelectScreen extends Screen {
    private static final int TEXTURE_RENDER_WIDTH = 20;
    private static final int TEXTURE_RENDER_HEIGHT = 32;
    private static final int PADDING = 8;
    private static final int BORDER_SIZE = 1;

    private final String[] categories = {"APRIL", "DEFAULT", "XBOX", "MCD", "CUSTOM", "STAFF"};

    public CapeSelectScreen() {
        super(Text.of("Cape Textures"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = PADDING;
        int y = PADDING;
        int categoryY = y; // Начальная позиция для отображения названия категории

        for (String category : categories) {
            // Отображение названия категории
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.of(category).copy().formatted(Formatting.BOLD),
                    x,
                    categoryY, 0xFFFFFFFF, true
            );
            categoryY += 10; // Смещение вниз на 10 пикселей для текстур

            int textureX = x; // Начальная позиция для текстур в данной категории
            int textureY = categoryY; // Начальная позиция для текстур

            for (Capes.CapeTextures cape : Capes.CapeTextures.values()) {
                // Проверка, соответствует ли плащ текущей категории
                if (cape.name().startsWith(category)) {
                    Identifier texture = cape.getTexturePath();

                    // Рендер текстуры плаща с заданными координатами и размером
                    drawTexture(context, texture, textureX, textureY);

                    // Если текущий плащ выбран, рисуем желтую рамку
                    if (cape == CONFIG.capesTexture) {
                        context.drawBorder(textureX - BORDER_SIZE, textureY - BORDER_SIZE, TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFF00FFFF);
                        context.drawBorder(textureX - BORDER_SIZE - 1, textureY - BORDER_SIZE - 1, 2 + TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, 2 + TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFF00FFFF);
                    }
                    // Если курсор находится над текстурой, рисуем белую рамку
                    else if (isMouseOverTexture(mouseX, mouseY, textureX, textureY)) {
                        context.drawBorder(textureX - BORDER_SIZE, textureY - BORDER_SIZE, TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFFFFFFFF);
                    }

                    // Переход на следующую позицию по X для следующей текстуры
                    textureX += TEXTURE_RENDER_WIDTH + PADDING;
                    // Если текстуры не помещаются по ширине, переходим на новую строку
                    if (textureX + TEXTURE_RENDER_WIDTH > width - PADDING) {
                        textureX = x; // Возвращаемся в начало по X
                        textureY += TEXTURE_RENDER_HEIGHT + PADDING; // Переходим на следующую строку
                    }
                }
            }

            // Обновляем y для следующей категории, добавляя отступ в 10 пикселей
            categoryY = textureY + TEXTURE_RENDER_HEIGHT + 10; // Устанавливаем y на нижний уровень последней текстуры и добавляем отступ
        }
    }

    private boolean isMouseOverTexture(int mouseX, int mouseY, int textureX, int textureY) {
        return mouseX >= textureX && mouseX <= textureX + TEXTURE_RENDER_WIDTH &&
                mouseY >= textureY && mouseY <= textureY + TEXTURE_RENDER_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = PADDING;
        int y = PADDING;
        int categoryY = y;

        for (String category : categories) {
            // Увеличиваем categoryY на отступы перед текстурами
            categoryY += 10 + PADDING; // Учитываем название категории и отступ

            int textureX = x;
            int textureY = categoryY; // Позиция для текстур

            for (Capes.CapeTextures cape : Capes.CapeTextures.values()) {
                if (cape.name().startsWith(category)) {
                    // Проверяем, попадает ли клик на текстуру
                    if (isMouseOverTexture((int) mouseX, (int) mouseY, textureX, textureY)) {
                        // Устанавливаем выбранную текстуру
                        CONFIG.capesTexture = cape;
                        saveConfig();
                        return true;
                    }

                    // Переход на следующую позицию по X для следующей текстуры
                    textureX += TEXTURE_RENDER_WIDTH + PADDING;

                    // Если текстуры не помещаются по ширине, переходим на новую строку
                    if (textureX + TEXTURE_RENDER_WIDTH > width - PADDING) {
                        textureX = x;
                        textureY += TEXTURE_RENDER_HEIGHT + PADDING; // Переходим на новую строку
                    }
                }
            }

            // Обновляем categoryY на нижний уровень последней текстуры и добавляем отступ
            categoryY = textureY + TEXTURE_RENDER_HEIGHT; // Устанавливаем categoryY для следующей категории
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void close() {
        super.close();
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.setScreen(AutoConfig.getConfigScreen(SoupAPI_Config.class, mc.currentScreen).get());
    }

    private void drawTexture(DrawContext context, Identifier texture, int x, int y) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 2, 2, TEXTURE_RENDER_WIDTH, TEXTURE_RENDER_HEIGHT, 128, 64);
    }
}
