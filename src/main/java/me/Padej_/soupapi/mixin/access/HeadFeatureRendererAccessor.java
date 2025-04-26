package me.Padej_.soupapi.mixin.access;

import net.minecraft.block.SkullBlock;
import net.minecraft.client.render.block.entity.SkullBlockEntityModel;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.function.Function;

@Mixin(HeadFeatureRenderer.class)
public interface HeadFeatureRendererAccessor {
    @Accessor("headTransformation")
    HeadFeatureRenderer.HeadTransformation getHeadTransformation();

    @Accessor("headModels")
    Function<SkullBlock.SkullType, SkullBlockEntityModel> getHeadModels();
}

