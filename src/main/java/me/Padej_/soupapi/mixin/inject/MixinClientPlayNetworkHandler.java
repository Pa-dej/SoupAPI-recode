package me.Padej_.soupapi.mixin.inject;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler {

    @Inject(method = "getServerInfo", at = @At("HEAD"), cancellable = true)
    private void injectFakeServerInfo(CallbackInfoReturnable<ServerInfo> cir) {
        if (MinecraftClient.getInstance().isInSingleplayer()) {
            ServerInfo fakeInfo = new ServerInfo("Singleplayer", "localhost", ServerInfo.ServerType.LAN);
            cir.setReturnValue(fakeInfo);
        }
    }
}

