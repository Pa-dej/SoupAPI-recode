package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.screen.CapeSelectScreen;
import me.Padej_.soupapi.screen.ConfigHudPositionsScreen;
import me.Padej_.soupapi.screen.SwingHandScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin extends PressableWidget {

    public ButtonWidgetMixin(int x, int y, int width, int height, Text message) {
        super(x, y, width, height, message);
    }

    @Inject(method = "onPress", at = @At("HEAD"), cancellable = true)
    private void onPress(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        switch (this.getMessage().getString()) {
            case "CONFIG_POS" -> {
                mc.setScreen(new ConfigHudPositionsScreen());
                ci.cancel();
            }
            case "CONFIG_HANDS" -> {
                mc.setScreen(new SwingHandScreen());
                ci.cancel();
            }
            case "SELECT_CAPE" -> {
                mc.setScreen(new CapeSelectScreen());
                ci.cancel();
            }
            case "UPDATE_CAPE" -> {
                if (CONFIG.customCapesEnabled && CONFIG.customCapesLink != null) {
                    Identifier textureId = Identifier.of("soupapi", "custom_cape_" + CONFIG.customCapesLink.hashCode());
                    if (MinecraftClient.getInstance().getTextureManager().getTexture(textureId) == null) {
                        updateCapeFromUrl(CONFIG.customCapesLink, textureId);
                    }
                }
                ci.cancel();
            }
        }
    }

    @Unique
    private void updateCapeFromUrl(String url, Identifier textureId) {
        if (url == null || url.isEmpty()) return;

        new Thread(() -> {
            try {
                URL capeUrl = new URL(url);
                BufferedImage image = ImageIO.read(capeUrl);

                if (image == null) {
                    System.err.println("[Cape] Не удалось прочитать изображение по ссылке: " + url);
                    return;
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "PNG", baos);
                baos.flush();
                byte[] data = baos.toByteArray();
                baos.close();

                NativeImage nativeImage;
                try {
                    nativeImage = NativeImage.read(new ByteArrayInputStream(data));
                } catch (Exception e) {
                    System.err.println("[Cape] Ошибка при чтении NativeImage:");
                    e.printStackTrace();
                    return;
                }

                if (nativeImage == null) {
                    System.err.println("[Cape] NativeImage оказался null");
                    return;
                }

                NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);

                MinecraftClient client = MinecraftClient.getInstance();
                client.execute(() -> {
                    client.getTextureManager().registerTexture(textureId, texture);
                    System.out.println("[Cape] Кастомный плащ загружен и зарегистрирован: " + textureId);
                });

            } catch (IOException e) {
                System.err.println("[Cape] Ошибка при загрузке кастомного плаща по ссылке: " + url);
                e.printStackTrace();
            }
        }, "CustomCapeLoader").start();
    }


}
