package me.Padej_.soupapi.mixin.modifyReturnValue;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import me.Padej_.soupapi.utils.EntityUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(value = AbstractClientPlayerEntity.class, priority = 2000)
public abstract class CapeTextureChanger extends PlayerEntity {

    @Shadow
    private @Nullable PlayerListEntry playerListEntry;
    @Unique
    private static final Map<String, Boolean> loadedCapes = new ConcurrentHashMap<>();

    public CapeTextureChanger(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @ModifyReturnValue(method = "getSkinTextures", at = @At("TAIL"))
    protected SkinTextures useCapeIfPresent(SkinTextures original) {
        Identifier cape = getDirCape();
        String currentUsername = MinecraftClient.getInstance().getSession().getUsername();
        String playerName = playerListEntry != null ? playerListEntry.getProfile().getName() : null;

        // Если капа нет, возвращаем оригинальные текстуры
        if (cape == null) {
            return original;
        }

        // Проверка, является ли игрок другом
        if (playerName != null && EntityUtils.isFriend(playerName)) {
            return new SkinTextures(
                    original.texture(),
                    null,
                    cape, // Используем плащ для друзей
                    original.elytraTexture(),
                    original.model(),
                    true
            );
        }

        // Используем плащ для текущего игрока
        return new SkinTextures(
                original.texture(),
                null,
                playerName != null && playerName.equals(currentUsername) ? cape : original.capeTexture(),
                original.elytraTexture(),
                original.model(),
                true
        );
    }

    @Unique
    private static Identifier getDirCape() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (CONFIG.customCapesEnabled && CONFIG.customCapesLink != null && !CONFIG.customCapesLink.isEmpty()) {
            String url = CONFIG.customCapesLink;
            String idString = "custom_cape_" + url.hashCode();
            Identifier textureId = Identifier.of("soupapi", idString);

            // Не возвращаем, если кап не загружен
            if (!Boolean.TRUE.equals(loadedCapes.get(url))) {
                if (!loadedCapes.containsKey(url)) {
                    // Асинхронная загрузка, только один раз
                    loadedCapes.put(url, false); // ставим флаг, что начали загрузку

                    new Thread(() -> {
                        try {
                            BufferedImage image = ImageIO.read(new URL(url));
                            if (image == null) {
                                System.err.println("ImageIO.read вернул null для URL: " + url);
                                return;
                            }

                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            ImageIO.write(image, "PNG", baos);
                            byte[] data = baos.toByteArray();

                            NativeImage nativeImage = NativeImage.read(new ByteArrayInputStream(data));
                            if (nativeImage == null) {
                                System.err.println("NativeImage оказался null при чтении из байтов");
                                return;
                            }

                            NativeImageBackedTexture texture = new NativeImageBackedTexture(nativeImage);

                            client.execute(() -> {
                                client.getTextureManager().registerTexture(textureId, texture);
                                loadedCapes.put(url, true); // загружено
                                System.out.println("Плащ загружен: " + textureId);
                            });

                        } catch (Exception e) {
                            System.err.println("Ошибка при загрузке кастомного плаща:");
                            e.printStackTrace();
                        }
                    }, "CapeLoader").start();
                }

                return null; // пока не загружено — не использовать
            }

            return textureId; // всё загружено
        }

        // Фоллбэк: обычный плащ
        Identifier cape = CONFIG.capesTexture.getTexturePath();
        if (CONFIG.capesEnabled && client.getResourceManager().getResource(cape).isPresent()) {
            return cape;
        }

        return null;
    }


}
