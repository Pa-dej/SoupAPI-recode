package me.Padej_.soupapi.mixin.access;

import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(LivingEntityRenderer.class)
public interface LivingEntityRendererAccessor {
    @Accessor("features")
    List<FeatureRenderer<?, ?>> getFeatures();

    @Invoker("addFeature")
    boolean callAddFeature(FeatureRenderer<?, ?> feature);
}
