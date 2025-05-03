package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.mixin.access.HeadFeatureRendererAccessor;
import me.Padej_.soupapi.utils.EntityUtils;
import net.minecraft.block.SkullBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.block.entity.SkullBlockEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Function;

public abstract class ChinaHatFeatureRenderer<S extends LivingEntityRenderState, M extends EntityModel<S> & ModelWithHead> extends FeatureRenderer<S, M> {
    private final HeadFeatureRenderer<S, M> headFeatureRenderer; // Поле для HeadFeatureRenderer

    public ChinaHatFeatureRenderer(FeatureRendererContext<S, M> context, HeadFeatureRenderer<S, M> headFeatureRenderer) {
        super(context);
        this.headFeatureRenderer = headFeatureRenderer;
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumerProvider, int light, S state, float limbAngle, float limbDistance) {
        if (!shouldRender(state)) return;
        if (!state.headItemRenderState.isEmpty() || state.wearingSkullType != null) {
            HeadFeatureRenderer.HeadTransformation headTransformation = ((HeadFeatureRendererAccessor) headFeatureRenderer).getHeadTransformation();
            Function<SkullBlock.SkullType, SkullBlockEntityModel> headModels = ((HeadFeatureRendererAccessor) headFeatureRenderer).getHeadModels();

            matrices.push();
            matrices.scale(headTransformation.horizontalScale(), 1.0F, headTransformation.horizontalScale());
            M entityModel = this.getContextModel();
            entityModel.getRootPart().rotate(matrices);
            entityModel.getHead().rotate(matrices);
            if (state.wearingSkullType != null) {
                matrices.translate(0.0F, headTransformation.skullYOffset(), 0.0F);
                matrices.scale(1.1875F, -1.1875F, -1.1875F);
                matrices.translate(-0.5, 0.0, -0.5);
                SkullBlock.SkullType skullType = state.wearingSkullType;
                SkullBlockEntityModel skullBlockEntityModel = headModels.apply(skullType);
                RenderLayer renderLayer = SkullBlockEntityRenderer.getRenderLayer(skullType, state.wearingSkullProfile);
                SkullBlockEntityRenderer.renderSkull(null, 180.0F, state.headItemAnimationProgress, matrices, vertexConsumerProvider, light, skullBlockEntityModel, renderLayer);
            } else {
                HeadFeatureRenderer.translate(matrices, headTransformation);
                state.headItemRenderState.render(matrices, vertexConsumerProvider, light, OverlayTexture.DEFAULT_UV);
            }

            matrices.pop();
        }

        if (!SoupAPI_Main.configHolder.get().chinaHatEnabled) return;

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;
        if (state instanceof PlayerEntityRenderState playerState) {
            // Проверяем, является ли это локальным игроком или другом
            String localPlayerName = client.player.getName().getString();
            String stateName = playerState.name;
            boolean isLocalPlayer = stateName.equals(localPlayerName);
            boolean isFriend = EntityUtils.isFriend(stateName);

            if (!isLocalPlayer && !isFriend) {
                return;
            }

            VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(CustomRenderLayers.CHINA_HAT_LAYER.apply(1.0));

            matrices.push();

            M entityModel = this.getContextModel();
            entityModel.getRootPart().rotate(matrices);
            entityModel.getHead().rotate(matrices);

            matrices.translate(0.0F, -0.76f, 0.0F); // Поднимаем чуть выше головы
            RenderSystem.enableDepthTest();
            RenderSystem.enableBlend();
            Render3D.renderChinaHat(matrices, vertexConsumer);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();

            matrices.pop();
        }
    }

    protected abstract boolean shouldRender(S state);
}
