package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.variables.LifeKnightNumber;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;

import static com.lifeknight.combatanalysis.utilities.Video.get2ndPanelCenter;

public class LifeKnightSlider extends GuiSlider {
    private final LifeKnightNumber lifeKnightNumber;
    public int originalYPosition = 0;

    public LifeKnightSlider(int componentId, boolean showDecimals, LifeKnightNumber lifeKnightNumber) {
        super(componentId, get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20, lifeKnightNumber.getCustomDisplayString(), "", lifeKnightNumber.getMinimumAsDouble(), lifeKnightNumber.getMaximumAsDouble(), lifeKnightNumber.getAsDouble(), showDecimals, false);
        this.lifeKnightNumber = lifeKnightNumber;
        this.originalYPosition = this.yPosition;
    }

    @Override
    public void mouseReleased(int par1, int par2) {
        super.mouseReleased(par1, par2);
        this.lifeKnightNumber.setValue(this.getValue());
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        this.minValue = this.lifeKnightNumber.getMinimumAsDouble();
        this.maxValue = this.lifeKnightNumber.getMaximumAsDouble();
        if (!this.dragging) {
            this.sliderValue = (this.lifeKnightNumber.getAsDouble() - this.minValue) / (this.maxValue - this.minValue);
        }
        this.displayString = this.lifeKnightNumber.getCustomDisplayString(this.getValue());
        super.drawButton(mc, mouseX, mouseY);
    }

    public void updateOriginalYPosition() {
        this.originalYPosition = this.yPosition;
    }
}
