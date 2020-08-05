package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
	
	public static String getUsername() {
		return Minecraft.getMinecraft().thePlayer.getName();
	}
	
	public static List<String> getVisiblePlayerList() {
		List<String> playerNames = new ArrayList<>();
		if (Minecraft.getMinecraft().theWorld != null) {
			for (NetworkPlayerInfo playerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
				playerNames.add(playerInfo.getGameProfile().getName());
			}
			Collections.sort(playerNames);
		}
		return playerNames;
	}

	public static String getActualPlayerName(String input) {
		for (String name: getVisiblePlayerList()) {
			if (input.equalsIgnoreCase(name)) {
				return name;
			}
		}

		return input;
	}

	public static List<String> getPlayerList() {
		List<String> playerNames = new ArrayList<>();
		if (Minecraft.getMinecraft().theWorld != null) {
			for (EntityPlayer entityPlayer : Minecraft.getMinecraft().theWorld.playerEntities) {
				playerNames.add(entityPlayer.getName());
			}
		}
		return playerNames;
	}
	
}
