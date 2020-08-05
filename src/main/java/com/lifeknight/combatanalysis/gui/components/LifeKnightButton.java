package com.lifeknight.combatanalysis.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static com.lifeknight.combatanalysis.utilities.Video.get2ndPanelCenter;

public abstract class LifeKnightButton extends GuiButton {
    private final String buttonText;
    public int originalYPosition = 0;

    public LifeKnightButton(int componentId, String buttonText) {
        super(componentId, get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20, buttonText);
        this.buttonText = buttonText;
        int j;
        if ((j = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText) + 30) > this.width) {
            this.width = j;
            this.xPosition = get2ndPanelCenter() - this.width / 2;
        }
        originalYPosition = this.yPosition;
    }

    public LifeKnightButton(int componentId, int x, int y, int width, int height, String buttonText) {
        super(componentId, x, y, width, height, buttonText);
        this.buttonText = buttonText;
    }

    public LifeKnightButton(String buttonText, int componentId, int x, int y, int width) {
        super(componentId, x,
                y,
                width,
                20,
                buttonText);
        this.buttonText = buttonText;
    }

    public LifeKnightButton(String buttonText) {
        super(0, 0, 0, 200, 20, buttonText);
        this.buttonText = buttonText;
    }

    public String getButtonText() {
        return buttonText;
    }

    public void updateOriginalYPosition() {
        originalYPosition = this.yPosition;
    }

    public abstract void work();
}
