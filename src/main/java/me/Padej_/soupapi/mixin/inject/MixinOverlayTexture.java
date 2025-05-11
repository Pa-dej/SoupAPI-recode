package me.Padej_.soupapi.mixin.inject;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.interfaces.OverlayReloadListener;
import me.Padej_.soupapi.render.TargetHudRenderer;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(OverlayTexture.class)
public abstract class MixinOverlayTexture implements OverlayReloadListener {
    @Shadow
    @Final
    private NativeImageBackedTexture texture;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void modifyHitColor(CallbackInfo ci) {
        this.reloadOverlay();
        OverlayReloadListener.register(this);
    }

    public void soupAPI$onOverlayReload() {
        this.reloadOverlay();
    }

    @Unique
    private static int getColorInt(int red, int green, int blue, int alpha) {
        alpha = 255 - alpha;
        return (alpha << 24) + (blue << 16) + (green << 8) + red;
    }

    @Unique
    public void reloadOverlay() {
        NativeImage nativeImage = this.texture.getImage();
        if (nativeImage == null) return;

        Color color = !CONFIG.hitColorCustomColor ? TargetHudRenderer.bottomLeft : new Color(CONFIG.hitColorColor);
        int alpha = (int) (255 * (CONFIG.hitColorAlpha / 100f));

        for (int y = 0; y < 16; ++y) {
            for (int x = 0; x < 16; ++x) {
                if (y < 8) {
                    if (CONFIG.hitColorEnabled) {
                        nativeImage.setColorArgb(x, y, getColorInt(color.getRed(), color.getGreen(), color.getBlue(), alpha));
                    } else {
                        nativeImage.setColorArgb(x, y, -1291911168);
                    }
                }
            }
        }

        this.texture.upload();
    }

}
