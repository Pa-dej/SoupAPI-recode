package me.Padej_.soupapi.main.client;

import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.screen.ConfigScreen;
import me.Padej_.soupapi.utils.EntityUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class SoupAPI_Client implements ClientModInitializer {

    private static final Identifier TARGET_HUD_LAYER = Identifier.of("soupapi", "target_hud_layer");

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::doEndClientTick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::doRenderAfterEntities);
        WorldRenderEvents.LAST.register(this::doRenderLast);

        AttackEntityCallback.EVENT.register(EntityUtils::onHitEntity);

        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, TARGET_HUD_LAYER, TargetHud::render));
    }

    private void doEndClientTick(MinecraftClient client) {
        EntityUtils.updateEntities(client);

        Trails.onTick();
        JumpCircles.onTick();
        TargetHud.onTick();
        AmbientParticle.onTick();
        TargetRender.onTick();

        if (MinecraftClient.getInstance().options.sprintKey.isPressed()) {
            MinecraftClient.getInstance().setScreen(new ConfigScreen());
        }
    }

    private void doRenderAfterEntities(WorldRenderContext context) {
        Trails.renderTrail(context);
        JumpCircles.renderCircles(context);
        AmbientParticle.renderParticlesInWorld(context);
    }

    private void doRenderLast(WorldRenderContext context) {
        TargetRender.renderTarget(context);
        TargetRender.renderTargetLegacy(context);
    }

}