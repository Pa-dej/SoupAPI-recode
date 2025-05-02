package me.Padej_.soupapi.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.util.TriState;
import net.minecraft.util.Util;

import java.util.function.Function;

import static net.minecraft.client.render.RenderPhase.*;
import static net.minecraft.client.render.RenderPhase.ENABLE_LIGHTMAP;
import static net.minecraft.client.render.VertexFormats.POSITION_TEXTURE_COLOR_LIGHT;

public class CustomRenderLayers {
    public static final Function<Double, RenderLayer.MultiPhase> CHINA_HAT_LAYER = Util.memoize((lineWidth) -> RenderLayer.of("soupapi:china_hat_layer",
                    VertexFormats.POSITION_COLOR,
                    VertexFormat.DrawMode.TRIANGLE_STRIP,
                    1536,
                    RenderLayer.MultiPhaseParameters.builder()
                            .program(POSITION_COLOR_PROGRAM)
                            .transparency(RenderPhase.TRANSLUCENT_TRANSPARENCY)
                            .cull(DISABLE_CULLING)
                            .build(false)
            )
    );
    public static final Function<Identifier, RenderLayer> QUAD_IN_BLOCKS = Util.memoize((texture) -> RenderLayer.of("quad",
                    POSITION_TEXTURE_COLOR_LIGHT,
                    VertexFormat.DrawMode.QUADS,
                    1536,
                    true,
                    true,
                    RenderLayer.MultiPhaseParameters.builder()
                            .depthTest(ALWAYS_DEPTH_TEST)
                            .program(POSITION_TEXTURE_COLOR_PROGRAM)
                            .texture(new Texture(texture, TriState.FALSE, false))
                            .cull(DISABLE_CULLING)
                            .transparency(TRANSLUCENT_TRANSPARENCY)
                            .lightmap(ENABLE_LIGHTMAP)
                            .build(true)
            )
    );
    public static RenderLayer getCustomEnergySwirl(Identifier texture, float x, float y) {
        return RenderLayer.of("custom_energy_swirl",
                VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL,
                VertexFormat.DrawMode.QUADS,
                1536,
                true,
                true,
                RenderLayer.MultiPhaseParameters.builder()
                        .program(ENERGY_SWIRL_PROGRAM) // Здесь можно установить свой шейдер
                        .texture(new RenderPhase.Texture(texture, TriState.FALSE, false))
                        .texturing(new RenderPhase.OffsetTexturing(x, y))
                        .transparency(ADDITIVE_TRANSPARENCY)
                        .lightmap(ENABLE_LIGHTMAP)
                        .overlay(ENABLE_OVERLAY_COLOR)
                        .build(false)
        );
    }
}