package me.Padej_.soupapi.screen;

import me.Padej_.soupapi.modules.Capes;
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

    private final String[] categories = {"APRIL", "DEFAULT", "XBOX", "MCD", "CUSTOM", "OPTIFINE", "STAFF"};

    public CapeSelectScreen() {
        super(Text.of("Cape Textures"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        int x = PADDING;
        int categoryY = PADDING;

        for (String category : categories) {
            context.drawText(
                    MinecraftClient.getInstance().textRenderer,
                    Text.of(category).copy().formatted(Formatting.BOLD),
                    x,
                    categoryY, 0xFFFFFFFF, true
            );
            categoryY += 10;

            int textureX = x;
            int textureY = categoryY;

            for (Capes.CapeTextures cape : Capes.CapeTextures.values()) {
                if (cape.name().startsWith(category)) {
                    Identifier texture = cape.getTexturePath();

                    drawTexture(context, texture, textureX, textureY);

                    if (cape == CONFIG.capesTexture) {
                        context.drawBorder(textureX - BORDER_SIZE, textureY - BORDER_SIZE, TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFF00FFFF);
                        context.drawBorder(textureX - BORDER_SIZE - 1, textureY - BORDER_SIZE - 1, 2 + TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, 2 + TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFF00FFFF);
                    }
                    else if (isMouseOverTexture(mouseX, mouseY, textureX, textureY)) {
                        context.drawBorder(textureX - BORDER_SIZE, textureY - BORDER_SIZE, TEXTURE_RENDER_WIDTH + BORDER_SIZE * 2, TEXTURE_RENDER_HEIGHT + BORDER_SIZE * 2, 0xFFFFFFFF);
                    }

                    textureX += TEXTURE_RENDER_WIDTH + PADDING;
                    if (textureX + TEXTURE_RENDER_WIDTH > width - PADDING) {
                        textureX = x;
                        textureY += TEXTURE_RENDER_HEIGHT + PADDING;
                    }
                }
            }

            categoryY = textureY + TEXTURE_RENDER_HEIGHT + 10;
        }
    }

    private boolean isMouseOverTexture(int mouseX, int mouseY, int textureX, int textureY) {
        return mouseX >= textureX && mouseX <= textureX + TEXTURE_RENDER_WIDTH &&
                mouseY >= textureY && mouseY <= textureY + TEXTURE_RENDER_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = PADDING;
        int categoryY = PADDING;

        for (String category : categories) {
            categoryY += 10 + PADDING;

            int textureX = x;
            int textureY = categoryY;

            for (Capes.CapeTextures cape : Capes.CapeTextures.values()) {
                if (cape.name().startsWith(category)) {
                    if (isMouseOverTexture((int) mouseX, (int) mouseY, textureX, textureY)) {
                        CONFIG.capesTexture = cape;
                        saveConfig();
                        return true;
                    }

                    textureX += TEXTURE_RENDER_WIDTH + PADDING;

                    if (textureX + TEXTURE_RENDER_WIDTH > width - PADDING) {
                        textureX = x;
                        textureY += TEXTURE_RENDER_HEIGHT + PADDING;
                    }
                }
            }

            categoryY = textureY + TEXTURE_RENDER_HEIGHT;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void drawTexture(DrawContext context, Identifier texture, int x, int y) {
        context.drawTexture(RenderLayer::getGuiTextured, texture, x, y, 2, 2, TEXTURE_RENDER_WIDTH, TEXTURE_RENDER_HEIGHT, 128, 64);
    }
}
