package com.lifeknight.combatanalysis.gui.hud;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import com.lifeknight.combatanalysis.variables.LifeKnightCycle;
import com.lifeknight.combatanalysis.variables.LifeKnightString;
import net.minecraft.client.Minecraft;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Arrays;

import static com.lifeknight.combatanalysis.mod.Core.hudTextShadow;
import static net.minecraft.util.EnumChatFormatting.*;

public abstract class EnhancedHudText extends Manipulable {
    public static ArrayList<EnhancedHudText> textToRender = new ArrayList<>();
    private final String prefix;
    private final LifeKnightBoolean hudTextVisible;
    private final LifeKnightCycle separator;
    private final LifeKnightCycle prefixColor;
    private final LifeKnightCycle contentColor;
    private final LifeKnightCycle alignment;
    private final LifeKnightString lastString;
    public final ArrayList<LifeKnightButton> connectedButtons = new ArrayList<>();

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix, LifeKnightCycle separator, LifeKnightCycle prefixColor, LifeKnightCycle contentColor, LifeKnightCycle alignment, LifeKnightBoolean hudTextVisible) {
        super(name, defaultX, defaultY);
        this.prefix = prefix;
        this.hudTextVisible = hudTextVisible;
        this.separator = separator;
        this.prefixColor = prefixColor;
        this.contentColor = contentColor;
        this.alignment = alignment;
        lastString = new LifeKnightString(name + "LastString", "Invisible", "");

        connectedButtons.add(new LifeKnightButton("", 0, 0, 0, 100) {
            @Override
            public void work() {
                EnhancedHudText.this.hudTextVisible.toggle();
            }

            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                this.displayString = EnhancedHudText.this.hudTextVisible.getValue() ? GREEN + "Shown" : RED + "Hidden";
                super.drawButton(mc, mouseX, mouseY);
            }
        });
        if (!prefix.isEmpty()) {
            connectedButtons.add(new LifeKnightButton("Separator: " + separator.getCurrentValueString(), 0, 0, 0, 100) {
                @Override
                public void work() {
                    separator.next();
                }

                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    this.displayString = "Separator: " + separator.getCurrentValueString().replace(" ", "");
                    super.drawButton(mc, mouseX, mouseY);
                }
            });

            connectedButtons.add(new LifeKnightButton("Prefix Color: " + getEnumChatFormatting(prefixColor) + prefixColor.getCurrentValueString(), 0, 0, 0, 100) {
                @Override
                public void work() {
                    prefixColor.next();
                }

                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    this.displayString = "Prefix Color: " + getEnumChatFormatting(prefixColor) + prefixColor.getCurrentValueString();
                    int i;
                    if (!((i = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 15) < 100)) {
                        this.width = i;
                    } else {
                        this.width = 100;
                    }
                    super.drawButton(mc, mouseX, mouseY);
                }
            });
        }

        connectedButtons.add(new LifeKnightButton("Content Color: " + getEnumChatFormatting(contentColor) + contentColor.getCurrentValueString(), 0, 0, 0, 100) {
            @Override
            public void work() {
                contentColor.next();
            }

            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                this.displayString = "Content Color: " + getEnumChatFormatting(contentColor) + contentColor.getCurrentValueString();
                int i;
                if (!((i = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 15) < 100)) {
                    this.width = i;
                } else {
                    this.width = 100;
                }
                super.drawButton(mc, mouseX, mouseY);
            }

        });

        connectedButtons.add(new LifeKnightButton("", 0, 0, 0, 100) {
            @Override
            public void work() {
                alignment.next();
            }

            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                this.displayString = "Alignment: " + alignment.getCurrentValueString();
                super.drawButton(mc, mouseX, mouseY);
            }
        });

        super.connectedComponent = this;
        textToRender.add(this);
    }

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix) {
        this(name, defaultX, defaultY, prefix, new LifeKnightCycle(name + "PrefixColor", "Invisible", new ArrayList<>(Arrays.asList(" > ", ": ", " | ", " - "))),
                new LifeKnightCycle(name + "Color", "Invisible", new ArrayList<>(Arrays.asList(
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
                        "Black"
                )), 12), new LifeKnightCycle(name + "ContentColor", "Invisible", new ArrayList<>(Arrays.asList(
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
                        "Black"
                )), 12), new LifeKnightCycle(name + "Alignment", "Invisible", new ArrayList<>(Arrays.asList(
                        "Left",
                        "Center",
                        "Right"
                ))), new LifeKnightBoolean(name + "Visible", "Invisible", true));
    }

    public EnhancedHudText(String name, int defaultX, int defaultY) {
        this(name, defaultX, defaultY, "");
    }

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix, int defaultPrefixColor) {
        this(name, defaultX, defaultY, prefix);
        prefixColor.setCurrentValue(defaultPrefixColor);
    }

    public EnhancedHudText(String name) {
        this(name, 0, 0);
    }

    public abstract String getTextToDisplay();

    @Override
    public String getDisplayText() {
        if (prefix.isEmpty()) {
            return getEnumChatFormatting(contentColor) + getTextToDisplay();
        } else {
            return getEnumChatFormatting(prefixColor) + prefix + separator.getCurrentValueString() + getEnumChatFormatting(contentColor) + getTextToDisplay();
        }
    }

    public EnumChatFormatting getEnumChatFormatting(LifeKnightCycle colorCycle) {
        switch (colorCycle.getCurrentValueString()) {
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

    public abstract boolean isVisible();

    public void render() {
        if (this.isVisible() && hudTextVisible.getValue()) {
            Minecraft.getMinecraft().fontRendererObj.drawString(getDisplayText(), getXCoordinate(), getYCoordinate(), 0xffffffff, hudTextShadow.getValue());
        }
    }

    @Override
    public int getXCoordinate() {
        int xCoordinate = super.getXCoordinate();
        switch (alignment.getValue()) {
            case 0:
                return xCoordinate;
            case 1:
                return (int) ((xCoordinate - Minecraft.getMinecraft().fontRendererObj.getStringWidth(getDisplayText()) / 2F) + Minecraft.getMinecraft().fontRendererObj.getStringWidth(lastString.getValue()) / 2F);
            default:
                return xCoordinate + Minecraft.getMinecraft().fontRendererObj.getStringWidth(lastString.getValue()) - (Minecraft.getMinecraft().fontRendererObj.getStringWidth(getDisplayText()));
        }
    }

    public void updateString(String newString) {
        lastString.setValue(Text.removeFormattingCodes(newString));
    }

    public static void doRender() {
        for (EnhancedHudText hudText : textToRender) {
            hudText.render();
        }
    }

    public void setVisibility(boolean newVisibility) {
        hudTextVisible.setValue(newVisibility);
    }

    public void setSeparator(int newSeparatorId) {
        separator.setCurrentValue(newSeparatorId);
    }

    public void setPrefixColor(int newPrefixColorId) {
        prefixColor.setCurrentValue(newPrefixColorId);
    }

    public void setContentColor(int newContentColorId) {
        contentColor.setCurrentValue(newContentColorId);
    }
}
