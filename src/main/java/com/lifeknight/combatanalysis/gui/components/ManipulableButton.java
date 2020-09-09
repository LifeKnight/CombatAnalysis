package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.gui.ManipulableGui;
import com.lifeknight.combatanalysis.mod.Core;
import com.lifeknight.combatanalysis.utilities.Logic;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.utilities.Render;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

import java.awt.*;

public class ManipulableButton extends GuiButton {
    private static final int SCALE_BUTTON_SIZE = 5;
    private final Manipulable manipulable;
    private boolean isSelectedButton = false;
    private boolean dragging = false;
    private float scale;
    private float originalScale;
    private final ScaleButton[] scaleButtons;
    private int originalXPosition;
    private int originalYPosition;
    private int originalWidth;
    private int originalHeight;
    private int originalMouseXPosition;
    private int originalMouseYPosition;

    public ManipulableButton(Manipulable manipulable) {
        super(Manipulable.manipulableComponents.indexOf(manipulable),
                (int) manipulable.getRawXPosition() - SCALE_BUTTON_SIZE,
                (int) manipulable.getRawYPosition() - SCALE_BUTTON_SIZE,
                (int) manipulable.getWidth() + SCALE_BUTTON_SIZE * 2,
                (int) manipulable.getHeight() + SCALE_BUTTON_SIZE * 2,
                "");
        this.manipulable = manipulable;
        this.scale = manipulable.getScale();
        this.scaleButtons = new ScaleButton[]{
                new ScaleButton(0, this.xPosition, this.yPosition) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        int xDistance = originalXPosition - newXPosition;

                        ManipulableButton.this.scale = ((float) (ManipulableButton.this.originalWidth + xDistance) / (float) ManipulableButton.this.originalWidth) * ManipulableButton.this.originalScale;

                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition + ManipulableButton.this.originalWidth - ManipulableButton.this.width;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition + ManipulableButton.this.originalHeight - ManipulableButton.this.height;
                    }
                },
                new ScaleButton(1, this.xPosition + this.width - SCALE_BUTTON_SIZE, this.yPosition) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        int xDistance = newXPosition - originalXPosition;

