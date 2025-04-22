package me.Padej_.soupapi.discord.callback;

import com.sun.jna.Callback;
import me.Padej_.soupapi.discord.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(final DiscordUser p0);
}
