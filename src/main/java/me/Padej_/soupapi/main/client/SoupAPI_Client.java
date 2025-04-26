package me.Padej_.soupapi.main.client;

import me.Padej_.soupapi.config.SoupAPI_Config;
import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.utils.EntityUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SoupAPI_Client implements ClientModInitializer {

    private static final Identifier TARGET_HUD_LAYER = Identifier.of("soupapi", "target_hud_layer");

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::doEndClientTick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::doRenderAfterEntities);
        WorldRenderEvents.LAST.register(this::doRenderLast);

        // Использовать, когда старый вариант удалят или перестанут поддерживать окончательно
//        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.MISC_OVERLAYS, TARGET_HUD_LAYER, TargetHud::render));

        // For stupid lunar legacy code -_-.
        // Для гребаного лунара, который использует старые методы...
//        HudRenderCallback.EVENT.register(TargetHud::render);

        registerOnHit();
    }

    private void doEndClientTick(MinecraftClient client) {
        EntityUtils.updateEntities(client);

        Trails.onTick();
        JumpCircles.onTick();
        TargetHud.onTick();
        AmbientParticle.onTick();
        RPC.onTick();

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) && InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_BACKSPACE)) {
            client.setScreen(AutoConfig.getConfigScreen(SoupAPI_Config.class, null).get());
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
        HitBubbles.render(context);
        Halo.render(context);
    }

    private void registerOnHit() {
        HitBubbles.registerOnHit();
    }

}