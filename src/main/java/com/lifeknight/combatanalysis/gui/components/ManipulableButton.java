package com.lifeknight.combatanalysis.gui.components;

import com.lifeknight.combatanalysis.gui.Manipulable;
import com.lifeknight.combatanalysis.utilities.Logic;
import com.lifeknight.combatanalysis.utilities.Video;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;

import static com.lifeknight.combatanalysis.gui.ManipulableGui.manipulableButtons;
import static com.lifeknight.combatanalysis.mod.Mod.gridSnapping;

public class ManipulableButton extends GuiButton {
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
                (int) manipulable.getRawXPosition() - 5,
                (int) manipulable.getRawYPosition() - 5,
                (int) manipulable.getWidth() + 10,
                (int) manipulable.getHeight() + 10,
                "");
        this.manipulable = manipulable;
        this.scale = manipulable.getScale();
        scaleButtons = new ScaleButton[]{
                new ScaleButton(0, this.xPosition, this.yPosition) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        if (Math.abs(newXPosition - originalXPosition) > Math.abs(newYPosition - originalYPosition)) {
                            ManipulableButton.this.scale = Math.abs(originalXPosition / (float) newXPosition) * originalScale;
                        } else {
                            ManipulableButton.this.scale = Math.abs(originalYPosition / (float) newYPosition) * originalScale;
                        }

                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * scale) + 10;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * scale) + 10;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition + ManipulableButton.this.originalWidth - ManipulableButton.this.width;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition + ManipulableButton.this.originalHeight - ManipulableButton.this.height;
                    }
                },
                new ScaleButton(1, this.xPosition + this.width - 5, this.yPosition) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        if (Math.abs(newXPosition - originalXPosition) > Math.abs(newYPosition - originalYPosition)) {
                            ManipulableButton.this.scale = Math.abs(newXPosition / (float) originalXPosition) * originalScale;
                        } else {
                            ManipulableButton.this.scale = Math.abs(originalYPosition / (float) newYPosition) * originalScale;
                        }
                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * scale) + 10;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * scale) + 10;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition + ManipulableButton.this.originalHeight - ManipulableButton.this.height;
                    }
                },
                new ScaleButton(2, this.xPosition, this.yPosition + this.height - 5) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        if (Math.abs(newXPosition - originalXPosition) > Math.abs(newYPosition - originalYPosition)) {
                            ManipulableButton.this.scale = Math.abs(originalXPosition / (float) newXPosition) * originalScale;
                        } else {
                            ManipulableButton.this.scale = Math.abs(newYPosition / (float) originalYPosition) * originalScale;
                        }
                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * scale) + 10;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * scale) + 10;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition + ManipulableButton.this.originalWidth - ManipulableButton.this.width;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition;
                    }
                },
                new ScaleButton(2, this.xPosition + this.width - 5, this.yPosition + this.height - 5) {
                    @Override
                    public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {
                        if (Math.abs(newXPosition - originalXPosition) > Math.abs(newYPosition - originalYPosition)) {
                            ManipulableButton.this.scale = Math.abs(newXPosition / (float) originalXPosition) * originalScale;
                        } else {
                            ManipulableButton.this.scale = Math.abs(newYPosition / (float) originalYPosition) * originalScale;
                        }
                        ManipulableButton.this.width = (int) (manipulable.getDefaultWidth() * scale) + 10;
                        ManipulableButton.this.height = (int) (manipulable.getDefaultHeight() * scale) + 10;
                        ManipulableButton.this.xPosition = ManipulableButton.this.originalXPosition;
                        ManipulableButton.this.yPosition = ManipulableButton.this.originalYPosition;
                    }
                }
        };
        fixPosition();
    }

    private int getManipulableWidth() {
        return this.width - 10;
    }

    public int getManipulableHeight() {
        return this.height - 10;
    }

    public int getManipulableXPosition() {
        return this.xPosition + 5;
    }

    public int getManipulableYPosition() {
        return this.yPosition + 5;
    }

    @Override
    public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
        if (this.visible) {
            drawEmptyBox(this.getManipulableXPosition(), this.getManipulableYPosition(), this.getManipulableXPosition() + this.getManipulableWidth(), this.getManipulableYPosition() + this.getManipulableHeight(), isSelectedButton ? 0xeaff0000 : 0xffffffff);

            if (this.isSelectedButton) {
                scaleButtons[0].xPosition = this.xPosition;
                scaleButtons[0].yPosition = this.yPosition;
                scaleButtons[1].xPosition = this.xPosition + this.width - 5;
                scaleButtons[1].yPosition = this.yPosition;
                scaleButtons[2].xPosition = this.xPosition;
                scaleButtons[2].yPosition = this.yPosition + this.height - 5;
                scaleButtons[3].xPosition = this.xPosition + this.width - 5;
                scaleButtons[3].yPosition = this.yPosition + this.height - 5;
                for (ScaleButton scaleButton : scaleButtons) {
                    scaleButton.drawButton(minecraft, mouseX, mouseY);
                }
            }
            this.manipulable.drawButton(minecraft, mouseX, mouseY, this.getManipulableXPosition(), this.getManipulableYPosition(), this.getManipulableWidth(), this.getManipulableHeight(), scale, isSelectedButton);
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
        for (ScaleButton scaleButton : scaleButtons) {
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
            for (ManipulableButton manipulableButton : manipulableButtons) {
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
                this.originalScale = scale;
                return true;
            }
        } else if (isSelectedButton) {
            if (oneOfScaledButtonsPressed(minecraft, mouseX, mouseY)) {
                return true;
            }
            boolean oneOfExtrasClicked = this.oneOfButtonsPressed(minecraft, mouseX, mouseY);
            if (!oneOfExtrasClicked) {
                isSelectedButton = false;
            }
        }
        return false;
    }

    @Override
    public void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
        if (super.visible && this.dragging) {
            int newXPosition = originalXPosition + mouseX - originalMouseXPosition;
            int newYPosition = originalYPosition + mouseY - originalMouseYPosition;

            if (gridSnapping.getValue()) {
                int replacementXPosition = newXPosition;
                int replacementYPosition = newYPosition;
                int toDrawX = Integer.MIN_VALUE;
                int toDrawY = Integer.MIN_VALUE;
                for (ManipulableButton manipulableButton : manipulableButtons) {
                    if (manipulableButton != this) {
                        if (Logic.isWithinRange(newXPosition + this.width, manipulableButton.xPosition + manipulableButton.width, 5)) {
                            replacementXPosition = manipulableButton.xPosition + manipulableButton.width - this.width;
                            toDrawX = replacementXPosition + this.width - 5;
                        }
                        if (Logic.isWithinRange(newXPosition, manipulableButton.xPosition, 5)) {
                            replacementXPosition = manipulableButton.xPosition;
                            toDrawX = replacementXPosition + 5;
                        }
                        if (Logic.isWithinRange(newYPosition + this.height, manipulableButton.yPosition + manipulableButton.height, 5)) {
                            replacementYPosition = manipulableButton.yPosition + manipulableButton.height - this.height;
                            toDrawY = replacementYPosition + this.height - 5;
                        }
                        if (Logic.isWithinRange(newYPosition, manipulableButton.yPosition, 5)) {
                            replacementYPosition = manipulableButton.yPosition;
                            toDrawY = replacementYPosition + 5;
                        }
                    }
                    if (toDrawX != Integer.MIN_VALUE) drawVerticalLine(toDrawX, 0, Video.getGameHeight(), 0xeaff0000);

                    if (toDrawY != Integer.MIN_VALUE) drawHorizontalLine(0, Video.getGameWidth(), toDrawY, 0xeaff0000);
                }
                newXPosition = replacementXPosition;
                newYPosition = replacementYPosition;
            }

            int xIterations = 0;
            if (newXPosition != this.xPosition) {
                int toAddX = newXPosition > this.getManipulableXPosition() ? -1 : 1;
                while (newXPosition < -5 || newXPosition + this.getManipulableWidth() + 5 > Video.getGameWidth() || cannotTranslateToX(newXPosition + 5)) {
                    if (newXPosition < -5) {
                        newXPosition = -5;
                    } else if (newXPosition + this.getManipulableWidth() + 5 > Video.getGameWidth()) {
                        newXPosition = Video.getGameWidth() - this.getManipulableWidth() - 5;
                    } else if (newXPosition + this.getManipulableWidth() + 5 + toAddX > Video.getGameWidth() && !(newXPosition + toAddX < -5)) {
                        newXPosition += toAddX;
                    } else {
                        break;
                    }
                    xIterations++;
                    if (xIterations > 10000) break;
                }
                this.xPosition = newXPosition;
            }

            int yIterations = 0;
            if (newYPosition != this.yPosition) {
                int toAddY = newYPosition > this.getManipulableYPosition() ? -1 : 1;
                while ((newYPosition < -5) || (newYPosition + this.getManipulableHeight() + 5 > Video.getGameHeight()) || cannotTranslateToY(newYPosition + 5)) {
                    if (newYPosition < -5) {
                        newYPosition = -5;
                    } else if (newYPosition + this.getManipulableHeight() + 5 > Video.getGameHeight()) {
                        newYPosition = Video.getGameHeight() - this.getManipulableHeight() - 5;
                    } else if (!(newYPosition + this.getManipulableHeight() + 5 + toAddY > Video.getGameHeight()) && !(newYPosition + toAddY < -6)) {
                        newYPosition += toAddY;
                    } else {
                        break;
                    }
                    yIterations++;
                    if (yIterations > 10000) break;
                }
                this.yPosition = newYPosition;
            }
        }
    }

    private boolean cannotTranslateToX(int newXPosition) {
        for (ManipulableButton manipulableButton : manipulableButtons) {
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
        for (ManipulableButton manipulableButton : manipulableButtons) {
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
        this.originalScale = scale;
        for (ScaleButton scaleButton: scaleButtons) {
            scaleButton.mouseReleased(mouseX, mouseY);
        }

        this.manipulable.update(this.getManipulableXPosition(), this.getManipulableYPosition(), this.scale);
    }

    @Override
    public void playPressSound(SoundHandler soundHandlerIn) {
    }

    public void drawEmptyBox(int left, int top, int right, int bottom, int color) {
        drawHorizontalLine(left, right, top, color);
        drawHorizontalLine(left, right, bottom, color);

        drawVerticalLine(left, top, bottom, color);
        drawVerticalLine(right, top, bottom, color);
    }

    public void reset() {
        this.manipulable.reset();
        this.scale = manipulable.getScale();
        this.xPosition = (int) manipulable.getRawXPosition() - 5;
        this.yPosition = (int) manipulable.getRawYPosition() - 5;
        this.width = (int) manipulable.getWidth() + 10;
        this.height = (int) manipulable.getHeight() + 10;
        fixPosition();
    }

    private void fixPosition() {
        if (this.getManipulableXPosition() < -1) {
            this.xPosition = -6;
        }

        if (this.getManipulableXPosition() + this.getManipulableWidth() - 5 > Video.getGameWidth() + 1) {
            this.xPosition = Video.getGameWidth() - this.getManipulableWidth() + 5;
        }

        if (this.getManipulableYPosition() < -1) {
            this.yPosition = -6;
        }

        if (this.getManipulableYPosition() + this.getManipulableHeight() > Video.getGameHeight() + 1) {
            this.yPosition = Video.getGameHeight() - this.getManipulableHeight() - 5;
        }

        if (cannotTranslateToX(this.getManipulableXPosition())) {
            this.xPosition = -5;
            this.yPosition = -5;
            while (cannotTranslateToX(this.getManipulableXPosition())) {
                if (!(this.getManipulableXPosition() + this.getManipulableWidth() > Video.getGameWidth() + 1)) {
                    this.yPosition++;
                } else if (!(this.getManipulableYPosition() + this.getManipulableHeight() >= Video.getGameHeight())) {
                    this.yPosition++;
                } else {
                    break;
                }
            }
        }

        if (cannotTranslateToY(this.getManipulableYPosition())) {
            this.xPosition = -5;
            this.yPosition = -5;
            while (cannotTranslateToY(this.getManipulableYPosition())) {
                if (!(this.getManipulableYPosition() + this.getManipulableHeight() >= Video.getGameHeight())) {
                    this.xPosition++;
                } else if (!(this.getManipulableXPosition() + this.getManipulableWidth() > Video.getGameWidth() + 1)) {
                    this.xPosition++;
                } else {
                    break;
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
            super(buttonId, x, y, 5, 5, "");
            updateOriginalPosition(0,0);
        }

        @Override
        public void drawButton(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.visible) {
                drawRect(this.xPosition, this.yPosition, this.xPosition + this.width, this.yPosition + this.height, this.dragging ? 0xeaff0000 : 0xffffffff);
                mouseDragged(minecraft, mouseX, mouseY);
            }
        }

        @Override
        public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
            if (super.mousePressed(minecraft, mouseX, mouseY)) {
                dragging = true;
                updateOriginalPosition(mouseX, mouseY);
                return true;
            }
            dragging = false;
            return false;
        }

        @Override
        protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY) {
            if (this.dragging) {
                int newXPosition = originalXPosition + mouseX - originalMouseXPosition;
                int newYPosition = originalYPosition + mouseY - originalMouseYPosition;
                onDrag(newXPosition, newYPosition, originalXPosition, originalYPosition);
            }
        }

        @Override
        public void mouseReleased(int mouseX, int mouseY) {
            dragging = false;
            updateOriginalPosition(mouseX, mouseY);
        }

        @Override
        public void playPressSound(SoundHandler soundHandlerIn) {

        }

        public void onDrag(int newXPosition, int newYPosition, int originalXPosition, int originalYPosition) {

        }

        public void updateOriginalPosition(int mouseX, int mouseY) {
            originalXPosition = this.xPosition;
            originalYPosition = this.yPosition;
            originalMouseXPosition = mouseX;
            originalMouseYPosition = mouseY;
        }
    }
}
