package me.Padej_.soupapi.modules;

import me.Padej_.soupapi.config.ConfigurableModule;
import net.minecraft.util.Identifier;

public class Capes extends ConfigurableModule {

    public enum Config {
        SELECT_CAPE, UPDATE_CAPE
    }

    public enum CapeTextures {
        // April Fools Caeps
        APRIL_AWESOME_CAEP(Identifier.of("soupapi:textures/capes/april/awesome_caep.png")),
        APRIL_BLONK_CAEP(Identifier.of("soupapi:textures/capes/april/blonk_caep.png")),
        APRIL_NO_CIRCLE_CAEP(Identifier.of("soupapi:textures/capes/april/no_circle_caep.png")),
        APRIL_NYAN_CAEP(Identifier.of("soupapi:textures/capes/april/nyan_caep.png")),
        APRIL_SQUID_CAEP(Identifier.of("soupapi:textures/capes/april/squid_caep.png")),
        APRIL_VETERINARIAN_CAEP(Identifier.of("soupapi:textures/capes/april/veterinarian_caep.png")),

        // Default Capes
        DEFAULT_15TH_ANNIVERSARY(Identifier.of("soupapi:textures/capes/default/15th_anniversary.png")),
        DEFAULT_BACON(Identifier.of("soupapi:textures/capes/default/bacon.png")),
        DEFAULT_BIRTHDAY(Identifier.of("soupapi:textures/capes/default/birthday.png")),
        DEFAULT_CHERRY_BLOSSOM(Identifier.of("soupapi:textures/capes/default/cherry_blossom.png")),
        DEFAULT_COBALT(Identifier.of("soupapi:textures/capes/default/cobalt.png")),
        DEFAULT_DANNY_BSTYLE(Identifier.of("soupapi:textures/capes/default/danny_bstyle.png")),
        DEFAULT_FOLLOWERS(Identifier.of("soupapi:textures/capes/default/followers.png")),
        DEFAULT_MCC_15TH_YEARS(Identifier.of("soupapi:textures/capes/default/mcc_15th_years.png")),
        DEFAULT_MIGRATOR(Identifier.of("soupapi:textures/capes/default/migrator.png")),
        DEFAULT_MILLIONTH_CUSTOMER(Identifier.of("soupapi:textures/capes/default/millionth_customer.png")),
        DEFAULT_MINECON_2011(Identifier.of("soupapi:textures/capes/default/minecon_2011.png")),
        DEFAULT_MINECON_2012(Identifier.of("soupapi:textures/capes/default/minecon_2012.png")),
        DEFAULT_MINECON_2013(Identifier.of("soupapi:textures/capes/default/minecon_2013.png")),
        DEFAULT_MINECON_2014(Identifier.of("soupapi:textures/capes/default/minecon_2014.png")),
        DEFAULT_MINECON_2015(Identifier.of("soupapi:textures/capes/default/minecon_2015.png")),
        DEFAULT_MINECON_2016(Identifier.of("soupapi:textures/capes/default/minecon_2016.png")),
        DEFAULT_MINECON_2017(Identifier.of("soupapi:textures/capes/default/minecon_2017.png")),
        DEFAULT_MINECON_2018(Identifier.of("soupapi:textures/capes/default/minecon_2018.png")),
        DEFAULT_MINECON_2019(Identifier.of("soupapi:textures/capes/default/minecon_2019.png")),
        DEFAULT_MINECRAFT_EXPERIENCE(Identifier.of("soupapi:textures/capes/default/minecraft_experience.png")),
        DEFAULT_MOJANG(Identifier.of("soupapi:textures/capes/default/mojang.png")),
        DEFAULT_MOJANG_OLD(Identifier.of("soupapi:textures/capes/default/mojang_old.png")),
        DEFAULT_MOJANG_STUDIOS(Identifier.of("soupapi:textures/capes/default/mojang_studios.png")),
        DEFAULT_MOJIRA_MODERATOR(Identifier.of("soupapi:textures/capes/default/mojira_moderator.png")),
        DEFAULT_NEW_YEARS_2010(Identifier.of("soupapi:textures/capes/default/new_years_2010.png")),
        DEFAULT_NEW_YEARS_2011(Identifier.of("soupapi:textures/capes/default/new_years_2011.png")),
        DEFAULT_PANCAKE(Identifier.of("soupapi:textures/capes/default/pancake.png")),
        DEFAULT_PRISMARINE(Identifier.of("soupapi:textures/capes/default/prismarine.png")),
        DEFAULT_PURPLE_HEART(Identifier.of("soupapi:textures/capes/default/purple_heart.png")),
        DEFAULT_REALMS_NEW(Identifier.of("soupapi:textures/capes/default/realms_new.png")),
        DEFAULT_REALMS_OLD(Identifier.of("soupapi:textures/capes/default/realms_old.png")),
        DEFAULT_SCROLLS(Identifier.of("soupapi:textures/capes/default/scrolls.png")),
        DEFAULT_SNOWMAN(Identifier.of("soupapi:textures/capes/default/snowman.png")),
        DEFAULT_SPADE(Identifier.of("soupapi:textures/capes/default/spade.png")),
        DEFAULT_TEST(Identifier.of("soupapi:textures/capes/default/test.png")),
        DEFAULT_TRANSLATOR(Identifier.of("soupapi:textures/capes/default/translator.png")),
        DEFAULT_TURTLE(Identifier.of("soupapi:textures/capes/default/turtle.png")),
        DEFAULT_UNUSED_1(Identifier.of("soupapi:textures/capes/default/unused_1.png")),
        DEFAULT_UNUSED_2(Identifier.of("soupapi:textures/capes/default/unused_2.png")),
        DEFAULT_UNUSED_3(Identifier.of("soupapi:textures/capes/default/unused_3.png")),
        DEFAULT_VALENTINE(Identifier.of("soupapi:textures/capes/default/valentine.png")),
        DEFAULT_VANILLA(Identifier.of("soupapi:textures/capes/default/vanilla.png")),
        DEFAULT_MENACE(Identifier.of("soupapi:textures/capes/default/menace.png")),
        DEFAULT_HOME(Identifier.of("soupapi:textures/capes/default/home.png")),
        DEFAULT_OFFICE(Identifier.of("soupapi:textures/capes/default/office.png")),
        DEFAULT_YEARN(Identifier.of("soupapi:textures/capes/default/yearn.png")),
        DEFAULT_COMMON(Identifier.of("soupapi:textures/capes/default/common.png")),

