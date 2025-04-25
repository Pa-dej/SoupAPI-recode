package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.render.ChargedPlayerFeatureRenderer;
import net.fabricmc.fabric.mixin.client.rendering.LivingEntityRendererAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        ModelPart modelPart = ctx.getPart(EntityModelLayers.PLAYER);
        PlayerEntityModel playerModel = new PlayerEntityModel(modelPart, slim);

        ChargedPlayerFeatureRenderer featureRenderer = new ChargedPlayerFeatureRenderer(
                (FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel>) this,
                playerModel
        );

        ((LivingEntityRendererAccessor) this).callAddFeature(featureRenderer);
    }
}
