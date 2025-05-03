package me.Padej_.soupapi.main.client;

import me.Padej_.soupapi.config.SoupAPI_Config;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.particle.CustomPhysicParticleFactory;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.HitSound;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class SoupAPI_Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::doEndClientTick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::doRenderAfterEntities);
        WorldRenderEvents.LAST.register(this::doRenderLast);

        registerClientSideParticles();

        registerOnHit();
        Translator.loadCache();
    }

    private void doEndClientTick(MinecraftClient client) {
        EntityUtils.updateEntities(client);

        Trails.onTick();
        JumpCircles.onTick();
        TargetHud.onTick();
        AmbientParticle.onTick();
        RPC.onTick();
        Translator.onTick();
        HitParticle.onTick();
        TotemPopParticles.onTick();
//        KillEffect.onTick();

        if (InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_ALT) && InputUtil.isKeyPressed(client.getWindow().getHandle(), GLFW.GLFW_KEY_BACKSPACE)) {
            client.setScreen(AutoConfig.getConfigScreen(SoupAPI_Config.class, null).get());
        }
    }

    private void doRenderAfterEntities(WorldRenderContext context) {
        TargetRender.renderTargetLegacy(context);
        JumpCircles.renderCircles(context);
        AmbientParticle.renderParticlesInWorld(context);
        HitParticle.render(context);
        TotemPopParticles.render(context);
    }

    private void doRenderLast(WorldRenderContext context) {
        TargetRender.renderTarget(context);
        HitBubbles.render(context);
        Halo.render(context);
        Trails.renderTrail(context);
    }

    private void registerOnHit() {
        HitBubbles.registerOnHit();
        HitSound.registerOnHit();
    }

    private void registerClientSideParticles() {
        ParticleFactoryRegistry factoryRegistry = ParticleFactoryRegistry.getInstance();
        factoryRegistry.register(SoupAPI_Main.STAR, CustomPhysicParticleFactory::new);
    }

}