        // Microsoft(XBox) capes
        XBOX_XBOX(Identifier.of("soupapi:textures/capes/microsoft/xbox.png")),
        XBOX_1ST_BIRTHDAY(Identifier.of("soupapi:textures/capes/microsoft/xbox_1st_birthday.png")),
        XBOX_UNUSED_STARWARS_1(Identifier.of("soupapi:textures/capes/microsoft/unused_starwars_1.png")),
        XBOX_UNUSED_STARWARS_2(Identifier.of("soupapi:textures/capes/microsoft/unused_starwars_2.png")),
        XBOX_ADVENTURE_TIME_UNUSED(Identifier.of("soupapi:textures/capes/microsoft/adventuretime_unused.png")),

        // Minecraft dungeons capes
        MCD_AMETHYST(Identifier.of("soupapi:textures/capes/mcd/amethyst.png")),
        MCD_CLOUDY_CLIMB(Identifier.of("soupapi:textures/capes/mcd/cloudy_climb.png")),
        MCD_COW(Identifier.of("soupapi:textures/capes/mcd/cow.png")),
        MCD_DOWNPOUR(Identifier.of("soupapi:textures/capes/mcd/downpour.png")),
        MCD_FAUNA_FAIRE(Identifier.of("soupapi:textures/capes/mcd/fauna_faire.png")),
        MCD_GIFT_WARP(Identifier.of("soupapi:textures/capes/mcd/gift_warp.png")),
        MCD_GLOW(Identifier.of("soupapi:textures/capes/mcd/glow.png")),
        MCD_HAMMER(Identifier.of("soupapi:textures/capes/mcd/hammer.png")),
        MCD_HERO(Identifier.of("soupapi:textures/capes/mcd/hero.png")),
        MCD_ICOLOGER(Identifier.of("soupapi:textures/capes/mcd/icologer.png")),
        MCD_LUMINOUS_NIGHT(Identifier.of("soupapi:textures/capes/mcd/luminous_night.png")),
        MCD_PHANTOM(Identifier.of("soupapi:textures/capes/mcd/phantom.png")),
        MCD_SINISTER(Identifier.of("soupapi:textures/capes/mcd/sinister.png")),
        MCD_TURTLE(Identifier.of("soupapi:textures/capes/mcd/turtle.png")),

