package me.Padej_.soupapi.reduce;

import me.Padej_.soupapi.config.ConfigurableModule;

import java.util.Objects;

public class ServerReduce extends ConfigurableModule {

    public static boolean dontShowTargetHudItemsOverlay() {
        if (Objects.requireNonNull(mc.getNetworkHandler()).getServerInfo() == null) return true;
        String ip = mc.getNetworkHandler().getServerInfo().address.toLowerCase();
        return
                !ip.contains("minefun") &&
                        !ip.contains("mineblaze"); // test
    }
}
