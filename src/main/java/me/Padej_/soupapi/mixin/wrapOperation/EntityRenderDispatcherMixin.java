package me.Padej_.soupapi.mixin.wrapOperation;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import me.Padej_.soupapi.utils.CaptureArmoredEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @WrapOperation(
            method = "render(Lnet/minecraft/entity/Entity;DDDFLnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/EntityRenderer;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/entity/EntityRenderer;render(Lnet/minecraft/client/render/entity/state/EntityRenderState;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;I)V"
            )
    )
    private void wrapEntityRenderCall(
            EntityRenderer instance,
            EntityRenderState entityRenderState,
            MatrixStack matrixStack,
            VertexConsumerProvider provider,
            int i,
            Operation<Void> original,
            @Local(argsOnly = true) Entity entity
    ) {
        CaptureArmoredEntity.INSTANCE.setEntity(entity);
        original.call(instance, entityRenderState, matrixStack, provider, i);
        CaptureArmoredEntity.INSTANCE.clear();
    }
}




