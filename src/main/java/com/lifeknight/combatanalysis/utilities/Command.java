package com.lifeknight.combatanalysis.utilities;

import com.lifeknight.combatanalysis.variables.LifeKnightBoolean;

import java.util.ArrayList;

import static com.lifeknight.combatanalysis.mod.Core.modColor;
import static net.minecraft.util.EnumChatFormatting.*;

public class Command {

    public static void addToList(String toAdd, ArrayList<String> elements, String name) {
        if (!elements.contains(toAdd)) {
            elements.add(toAdd);
            Chat.addSuccessMessage("Successfully added " + toAdd + " to " + name + ".");
        } else {
            Chat.addErrorMessage(name + " already contains " + toAdd + ".");
        }
    }

    public static void removeFromList(String toAdd, ArrayList<String> elements, String name) {
        if (elements.contains(toAdd)) {
            elements.remove(toAdd);
            Chat.addSuccessMessage("Successfully removed " + toAdd + " from " + name + ".");
        } else {
            Chat.addErrorMessage(name + " does not contain " + toAdd + ".");
        }
    }

    public static void addListToChat(ArrayList<String> elements) {
        Chat.addChatMessageWithoutName(modColor + "------------------");

        for (String element: elements) {
            Chat.addChatMessageWithoutName(AQUA + "> " + element);
        }

        Chat.addChatMessageWithoutName(modColor + "------------------");
    }

    public static void processListCommand(String[] args, ArrayList<String> elements, String precedingCommands, String elementType, String name) {
        if (args.length > 0) {
            switch (args[0].toLowerCase())  {
                case "add": {
                    if (args.length > 1) {
                        addToList(args[1], elements, name);
                    } else {
                        Chat.addCommandUsageMessage("/" + precedingCommands + " add [" + elementType + "]");
                    }
                    break;
                }
                case "remove": {
                    if (args.length > 1) {
                        removeFromList(args[1], elements, name);
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
