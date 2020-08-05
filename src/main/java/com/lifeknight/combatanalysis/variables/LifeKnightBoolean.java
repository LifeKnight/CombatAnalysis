package com.lifeknight.combatanalysis.variables;


import net.minecraft.util.EnumChatFormatting;

import static com.lifeknight.combatanalysis.mod.Mod.configuration;
import static net.minecraft.util.EnumChatFormatting.GREEN;
import static net.minecraft.util.EnumChatFormatting.RED;

public class LifeKnightBoolean extends LifeKnightVariable {
    private final boolean defaultValue;
    private boolean value;
    private LifeKnightVariable lifeKnightList;

    public LifeKnightBoolean(String name, String group, boolean value) {
        super(name, group);
        this.value = value;
        defaultValue = value;
    }

    public LifeKnightBoolean(String name, String group, boolean value, LifeKnightList<?> lifeKnightList) {
        this(name, group, value);
        this.lifeKnightList = lifeKnightList;
        lifeKnightList.setIndependent(false);
    }

    public LifeKnightBoolean(String name, String group, boolean value, LifeKnightObjectList lifeKnightObjectList) {
        this(name, group, value);
        this.lifeKnightList = lifeKnightObjectList;
        lifeKnightObjectList.setIndependent(false);
    }

    public Boolean getDefaultValue() {
        return defaultValue;
    }

    public boolean hasList() {
        return lifeKnightList != null;
    }

    public LifeKnightVariable getList() {
        return lifeKnightList;
    }

    public Boolean getValue() {
        return value;
    }

    public void toggle() {
        value = !value;
        configuration.updateConfigurationFromVariables();
        onSetValue();
    }

    public void setValue(boolean newValue) {
        value = newValue;
        if (configuration != null) {
            configuration.updateConfigurationFromVariables();
            onSetValue();
        }
    }

    public String getAsString() {
        if (value) {
            return super.getName() + ": " + GREEN + "ENABLED";
        } else {
            return super.getName() + ": " + RED + "DISABLED";
        }
    }

    public void onSetValue() {}

    @Override
    public void reset() {
        value = defaultValue;
    }

    @Override
    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return name + ": " + (value ? EnumChatFormatting.GREEN + "ENABLED" : EnumChatFormatting.RED + "DISABLED");
    }
}
