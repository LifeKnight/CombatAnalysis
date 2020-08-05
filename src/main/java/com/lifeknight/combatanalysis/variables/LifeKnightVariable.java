package com.lifeknight.combatanalysis.variables;

import com.lifeknight.combatanalysis.utilities.Text;

import java.util.ArrayList;
import java.util.List;

public abstract class LifeKnightVariable {
    protected static final List<LifeKnightVariable> variables = new ArrayList<>();
    protected final String name;
    protected final String group;
    private boolean storeValue = true;
    private boolean showInLifeKnightGui = true;
    protected ICustomDisplayString iCustomDisplayString = null;

    public LifeKnightVariable(String name, String group) {
        this.name = name;
        this.group = group;
        variables.add(this);
    }

    public static List<LifeKnightVariable> getVariables() {
        return variables;
    }

    public String getName() {
        return name;
    }

    public String getNameForConfiguration() {
        String modifiedGroup = Text.removeAllPunctuation(name);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = modifiedGroup.length() - 1; i > 0; i--) {
            char toInsert = (modifiedGroup.charAt(i - 1) == Character.toUpperCase(modifiedGroup.charAt(i - 1)) &&
                    i != modifiedGroup.length() - 1 &&
                    modifiedGroup.charAt(i + 1) == Character.toUpperCase(modifiedGroup.charAt(i + 1))) ?
                    Character.toLowerCase(modifiedGroup.charAt(i)) :
                    modifiedGroup.charAt(i);
            stringBuilder.insert(0, toInsert);
        }

        return stringBuilder.insert(0, Character.toLowerCase(modifiedGroup.charAt(0))).toString();
    }

    public String getGroup() {
        return group;
    }

    public String getGroupForConfiguration() {
        String modifiedName = Text.removeAllPunctuation(group);
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = modifiedName.length() - 1; i > 0; i--) {
            char toInsert = (modifiedName.charAt(i - 1) == Character.toUpperCase(modifiedName.charAt(i - 1)) &&
                    i != modifiedName.length() - 1 &&
                    modifiedName.charAt(i + 1) == Character.toUpperCase(modifiedName.charAt(i + 1))) ?
                    Character.toLowerCase(modifiedName.charAt(i)) :
                    modifiedName.charAt(i);
            stringBuilder.insert(0, toInsert);
        }

        return stringBuilder.insert(0, Character.toLowerCase(modifiedName.charAt(0))).toString();
    }

    public abstract Object getValue();

    public abstract void reset();

    public boolean isStoreValue() {
        return storeValue;
    }

    public void setStoreValue(boolean storeValue) {
        this.storeValue = storeValue;
    }

    public boolean showInLifeKnightGui() {
        return showInLifeKnightGui;
    }

    public void setShowInLifeKnightGui(boolean showInLifeKnightGui) {
        this.showInLifeKnightGui = showInLifeKnightGui;
    }

    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return name + ": " + this.getValue();
    }

    public void setiCustomDisplayString(ICustomDisplayString iCustomDisplayString) {
        this.iCustomDisplayString = iCustomDisplayString;
    }

    public interface ICustomDisplayString {
        public String customDisplayString(Object... objects);
    }
}
