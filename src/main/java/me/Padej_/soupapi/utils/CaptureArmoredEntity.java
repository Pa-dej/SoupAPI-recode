package me.Padej_.soupapi.utils;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

public class CaptureArmoredEntity {

    public static final CaptureArmoredEntity INSTANCE = new CaptureArmoredEntity();

    private static final ThreadLocal<Entity> entity = new ThreadLocal<>();

    public void setEntity(@Nullable Entity e) {
        entity.set(e);
    }

    public void clear() {
        entity.remove();
    }

    @Nullable
    public static Entity get() {
        return entity.get();
    }
}



