package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.CombatSessionGui;
import com.lifeknight.combatanalysis.gui.LifeKnightGui;
import com.lifeknight.combatanalysis.gui.ManipulableGui;
import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.variables.LifeKnightVariable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.lifeknight.combatanalysis.mod.Core.*;

public class ModCommand extends CommandBase {
    private final List<String> aliases = Collections.singletonList("ca");

    public String getCommandName() {
        return MOD_ID;
    }

    public String getCommandUsage(ICommandSender iCommandSender) {
        return MOD_ID;
    }

    public boolean canCommandSenderUseCommand(ICommandSender arg0) {
        return true;
    }

    public List<String> getCommandAliases() {
        return aliases;
    }

    public boolean isUsernameIndex(String[] arguments, int argument1) {
        return false;
    }

    public int compareTo(ICommand o) {
        return 0;
    }

    public void processCommand(ICommandSender iCommandSender, String[] arguments) throws CommandException {
        if (arguments.length == 0) {
            Core.openGui(new LifeKnightGui("[" + MOD_VERSION + "] " + MOD_NAME, LifeKnightVariable.getVariables(), Arrays.asList(
                    new LifeKnightButton("View Sessions") {
                        @Override
                        public void work() {
                            Core.openGui(new CombatSessionGui(CombatSession.getLatestAnalysisForGui()));
                        }
                    },
                    new LifeKnightButton("Edit HUD") {
                        @Override
                        public void work() {
                            Core.openGui(new ManipulableGui());
                        }
                    })));
        }
    }
}
