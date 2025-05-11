package me.Padej_.soupapi.interfaces;

import java.util.ArrayList;
import java.util.List;

public interface OverlayReloadListener {
    List<OverlayReloadListener> listeners = new ArrayList<>();
    void soupAPI$onOverlayReload();

    static void register(OverlayReloadListener listener) {
        listeners.add(listener);
    }

    static void callEvent() {
        List<OverlayReloadListener> copy = new ArrayList<>(listeners);
        for (OverlayReloadListener listener : copy) {
            listener.soupAPI$onOverlayReload();
        }
    }
}
