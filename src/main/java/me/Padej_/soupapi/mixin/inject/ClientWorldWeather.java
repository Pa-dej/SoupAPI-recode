package me.Padej_.soupapi.mixin.inject;

import me.Padej_.soupapi.utils.Weather;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(ClientWorld.class)
public abstract class ClientWorldWeather extends World {
    protected ClientWorldWeather(MutableWorldProperties properties, RegistryKey<World> registryRef, DynamicRegistryManager registryManager, RegistryEntry<DimensionType> dimensionEntry, boolean isClient, boolean debugWorld, long seed, int maxChainedNeighborUpdates) {
        super(properties, registryRef, registryManager, dimensionEntry, isClient, debugWorld, seed, maxChainedNeighborUpdates);
    }

    @Override
    public boolean isRaining() {
        return CONFIG.weatherChangerEnabled;
    }

    @Override
    public float getRainGradient(float delta) {
        if (CONFIG.weatherChangerEnabled) {
            if (CONFIG.weatherType.equals(Weather.CLEAN)) {
                return 0;
            } else if (CONFIG.weatherType.equals(Weather.RAIN)) {
                return 1.0f;
            }
        }
        return MathHelper.lerp(delta, this.rainGradientPrev, this.rainGradient);
    }
}
