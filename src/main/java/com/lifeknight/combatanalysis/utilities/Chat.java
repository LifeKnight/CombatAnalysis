package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.lifeknight.combatanalysis.mod.Mod.*;
import static net.minecraft.util.EnumChatFormatting.*;

public enum Chat {
	NORMAL(""),
	ALL("/allchat"),
	PARTY("/partychat"),
	GUILD("/guildchat"),
	SHOUT("/shout"),
	REPLY("/reply");

	private final String prefix;
	Chat(String prefix) {
		this.prefix = prefix;
	}

	public static final List<String> queuedMessages = new ArrayList<>();

	public static void addChatMessage(String msg) {
		if (Minecraft.getMinecraft().thePlayer != null) {
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
		if (Minecraft.getMinecraft().thePlayer != null) {
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
		if (Minecraft.getMinecraft().thePlayer != null) {
			Minecraft.getMinecraft().thePlayer.sendChatMessage(chatType.prefix + msg);
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

	public static void sendQueuedChatMessages() {
		for (String queuedMessage : queuedMessages) {
			Chat.addChatMessage(queuedMessage);
		}
		queuedMessages.clear();
	}
}
