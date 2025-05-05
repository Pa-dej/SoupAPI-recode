package me.Padej_.soupapi.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

public class TestRenderer extends Screen {

    private Identifier avatarTexture;

    public TestRenderer() {
        super(Text.of("test"));

        // Инициализация дефолтной текстуры
        loadImageFromUrl("https://raw.githubusercontent.com/github/explore/main/topics/minecraft/minecraft.png");

        long handle = MinecraftClient.getInstance().getWindow().getHandle();

        // Устанавливаем Drag & Drop callback
        GLFW.glfwSetDropCallback(handle, (window, count, namesPtr) -> {
            for (int i = 0; i < count; i++) {
                long ptr = MemoryUtil.memGetAddress(namesPtr + (long) i * Long.BYTES);
                String dropped = MemoryUtil.memUTF8(ptr);

                if (dropped.startsWith("http")) {
                    // URL из браузера
                    loadImageFromUrl(dropped.trim());
                    break;
                }

                // Файл с диска
                try {
                    File file = new File(dropped);
                    if (file.exists()) {
                        BufferedImage img = ImageIO.read(file);
                        if (img != null) {
                            loadImageFromBufferedImage(img);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        // Устанавливаем callback для обработки клавиш
        GLFW.glfwSetKeyCallback(handle, (window, key, scancode, action, mods) -> {
            if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_V && (mods & GLFW.GLFW_MOD_CONTROL) != 0) {
                try {
                    if (!GraphicsEnvironment.isHeadless()) {
                        // Пытаемся получить изображение из буфера обмена
                        Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
                        if (transferable != null) {
                            if (transferable.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                                BufferedImage image = (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
                                if (image != null) {
                                    loadImageFromBufferedImage(image);
                                    return;
                                }
                            } else if (transferable.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                                // Проверяем, есть ли URL в текстовом формате
                                String text = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                                if (text != null && text.startsWith("http")) {
                                    loadImageFromUrl(text.trim());
                                    return;
                                }
                            }
                        }
                    }

                    // В headless-режиме или если изображение/URL не найдены, пробуем получить текст через GLFW
                    String clipboardContent = GLFW.glfwGetClipboardString(window);
                    if (clipboardContent != null && clipboardContent.startsWith("http")) {
                        loadImageFromUrl(clipboardContent.trim());
                    }
                } catch (Exception e) {
                    // Игнорируем ошибки, связанные с бинарными данными или другими проблемами
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadImageFromUrl(String urlStr) {
        new Thread(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                InputStream inputStream = connection.getInputStream();

                NativeImage image = NativeImage.read(inputStream);
                NativeImageBackedTexture texture = new NativeImageBackedTexture(image);
                Identifier id = Identifier.of("soupapi", "avatar_" + UUID.randomUUID());
                MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
                this.avatarTexture = id;

                inputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadImageFromBufferedImage(BufferedImage bufferedImage) {
        try {
            NativeImage nativeImage = new NativeImage(bufferedImage.getWidth(), bufferedImage.getHeight(), true);
            for (int x = 0; x < bufferedImage.getWidth(); x++) {
                for (int y = 0; y < bufferedImage.getHeight(); y++) {
                    int argb = bufferedImage.getRGB(x, y);
                    nativeImage.setColorArgb(x, y, argb);
                }
            }
            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);
            Identifier id = Identifier.of("soupapi", "avatar_" + UUID.randomUUID());
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, texture);
            this.avatarTexture = id;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (avatarTexture != null) {
            RenderSystem.setShaderTexture(0, avatarTexture);
            context.drawTexture(RenderLayer::getGuiTextured, avatarTexture, 20, 20, 0, 0, 128, 128, 128, 128, 128, 128);
        }
    }
}

