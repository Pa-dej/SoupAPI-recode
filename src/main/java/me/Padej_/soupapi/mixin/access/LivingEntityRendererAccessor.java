package me.Padej_.soupapi.mixin.access;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @Invoker("addFeature")
    boolean callAddFeature(FeatureRenderer<?, ?> feature);

    @Invoker("getFeatures")
    List<FeatureRenderer<?, ?>> getFeatures();
}
