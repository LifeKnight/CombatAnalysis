package com.lifeknight.combatanalysis.variables;

import java.util.ArrayList;

public abstract class LifeKnightVariable {
    public static final ArrayList<LifeKnightVariable> variables = new ArrayList<>();
    private final String name;
    private final String group;
    private boolean storeValue;

    public LifeKnightVariable(String name, String group, boolean storeValue) {
        this.name = name;
        this.group = group;
        this.storeValue = storeValue;
    }

    public static ArrayList<LifeKnightVariable> getVariables() {
        return variables;
    }

    public String getName() {
        return name;
    }

    public String getLowerCaseName() {
        return name.toLowerCase();
    }

    public String getGroup() {
        return group;
    }

    public String getLowerCaseGroup() {
        return group.toLowerCase();
    }

    public abstract Object getValue();

    public abstract void reset();

    public boolean isStoreValue() {
        return storeValue;
    }

    public void setStoreValue(boolean storeValue) {
        this.storeValue = storeValue;
    }
}
