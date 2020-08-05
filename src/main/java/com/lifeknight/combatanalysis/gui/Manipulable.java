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
        this.positionX = new LifeKnightNumber.LifeKnightFloat( "Position X", name + " HUD Text", defaultX, 0F, 1920F);
        this.positionY = new LifeKnightNumber.LifeKnightFloat("Position Y", name + " HUD Text", defaultY, 0F, 1080F);
        this.scale = new LifeKnightNumber.LifeKnightFloat("Scale", name + " HUD Text", 1.0F, 0.1F, 5.0F);
        this.positionX.setShowInLifeKnightGui(false);
        this.positionY.setShowInLifeKnightGui(false);
        this.scale.setShowInLifeKnightGui(false);
    }

    public void update(int newX, int newY, float newScale) {
        positionX.setValue(Video.scaleTo1080pWidth(newX));
        positionY.setValue(Video.scaleTo1080pHeight(newY));
        scale.setValue(newScale);
    }

    public float getScale() {
        return scale.getValue();
    }

    public void setScale(float newScale) {
        scale.setValue(newScale);
    }

    public abstract float getDefaultWidth();

    public abstract float getDefaultHeight();

    public float getWidth() {
        return getDefaultWidth() * scale.getValue();
    }

    public float getHeight() {
        return getDefaultHeight() * scale.getValue();
    }

    public float getXCoordinate() {
        float returnValue;
        if ((returnValue = Video.scaleFrom1080pWidth(positionX.getValue()) / scale.getValue()) < -1) {
            returnValue = -1;
            positionX.setValue(returnValue);
        } else if (returnValue + getWidth() > Video.getGameWidth() + 1) {
            returnValue = Video.getGameWidth() + 1 - getWidth();
            positionX.setValue(returnValue);
        }
        return returnValue;
    }

    public float getYCoordinate() {
        float returnValue;
        if ((returnValue = Video.scaleFrom1080pHeight(positionY.getValue()) / scale.getValue()) < -1) {
            returnValue = -1;
            positionY.setValue(returnValue);
        } else if (returnValue + getHeight() > Video.getGameHeight() + 1) {
            returnValue = Video.getGameHeight() + 1 - getHeight();
            positionY.setValue(returnValue);
        }
        return returnValue;
    }

    public float getUncheckedXPosition() {
        return Video.scaleFrom1080pWidth(positionX.getValue()) / scale.getValue();
    }

    public float getUncheckedYPosition() {
        return Video.scaleFrom1080pWidth(positionY.getValue()) / scale.getValue();
    }

    public float getRawXPosition() {
        return Video.scaleFrom1080pWidth(positionX.getValue());
    }

    public float getRawYPosition() {
        return Video.scaleFrom1080pHeight(positionY.getValue());
    }

    public void reset() {
        positionX.reset();
        positionY.reset();
        scale.reset();
    }

    public abstract boolean isVisible();

    public abstract void doRender();
    
    public void render() {
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale.getValue(), scale.getValue(), scale.getValue());
        doRender();
        GlStateManager.popMatrix();
    }

    public abstract void drawButton(Minecraft minecraft, int mouseX, int mouseY, int xPosition, int yPosition, int width, int height, float scale, boolean isSelectedButton);

    public static void renderManipulables() {
        for (Manipulable manipulable : manipulableComponents) {
            if (manipulable.isVisible()) manipulable.render();
        }
    }
}
