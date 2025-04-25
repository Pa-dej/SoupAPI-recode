package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.discord.DiscordEventHandlers;
import me.Padej_.soupapi.discord.DiscordRPC;
import me.Padej_.soupapi.discord.DiscordRichPresence;
import me.Padej_.soupapi.utils.MC_Tiers;
import org.lwjgl.glfw.GLFW;

public class RPC extends ConfigurableModule {
    private static boolean initialized = false;
    private static final String DEFAULT_ICON = "icon";
    private static long lastUpdate = 0;
    private static String lastState = "";
    private static final long UPDATE_INTERVAL = 5000L; // 5 секунд
    private static DiscordRichPresence presence;

    public static void init() {
        long startTimestamp = System.currentTimeMillis() / 1000;

        DiscordEventHandlers handlers = new DiscordEventHandlers();
        DiscordRPC.INSTANCE.Discord_Initialize("1363751101794619463", handlers, true, "");

        presence = new DiscordRichPresence();
        presence.state = getState();
        lastState = presence.state;

        presence.startTimestamp = startTimestamp;
        presence.largeImageKey = CONFIG.rpcMctiersEnabled ? MC_Tiers.getMcTiersGameModeIcon() : DEFAULT_ICON;
        presence.largeImageText = mc.getVersionType() + " " + mc.getGameVersion();
        presence.instance = 1;

        presence.button_label_1 = "View On Modrinth";
        presence.button_url_1 = "https://modrinth.com/mod/soup-api";

        DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);

        initialized = true;
    }

    public static void onTick() {
        boolean enabled = CONFIG.rpcEnabled;

        if (enabled && !initialized) {
            init();
        }

        if (!enabled && initialized) {
            shutdown();
        }

        if (initialized) {
            DiscordRPC.INSTANCE.Discord_RunCallbacks();

            long currentTime = System.currentTimeMillis();
            if (currentTime - lastUpdate >= UPDATE_INTERVAL) {
                String newState = getState();
                String newLargeImageKey = CONFIG.rpcMctiersEnabled ? MC_Tiers.getMcTiersGameModeIcon() : DEFAULT_ICON;
                String newLargeImageText = CONFIG.rpcMctiersEnabled ? CONFIG.mctiersGameMode.name() : mc.getGameVersion();

                boolean shouldUpdate = false;

                if (!newState.equals(lastState)) {
                    lastState = newState;
                    presence.state = newState;
                    shouldUpdate = true;
                }

                if (!newLargeImageKey.equals(presence.largeImageKey)) {
                    presence.largeImageKey = newLargeImageKey;
                    shouldUpdate = true;
                }

                if (!newLargeImageText.equals(presence.largeImageText)) {
                    presence.largeImageText = newLargeImageText;
                    shouldUpdate = true;
                }

                if (shouldUpdate) {
                    DiscordRPC.INSTANCE.Discord_UpdatePresence(presence);
                }

                lastUpdate = currentTime;
            }
        }
    }

    public static void shutdown() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
        initialized = false;
        presence = null;
    }

    private static String getState() {
        return switch (CONFIG.rpcState) {
            case NAME -> {
                if (mc.getGameProfile() != null && mc.getGameProfile().getName() != null) {
                    yield mc.getGameProfile().getName();
                } else {
                    yield "Bruh -_-. Name is Null";
                }
            }
            case IP -> {
                if (mc.getNetworkHandler() != null && mc.getNetworkHandler().getServerInfo() != null
                        && mc.getNetworkHandler().getServerInfo().address != null) {
                    yield mc.getNetworkHandler().getServerInfo().address.toLowerCase();
                } else {
                    yield "Offline";
                }
            }
            case CUSTOM -> CONFIG.rpcCustomStateText != null ? CONFIG.rpcCustomStateText : "No state";
        };
    }

    public enum State {
        NAME, IP, CUSTOM
    }
}


