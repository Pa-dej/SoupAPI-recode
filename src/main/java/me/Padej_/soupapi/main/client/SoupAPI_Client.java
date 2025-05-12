package me.Padej_.soupapi.main.client;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.config.SoupAPI_Config;
import me.Padej_.soupapi.gui.SoupSettingsScreen;
import me.Padej_.soupapi.interfaces.OverlayReloadListener;
import me.Padej_.soupapi.main.SoupAPI_Main;
import me.Padej_.soupapi.modules.*;
import me.Padej_.soupapi.particle.CustomPhysicParticleFactory;
import me.Padej_.soupapi.render.Render3D_Shapes;
import me.Padej_.soupapi.render.WatermarkRenderer;
import me.Padej_.soupapi.utils.EntityUtils;
import me.Padej_.soupapi.utils.HitSound;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudLayerRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class SoupAPI_Client implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(this::doEndClientTick);
        WorldRenderEvents.AFTER_ENTITIES.register(this::doRenderAfterEntities);
        WorldRenderEvents.LAST.register(this::doRenderLast);
        HudLayerRegistrationCallback.EVENT.register(layeredDrawer -> layeredDrawer.attachLayerBefore(IdentifiedLayer.STATUS_EFFECTS, Identifier.of("soupapi", "hud"), this::renderHud));

        registerClientSideParticles();

        registerOnHit();
        Translator.loadCache();
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        TargetHud.render(context, tickCounter);
        WatermarkRenderer.render(context);
        MouseMove.render(context);
    }

    private void doEndClientTick(MinecraftClient client) {
        EntityUtils.updateEntities(client);
        OverlayReloadListener.callEvent();

        Trails.onTick();
        JumpCircles.onTick();
        TargetHud.onTick();
        RPC.onTick();
        Translator.onTick();

        HitParticle.onTick();
        TotemPopParticles.onTick();
        JumpParticles.onTick();
        AmbientParticle.onTick();

//        KillEffect.onTick();

        long handle = client.getWindow().getHandle();
        Screen parent = null;
        Screen currentScreen = client.currentScreen;
        if (currentScreen instanceof ChatScreen) return;
        if (InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_LEFT_ALT) && InputUtil.isKeyPressed(handle, GLFW.GLFW_KEY_BACKSPACE)) {
            if (currentScreen != null && !currentScreen.getTitle().equals(Text.translatable("text.autoconfig.soupapi.title"))) {
                parent = currentScreen;
            }
            client.setScreen(AutoConfig.getConfigScreen(SoupAPI_Config.class, parent).get());
        }
        if (client.options.sprintKey.isPressed()){
//            client.setScreen(new TestRenderer());
//            client.setScreen(new SoupSettingsScreen());
        }
    }

    private void doRenderAfterEntities(WorldRenderContext context) {

    }

    private void doRenderLast(WorldRenderContext context) {
        TargetRender.renderTarget(context);
        HitBubbles.render(context);
        Halo.render(context);
        Trails.renderTrail(context);
        JumpCircles.renderCircles(context);
        Trajectories.render(context);

        renderParticles(context);
        Render3D_Shapes.render(context);
    }

    private void registerOnHit() {
        HitBubbles.registerOnHit();
        HitSound.registerOnHit();
        HitParticle.registerOnHit();
    }

    private void registerClientSideParticles() {
        ParticleFactoryRegistry factoryRegistry = ParticleFactoryRegistry.getInstance();
        factoryRegistry.register(SoupAPI_Main.STAR, CustomPhysicParticleFactory::new);
    }

    private void renderParticles(WorldRenderContext context) {
        JumpParticles.render(context);
        HitParticle.render(context);
        TotemPopParticles.render(context);
        AmbientParticle.render(context);
    }

}