package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.variables.LifeKnightVariable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;

import static com.lifeknight.combatanalysis.utilities.Video.get2ndPanelCenter;

public abstract class LifeKnightTextField extends GuiTextField {
    private final int width;
    private final int height;
    public String name;
    public LifeKnightVariable lifeKnightVariable = null;
    public String lastInput = "";
    public int originalYPosition = 0;
    private String subDisplayMessage = "";

    public LifeKnightTextField(int componentId, int x, int y, int par5Width, int par6Height, String name, LifeKnightVariable lifeKnightVariable) {
        this(componentId, x, y, par5Width, par6Height, name);
        this.lifeKnightVariable = lifeKnightVariable;
    }

    public LifeKnightTextField(int componentId, int x, int y, int par5Width, int par6Height, String name) {
        super(componentId, Minecraft.getMinecraft().fontRendererObj, x, y, par5Width, par6Height);
        this.width = par5Width;
        this.height = par6Height;
        this.name = name;
        super.setMaxStringLength(100);
        this.setFocused(false);
        this.setCanLoseFocus(true);
    }

    public LifeKnightTextField(int componentId, LifeKnightVariable lifeKnightVariable) {
        this(componentId, get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20,
                lifeKnightVariable.getCustomDisplayString(),
                lifeKnightVariable);
        originalYPosition = this.yPosition;
    }

    public void drawTextBoxAndName() {
        super.drawTextBox();
        if (lifeKnightVariable != null) {
            name = lifeKnightVariable.getCustomDisplayString();
        }
        super.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, name, this.xPosition + this.width / 2, this.yPosition - 15, 0xffffffff);
    }

    public void drawStringBelowBox() {
        super.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, subDisplayMessage, this.xPosition + this.width / 2, this.yPosition + this.height + 10, 0xffffffff);
    }

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if (super.textboxKeyTyped(p_146201_1_, p_146201_2_)) {
            return true;
        } else if (this.isFocused() && p_146201_2_ == 0x1C) {
            this.handleInput();
            return true;
        }
        return false;
    }

    public void updateOriginalYPosition() {
        originalYPosition = this.yPosition;
    }

    public abstract void handleInput();

    public void setSubDisplayMessage(String subDisplayMessage) {
        this.subDisplayMessage = subDisplayMessage;
    }
}
