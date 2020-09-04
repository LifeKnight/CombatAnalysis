package com.lifeknight.combatanalysis.gui.hud;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Render;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.utilities.Video;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import com.lifeknight.combatanalysis.variables.LifeKnightCycle;
import com.lifeknight.combatanalysis.variables.LifeKnightString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.EnumChatFormatting;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.RED;

public abstract class EnhancedHudText extends Manipulable {
    public static final List<EnhancedHudText> textToRender = new ArrayList<>();
    private static final Color textBoxColor = new Color(26, 26, 26);
    private final String prefix;
    private final LifeKnightBoolean hudTextVisible;
    private final LifeKnightCycle separator;
    private final LifeKnightCycle prefixColor;
    private final LifeKnightCycle contentColor;
    private final LifeKnightCycle alignment;
    private final LifeKnightString lastString;
    public final List<LifeKnightButton> connectedButtons = new ArrayList<>();

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix) {
        super(name + " HUD Text", defaultX, defaultY);
        this.prefix = prefix;
        this.hudTextVisible = new LifeKnightBoolean("Visible", name + " HUD Text", true);
        this.separator = new LifeKnightCycle("Separator", name + " HUD Text", Arrays.asList(">", ":", "|", "-", "[]"));
        this.prefixColor = new LifeKnightCycle("Prefix Color", name + " HUD Text", Arrays.asList(
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
            public String getCustomDisplayString() {
                return "Prefix Color: " + (this.getValue() == 16 ? Miscellaneous.CHROMA_STRING : Miscellaneous.getEnumChatFormatting(this.getCurrentValueString()) + this.getCurrentValueString());
            }
        };
        this.contentColor = new LifeKnightCycle("Content Color", name + " HUD Text", Arrays.asList(
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
            public String getCustomDisplayString() {
                return "Content Color: " + (this.getValue() == 16 ? Miscellaneous.CHROMA_STRING : Miscellaneous.getEnumChatFormatting(this.getCurrentValueString()) + this.getCurrentValueString());
            }
        };
        this.alignment = new LifeKnightCycle("Alignment", name + " HUD Text", Arrays.asList(
                "Left",
                "Center",
                "Right"
        ));
        this.lastString = new LifeKnightString("Last String", name + " HUD Text", this.getTextToDisplay());
        this.lastString.setShowInLifeKnightGui(false);
        this.hudTextVisible.setShowInLifeKnightGui(false);
        this.separator.setShowInLifeKnightGui(false);
        this.prefixColor.setShowInLifeKnightGui(false);
        this.contentColor.setShowInLifeKnightGui(false);
        this.alignment.setShowInLifeKnightGui(false);

        this.hudTextVisible.setiCustomDisplayString(objects -> this.hudTextVisible.getValue() ? GREEN + "Shown" : RED + "Hidden");
        this.separator.setiCustomDisplayString(objects -> "Separator: " + this.separator.getCurrentValueString());
        this.alignment.setiCustomDisplayString(objects -> "Alignment: " + this.alignment.getCurrentValueString());

        this.connectedButtons.add(new LifeKnightButton(EnhancedHudText.this.hudTextVisible.getCustomDisplayString(), 0, 0, 0, 100) {
            @Override
            public void work() {
                EnhancedHudText.this.hudTextVisible.toggle();
                this.displayString = EnhancedHudText.this.hudTextVisible.getCustomDisplayString();
            }

        });

        if (!prefix.isEmpty()) {
            this.connectedButtons.add(new LifeKnightButton(this.separator.getCurrentValueString(), 0, 0, 0, 100) {
                @Override
                public void work() {
                    EnhancedHudText.this.separator.next();
                }

                @Override
                public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
                    this.displayString = EnhancedHudText.this.separator.getCustomDisplayString();
                    super.drawButton(minecraft, mouseX, mouseY);
                }
            });
            this.connectedButtons.add(new LifeKnightButton(this.prefixColor.getCustomDisplayString(), 0, 0, 0, 100) {
                @Override
                public void work() {
                    EnhancedHudText.this.prefixColor.next();
                }

                @Override
                public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
                    this.displayString = EnhancedHudText.this.prefixColor.getCustomDisplayString();
                    int i;
                    if (!((i = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 15) < 100)) {
                        this.width = i;
                    } else {
                        this.width = 100;
                    }
                    super.drawButton(minecraft, mouseX, mouseY);
                }
            });
        }

        this.connectedButtons.add(new LifeKnightButton(this.contentColor.getCustomDisplayString(), 0, 0, 0, 100) {
            @Override
            public void work() {
                EnhancedHudText.this.contentColor.next();
            }

            @Override
            public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
                this.displayString = EnhancedHudText.this.contentColor.getCustomDisplayString();
                int i;
                if (!((i = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 15) < 100)) {
                    this.width = i;
                } else {
                    this.width = 100;
                }
                super.drawButton(minecraft, mouseX, mouseY);
            }
        });

        this.connectedButtons.add(new LifeKnightButton(this.alignment.getCustomDisplayString(), 0, 0, 0, 100) {
            @Override
            public void work() {
                EnhancedHudText.this.alignment.next();
                this.displayString = EnhancedHudText.this.alignment.getCustomDisplayString();
            }
        });

        super.connectedComponents.addAll(this.connectedButtons);
        textToRender.add(this);
    }

    public EnhancedHudText(String name, int defaultX, int defaultY) {
        this(name, defaultX, defaultY, "");
    }

    public EnhancedHudText(String name, String prefix) {
        this(name, 0, 0, prefix);
    }

    public EnhancedHudText(String name) {
        this(name, 0, 0);
    }

    public abstract String getTextToDisplay();

    public String getDisplayText() {
        if (this.prefix.isEmpty()) {
            return Miscellaneous.getEnumChatFormatting(this.contentColor.getCurrentValueString()) + this.getTextToDisplay();
        } else {
            switch (this.separator.getValue()) {
                case 1:
                    return Miscellaneous.getEnumChatFormatting(this.prefixColor.getCurrentValueString()) + this.prefix + ": " + Miscellaneous.getEnumChatFormatting(this.contentColor.getCurrentValueString()) + this.getTextToDisplay();
                case 4:
                    return Miscellaneous.getEnumChatFormatting(this.prefixColor.getCurrentValueString()) + "[" + this.prefix + "] " + Miscellaneous.getEnumChatFormatting(this.contentColor.getCurrentValueString()) + this.getTextToDisplay();
            }
            return Miscellaneous.getEnumChatFormatting(this.prefixColor.getCurrentValueString()) + this.prefix + " " + this.separator.getCurrentValueString() + " " + Miscellaneous.getEnumChatFormatting(this.contentColor.getCurrentValueString()) + this.getTextToDisplay();
        }
    }

    private String getCleanDisplayText() {
        return EnumChatFormatting.getTextWithoutFormattingCodes(this.getDisplayText());
    }

    public abstract boolean isVisible();

    public void doRender() {
        if (Minecraft.getMinecraft().inGameHasFocus && this.hudTextVisible.getValue() && this.isVisible()) {
            float scale = this.getScale();
            float xPosition = this.getXCoordinate();
            float yPosition = this.getRawYPosition();
            float height = this.getHeight();
            float width = this.getWidth();
            if (Core.hudTextBox.getValue()) {
                Render.drawRectangle(xPosition, yPosition, xPosition + width, yPosition + height, scale, textBoxColor, 255F * Core.hudTextBoxOpacity.getValue());
            }
            this.drawText(xPosition, yPosition, width, scale);
        }
    }

    private void drawText(float xPosition, float yPosition, float width, float scale) {
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRendererObj;
        if (Core.hudTextBox.getValue()) yPosition += 1.7F * scale;

        boolean dropShadow = Core.hudTextShadow.getValue();
        boolean contentChroma = this.contentColor.getValue() == 16;

        float chromaSpeed = 1000F / Core.chromaSpeed.getValue();

        EnumChatFormatting prefixColor = Miscellaneous.getEnumChatFormatting(this.prefixColor.getCurrentValueString());
        EnumChatFormatting contentColor = Miscellaneous.getEnumChatFormatting(this.contentColor.getCurrentValueString());

        if (this.prefix.isEmpty()) {
            if (contentChroma) {
                if (Core.hudTextBox.getValue()) {
                    Render.drawHorizontallyCenteredChromaString(xPosition + width / 2F, yPosition, scale, dropShadow, chromaSpeed, this.getCleanDisplayText());
                } else {
                    Render.drawHorizontallyCenteredString(xPosition + width / 2F, yPosition, scale, dropShadow, this.getDisplayText());
                }
            }
        } else {
            boolean prefixChroma = this.prefixColor.getValue() == 16;
            float textXPosition = Core.hudTextBox.getValue() ? xPosition + width / 2 - scale * fontRenderer.getStringWidth(this.getDisplayText()) / 2F + 0.7F : xPosition;

            if (this.separator.getValue() == 4) {
                if (prefixChroma) {
                    Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, "[");
                } else {
                    Render.drawString(textXPosition, yPosition, scale, dropShadow, prefixColor + "[");
                }
                textXPosition += fontRenderer.getCharWidth('[') * scale;
            }

            if (prefixChroma) {
                Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, this.prefix);
            } else {
                Render.drawString(textXPosition, yPosition, scale, dropShadow, prefixColor + this.prefix);
            }


            textXPosition += fontRenderer.getStringWidth(this.prefix) * scale;

            switch (this.separator.getValue()) {
                case 1:
                    if (prefixChroma) {
                        Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, ":");
                    } else {
                        Render.drawString(textXPosition, yPosition, scale, dropShadow, prefixColor + ":");
                    }
                    textXPosition += fontRenderer.getCharWidth(':') * scale;
                    break;
                case 4:
                    if (prefixChroma) {
                        Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, "]");
                    } else {
                        Render.drawString(textXPosition, yPosition, scale, dropShadow, prefixColor + "]");
                    }
                    textXPosition += fontRenderer.getCharWidth(']') * scale;
                    break;

                default:
                    String separator = " " + this.separator.getCurrentValueString();
                    if (prefixChroma) {
                        Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, separator);
                    } else {
                        Render.drawString(textXPosition, yPosition, scale, dropShadow, prefixColor + separator);
                    }
                    textXPosition += fontRenderer.getStringWidth(separator) * scale;
                    break;
            }

            if (contentChroma) {
                Render.drawChromaString(textXPosition, yPosition, scale, dropShadow, chromaSpeed, " " + this.getTextToDisplay());
            } else {
                Render.drawString(textXPosition, yPosition, scale, dropShadow, " " + contentColor + this.getTextToDisplay());
            }
        }
    }

    @Override
    public void update(int newX, int newY, float newScale) {
        this.updateString(this.getDisplayText());
        super.update(newX, newY, newScale);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, int xPosition, int yPosition, int width, int height, float scale, boolean isSelectedButton) {
        if (Core.hudTextBox.getValue()) {
            Render.drawRectangle(xPosition, yPosition, xPosition + width, yPosition + height, scale, textBoxColor, 255F * Core.hudTextBoxOpacity.getValue());
        }
        this.drawText(xPosition, yPosition, width, scale);
        for (LifeKnightButton lifeKnightButton : this.connectedButtons) {
            lifeKnightButton.visible = isSelectedButton;
            lifeKnightButton.xPosition = xPosition - 120 < 0 ? xPosition + width + 20 : xPosition - 120;
            lifeKnightButton.yPosition = yPosition + this.connectedButtons.size() * 30 + 5 > Video.getGameHeight() ?
                    yPosition - 30 * this.connectedButtons.indexOf(lifeKnightButton) - 2 :
                    yPosition + this.connectedButtons.indexOf(lifeKnightButton) * 30 - 2;
        }
    }

    @Override
    public float getXCoordinate() {
        float xCoordinate = this.getRawXPosition();
        float toAddX;
        switch (this.alignment.getValue()) {
            case 0:
                toAddX = 0;
                break;
            case 1:
                toAddX = (int) ((-this.getDefaultWidth() / 2F) + Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.lastString.getValue()) / 2F);
                break;
            default:
                toAddX = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.lastString.getValue()) - this.getDefaultWidth();
                break;
        }
        xCoordinate += toAddX;
        if (xCoordinate + this.getDefaultWidth() > Video.getGameWidth() + 1) {
            xCoordinate = Video.getGameWidth() + 1 - this.getDefaultWidth();
        }

        return Math.max(xCoordinate, 0F);
    }

    private void updateString(String newString) {
        this.lastString.setValue(Text.removeFormattingCodes(newString));
    }

    @Override
    public float getDefaultWidth() {
        float defaultWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.getDisplayText()) + (Core.hudTextShadow.getValue() ? 0.3F : -0.2F);

        if (!Core.hudTextBox.getValue()) return defaultWidth;

        return defaultWidth + 10F;
    }

    @Override
    public float getDefaultHeight() {
        float defaultHeight = 8F + (Core.hudTextShadow.getValue() ? 0.5F : -0.5F);

        if (!Core.hudTextBox.getValue()) return defaultHeight;

        return defaultHeight + 3F;
    }

    @Override
    public float getWidth() {
        return (float) Math.ceil(this.getDefaultWidth() * super.getScale());
    }

    @Override
    public float getHeight() {
        return (float) Math.ceil(this.getDefaultHeight() * super.getScale());
    }

    public void setVisibility(boolean newVisibility) {
        this.hudTextVisible.setValue(newVisibility);
    }

    public void setSeparator(int newSeparatorId) {
        this.separator.setCurrentValue(newSeparatorId);
    }

    public void setPrefixColor(int newPrefixColorId) {
        this.prefixColor.setCurrentValue(newPrefixColorId);
    }

    public void setContentColor(int newContentColorId) {
        this.contentColor.setCurrentValue(newContentColorId);
    }

    public boolean hudTextVisible() {
        return this.hudTextVisible.getValue();
    }
}