        // Custom Capes
        CUSTOM_1UP(Identifier.of("soupapi:textures/capes/custom/1up.png")),
        CUSTOM_ADIDAS(Identifier.of("soupapi:textures/capes/custom/adidas.png")),
        CUSTOM_ALPHA(Identifier.of("soupapi:textures/capes/custom/alpha.png")),
        CUSTOM_AZURE(Identifier.of("soupapi:textures/capes/custom/azure.png")),
        CUSTOM_BLUE_FLAME(Identifier.of("soupapi:textures/capes/custom/blue_flame.png")),
        CUSTOM_BROWN_FEATHER(Identifier.of("soupapi:textures/capes/custom/brown_feather.png")),
        CUSTOM_BUGGED(Identifier.of("soupapi:textures/capes/custom/bugged.png")),
        CUSTOM_CB(Identifier.of("soupapi:textures/capes/custom/cb.png")),
        CUSTOM_CHEST(Identifier.of("soupapi:textures/capes/custom/chest.png")),
        CUSTOM_CHRISTMAS_LUNAR(Identifier.of("soupapi:textures/capes/custom/christmas_lunar.png")),
        CUSTOM_DARK_GOLD(Identifier.of("soupapi:textures/capes/custom/dark_gold.png")),
        CUSTOM_DISCORD(Identifier.of("soupapi:textures/capes/custom/discord.png")),
        CUSTOM_FABRIC(Identifier.of("soupapi:textures/capes/custom/fabric.png")),
        CUSTOM_GALAXY(Identifier.of("soupapi:textures/capes/custom/galaxy.png")),
        CUSTOM_GAMEBOY(Identifier.of("soupapi:textures/capes/custom/gameboy.png")),
        CUSTOM_GENDERFLUID(Identifier.of("soupapi:textures/capes/custom/genderfluid.png")),
        CUSTOM_GRAY(Identifier.of("soupapi:textures/capes/custom/gray.png")),
        CUSTOM_HALLOWEEN(Identifier.of("soupapi:textures/capes/custom/halloween.png")),
        CUSTOM_ICE(Identifier.of("soupapi:textures/capes/custom/ice.png")),
        CUSTOM_JAVA(Identifier.of("soupapi:textures/capes/custom/java.png")),
        CUSTOM_JUKE_BOX(Identifier.of("soupapi:textures/capes/custom/juke_box.png")),
        CUSTOM_KIRBY(Identifier.of("soupapi:textures/capes/custom/kirby.png")),
        CUSTOM_MATRIX(Identifier.of("soupapi:textures/capes/custom/matrix.png")),
        CUSTOM_NASA(Identifier.of("soupapi:textures/capes/custom/nasa.png")),
        CUSTOM_NETHERITE(Identifier.of("soupapi:textures/capes/custom/netherite.png")),
        CUSTOM_OMEGA_1(Identifier.of("soupapi:textures/capes/custom/omega_1.png")),
        CUSTOM_OMEGA_2(Identifier.of("soupapi:textures/capes/custom/omega_2.png")),
        CUSTOM_OMEGA_3(Identifier.of("soupapi:textures/capes/custom/omega_3.png")),
        CUSTOM_ORANGE(Identifier.of("soupapi:textures/capes/custom/orange.png")),
        CUSTOM_PURPLE(Identifier.of("soupapi:textures/capes/custom/purple.png")),
        CUSTOM_RAIN(Identifier.of("soupapi:textures/capes/custom/rain.png")),
        CUSTOM_ROSE(Identifier.of("soupapi:textures/capes/custom/rose.png")),
        CUSTOM_SMOOTH_BLUE(Identifier.of("soupapi:textures/capes/custom/smooth_blue.png")),
        CUSTOM_SNOWFLAKE_LUNAR(Identifier.of("soupapi:textures/capes/custom/snowflake_lunar.png")),
        CUSTOM_SPEEDSILVER(Identifier.of("soupapi:textures/capes/custom/speedsilver.png")),
        CUSTOM_VETERAN(Identifier.of("soupapi:textures/capes/custom/veteran.png")),
        CUSTOM_WINTER(Identifier.of("soupapi:textures/capes/custom/winter.png")),
        CUSTOM_YOUTUBE(Identifier.of("soupapi:textures/capes/custom/youtube.png")),

        // Staff Capes by winvi
        STAFF_TEAM_PADEJ(Identifier.of("soupapi:textures/capes/team/padej_.png")),
        STAFF_TEAM_WINVI(Identifier.of("soupapi:textures/capes/team/winvi.png")),
        STAFF_TEAM_ICYCROW(Identifier.of("soupapi:textures/capes/team/icycrow.png")),
        STAFF_TEAM_NOVADVORGA(Identifier.of("soupapi:textures/capes/team/novadvorga.png")),
        STAFF_TEAM_IBRRPATAPIM_(Identifier.of("soupapi:textures/capes/team/ibrrpatapim_.png"));

        private final Identifier texturePath;

        CapeTextures(Identifier texturePath) {
            this.texturePath = texturePath;
        }

        public Identifier getTexturePath() {
            return texturePath;
        }
    }
}
