package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public abstract class CustomEnergySwirlOverlayFeatureRenderer<S extends EntityRenderState, M extends EntityModel<S>> extends FeatureRenderer<S, M> {
    public CustomEnergySwirlOverlayFeatureRenderer(FeatureRendererContext<S, M> featureRendererContext) {
        super(featureRendererContext);
    }

    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, S state, float limbAngle, float limbDistance) {
        if (this.shouldRender(state)) {
            float f = state.age;
            M entityModel = this.getEnergySwirlModel();
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(CustomRenderLayers.getCustomEnergySwirl(this.getEnergySwirlTexture(),
                    this.getEnergySwirlX(f) % 1.0F,
                    f * 0.01F % 1.0F));
            entityModel.setAngles(state);
            entityModel.render(matrices, vertexConsumer, light, OverlayTexture.DEFAULT_UV, 0xFF00FF00);
        }
    }

    protected abstract boolean shouldRender(S state);

    protected abstract float getEnergySwirlX(float partialAge);

    protected abstract Identifier getEnergySwirlTexture();

    protected abstract M getEnergySwirlModel();
}
