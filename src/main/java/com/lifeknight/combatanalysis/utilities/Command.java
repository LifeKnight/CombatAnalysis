package com.lifeknight.combatanalysis.utilities;

import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;

import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.MOD_COLOR;
import static net.minecraft.util.EnumChatFormatting.AQUA;

public class Command {

    public static void addToList(String toAdd, List<String> elements, String name) {
        if (!elements.contains(toAdd)) {
            elements.add(toAdd);
            Chat.addSuccessMessage("Successfully added " + toAdd + " to " + name + ".");
        } else {
            Chat.addErrorMessage(name + " already contains " + toAdd + ".");
        }
    }

    public static void removeFromList(String toAdd, List<String> elements, String name) {
        if (elements.contains(toAdd)) {
            elements.remove(toAdd);
            Chat.addSuccessMessage("Successfully removed " + toAdd + " from " + name + ".");
        } else {
            Chat.addErrorMessage(name + " does not contain " + toAdd + ".");
        }
    }

    public static void addListToChat(List<String> elements) {
        Chat.addChatMessageWithoutName(MOD_COLOR + "------------------");
        for (String element: elements) {
            Chat.addChatMessageWithoutName(AQUA + " > " + element);
        }
        Chat.addChatMessageWithoutName(MOD_COLOR + "------------------");
    }

    public static void processListCommand(String[] arguments, List<String> elements, String precedingCommands, String elementType, String name) {
        if (arguments.length > 0) {
            switch (arguments[0].toLowerCase())  {
                case "add": {
                    if (arguments.length > 1) {
                        addToList(arguments[1], elements, name);
                    } else {
                        Chat.addCommandUsageMessage("/" + precedingCommands + " add [" + elementType + "]");
                    }
                    break;
                }
                case "remove": {
                    if (arguments.length > 1) {
                        removeFromList(arguments[1], elements, name);
                    } else {
                        Chat.addCommandUsageMessage("/" + precedingCommands + " remove [" + elementType + "]");
                    }
                    break;
                }
                case "clear": {
                    elements.clear();
                    Chat.addSuccessMessage("Successfully cleared " + name + ".");
                    break;
                }
                case "info": {
                    addListToChat(elements);
                    break;
                }
                default: {
                    Chat.addCommandUsageMessage("/" + precedingCommands + " add, remove, clear, info");
                    break;
                }
            }
        } else {
            Chat.addCommandUsageMessage("/" + precedingCommands + " add, remove, clear, info");
        }
    }

    public static void toggleCommand(LifeKnightBoolean state) {
        state.toggle();
        Chat.addChatMessage(state.getAsString());
    }
}
