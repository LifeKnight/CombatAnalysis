package com.lifeknight.combatanalysis.utilities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

import java.util.ArrayList;
import java.util.Collections;

public class User {
	
	public static String getUsername() {
		return Minecraft.getMinecraft().thePlayer.getName();
	}
	
	public static ArrayList<String> getPlayerList() {
		ArrayList<String> playerNames = new ArrayList<>();
		if (Minecraft.getMinecraft().theWorld != null) {
			for (NetworkPlayerInfo playerInfo : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
				playerNames.add(playerInfo.getGameProfile().getName());
			}
			Collections.sort(playerNames);
		}
		return playerNames;
	}

	public static String getActualPlayerName(String input) {

		for (String name: getPlayerList()) {
			if (input.equalsIgnoreCase(name)) {
				return name;
			}
		}

		return input;
	}
	
}
