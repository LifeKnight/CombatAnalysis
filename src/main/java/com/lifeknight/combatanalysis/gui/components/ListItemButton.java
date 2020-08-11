package com.lifeknight.combatanalysis.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

import static com.lifeknight.combatanalysis.utilities.Video.get2ndPanelCenter;

public abstract class ListItemButton extends GuiButton {
    public boolean isSelectedButton = false;
    public int originalYPosition;

    public ListItemButton(int componentId, String element) {
        super(componentId, get2ndPanelCenter() - 100,
                (componentId - 6) * 30 + 10,
                200,
                20, element);
        int j;
        if ((j = Minecraft.getMinecraft().fontRendererObj.getStringWidth(this.displayString) + 30) > this.width) {
            this.width = j;
            this.xPosition = get2ndPanelCenter() - this.width / 2;
        }
        this.originalYPosition = this.yPosition;
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            this.isSelectedButton = true;
            this.work();
            return true;
        } else {
            this.isSelectedButton = false;
            return false;
        }
    }

    public void updateOriginalYPosition() {
        this.originalYPosition = this.yPosition;
    }

    public abstract void work();

    public void drawButton(Minecraft mc, int mouseX, int mouseY)
    {
        if (this.visible)
        {
            FontRenderer fontrenderer = mc.fontRendererObj;
            int color = this.isSelectedButton ? 0xeaff0000 : 0xffffffff;
            drawEmptyBox(this.xPosition, this.yPosition, this.xPosition + super.width, this.yPosition + super.height, color);
            this.mouseDragged(mc, mouseX, mouseY);
            this.drawCenteredString(fontrenderer, this.displayString, this.xPosition + super.width / 2, this.yPosition + (super.height - 8) / 2, 0xffffffff);
        }
    }


    public void drawEmptyBox(int left, int top, int right, int bottom, int color) {
        drawHorizontalLine(left, right, top, color);
        drawHorizontalLine(left, right, bottom, color);

        drawVerticalLine(left, top, bottom, color);
        drawVerticalLine(right, top, bottom, color);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }
}
