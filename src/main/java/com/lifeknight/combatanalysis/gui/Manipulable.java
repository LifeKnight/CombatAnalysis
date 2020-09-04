package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.utilities.Video;
import com.lifeknight.combatanalysis.variables.LifeKnightNumber;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public abstract class Manipulable {
    public static final List<Manipulable> manipulableComponents = new ArrayList<>();
    public final List<Object> connectedComponents = new ArrayList<>();
    private final LifeKnightNumber.LifeKnightFloat positionX;
    private final LifeKnightNumber.LifeKnightFloat positionY;
    private final LifeKnightNumber.LifeKnightFloat scale;

    public Manipulable(String name, float defaultX, float defaultY) {
        manipulableComponents.add(this);
        this.positionX = new LifeKnightNumber.LifeKnightFloat( "Position X", name, defaultX, 0F, 1920F);
        this.positionY = new LifeKnightNumber.LifeKnightFloat("Position Y", name, defaultY, 0F, 1080F);
        this.scale = new LifeKnightNumber.LifeKnightFloat("Scale", name, 1.0F, 0.1F, 5.0F);
        this.positionX.setShowInLifeKnightGui(false);
        this.positionY.setShowInLifeKnightGui(false);
        this.scale.setShowInLifeKnightGui(false);
    }

    public void update(int newX, int newY, float newScale) {
        this.positionX.setValue(Video.scaleTo1080pWidth(newX));
        this.positionY.setValue(Video.scaleTo1080pHeight(newY));
        this.scale.setValue(newScale);
    }

    public float getScale() {
        return this.scale.getValue();
    }

    public void setScale(float newScale) {
        this.scale.setValue(newScale);
    }

    public abstract float getDefaultWidth();

    public abstract float getDefaultHeight();

    public float getWidth() {
        return this.getDefaultWidth() * this.scale.getValue();
    }

    public float getHeight() {
        return this.getDefaultHeight() * this.scale.getValue();
    }

    public float getXCoordinate() {
        float returnValue;
        if ((returnValue = Video.scaleFrom1080pWidth(this.positionX.getValue()) / this.scale.getValue()) < -1) {
            returnValue = -1;
            this.positionX.setValue(returnValue);
        } else if (returnValue + getWidth() > Video.getGameWidth() + 1) {
            returnValue = Video.getGameWidth() + 1 - getWidth();
            this.positionX.setValue(returnValue);
        }
        return returnValue;
    }

    public float getYCoordinate() {
        float returnValue;
        if ((returnValue = Video.scaleFrom1080pHeight(this.positionY.getValue()) / this.scale.getValue()) < -1) {
            returnValue = -1;
            this.positionY.setValue(returnValue);
        } else if (returnValue + getHeight() > Video.getGameHeight() + 1) {
            returnValue = Video.getGameHeight() + 1 - getHeight();
            this.positionY.setValue(returnValue);
        }
        return returnValue;
    }

    public float getUncheckedXPosition() {
        return Video.scaleFrom1080pWidth(this.positionX.getValue()) / this.scale.getValue();
    }

    public float getUncheckedYPosition() {
        return Video.scaleFrom1080pWidth(this.positionY.getValue()) / this.scale.getValue();
    }

    public float getRawXPosition() {
        return Video.scaleFrom1080pWidth(this.positionX.getValue());
    }

    public float getRawYPosition() {
        return Video.scaleFrom1080pHeight(this.positionY.getValue());
    }

    public void reset() {
        this.positionX.reset();
        this.positionY.reset();
        this.scale.reset();
    }

    public abstract boolean isVisible();

    public abstract void doRender();

    public void render() {
        this.doRender();
    }

    public abstract void drawButton(Minecraft minecraft, int mouseX, int mouseY, int xPosition, int yPosition, int width, int height, float scale, boolean isSelectedButton);

    public static void renderManipulables() {
        for (Manipulable manipulable : manipulableComponents) {
            if (manipulable.isVisible()) manipulable.render();
        }
    }
}
