package com.lifeknight.combatanalysis.mod;

import com.lifeknight.combatanalysis.gui.LifeKnightGui;
import com.lifeknight.combatanalysis.gui.ManipulableGui;
import com.lifeknight.combatanalysis.gui.components.LifeKnightButton;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Text;
import com.lifeknight.combatanalysis.variables.LifeKnightVariable;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


import static com.lifeknight.combatanalysis.mod.Core.*;
import static net.minecraft.util.EnumChatFormatting.*;

public class ModCommand extends CommandBase {
	private final List<String> aliases = Collections.singletonList("mb");
	private final String[] mainCommands = {};

	public String getCommandName() {
		return modId;
	}

	public String getCommandUsage(ICommandSender arg0) {
		return modId;
	}

	public List<String> addTabCompletionOptions(ICommandSender arg0, String[] arg1, BlockPos arg2) {

		if (arg1.length >= 1) {
			return Text.returnStartingEntries(new ArrayList<>(Arrays.asList(mainCommands)), arg1[0]);
		}

		return new ArrayList<>(Arrays.asList(mainCommands));
	}

	public boolean canCommandSenderUseCommand(ICommandSender arg0) {
		return true;
	}

	public List<String> getCommandAliases() {
		return aliases;
	}

	public boolean isUsernameIndex(String[] arg0, int arg1) {
		return false;
	}

	public int compareTo(ICommand o) {
		return 0;
	}

	public void processCommand(ICommandSender arg0, String[] arg1) throws CommandException {
		openGui(new LifeKnightGui("[" + modVersion + "] " + modName, LifeKnightVariable.getVariables(), new ArrayList<>(
				Collections.singletonList(
					new LifeKnightButton("Edit HUD") {
			@Override
			public void work() {
				openGui(new ManipulableGui());
			}
		}))));
	}

	public void addMainCommandMessage() {
		StringBuilder result = new StringBuilder(DARK_GREEN + "/" + modId);

		for (String command: mainCommands) {
			result.append(" ").append(command).append(",");
		}

		Chat.addChatMessage(result.substring(0, result.length() - 1));
	}
}
