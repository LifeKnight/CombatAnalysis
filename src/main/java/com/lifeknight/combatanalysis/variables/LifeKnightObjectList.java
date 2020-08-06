package com.lifeknight.combatanalysis.variables;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.lifeknight.combatanalysis.utilities.Chat;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public abstract class LifeKnightObjectList extends LifeKnightVariable {
    private final List<LifeKnightObject> defaultValues;
    private List<LifeKnightObject> values = new ArrayList<>();
    private boolean isIndependent = true;

    public LifeKnightObjectList(String name, String group) {
        super(name, group);
        defaultValues = new ArrayList<>();
    }

    public LifeKnightObjectList(String name, String group, List<LifeKnightObject> value) {
        super(name, group);
        defaultValues = value;
    }

    @Override
    public List<LifeKnightObject> getValue() {
        return values;
    }

    public void setValues(List<LifeKnightObject> values) {
        this.values = values;
        configuration.updateConfigurationFromVariables();
        onSetValue();
    }

    public void addElement(LifeKnightObject element) throws IOException {
        if (!values.contains(element)) {
            values.add(element);
            configuration.updateConfigurationFromVariables();
            onAddElement(element);
        } else {
            throw new IOException(name + " already contains " + element + "!");
        }
    }

    public void removeElement(LifeKnightObject element) throws IOException {
        if (element == null) throw new IOException("Cannot remove null element!");
        if (values.contains(element)) {
            values.remove(element);
            configuration.updateConfigurationFromVariables();
            onRemoveElement(element);
        } else {
            throw new IOException(name + " does not contain " + element + "!");
        }
    }

    public void removeByDisplayString(String displayString) {
        for (LifeKnightObject element : values) {
            if (element.getCustomDisplayString().equals(displayString)) {
                try {
                    this.removeElement(element);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
                break;
            }
        }
    }

    public void clear() {
        values.clear();
        configuration.updateConfigurationFromVariables();
        onClear();
    }

    public void setValueFromJsonArray(JsonArray jsonElements) {
        for (JsonElement element : jsonElements) {
            try {
                LifeKnightObject procuredElement = fromString(element.toString());
                values.add(procuredElement);
            } catch (Exception e) {
                Chat.queueChatMessageForConnection(EnumChatFormatting.RED + "An error occurred when trying to parse the value of " +
                        EnumChatFormatting.YELLOW + "\"" + element + ".\"" + EnumChatFormatting.RED + " It will not be added to " + name + ".");
            }

            if (values.size() == 0) {
                values = defaultValues;
            }
        }
    }

    public LifeKnightObject fromString(String string) {
        LifeKnightObject lifeKnightObject = getDefault();
        lifeKnightObject.setValueFromJsonObject(new JsonParser().parse(string).getAsJsonObject());
        return lifeKnightObject;
    }

    public JsonArray toJsonArray() {
        JsonArray jsonArray = new JsonArray();

        for (LifeKnightObject element : values) {
            jsonArray.add(element.getAsJsonObject());
        }

        return jsonArray;
    }

    @Override
    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return "Edit " + name;
    }

    @Override
    public void reset() {
        values = defaultValues;
    }

    public void onAddElement(LifeKnightObject element) {
    }

    public void onRemoveElement(LifeKnightObject element) {
    }

    public void onClear() {
    }

    public void onSetValue() {
    }

    public void setIndependent(boolean isIndependent) {
        this.isIndependent = isIndependent;
    }

    public boolean isIndependent() {
        return isIndependent;
    }

    public abstract LifeKnightObject getDefault();
}
