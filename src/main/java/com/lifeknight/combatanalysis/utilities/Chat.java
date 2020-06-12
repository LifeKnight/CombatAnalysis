package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.lifeknight.combatanalysis.mod.Core.*;
import static net.minecraft.util.EnumChatFormatting.*;

public enum Chat {

	NORMAL, ALL, PARTY, GUILD, SHOUT, REPLY;

	public static final ArrayList<String> queuedMessages = new ArrayList<>();

	public static void addChatMessage(String msg) {
		if (Minecraft.getMinecraft().theWorld != null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(modColor + "" + EnumChatFormatting.BOLD + modName + " > " + EnumChatFormatting.RESET + msg));
		} else {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					addChatMessage(msg);
				}
			}, 100L);
		}
	}
	public static void addCommandUsageMessage(String msg) {
		addChatMessage(DARK_GREEN + msg);
	}

	public static void addErrorMessage(String msg) {
		addChatMessage(RED + msg);
	}

	public static void addSuccessMessage(String msg) {
		addChatMessage(GREEN + msg);
	}

	public static void addChatMessageWithoutName(String msg) {
		if (Minecraft.getMinecraft().theWorld != null) {
			Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(msg));
		} else {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					addChatMessageWithoutName(msg);
				}
			}, 100L);
		}
	}

	public static void sendChatMessage(String msg, Chat chatType) {
		if (Minecraft.getMinecraft().theWorld != null) {
			String prefix = "";

			switch (chatType) {
				case ALL:
					prefix = "/ac ";
					break;
				case PARTY:
					prefix = "/pc ";
					break;
				case GUILD:
					prefix = "/gc ";
					break;
				case SHOUT:
					prefix = "/shout ";
					break;
				case REPLY:
					prefix = "/r ";
					break;
				default:
					break;
			}

			Minecraft.getMinecraft().thePlayer.sendChatMessage(prefix + msg);
		} else {
			new Timer().schedule(new TimerTask() {
				@Override
				public void run() {
					sendChatMessage(msg, chatType);
				}
			}, 100L);
		}

	}

	public static void queueChatMessageForConnection(String msg) {
		queuedMessages.add(msg);
	}
}
