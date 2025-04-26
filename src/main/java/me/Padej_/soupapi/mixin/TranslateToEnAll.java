package me.Padej_.soupapi.mixin;

import me.Padej_.soupapi.modules.Translator;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TextVisitFactory.class)
public class TranslateToEnAll {

    @ModifyArg(
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
                    ordinal = 0
            ),
            method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z"},
            index = 0
    )
    private static String adjustText(String text) {
        if (Translator.containsCyrillic(text)) {
            if (Translator.translationCache.containsKey(text)) {
                return Translator.translationCache.get(text);
            } else {
                Translator.queueTranslation(text);
            }
        }
        return text;
    }
}
