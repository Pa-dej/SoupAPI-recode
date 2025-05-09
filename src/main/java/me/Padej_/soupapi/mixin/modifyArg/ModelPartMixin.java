package me.Padej_.soupapi.mixin.modifyArg;

import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.Map;

@Mixin(ModelPart.class)
public class ModelPartMixin {
    @Shadow @Final private Map<String, ModelPart> children;

    @ModifyArgs(
            method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;III)V"
            )
    )
    private void modifyRenderArgs(Args args) {
        ModelPart self = (ModelPart) (Object) this;

        // Проверяем, является ли this головой (value в Map равен this)
        for (Map.Entry<String, ModelPart> entry : children.entrySet()) {
            if ("head".equals(entry.getKey()) && entry.getValue() == self) {
                args.set(4, 0xFFFF0000); // красим только head
                return;
            }
        }

        // иначе — оставляем оригинальный цвет
    }
}


