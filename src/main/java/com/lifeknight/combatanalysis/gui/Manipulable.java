package com.lifeknight.combatanalysis.gui;

import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.LifeKnightInteger;

import java.util.ArrayList;

public abstract class Manipulable {
    public static ArrayList<Manipulable> manipulableComponents = new ArrayList<>();
    public final LifeKnightInteger positionX;
    public final LifeKnightInteger positionY;
    public Object connectedComponent = null;

    public Manipulable(String name, int defaultX, int defaultY) {
        manipulableComponents.add(this);
        this.positionX = new LifeKnightInteger(name + "PositionX", "Invisible", defaultX, 0, 1920);
        this.positionY = new LifeKnightInteger(name + "PositionY", "Invisible", defaultY, 0, 1080);
    }

    public void updatePosition(int x, int y) {
        positionX.setValue(Miscellaneous.scaleTo1080pWidth(x));
        positionY.setValue(Miscellaneous.scaleTo1080pHeight(y));
    }

    public int getXCoordinate() {
        int returnValue;
        if ((returnValue = Miscellaneous.scaleFrom1080pWidth(positionX.getValue())) < -3) {
            positionX.reset();
            returnValue = Miscellaneous.scaleFrom1080pWidth(positionX.getValue());
        }
        return returnValue;
    }

    public int getYCoordinate() {
        int returnValue;
        if ((returnValue = Miscellaneous.scaleFrom1080pWidth(positionY.getValue())) < -3) {
            positionY.reset();
            returnValue = Miscellaneous.scaleFrom1080pHeight(positionY.getValue());
        }
        return returnValue;
    }

    public void resetPosition() {
        positionX.reset();
        positionY.reset();
    }

    public abstract String getDisplayText();
}
