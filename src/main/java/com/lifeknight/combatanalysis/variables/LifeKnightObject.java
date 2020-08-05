package com.lifeknight.combatanalysis.variables;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

public class LifeKnightObject extends LifeKnightVariable {
    protected final List<LifeKnightVariable> connectedVariables = new ArrayList<>();

    public LifeKnightObject(String name, String group) {
        super(name, group);
    }

    @Override
    public Object getValue() {
        return this;
    }

    @Override
    public void reset() {

    }

    @Override
    public String getCustomDisplayString() {
        return null;
    }

    public List<LifeKnightVariable> getConnectedVariables() {
        return connectedVariables;
    }

    public LifeKnightBoolean createBoolean(String name, String group, boolean value) {
        LifeKnightBoolean lifeKnightBoolean = new LifeKnightBoolean(name, group, value);
        lifeKnightBoolean.setShowInLifeKnightGui(false);
        lifeKnightBoolean.setStoreValue(false);
        connectedVariables.add(lifeKnightBoolean);
        return lifeKnightBoolean;
    }

    public LifeKnightNumber createNumber(String name, String group, Number value) {
        LifeKnightNumber lifeKnightNumber;
        if (value instanceof Integer) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightInteger(name, group, (Integer) value);
        } else if (value instanceof Double) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightDouble(name, group, (Double) value);
        } else if (value instanceof Long) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightLong(name, group, (Long) value);
        } else {
            lifeKnightNumber = null;
        }
        if (lifeKnightNumber != null) {
            lifeKnightNumber.setShowInLifeKnightGui(false);
            lifeKnightNumber.setStoreValue(false);
        }
        connectedVariables.add(lifeKnightNumber);
        return lifeKnightNumber;
    }

    public LifeKnightNumber createNumber(String name, String group, Number value, Number minimumValue, Number maximumValue) {
        LifeKnightNumber lifeKnightNumber;
        if (value instanceof Integer) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightInteger(name, group, (Integer) value, (Integer) minimumValue, (Integer) maximumValue);
        } else if (value instanceof Double) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightDouble(name, group, (Double) value, (Double) minimumValue, (Double) maximumValue);
        } else if (value instanceof Long) {
            lifeKnightNumber = new LifeKnightNumber.LifeKnightLong(name, group, (Long) value, (Long) minimumValue, (Long) maximumValue);
        } else {
            throw new IllegalArgumentException("Invalid parameters!");
        }
        lifeKnightNumber.setShowInLifeKnightGui(false);
        lifeKnightNumber.setStoreValue(false);
        connectedVariables.add(lifeKnightNumber);
        return lifeKnightNumber;
    }

    public LifeKnightString createString(String name, String group, String value) {
        LifeKnightString lifeKnightString = new LifeKnightString(name, group, value);
        lifeKnightString.setShowInLifeKnightGui(false);
        lifeKnightString.setStoreValue(false);
        connectedVariables.add(lifeKnightString);
        return lifeKnightString;
    }

    public LifeKnightCycle createCycle(String name, String group, List<String> elements, int startingIndex) {
        LifeKnightCycle lifeKnightCycle = new LifeKnightCycle(name, group, elements, startingIndex);
        lifeKnightCycle.setShowInLifeKnightGui(false);
        lifeKnightCycle.setStoreValue(false);
        connectedVariables.add(lifeKnightCycle);
        return lifeKnightCycle;
    }

    public boolean isSearchResult(String search) {
        return this.name.toLowerCase().contains(search.toLowerCase());
    }

    public JsonObject getAsJsonObject() {
        JsonObject asJsonObject = new JsonObject();

        List<String> groups = new ArrayList<>();

        for (LifeKnightVariable variable : this.connectedVariables) {
            if (!groups.contains(variable.getGroupForConfiguration())) {
                groups.add(variable.getGroupForConfiguration());
            }
        }

        for (String group : groups) {
            JsonObject jsonObject = new JsonObject();
            for (LifeKnightVariable variable : this.connectedVariables) {
                if (variable.getGroupForConfiguration().equals(group)) {
                    if (variable instanceof LifeKnightBoolean) {
                        jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightBoolean) variable).getValue()));
                    } else if (variable instanceof LifeKnightString) {
                        jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightString) variable).getValue()));
                    } else if (variable instanceof LifeKnightNumber) {
                        jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightNumber) variable).getValue()));
                    } else if (variable instanceof LifeKnightList<?>) {
                        jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightList<?>) variable).toJsonArray());
                    } else if (variable instanceof LifeKnightCycle) {
                        jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightCycle) variable).getValue()));
                    } else if (variable instanceof LifeKnightObject) {
                        jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightObject) variable).getAsJsonObject());
                    } else if (variable instanceof LifeKnightObjectList) {
                        jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightObjectList) variable).toJsonArray());
                    }
                }
            }
            asJsonObject.add(group, jsonObject);
        }
        return asJsonObject;
    }

    public void setValueFromJsonObject(JsonObject jsonObject) {
        for (LifeKnightVariable variable : connectedVariables) {
            if (variable instanceof LifeKnightBoolean) {
                ((LifeKnightBoolean) variable).setValue(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsBoolean());
            } else if (variable instanceof LifeKnightString) {
                ((LifeKnightString) variable).setValue(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsString());
            } else if (variable instanceof LifeKnightNumber) {
                ((LifeKnightNumber) variable).setValue(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsNumber());
            } else if (variable instanceof LifeKnightList<?>) {
                ((LifeKnightList<?>) variable).setValueFromJsonArray(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonArray());
            } else if (variable instanceof LifeKnightCycle) {
                ((LifeKnightCycle) variable).setCurrentValue(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsInt());
            } else if (variable instanceof LifeKnightObject) {
                ((LifeKnightObject) variable).setValueFromJsonObject(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonObject());
            } else if (variable instanceof LifeKnightObjectList) {
                ((LifeKnightObjectList)variable).setValueFromJsonArray(jsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonArray());
            }
        }
    }

    public static LifeKnightObject getDefault() {
        LifeKnightObject lifeKnightObject = new LifeKnightObject("Default Name", "Default Group");
        lifeKnightObject.setStoreValue(false);
        lifeKnightObject.setShowInLifeKnightGui(false);
        return lifeKnightObject;
    }
}
