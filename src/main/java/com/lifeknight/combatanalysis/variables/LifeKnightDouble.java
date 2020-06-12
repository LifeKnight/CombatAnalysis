package com.lifeknight.combatanalysis.variables;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public class LifeKnightDouble extends LifeKnightVariable {
    private final double defaultValue;
    private double defaultMaximumValue = Double.MIN_VALUE;
    private double defaultMinimumValue = Double.MAX_VALUE;
    private double value;
    private double minimumValue = Double.MIN_VALUE;
    private double maximumValue = Double.MAX_VALUE;

    public LifeKnightDouble(String name, String group, double value) {
        super(name, group, true);
        defaultValue = value;
        this.value = value;
        getVariables().add(this);
    }

    public LifeKnightDouble(String name, String group, double value, double minimumValue, double maximumValue) {
        this(name, group, value);
        defaultMinimumValue = minimumValue;
        defaultMaximumValue = maximumValue;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
    }
    public LifeKnightDouble(LifeKnightDouble toCopy) {
        this(toCopy.getName(), toCopy.getGroup(), toCopy.getValue(), toCopy.getMinimumValue(), toCopy.getMaximumValue());
    }

    public Double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
        if (configuration != null) {
            configuration.updateConfigFromVariables();
            onSetValue();
        }
    }

    public double getMinimumValue() {
        return minimumValue;
    }

    public double getMaximumValue() {
        return maximumValue;
    }

    public void setMinimumValue(double minimumValue) {
        this.minimumValue = minimumValue;
        onSetMinimumValue();
    }

    public void setMaximumValue(double maximumValue) {
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
