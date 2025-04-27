package me.Padej_.soupapi.mixin;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.HashMap;
import java.util.Map;

import static me.Padej_.soupapi.config.ConfigurableModule.CONFIG;

@Mixin(TextVisitFactory.class)
public class TranslateToBruh {
    // Маппинг русских букв на английские
    private static final Map<Character, String> TRANSLIT_MAP = new HashMap<>();

    static {
        TRANSLIT_MAP.put('А', "A"); TRANSLIT_MAP.put('Б', "B"); TRANSLIT_MAP.put('В', "V");
        TRANSLIT_MAP.put('Г', "G"); TRANSLIT_MAP.put('Д', "D"); TRANSLIT_MAP.put('Е', "E");
        TRANSLIT_MAP.put('Ё', "Yo"); TRANSLIT_MAP.put('Ж', "Zh"); TRANSLIT_MAP.put('З', "Z");
        TRANSLIT_MAP.put('И', "I"); TRANSLIT_MAP.put('Й', "Y"); TRANSLIT_MAP.put('К', "K");
        TRANSLIT_MAP.put('Л', "L"); TRANSLIT_MAP.put('М', "M"); TRANSLIT_MAP.put('Н', "N");
        TRANSLIT_MAP.put('О', "O"); TRANSLIT_MAP.put('П', "P"); TRANSLIT_MAP.put('Р', "R");
        TRANSLIT_MAP.put('С', "S"); TRANSLIT_MAP.put('Т', "T"); TRANSLIT_MAP.put('У', "U");
        TRANSLIT_MAP.put('Ф', "F"); TRANSLIT_MAP.put('Х', "Kh"); TRANSLIT_MAP.put('Ц', "Ts");
        TRANSLIT_MAP.put('Ч', "Ch"); TRANSLIT_MAP.put('Ш', "Sh"); TRANSLIT_MAP.put('Щ', "Sch");
        TRANSLIT_MAP.put('Ъ', ""); TRANSLIT_MAP.put('Ы', "Y"); TRANSLIT_MAP.put('Ь', "");
        TRANSLIT_MAP.put('Э', "E"); TRANSLIT_MAP.put('Ю', "Yu"); TRANSLIT_MAP.put('Я', "Ya");

        // Добавляем строчные буквы
        TRANSLIT_MAP.put('а', "a"); TRANSLIT_MAP.put('б', "b"); TRANSLIT_MAP.put('в', "v");
        TRANSLIT_MAP.put('г', "g"); TRANSLIT_MAP.put('д', "d"); TRANSLIT_MAP.put('е', "e");
        TRANSLIT_MAP.put('ё', "yo"); TRANSLIT_MAP.put('ж', "zh"); TRANSLIT_MAP.put('з', "z");
        TRANSLIT_MAP.put('и', "i"); TRANSLIT_MAP.put('й', "y"); TRANSLIT_MAP.put('к', "k");
        TRANSLIT_MAP.put('л', "l"); TRANSLIT_MAP.put('м', "m"); TRANSLIT_MAP.put('н', "n");
        TRANSLIT_MAP.put('о', "o"); TRANSLIT_MAP.put('п', "p"); TRANSLIT_MAP.put('р', "r");
        TRANSLIT_MAP.put('с', "s"); TRANSLIT_MAP.put('т', "t"); TRANSLIT_MAP.put('у', "u");
        TRANSLIT_MAP.put('ф', "f"); TRANSLIT_MAP.put('х', "kh"); TRANSLIT_MAP.put('ц', "ts");
        TRANSLIT_MAP.put('ч', "ch"); TRANSLIT_MAP.put('ш', "sh"); TRANSLIT_MAP.put('щ', "sch");
        TRANSLIT_MAP.put('ъ', ""); TRANSLIT_MAP.put('ы', "y"); TRANSLIT_MAP.put('ь', "'");
        TRANSLIT_MAP.put('э', "e"); TRANSLIT_MAP.put('ю', "yu"); TRANSLIT_MAP.put('я', "ya");
    }

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
        if (!CONFIG.translatorBruhEnabled) return text;
        boolean hasRussian = text.chars().anyMatch(ch -> TRANSLIT_MAP.containsKey((char) ch));
        if (!hasRussian) {
            return text; // Возвращаем оригинальный текст, если русских букв нет
        }

        // Переводим текст
        StringBuilder translated = new StringBuilder();
        for (char ch : text.toCharArray()) {
            translated.append(TRANSLIT_MAP.getOrDefault(ch, String.valueOf(ch)));
        }
        return translated.toString();
    }
}
