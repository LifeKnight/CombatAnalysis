package com.lifeknight.combatanalysis.gui.components;

import net.minecraft.client.Minecraft;

public abstract class ConfirmButton extends LifeKnightButton {
    private boolean hasConfirmed = false;
    private final String buttonText;
    private final String confirmText;
    public ConfirmButton(int componentId, int x, int y, int width, String buttonText, String confirmText) {
        super(buttonText, componentId, x, y, width);
        this.buttonText = buttonText;
        this.confirmText = confirmText;
    }

    public void work() {
        if (hasConfirmed) {
            onConfirm();
            reset();
        } else {
            super.displayString = confirmText;
            hasConfirmed = true;
        }
    }

    public void reset() {
        hasConfirmed = false;
        super.displayString = buttonText;
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean returnValue = super.mousePressed(mc, mouseX, mouseY);
        if (!returnValue) {
            reset();
        }
        return returnValue;
    }

    public abstract void onConfirm();
}
