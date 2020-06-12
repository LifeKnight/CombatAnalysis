package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.variables.LifeKnightDouble;
import com.lifeknight.combatanalysis.variables.LifeKnightInteger;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.client.config.GuiSlider;

import static com.lifeknight.combatanalysis.utilities.Miscellaneous.get2ndPanelCenter;

public class LifeKnightSlider extends GuiSlider {
    private final LifeKnightInteger lifeKnightInteger;
    private final LifeKnightDouble lifeKnightDouble;
    public int originalYPosition = 0;

    public LifeKnightSlider(int componentId, boolean showDecimals, LifeKnightInteger lifeKnightInteger) {
        super(componentId, get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20,  lifeKnightInteger.getName() + ": ", "", lifeKnightInteger.getMinimumValue(), lifeKnightInteger.getMaximumValue(), lifeKnightInteger.getValue(), showDecimals, true);
        this.lifeKnightInteger = lifeKnightInteger;
        lifeKnightDouble = null;
        originalYPosition = this.yPosition;
    }

    public LifeKnightSlider(int componentId, boolean showDecimals, LifeKnightDouble lifeKnightDouble) {
        super(componentId, get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20, lifeKnightDouble.getName() + ": ", "", lifeKnightDouble.getMinimumValue(), lifeKnightDouble.getMaximumValue(), lifeKnightDouble.getValue(), showDecimals, true);
        this.lifeKnightDouble = lifeKnightDouble;
        lifeKnightInteger = null;
    }

    @Override
    public void mouseReleased(int par1, int par2) {
        super.mouseReleased(par1, par2);

        if (lifeKnightInteger == null) {
            this.lifeKnightDouble.setValue(getValue());
        } else {
            this.lifeKnightInteger.setValue(getValueInt());
        }
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (lifeKnightInteger == null) {
            super.minValue = lifeKnightDouble.getMinimumValue();
            super.maxValue = lifeKnightDouble.getMaximumValue();
            super.sliderValue = (lifeKnightDouble.getValue() - minValue) / (maxValue - minValue);
        } else {
            super.minValue = lifeKnightInteger.getMinimumValue();
            super.maxValue = lifeKnightInteger.getMaximumValue();
            super.sliderValue = (lifeKnightInteger.getValue() - minValue) / (maxValue - minValue);
        }
        super.drawButton(mc, mouseX, mouseY);
    }

    public void updateOriginalYPosition() {
        originalYPosition = this.yPosition;
    }
}
