package me.Padej_.soupapi.sounds;

import me.Padej_.soupapi.utils.EntityUtils;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Random;

public class CustomSounds {
    public static SoundEvent ON = SoundEvent.of(Identifier.of("soupapi", "on"));
    public static SoundEvent OFF = SoundEvent.of(Identifier.of("soupapi", "off"));
    public static SoundEvent GET = SoundEvent.of(Identifier.of("soupapi", "get"));
    public static SoundEvent BUBBLE = SoundEvent.of(Identifier.of("soupapi", "bubble"));
    public static SoundEvent BELL = SoundEvent.of(Identifier.of("soupapi", "bell"));
    public static SoundEvent BONK = SoundEvent.of(Identifier.of("soupapi", "bonk"));
    public static SoundEvent POK = SoundEvent.of(Identifier.of("soupapi", "pok"));
    public static SoundEvent MAGIC_POK = SoundEvent.of(Identifier.of("soupapi", "magic_pok"));

    public enum SoundType {
        GET,
        BUBBLE,
        BELL,
        BONK,
        POK,
        MAGIC_POK;
    }
}
