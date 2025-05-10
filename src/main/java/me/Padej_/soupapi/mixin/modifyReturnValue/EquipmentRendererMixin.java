package me.Padej_.soupapi.mixin.modifyReturnValue;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.CaptureArmoredEntity;
import me.Padej_.soupapi.utils.ColorUtils;
import me.Padej_.soupapi.utils.EntityUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.equipment.EquipmentModel;
import net.minecraft.client.render.entity.equipment.EquipmentRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(EquipmentRenderer.class)
public class EquipmentRendererMixin {

    @ModifyReturnValue(
            method = "getDyeColor",
            at = @At("RETURN")
    )
    private static int modifyNetheriteArmorColor(int original, EquipmentModel.Layer layer, int dyeColor) {
        Entity entity = CaptureArmoredEntity.get();
        if ((entity instanceof PlayerEntity player && player == MinecraftClient.getInstance().player || EntityUtils.isFriend(entity)) && CONFIG.friendsHighlight) {
            int customColor = CONFIG.friendCustomColor;
//            int syncColor = ColorUtils.getMaxSaturationColor(TargetHudRenderer.bottomRight.getRGB());
            int syncColor = TargetHudRenderer.bottomRight.getRGB();
            return CONFIG.friendsHighlightSyncColor ? syncColor : customColor;
        }
        return original;
    }
}