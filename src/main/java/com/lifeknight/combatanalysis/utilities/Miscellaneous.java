package com.lifeknight.combatanalysis.utilities;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.lifeknight.combatanalysis.gui.hud.EnhancedHudText;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.variables.LifeKnightCycle;
import com.lifeknight.combatanalysis.variables.LifeKnightNumber;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.lifeknight.combatanalysis.gui.hud.EnhancedHudText.textToRender;
import static net.minecraft.util.EnumChatFormatting.*;

public class Miscellaneous {
    public static final String CHROMA_STRING =
            RED + "C" +
                    GOLD + "h" +
                    YELLOW + "r" +
                    GREEN + "o" +
                    BLUE + "m" +
                    LIGHT_PURPLE + "a";

    public static String getCurrentDateString() {
        return new SimpleDateFormat("MM/dd/yyyy").format(System.currentTimeMillis());
    }

    public static String getCurrentTimeString() {
        return new SimpleDateFormat("hh:mm:ss a").format(System.currentTimeMillis());
    }

    public static String getTimeAndDate(long epochTime) {
        return new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a").format(epochTime);
    }

    public static int getRandomIntBetweenRange(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean getRandomTrueOrFalse() {
        return getRandomDoubleBetweenRange(0, 1) == 1;
    }

    public static double getRandomDoubleBetweenRange(double min, double max) {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static EnumChatFormatting getEnumChatFormatting(String formattedName) {
        switch (formattedName) {
            case "Red":
                return RED;
            case "Gold":
                return GOLD;
            case "Yellow":
                return YELLOW;
            case "Green":
                return GREEN;
            case "Aqua":
                return AQUA;
            case "Blue":
                return BLUE;
            case "Light Purple":
                return LIGHT_PURPLE;
            case "Dark Red":
                return DARK_RED;
            case "Dark Green":
                return DARK_GREEN;
            case "Dark Aqua":
                return DARK_AQUA;
            case "Dark Blue":
                return DARK_BLUE;
            case "Dark Purple":
                return DARK_PURPLE;
            case "White":
                return WHITE;
            case "Gray":
                return GRAY;
            case "Dark Gray":
                return DARK_GRAY;
        }
        return BLACK;
    }

    public static void createEnhancedHudTextDefaultPropertyVariables() {
        new LifeKnightNumber.LifeKnightFloat("Default Text Scale", "HUD", 1.0F, 0.1F, 5.0F) {
            @Override
            public void onSetValue() {
                for (EnhancedHudText enhancedHudText : textToRender) {
                    enhancedHudText.setScale(this.getValue());
                }
            }
        }.setiCustomDisplayString(objects -> {
            float value = (float) objects[0];
            return "Default HUD Text Scale: " + (int) value * 100 + "%";
        });
        new LifeKnightCycle("Default HUD Text Separator", "HUD", Arrays.asList(">", ":", "|", "-", "[]")) {
            @Override
            public void onValueChange() {
                for (EnhancedHudText enhancedHudText : textToRender) {
                    enhancedHudText.setSeparator(this.getValue());
                }
            }

            @Override
            public String getCustomDisplayString() {
                return "Default Separator: " + YELLOW + this.getCurrentValueString();
            }
        };
        new LifeKnightCycle("Default HUD Text Prefix Color", "HUD", Arrays.asList(
                "Red",
                "Gold",
                "Yellow",
                "Green",
                "Aqua",
                "Blue",
                "Light Purple",
                "Dark Red",
                "Dark Green",
                "Dark Aqua",
                "Dark Blue",
                "Dark Purple",
                "White",
                "Gray",
                "Dark Gray",
                "Black",
                "Chroma"
        ), 12) {
            @Override
            public void onValueChange() {
                for (EnhancedHudText enhancedHudText : textToRender) {
                    enhancedHudText.setPrefixColor(this.getValue());
                }
            }

            @Override
            public String getCustomDisplayString() {
                return "Default HUD Text Prefix Color: " + (this.getValue() == 16 ? CHROMA_STRING : Miscellaneous.getEnumChatFormatting(this.getCurrentValueString()) + this.getCurrentValueString());
            }
        };
        new LifeKnightCycle("Default HUD Text Content Color", "HUD", Arrays.asList(
                "Red",
                "Gold",
                "Yellow",
                "Green",
                "Aqua",
                "Blue",
                "Light Purple",
                "Dark Red",
                "Dark Green",
                "Dark Aqua",
                "Dark Blue",
                "Dark Purple",
                "White",
                "Gray",
                "Dark Gray",
                "Black",
                "Chroma"
        ), 12) {
            @Override
            public void onValueChange() {
                for (EnhancedHudText enhancedHudText : textToRender) {
                    enhancedHudText.setContentColor(this.getValue());
                }
            }

            @Override
            public String getCustomDisplayString() {
                return "Default HUD Text Content Color: " + (this.getValue() == 16 ? CHROMA_STRING : Miscellaneous.getEnumChatFormatting(this.getCurrentValueString()) + this.getCurrentValueString());
            }
        };
    }

    public static JsonArray toJsonArrayString(List<?> elements) {
        JsonArray asJsonArray = new JsonArray();

        for (Object element : elements) {
            asJsonArray.add(new JsonParser().parse(element.toString()).getAsJsonObject());
        }

        return asJsonArray;
    }

    public static String toCSV(List<?> elements) {
        if (elements.isEmpty()) return "";
        StringBuilder result = new StringBuilder();

        for (Object element : elements) {
            result.append(element.toString()).append(",");
        }

        return result.substring(0, result.length() - 1);
    }

    public static void info(String info, Object... data) {
        FMLLog.info(Core.MOD_NAME + " > " + info, data);
    }

    public static void logWarn(String warn, Object... data) {
        FMLLog.log(Level.WARN, Core.MOD_NAME + " > " + warn, data);
    }

    public static void logError(String error, Object... data) {
        FMLLog.log(Level.ERROR, Core.MOD_NAME + " > " + error, data);
    }

    public static String getScoreboardDisplayName() {
        Scoreboard scoreboard = Minecraft.getMinecraft().theWorld.getScoreboard();
        if (scoreboard == null) return null;
        String scoreboardDisplayName = "";

        for (ScoreObjective scoreObjective : scoreboard.getScoreObjectives()) {
            if (Text.removeFormattingCodes(scoreObjective.getDisplayName()).length() > scoreboardDisplayName.length()) scoreboardDisplayName = scoreObjective.getDisplayName();
        }

        scoreboardDisplayName = Text.removeFormattingCodes(scoreboardDisplayName);

        return scoreboardDisplayName;
    }

    public static boolean itemStacksAreEqual(ItemStack itemStackA, ItemStack itemStackB) {
        if (!itemStackA.getUnlocalizedName().equals(itemStackB.getUnlocalizedName())) return false;

        if (itemStackA.getEnchantmentTagList() == null && itemStackB.getEnchantmentTagList() == null) return true;

        if (itemStackA.getEnchantmentTagList() == null && itemStackB.getEnchantmentTagList() != null) return false;

        if (itemStackA.getEnchantmentTagList() != null && itemStackB.getEnchantmentTagList() == null) return false;

        if (itemStackA.getEnchantmentTagList().equals(itemStackB.getEnchantmentTagList())) return true;

        return false;
    }
}
