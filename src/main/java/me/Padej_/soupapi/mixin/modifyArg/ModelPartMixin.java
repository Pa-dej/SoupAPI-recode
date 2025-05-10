package me.Padej_.soupapi.mixin.modifyArg;

import me.Padej_.soupapi.render.TargetHudRenderer;
import me.Padej_.soupapi.utils.CaptureArmoredEntity;
import me.Padej_.soupapi.utils.ColorUtils;
import me.Padej_.soupapi.utils.EntityUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(ModelPart.class)
public class ModelPartMixin {

    @ModifyArgs(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;III)V"
            )
    )
    private void modifyRenderArgs(Args args) {
        if (CONFIG.friendsHighlightOnlyArmor == true) return;
        Entity entity = CaptureArmoredEntity.get();
        if ((entity instanceof PlayerEntity player && player == MinecraftClient.getInstance().player || EntityUtils.isFriend(entity)) && CONFIG.friendsHighlight) {
            int customColor = CONFIG.friendCustomColor;
            int syncColor = ColorUtils.getMaxSaturationColor(TargetHudRenderer.bottomRight.getRGB());
            args.set(4, CONFIG.friendsHighlightSyncColor ? syncColor : customColor);
        }
    }
}


