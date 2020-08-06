package com.lifeknight.combatanalysis.gui.hud;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.utilities.Video;
import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import com.lifeknight.combatanalysis.variables.LifeKnightCycle;
import com.lifeknight.combatanalysis.variables.LifeKnightString;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.hudTextShadow;
import static net.minecraft.util.EnumChatFormatting.*;

public abstract class EnhancedHudText extends Manipulable {
    public static final List<EnhancedHudText> textToRender = new ArrayList<>();
    private final String prefix;
    private final LifeKnightBoolean hudTextVisible;
    private final LifeKnightCycle separator;
    private final LifeKnightCycle prefixColor;
    private final LifeKnightCycle contentColor;
    private final LifeKnightCycle alignment;
    private final LifeKnightString lastString;
    public final List<LifeKnightButton> connectedButtons = new ArrayList<>();

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix, LifeKnightCycle separator, LifeKnightCycle prefixColor, LifeKnightCycle contentColor, LifeKnightCycle alignment, LifeKnightBoolean hudTextVisible) {
        super(name, defaultX, defaultY);
        this.prefix = prefix;
        this.hudTextVisible = hudTextVisible;
        this.separator = separator;
        this.prefixColor = prefixColor;
        this.contentColor = contentColor;
        this.alignment = alignment;
        lastString = new LifeKnightString("Last String", name + " HUD Text", getTextToDisplay());
        lastString.setShowInLifeKnightGui(false);
        hudTextVisible.setShowInLifeKnightGui(false);
        separator.setShowInLifeKnightGui(false);
        prefixColor.setShowInLifeKnightGui(false);
        contentColor.setShowInLifeKnightGui(false);
        alignment.setShowInLifeKnightGui(false);

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

            connectedButtons.add(new LifeKnightButton("Prefix Color: " + Miscellaneous.getEnumChatFormatting(prefixColor.getCurrentValueString()) + prefixColor.getCurrentValueString(), 0, 0, 0, 100) {
                @Override
                public void work() {
                    prefixColor.next();
                }

                @Override
                public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                    this.displayString = "Prefix Color: " + Miscellaneous.getEnumChatFormatting(prefixColor.getCurrentValueString()) + prefixColor.getCurrentValueString();
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

        connectedButtons.add(new LifeKnightButton("Content Color: " + Miscellaneous.getEnumChatFormatting(contentColor.getCurrentValueString()) + contentColor.getCurrentValueString(), 0, 0, 0, 100) {
            @Override
            public void work() {
                contentColor.next();
            }

            @Override
            public void drawButton(Minecraft mc, int mouseX, int mouseY) {
                this.displayString = "Content Color: " + Miscellaneous.getEnumChatFormatting(contentColor.getCurrentValueString()) + contentColor.getCurrentValueString();
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

        super.connectedComponents.addAll(connectedButtons);
        textToRender.add(this);
    }

    public EnhancedHudText(String name, int defaultX, int defaultY, String prefix) {
        this(name, defaultX, defaultY, prefix, new LifeKnightCycle(name + " Prefix Color", name + " HUD Text", new ArrayList<>(Arrays.asList(" > ", ": ", " | ", " - "))),
                new LifeKnightCycle("Color", name + " HUD Text", Arrays.asList(
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
                ), 12), new LifeKnightCycle("ContentColor", name + " HUD Text", Arrays.asList(
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
                ), 12), new LifeKnightCycle("Alignment", name + " HUD Text", Arrays.asList(
                        "Left",
                        "Center",
                        "Right"
                )), new LifeKnightBoolean("Visible", name + " HUD Text", true));
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

    public String getDisplayText() {
        if (prefix.isEmpty()) {
            return Miscellaneous.getEnumChatFormatting(contentColor.getCurrentValueString()) + getTextToDisplay();
        } else {
            return Miscellaneous.getEnumChatFormatting(prefixColor.getCurrentValueString()) + prefix + separator.getCurrentValueString() + Miscellaneous.getEnumChatFormatting(contentColor.getCurrentValueString()) + getTextToDisplay();
        }
    }
    
    public abstract boolean isVisible();

    public void doRender() {
        if (Minecraft.getMinecraft().inGameHasFocus && hudTextVisible.getValue() && this.isVisible()) {
            Minecraft.getMinecraft().fontRendererObj.drawString(getDisplayText(), getXCoordinate(), getYCoordinate(), 0xffffffff, hudTextShadow.getValue());
        }
    }

    @Override
    public void update(int newX, int newY, float s) {
        updateString(getDisplayText());
        super.update(newX, newY, s);
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY, int xPosition, int yPosition, int width, int height, float scale, boolean isSelectedButton) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, scale);
        Minecraft.getMinecraft().fontRendererObj.drawString(getDisplayText(), xPosition / scale, (yPosition + 1) / scale, 0xffffffff, hudTextShadow.getValue());
        GlStateManager.popMatrix();
        for (LifeKnightButton lifeKnightButton : connectedButtons) {
            lifeKnightButton.visible = isSelectedButton;
            lifeKnightButton.xPosition = xPosition - 120 < 0 ? xPosition + width + 20 : xPosition - 120;
            lifeKnightButton.yPosition = yPosition + connectedButtons.size() * 30 + 5 > Video.getGameHeight() ?
                    yPosition - 30 * connectedButtons.indexOf(lifeKnightButton) - 2 :
                    yPosition + connectedButtons.indexOf(lifeKnightButton) * 30 - 2;
        }
    }

    @Override
    public float getXCoordinate() {
        float xCoordinate = super.getUncheckedXPosition();
        float toAddX;
        switch (alignment.getValue()) {
            case 0:
                toAddX = 0;
                break;
            case 1:
                toAddX = (int) ((-this.getDefaultWidth() / 2F) + Minecraft.getMinecraft().fontRendererObj.getStringWidth(lastString.getValue()) / 2F);
                break;
            default:
                toAddX = Minecraft.getMinecraft().fontRendererObj.getStringWidth(lastString.getValue()) - this.getDefaultWidth();
                break;
        }
        xCoordinate += toAddX;
        if (xCoordinate + this.getDefaultWidth() > Video.getGameWidth() + 1) {
            xCoordinate = Video.getGameWidth() + 1 - this.getDefaultWidth();
        }

        return Math.max(xCoordinate, 0F);
    }

    public void updateString(String newString) {
        lastString.setValue(Text.removeFormattingCodes(newString));
    }

    @Override
    public float getDefaultWidth() {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(getDisplayText()) + 0.3F;
    }

    @Override
    public float getDefaultHeight() {
        return 8.5F;
    }

    @Override
    public float getWidth() {
        return (float) Math.ceil(getDefaultWidth() * super.getScale());
    }

    @Override
    public float getHeight() {
        return (float) Math.ceil(getDefaultHeight() * super.getScale());
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

    public boolean hudTextVisible() {
        return hudTextVisible.getValue();
    }
}
