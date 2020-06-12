package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;
import net.minecraft.client.Minecraft;

public class LifeKnightBooleanButton extends LifeKnightButton {
    private final LifeKnightBoolean lifeKnightBoolean;

    public LifeKnightBooleanButton(int componentId, LifeKnightBoolean lifeKnightBoolean, LifeKnightButton connectedButton) {
        super(componentId, lifeKnightBoolean.getAsString());
        this.lifeKnightBoolean = lifeKnightBoolean;
        if (connectedButton != null) {
            connectedButton.xPosition = this.xPosition + this.width + 10;
        }
    }

    public LifeKnightBooleanButton(int componentId, int x, int y, int width, LifeKnightBoolean lifeKnightBoolean) {
        super(lifeKnightBoolean.getAsString(), componentId, x, y, width);
        this.lifeKnightBoolean = lifeKnightBoolean;
    }

    @Override
    public void work() {
        lifeKnightBoolean.toggle();
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        super.displayString = lifeKnightBoolean.getAsString();
        super.drawButton(mc, mouseX, mouseY);
    }
}
