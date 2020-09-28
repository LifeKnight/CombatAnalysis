package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;

import static com.lifeknight.combatanalysis.utilities.Video.get2ndPanelCenter;

public abstract class LifeKnightButton extends GuiButton {
    public int originalYPosition = 0;

    public LifeKnightButton(int componentId, String buttonText) {
        super(componentId, Video.get2ndPanelCenter() - 100,
                componentId * 30 + 10,
                200,
                20, buttonText);
        int j;
        if ((j = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText) + 30) > this.width) {
            this.width = j;
            this.xPosition = Video.get2ndPanelCenter() - this.width / 2;
        }
        this.originalYPosition = this.yPosition;
    }

    public LifeKnightButton(int componentId, int x, int y, int width, int height, String buttonText) {
        super(componentId, x, y, width, height, buttonText);
    }

    public LifeKnightButton(String buttonText, int componentId, int x, int y, int width) {
        super(componentId, x,
                y,
                width,
                20,
                buttonText);
    }

    public LifeKnightButton(String buttonText) {
        super(0, 0, 0, 200, 20, buttonText);
    }

    public void updateOriginalYPosition() {
        this.originalYPosition = this.yPosition;
    }

    public abstract void work();

    public static class VersatileLifeKnightButton extends LifeKnightButton {
        public IAction iAction;
        public VersatileLifeKnightButton(String buttonText, IAction iAction) {
            super(0, 0, 0, Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText) + 15, 20, buttonText);
            this.iAction = iAction;
        }

        @Override
        public void work() {
            if (this.iAction != null) this.iAction.work(this);
        }
    }

    public interface IAction {
        void work(VersatileLifeKnightButton versatileLifeKnightButton);
    }
}
