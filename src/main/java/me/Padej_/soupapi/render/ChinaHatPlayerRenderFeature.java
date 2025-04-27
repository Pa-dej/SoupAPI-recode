package me.Padej_.soupapi.render;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class ChinaHatPlayerRenderFeature extends ChinaHatFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    public ChinaHatPlayerRenderFeature(
            FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> featureRendererContext,
            HeadFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> headFeatureRenderer
    ) {
        super(featureRendererContext, headFeatureRenderer);
    }

    @Override
    protected boolean shouldRender(PlayerEntityRenderState state) {
        return CONFIG.chinaHatEnabled;
    }
}
