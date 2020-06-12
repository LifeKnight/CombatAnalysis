package com.lifeknight.combatanalysis.variables;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public class LifeKnightString extends LifeKnightVariable {
    private final String defaultValue;
    private String value;

    public LifeKnightString(String name, String group, String value) {
        super(name, group, true);
        defaultValue = value;
        this.value = value;
        getVariables().add(this);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        if (configuration != null) {
            configuration.updateConfigFromVariables();
            onSetValue();
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void clear() {
        this.value = "";
        onClear();
    }

    public void onSetValue() {}

    public void onClear() {}

    @Override
    public void reset() {
        value = defaultValue;
    }
}
