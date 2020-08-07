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
        if (name.contains(" ")) {
            String[] words = name.split(" ");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                result.append(Text.formatCapitalization(words[i], i != 0));
            }
            return result.toString();
        }
        return Text.formatCapitalization(name, false);
    }

    public String getGroup() {
        return group;
    }

    public String getGroupForConfiguration() {
        if (group.contains(" ")) {
            String[] words = group.split(" ");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                result.append(Text.formatCapitalization(words[i], i != 0));
            }
            return result.toString();
        }
        return Text.formatCapitalization(group, false);
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
