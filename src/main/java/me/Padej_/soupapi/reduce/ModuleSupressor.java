package me.Padej_.soupapi.reduce;

import me.Padej_.soupapi.config.ConfigurableModule;
import me.Padej_.soupapi.utils.MC_Tiers;

import java.util.Objects;

public class ModuleSupressor extends ConfigurableModule {

    public static boolean disableItemOverlay() {
        if (Objects.requireNonNull(mc.getNetworkHandler()).getServerInfo() == null) return false;
        String ip = mc.getNetworkHandler().getServerInfo().address.toLowerCase();
        return
                // servers
                ip.contains("minefun") ||
                ip.contains("mineblaze") ||

                // MC Tiers
                isVanilla() ||
                isUHC() ||
                isSMP() ||
                isNethop() ||
                isPot()
                ;
    }

    public static boolean disableHPBar() {
        return
                isVanilla() ||
                isUHC() ||
                isSMP() ||
                isNethop() ||
                isPot()
                ;
    }

    private static boolean isLTMs() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.LTMs);
    }

    private static boolean isVanilla() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.VANILLA);
    }

    private static boolean isUHC() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.UHC);
    }

    private static boolean isPot() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.POT);
    }

    private static boolean isNethop() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.NETHER_OP);
    }

    private static boolean isSMP() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.SMP);
    }

    private static boolean isSword() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.SWORD);
    }

    private static boolean isMace() {
        return CONFIG.mctiersEnabled && CONFIG.mctiersGameMode.equals(MC_Tiers.TierGameModes.MACE);
    }

}
