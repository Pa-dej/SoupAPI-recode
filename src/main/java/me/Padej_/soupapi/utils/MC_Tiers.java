package me.Padej_.soupapi.utils;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

public class MC_Tiers {
    public static String getMcTiersIcon() {
        return "mctiers";
    }

    public static String getMcTiersGameModeIcon() {
        return switch (CONFIG.mctiersGameMode) {
            case LTMs -> "2v2";
            case VANILLA -> "vanilla";
            case UHC -> "uhc";
            case POT -> "pot";
            case NETHER_OP -> "nethop";
            case SMP -> "smp";
            case SWORD -> "sword";
            case AXE -> "axe";
            case MACE -> "mace";
        };
    }

    public enum TierGameModes {
        LTMs,
        VANILLA,
        UHC,
        POT,
        NETHER_OP,
        SMP,
        SWORD,
        AXE,
        MACE
    }

}
