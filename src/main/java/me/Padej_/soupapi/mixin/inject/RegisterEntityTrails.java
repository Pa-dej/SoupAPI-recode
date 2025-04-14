package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.interfaces.TrailEntity;
import me.Padej_.soupapi.modules.Trails;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(Entity.class)
public abstract class RegisterEntityTrails implements TrailEntity {

    @Override
    public List<Trails.TrailSegment> soupAPI$getTrails() {
        return trails;
    }

    @Unique
    public List<Trails.TrailSegment> trails = new ArrayList<>();
}
