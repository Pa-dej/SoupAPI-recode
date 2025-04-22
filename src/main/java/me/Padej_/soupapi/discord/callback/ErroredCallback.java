package me.Padej_.soupapi.discord.callback;

import com.sun.jna.Callback;

public interface ErroredCallback extends Callback {
    void apply(final int p0, final String p1);
}