                        ManipulableButton.this.scale = ((float) (ManipulableButton.this.originalWidth + xDistance) / (float) ManipulableButton.this.originalWidth) * ManipulableButton.this.originalScale;

                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition + ManipulableButton.this.originalHeight - ManipulableButton.this.height;
                    }
                },
                new ScaleButton(2, this.xPosition, this.yPosition + this.height - SCALE_BUTTON_SIZE) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        int xDistance = originalXPosition - newXPosition;

                        ManipulableButton.this.scale = ((float) (ManipulableButton.this.originalWidth + xDistance) / (float) ManipulableButton.this.originalWidth) * ManipulableButton.this.originalScale;

                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition + ManipulableButton.this.originalWidth - ManipulableButton.this.width;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition;
                    }
                },
                new ScaleButton(3, this.xPosition + this.width - SCALE_BUTTON_SIZE, this.yPosition + this.height - SCALE_BUTTON_SIZE) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        int xDistance = newXPosition - originalXPosition;

                        ManipulableButton.this.scale = ((float) (ManipulableButton.this.originalWidth + xDistance) / (float) ManipulableButton.this.originalWidth) * ManipulableButton.this.originalScale;

                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * ManipulableButton.this.scale) + SCALE_BUTTON_SIZE * 2;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition;
                    }
                }
        };
        this.fixPosition();
    }

    private int getManipulableWidth() {
        return this.width - SCALE_BUTTON_SIZE * 2;
    }

    public int getManipulableHeight() {
        return this.height - SCALE_BUTTON_SIZE * 2;
    }

    public int getManipulableXPosition() {
        return this.xPosition + SCALE_BUTTON_SIZE;
    }

    public int getManipulableYPosition() {
        return this.yPosition + SCALE_BUTTON_SIZE;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.visible) {
            Render.drawEmptyBox(this.getManipulableXPosition() - 1, this.getManipulableYPosition() - 1, this.getManipulableXPosition() + this.getManipulableWidth() + 1, this.getManipulableYPosition() + this.getManipulableHeight() + 1, this.isSelectedButton ? Color.RED : Color.WHITE, 255F, 1);

            if (this.isSelectedButton) {
                this.scaleButtons[0].xPosition = this.xPosition;
                this.scaleButtons[0].yPosition = this.yPosition;
                this.scaleButtons[1].xPosition = this.xPosition + this.width - SCALE_BUTTON_SIZE;
                this.scaleButtons[1].yPosition = this.yPosition;
                this.scaleButtons[2].xPosition = this.xPosition;
                this.scaleButtons[2].yPosition = this.yPosition + this.height - SCALE_BUTTON_SIZE;
                this.scaleButtons[3].xPosition = this.xPosition + this.width - SCALE_BUTTON_SIZE;
                this.scaleButtons[3].yPosition = this.yPosition + this.height - SCALE_BUTTON_SIZE;
                for (ScaleButton scaleButton : this.scaleButtons) {
                    scaleButton.drawButton(minecraft, mouseX, mouseY);
                }
            }
            this.manipulable.drawButton(minecraft, mouseX, mouseY, this.getManipulableXPosition(), this.getManipulableYPosition(), this.getManipulableWidth(), this.getManipulableHeight(), this.scale, this.isSelectedButton);
            this.mouseDragged(minecraft, mouseX, mouseY);
        }
    }

    public boolean oneOfButtonsPressed(Minecraft minecraft, int mouseX, int mouseY) {
        for (Object component : this.manipulable.connectedComponents) {
            if (component instanceof GuiButton && ((GuiButton) component).mousePressed(minecraft, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    public boolean oneOfScaledButtonsPressed(Minecraft minecraft, int mouseX, int mouseY) {
        for (ScaleButton scaleButton : this.scaleButtons) {
            if (scaleButton.mousePressed(minecraft, mouseX, mouseY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.enabled && this.visible && mouseX >= this.getManipulableXPosition() && mouseY >= this.getManipulableYPosition() && mouseX < this.getManipulableXPosition() + this.getManipulableWidth() && mouseY < this.getManipulableYPosition() + this.getManipulableHeight()) {
            boolean oneOfExtrasClicked = false;
            for (ManipulableButton manipulableButton : ManipulableGui.manipulableButtons) {
                if (manipulableButton != this && manipulableButton.oneOfButtonsPressed(minecraft, mouseX, mouseY)) {
                    oneOfExtrasClicked = true;
                    break;
                }
            }

            if (!oneOfExtrasClicked) {
                this.isSelectedButton = true;
                this.dragging = true;
                this.originalWidth = this.width;
                this.originalHeight = this.height;
                this.originalXPosition = this.xPosition;
                this.originalYPosition = this.yPosition;
                this.originalMouseXPosition = mouseX;
                this.originalMouseYPosition = mouseY;
                this.originalScale = this.scale;
                return true;
            }
        } else if (this.isSelectedButton) {
            if (this.oneOfScaledButtonsPressed(minecraft, mouseX, mouseY)) {
                return true;
            }
            boolean oneOfExtrasClicked = this.oneOfButtonsPressed(minecraft, mouseX, mouseY);
            if (!oneOfExtrasClicked) {
                this.isSelectedButton = false;
            }
        }
        return false;
    }

    @Override
    public void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
        if (super.visible && this.dragging) {
            int newXPosition = this.originalXPosition + mouseX - this.originalMouseXPosition;
            int newYPosition = this.originalYPosition + mouseY - this.originalMouseYPosition;

            if (Core.gridSnapping.getValue()) {
                int replacementXPosition = newXPosition;
                int replacementYPosition = newYPosition;
                int toDrawX = Integer.MIN_VALUE;
                int toDrawY = Integer.MIN_VALUE;
                for (ManipulableButton manipulableButton : ManipulableGui.manipulableButtons) {
                    if (manipulableButton != this) {
                        if (Logic.isWithinRange(newXPosition + this.width, manipulableButton.xPosition + manipulableButton.width, SCALE_BUTTON_SIZE)) {
                            replacementXPosition = manipulableButton.xPosition + manipulableButton.width - this.width;
                            toDrawX = replacementXPosition + this.width - SCALE_BUTTON_SIZE + 1;
                        }
                        if (Logic.isWithinRange(newXPosition, manipulableButton.xPosition, SCALE_BUTTON_SIZE)) {
                            replacementXPosition = manipulableButton.xPosition;
                            toDrawX = replacementXPosition + SCALE_BUTTON_SIZE - 1;
                        }
                        if (Logic.isWithinRange(newYPosition + this.height, manipulableButton.yPosition + manipulableButton.height, SCALE_BUTTON_SIZE)) {
                            replacementYPosition = manipulableButton.yPosition + manipulableButton.height - this.height;
                            toDrawY = replacementYPosition + this.height - SCALE_BUTTON_SIZE + 1;
                        }
                        if (Logic.isWithinRange(newYPosition, manipulableButton.yPosition, SCALE_BUTTON_SIZE)) {
                            replacementYPosition = manipulableButton.yPosition;
                            toDrawY = replacementYPosition + SCALE_BUTTON_SIZE - 1;
                        }
                    }
                    if (toDrawX != Integer.MIN_VALUE && toDrawX != 0 && toDrawX != Video.getGameWidth())
                        Render.drawVerticalLine(toDrawX, 0, Video.getGameHeight(), Color.RED, 255F, 1);

                    if (toDrawY != Integer.MIN_VALUE && toDrawY != 0 && toDrawY != Video.getGameHeight())
                        Render.drawHorizontalLine(toDrawY, 0, Video.getGameWidth(), Color.RED, 255F, 1);
                }
                newXPosition = replacementXPosition;
                newYPosition = replacementYPosition;
            }

            int xIterations = 0;
            if (newXPosition != this.xPosition) {
                int toAddX = newXPosition > this.getManipulableXPosition() ? -1 : 1;
                while (newXPosition < -SCALE_BUTTON_SIZE || newXPosition + this.getManipulableWidth() + SCALE_BUTTON_SIZE > Video.getGameWidth() || this.cannotTranslateToX(newXPosition + SCALE_BUTTON_SIZE)) {
                    if (newXPosition < -SCALE_BUTTON_SIZE) {
                        newXPosition = -SCALE_BUTTON_SIZE;
                    } else if (newXPosition + this.getManipulableWidth() + SCALE_BUTTON_SIZE > Video.getGameWidth()) {
                        newXPosition = Video.getGameWidth() - this.getManipulableWidth() - SCALE_BUTTON_SIZE;
                    } else if (newXPosition + this.getManipulableWidth() + SCALE_BUTTON_SIZE + toAddX > Video.getGameWidth() && !(newXPosition + toAddX < -SCALE_BUTTON_SIZE)) {
                        newXPosition += toAddX;
                    } else {
                        break;
                    }
                    xIterations++;
                    if (xIterations > 10000) {
                        Miscellaneous.logWarn("X-iteration check exceeded 10,000; breaking.");
                        break;
                    }
                }
                this.xPosition = newXPosition;
            }

            int yIterations = 0;
            if (newYPosition != this.yPosition) {
                int toAddY = newYPosition > this.getManipulableYPosition() ? -1 : 1;
                while ((newYPosition < -SCALE_BUTTON_SIZE) || (newYPosition + this.getManipulableHeight() + SCALE_BUTTON_SIZE > Video.getGameHeight()) || cannotTranslateToY(newYPosition + SCALE_BUTTON_SIZE)) {
                    if (newYPosition < -SCALE_BUTTON_SIZE) {
                        newYPosition = -SCALE_BUTTON_SIZE;
                    } else if (newYPosition + this.getManipulableHeight() + SCALE_BUTTON_SIZE > Video.getGameHeight()) {
                        newYPosition = Video.getGameHeight() - this.getManipulableHeight() - SCALE_BUTTON_SIZE;
                    } else if (!(newYPosition + this.getManipulableHeight() + SCALE_BUTTON_SIZE + toAddY > Video.getGameHeight()) && !(newYPosition + toAddY < -6)) {
                        newYPosition += toAddY;
                    } else {
                        break;
                    }
                    yIterations++;
                    if (yIterations > 10000) {
                        Miscellaneous.logWarn("Y-iteration check exceeded 10,000; breaking.");
                        break;
                    }
                }
                this.yPosition = newYPosition;
            }
        }
    }

    private boolean cannotTranslateToX(int newXPosition) {
        for (ManipulableButton manipulableButton : ManipulableGui.manipulableButtons) {
            if (manipulableButton != this) {
                if (((this.getManipulableYPosition() >= manipulableButton.getManipulableYPosition() && this.getManipulableYPosition() <= manipulableButton.getManipulableYPosition() + manipulableButton.getManipulableHeight()) ||
                        (this.getManipulableYPosition() + this.getManipulableHeight() >= manipulableButton.getManipulableYPosition() && this.getManipulableYPosition() + this.getManipulableHeight() <= manipulableButton.getManipulableYPosition() + manipulableButton.getManipulableHeight())) &&
                        !((this.getManipulableXPosition() >= manipulableButton.getManipulableXPosition() && this.getManipulableXPosition() <= manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth()) ||
                                (this.getManipulableXPosition() + this.getManipulableWidth() >= manipulableButton.getManipulableXPosition() && this.getManipulableXPosition() + this.getManipulableWidth() <= manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth()))) {
                    if ((newXPosition >= manipulableButton.getManipulableXPosition() && newXPosition <= manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth()) || (newXPosition <= manipulableButton.getManipulableXPosition() && newXPosition + this.getManipulableWidth() >= manipulableButton.getManipulableXPosition())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean cannotTranslateToY(int newYPosition) {
        for (ManipulableButton manipulableButton : ManipulableGui.manipulableButtons) {
            if (manipulableButton != this) {
                if ((this.getManipulableXPosition() >= manipulableButton.getManipulableXPosition() && this.getManipulableXPosition() <= manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth()) ||
                        (this.getManipulableXPosition() + this.getManipulableWidth() >= manipulableButton.getManipulableXPosition() && this.getManipulableXPosition() + this.getManipulableWidth() <= manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth()) ||
                        (manipulableButton.getManipulableXPosition() >= this.getManipulableXPosition() && manipulableButton.getManipulableXPosition() <= this.getManipulableXPosition() + this.getManipulableWidth()) ||
                        (manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth() >= this.getManipulableXPosition() && manipulableButton.getManipulableXPosition() + manipulableButton.getManipulableWidth() <= this.getManipulableXPosition() + this.getManipulableWidth()) && !((this.getManipulableYPosition() >= manipulableButton.getManipulableYPosition() && this.getManipulableYPosition() <= manipulableButton.getManipulableYPosition() + manipulableButton.getManipulableHeight()) ||
                                (this.getManipulableYPosition() + this.getManipulableHeight() >= manipulableButton.getManipulableYPosition() && this.getManipulableYPosition() + this.getManipulableHeight() <= manipulableButton.getManipulableYPosition() + manipulableButton.getManipulableHeight()))) {
                    if ((newYPosition >= manipulableButton.getManipulableYPosition() && newYPosition <= manipulableButton.getManipulableYPosition() + manipulableButton.getManipulableHeight()) || (newYPosition <= manipulableButton.getManipulableYPosition() && newYPosition + this.getManipulableHeight() >= manipulableButton.getManipulableYPosition())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        this.dragging = false;
        this.originalWidth = this.width;
        this.originalHeight = this.height;
        this.originalXPosition = this.xPosition;
        this.originalYPosition = this.yPosition;
        this.originalMouseXPosition = mouseX;
        this.originalMouseYPosition = mouseY;
        this.originalScale = this.scale;
        for (ScaleButton scaleButton : this.scaleButtons) {
            scaleButton.mouseReleased(mouseX, mouseY);
        }

        this.manipulable.update(this.getManipulableXPosition(), this.getManipulableYPosition(), this.scale);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }

    public void reset() {
        this.manipulable.reset();
        this.scale = manipulable.getScale();
        this.xPosition = (int) manipulable.getRawXPosition() - SCALE_BUTTON_SIZE;
        this.yPosition = (int) manipulable.getRawYPosition() - SCALE_BUTTON_SIZE;
        this.width = (int) manipulable.getWidth() + SCALE_BUTTON_SIZE * 2;
        this.height = (int) manipulable.getHeight() + SCALE_BUTTON_SIZE * 2;
        this.fixPosition();
    }

    private void fixPosition() {
        if (this.getManipulableXPosition() < -1) {
            this.xPosition = -6;
        }

        if (this.getManipulableXPosition() + this.getManipulableWidth() - SCALE_BUTTON_SIZE > Video.getGameWidth() + 1) {
            this.xPosition = Video.getGameWidth() - this.getManipulableWidth() + SCALE_BUTTON_SIZE;
        }

        if (this.getManipulableYPosition() < -1) {
            this.yPosition = -6;
        }

        if (this.getManipulableYPosition() + this.getManipulableHeight() > Video.getGameHeight() + 1) {
            this.yPosition = Video.getGameHeight() - this.getManipulableHeight() - SCALE_BUTTON_SIZE;
        }

        if (this.cannotTranslateToX(this.getManipulableXPosition())) {
            this.xPosition = -SCALE_BUTTON_SIZE;
            this.yPosition = -SCALE_BUTTON_SIZE;
            int iterations = 0;
            while (this.cannotTranslateToX(this.getManipulableXPosition())) {
                this.xPosition++;
                if (this.getManipulableXPosition() + this.getManipulableWidth() > Video.getGameWidth()) {
                    this.xPosition = -SCALE_BUTTON_SIZE;
                    this.yPosition++;
                }
                iterations++;
                if (iterations > 50000) {
                    Miscellaneous.logWarn("Fix Position iterations exceeded SCALE_BUTTON_SIZE0,000; breaking.");
                }
            }
        }

        this.mouseReleased(0, 0);
    }

    private static abstract class ScaleButton extends GuiButton {
        private boolean dragging = false;
        private int originalXPosition;
        private int originalYPosition;
        private int originalMouseXPosition;
        private int originalMouseYPosition;

        public ScaleButton(int buttonId, int x, int y) {
            super(buttonId, x, y, SCALE_BUTTON_SIZE, SCALE_BUTTON_SIZE, "");
            this.updateOriginalPosition(0, 0);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.visible) {
                Render.drawRectangle(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, this.dragging ? Color.RED : Color.WHITE, 255F);
                this.mouseDragged(minecraft, mouseX, mouseY);
            }
        }

        @Override
        public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
            if (super.mousePressed(minecraft, mouseX, mouseY)) {
                this.dragging = true;
                this.updateOriginalPosition(mouseX, mouseY);
                return true;
            }
            this.dragging = false;
            return false;
        }

        @Override
        protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.dragging) {
                int newXPosition = this.originalXPosition + mouseX - this.originalMouseXPosition;
                int newYPosition = this.originalYPosition + mouseY - this.originalMouseYPosition;
                this.onDrag(newXPosition, newYPosition, this.originalXPosition, this.originalYPosition);
            }
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            this.dragging = false;
            this.updateOriginalPosition(mouseX, mouseY);
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {
        }

        protected abstract void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition);

        public void updateOriginalPosition(int mouseX, int mouseY) {
            this.originalXPosition = this.xPosition;
            this.originalYPosition = this.yPosition;
            this.originalMouseXPosition = mouseX;
            this.originalMouseYPosition = mouseY;
        }
    }
}
