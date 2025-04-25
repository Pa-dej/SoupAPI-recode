package me.Padej_.soupapi.render;

import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.util.Identifier;

public class ChargedPlayerFeatureRenderer extends CustomEnergySwirlOverlayFeatureRenderer<PlayerEntityRenderState, PlayerEntityModel> {
    private static final Identifier SKIN = Identifier.of("soupapi", "textures/i.png");
    private final PlayerEntityModel model;

    public ChargedPlayerFeatureRenderer(FeatureRendererContext<PlayerEntityRenderState, PlayerEntityModel> context, PlayerEntityModel model) {
        super(context);
        this.model = model;
    }

    @Override
    protected boolean shouldRender(PlayerEntityRenderState state) {
        return true;
    }

    @Override
    protected float getEnergySwirlX(float partialAge) {
        return partialAge * 0.01f;
    }

    @Override
    protected Identifier getEnergySwirlTexture() {
        return SKIN;
    }

    @Override
    protected PlayerEntityModel getEnergySwirlModel() {
        return model;
    }
}


