package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;

public abstract class ScrollBar extends GuiButton {
    public boolean dragging = false;
    public int startY = 0;
    public int originalMouseYPosition = 0;
    public int originalYPosition = 0;

    public ScrollBar() {
        super(-1, Miscellaneous.getGameWidth() - 7, 0, 5, Miscellaneous.getGameHeight(), "");
        this.visible = false;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            Gui.drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, 0xffffffff);
            this.hovered = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            this.mouseDragged(mc, mouseX, mouseY);
        }
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (super.mousePressed(minecraft, mouseX, mouseY)) {
            startY = mouseY;
            dragging = true;
            originalMouseYPosition = mouseY;
            originalYPosition = this.yPosition;
            onMousePress();
            return true;
        } else {
            return false;
        }
    }

    protected abstract void onMousePress();

    public void mouseReleased(int par1, int par2) {
        dragging = false;
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }

    @Override
    public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (super.visible && this.dragging) {
            onDrag(mouseY - originalMouseYPosition);
        }
    }

    public abstract void onDrag(int scroll);
}
