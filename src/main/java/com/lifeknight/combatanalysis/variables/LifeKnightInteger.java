package com.lifeknight.combatanalysis.variables;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public class LifeKnightInteger extends LifeKnightVariable {
    private final int defaultValue;
    private int defaultMaximumValue = Integer.MIN_VALUE;
    private int defaultMinimumValue = Integer.MAX_VALUE;
    private int value;
    private int minimumValue = Integer.MIN_VALUE;
    private int maximumValue = Integer.MAX_VALUE;

    public LifeKnightInteger(String name, String group, int value) {
        super(name, group, true);
        defaultValue = value;
        this.value = value;
        getVariables().add(this);
    }

    public LifeKnightInteger(String name, String group, int value, int minimumValue, int maximumValue) {
        this(name, group, value);
        defaultMinimumValue = minimumValue;
        defaultMaximumValue = maximumValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }

    public LifeKnightInteger(LifeKnightInteger toCopy) {
        this(toCopy.getName(), toCopy.getGroup(), toCopy.getValue(), toCopy.getMinimumValue(), toCopy.getMaximumValue());
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(int newValue) {
        value = newValue;
        if (configuration != null) {
            configuration.updateConfigFromVariables();
            onSetValue();
        }
    }


    public int getMinimumValue() {
        return minimumValue;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
        onSetMinimumValue();
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
        onSetMaximumValue();
    }

    public void onSetValue() {}

    public void onSetMinimumValue() {}

    public void onSetMaximumValue() {}

    @Override
    public void reset() {
        value = defaultValue;
        maximumValue = defaultMaximumValue;
        minimumValue = defaultMinimumValue;
    }
}
