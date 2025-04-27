package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.mixin.access.LivingEntityRendererAccessor;
import me.Padej_.soupapi.render.ChinaHatPlayerRenderFeature;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin {

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(EntityRendererFactory.Context ctx, boolean slim, CallbackInfo ci) {
        PlayerEntityRenderer renderer = (PlayerEntityRenderer) (Object) this;

        // Получаем список фич через аксессор
        HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> headFeatureRenderer = getHeadFeatureRenderer((LivingEntityRendererAccessor) renderer);

        // Создаём ChinaHatPlayerRenderFeature
        ChinaHatPlayerRenderFeature chinaHatPlayerRenderFeature = new ChinaHatPlayerRenderFeature(
                renderer, // renderer реализует FeatureRendererContext
                headFeatureRenderer
        );

        // Добавляем фичу
        ((LivingEntityRendererAccessor) renderer).callAddFeature(chinaHatPlayerRenderFeature);
    }

    @Unique
    private @NotNull HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> getHeadFeatureRenderer(LivingEntityRendererAccessor renderer) {
        List<FeatureRenderer<?, ?>> features = renderer.getFeatures();

        // Находим HeadFeatureRenderer
        HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> headFeatureRenderer = null;
        for (FeatureRenderer<?, ?> feature : features) {
            if (feature instanceof HeadFeatureRenderer) {
                headFeatureRenderer = (HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel>) feature;
                break;
            }
        }

        if (headFeatureRenderer == null) {
            throw new IllegalStateException("HeadFeatureRenderer not found in PlayerEntityRenderer features");
        }
        return headFeatureRenderer;
    }
}
