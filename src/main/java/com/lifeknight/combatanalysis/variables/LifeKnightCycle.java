package com.lifeknight.combatanalysis.variables;

import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public class LifeKnightCycle extends LifeKnightVariable {
    private final List<String> defaultValues;
    private final int defaultCurrentValue;
    private List<String> values;
    private int currentValue;

    public LifeKnightCycle(String name, String group, List<String> values, int currentValue) {
        super(name, group);
        defaultValues = values;
        defaultCurrentValue = currentValue;
        this.values = values;
        this.currentValue = currentValue;
    }

    public LifeKnightCycle(String name, String group, List<String> values) {
        this(name, group, values, 0);
    }

    public String getCurrentValueString() {
        return currentValue >= 0 && currentValue < values.size() ? values.get(currentValue) : "";
    }

    public Integer getValue() {
        return currentValue;
    }

    public String next() {
        currentValue = currentValue == values.size() - 1 ? 0 : currentValue + 1;
        if (configuration != null) {
            configuration.updateConfigurationFromVariables();
            onValueChange();
        }
        return getCurrentValueString();
    }

    public String previous() {
        currentValue = currentValue == 0 ? values.size() - 1 : currentValue - 1;
        if (configuration != null) {
            configuration.updateConfigurationFromVariables();
            onValueChange();
        }
        return getCurrentValueString();
    }

    @SuppressWarnings("UnusedReturnValue")
    public String setCurrentValue(int newValue) {
        if (!(newValue > values.size() - 1)) {
            currentValue = newValue;
        }
        if (configuration != null) {
            configuration.updateConfigurationFromVariables();
            onSetCurrentValue();
        }
        return getCurrentValueString();
    }

    public void addToValues(String newValue) throws IOException {
        if (values.contains(newValue)) {
            throw new IOException(name + " already contains " + newValue + "!");
        } else {
            values.add(newValue);
            onAddValue();
        }
    }

    public void removeFromValues(String valueToRemove) throws IOException {
        if (values.contains(valueToRemove)) {
            values.remove(valueToRemove);
            onRemoveValue();
        } else {
            throw new IOException(name + " does not contain " + valueToRemove + "!");
        }
    }

    public void clearValues() {
        currentValue = -1;
        values.clear();
        onClearValues();
    }

    public void onValueChange() {}

    public void onSetCurrentValue() {}

    public void onAddValue() {}

    public void onRemoveValue() {}

    public void onClearValues() {}

    @Override
    public void reset() {
        values = defaultValues;
        currentValue = defaultCurrentValue;
    }

    @Override
    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return name + ": " + EnumChatFormatting.YELLOW + getCurrentValueString();
    }
}
