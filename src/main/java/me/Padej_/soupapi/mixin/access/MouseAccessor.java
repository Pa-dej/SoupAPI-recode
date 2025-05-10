package me.Padej_.soupapi.mixin.access;

import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mouse.class)
public interface MouseAccessor {
    @Accessor("cursorDeltaX")
    double getxVelocity();

    @Accessor("cursorDeltaY")
    double getyVelocity();
}

