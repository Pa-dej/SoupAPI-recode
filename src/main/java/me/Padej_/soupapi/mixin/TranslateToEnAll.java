package me.Padej_.soupapi.mixin;

import me.Padej_.soupapi.modules.Translator;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

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
        if (true) return text;
        if (Translator.containsCyrillic(text)) {
            boolean startsWithSpace = text.startsWith(" ");
            boolean endsWithSpace = text.endsWith(" ");
            String baseText = text.trim();

            if (Translator.translationCache.containsKey(baseText)) {
                String translated = Translator.translationCache.get(baseText);
                // Восстановить пробелы
                if (startsWithSpace) translated = " " + translated;
                if (endsWithSpace) translated = translated + " ";
                return translated;
            } else {
                Translator.queueTranslation(baseText);
            }
        }
        return text;
    }

}
