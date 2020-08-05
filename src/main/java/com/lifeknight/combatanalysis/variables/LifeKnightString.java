package com.lifeknight.combatanalysis.variables;

import net.minecraft.util.EnumChatFormatting;

import static com.lifeknight.combatanalysis.mod.Mod.configuration;

public class LifeKnightString extends LifeKnightVariable {
    private final String defaultValue;
    private String value;

    public LifeKnightString(String name, String group, String value) {
        super(name, group);
        defaultValue = value;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        if (configuration != null) {
            configuration.updateConfigurationFromVariables();
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

    @Override
    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return name + ": " + EnumChatFormatting.YELLOW + value;
    }
}
