package com.lifeknight.combatanalysis.variables;

import com.google.common.collect.HashBiMap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.lifeknight.combatanalysis.utilities.Chat;
import net.minecraft.util.EnumChatFormatting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.configuration;

public abstract class LifeKnightList<E> extends LifeKnightVariable {
    private final List<E> defaultValues;
    private List<E> values = new ArrayList<>();
    private final HashBiMap<E, String> eToDisplayedStringMap = HashBiMap.create();
    private boolean isIndependent = true;

    public LifeKnightList(String name, String group, List<E> values) {
        super(name, group);
        defaultValues = values;
    }

    public LifeKnightList(String name, String group) {
        this(name, group, new ArrayList<>());
    }

    @Override
    public List<E> getValue() {
        return values;
    }

    public void setValue(List<E> values) {
        this.values = values;
        eToDisplayedStringMap.clear();
        for (E element : values) {
            eToDisplayedStringMap.put(element, asDisplayString(element));
        }
        configuration.updateConfigurationFromVariables();
        onSetValue();
    }

    public void addElement(E element) throws IOException {
        if (!values.contains(element)) {
            values.add(element);
            eToDisplayedStringMap.put(element, asDisplayString(element));
            configuration.updateConfigurationFromVariables();
            onAddElement(element);
        } else {
            throw new IOException(name + " already contains " + element + "!");
        }
    }

    public void removeElement(E element) throws IOException {
        if (element == null) throw new IOException("Cannot remove null element!");
        if (values.contains(element)) {
            values.remove(element);
            eToDisplayedStringMap.remove(element);
            configuration.updateConfigurationFromVariables();
            onRemoveElement(element);
        } else {
            throw new IOException(name + " does not contain " + element + "!");
        }
    }

    public void clear() {
        values.clear();
        eToDisplayedStringMap.clear();
        configuration.updateConfigurationFromVariables();
        onClear();
    }

    public void setValueFromJsonArray(JsonArray jsonArray) {
        for (JsonElement element : jsonArray) {
            try {
                E procuredElement = fromString(element.getAsString());
                values.add(procuredElement);
                eToDisplayedStringMap.put(procuredElement, asDisplayString(procuredElement));
            } catch (Exception e) {
                Chat.queueChatMessageForConnection(EnumChatFormatting.RED + "An error occurred when trying to parse the value of " +
                        EnumChatFormatting.YELLOW + "\"" + element + ".\"" + EnumChatFormatting.RED + " It will not be added to " + name + ".");
            }
        }
        if (values.size() == 0) {
            values = defaultValues;
            for (E element : values) {
                eToDisplayedStringMap.put(element, asDisplayString(element));
            }
        }
    }

    public abstract E fromString(String string);

    public JsonArray toJsonArray() {
        JsonArray asJsonArray = new JsonArray();

        for (E element : values) {
            asJsonArray.add(new JsonPrimitive(asString(element)));
        }
        return asJsonArray;
    }

    public abstract String asString(E element);

    public E fromFormattedString(String formattedString) {
        return null;
    }

    public String asDisplayString(E element) {
        return element.toString();
    }

    public HashBiMap<E, String> getMap() {
        return eToDisplayedStringMap;
    }

    @Override
    public void reset() {
        values = defaultValues;
    }

    public void onAddElement(E element) {
    }

    public void onRemoveElement(E element) {
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

    @Override
    public String getCustomDisplayString() {
        if (iCustomDisplayString != null) {
            return iCustomDisplayString.customDisplayString();
        }
        return "Edit " + name;
    }

    public static class LifeKnightStringList extends LifeKnightList<String> {

        public LifeKnightStringList(String name, String group, List<String> values) {
            super(name, group, values);
        }

        public LifeKnightStringList(String name, String group) {
            super(name, group);
        }

        @Override
        public String fromString(String string) {
            return string;
        }

        @Override
        public String asString(String element) {
            return element;
        }

        @Override
        public String fromFormattedString(String formattedString) {
            return formattedString;
        }
    }

    public static class LifeKnightIntegerList extends LifeKnightList<Integer> {

        public LifeKnightIntegerList(String name, String group, List<Integer> values) {
            super(name, group, values);
        }

        public LifeKnightIntegerList(String name, String group) {
            super(name, group);
        }

        @Override
        public Integer fromString(String string) {
            return Integer.parseInt(string);
        }

        @Override
        public String asString(Integer element) {
            return String.valueOf(element);
        }

        @Override
        public Integer fromFormattedString(String formattedString) {
            return Integer.parseInt(formattedString);
        }
    }

    public static class LifeKnightDoubleList extends LifeKnightList<Double> {

        public LifeKnightDoubleList(String name, String group, List<Double> values) {
            super(name, group, values);
        }

        public LifeKnightDoubleList(String name, String group) {
            super(name, group);
        }

        @Override
        public Double fromString(String string) {
            return Double.parseDouble(string);
        }

        @Override
        public String asString(Double element) {
            return String.valueOf(element);
        }

        @Override
        public Double fromFormattedString(String formattedString) {
            return Double.parseDouble(formattedString);
        }
    }
}
