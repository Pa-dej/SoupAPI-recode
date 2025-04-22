package me.Padej_.soupapi.discord;

import com.sun.jna.Native;
import com.sun.jna.Library;

public interface DiscordRPC extends Library {
    DiscordRPC INSTANCE = Native.load("discord-rpc", DiscordRPC.class);
    void Discord_Initialize(String applicationId, DiscordEventHandlers handlers, boolean autoRegister, String optionalSteamId);
    void Discord_Shutdown();
    void Discord_RunCallbacks();
    void Discord_UpdatePresence(DiscordRichPresence presence);
}
