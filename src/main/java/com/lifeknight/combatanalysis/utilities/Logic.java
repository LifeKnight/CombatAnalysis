package com.lifeknight.combatanalysis.utilities;

import java.util.ArrayList;

public class Logic {
	
	public static boolean containsAny(String text, ArrayList<String> str) {
		for (String txt: str) {
			if (text.contains(txt)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean startsWithAny(String text, ArrayList<String> str) {
		for (String txt: str) {
			if (text.startsWith(txt)) {
				return true;
			}
		}
		return false;
	}


	public static boolean equalsAnyList(String text, ArrayList<String> str) {
		for (String txt: str) {
			if (text.equals(txt)) {
				return true;
			}
		}
		return false;
	}

	public static boolean equalsAnyIgnoreCaseList(String text, ArrayList<String> str) {
		for (String txt: str) {
			if (text.equalsIgnoreCase(txt)) {
				return true;
			}
		}
		return false;
	}

	public static boolean containsLetters(String input) {
		String[] letters = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","1","2","3","4","5","6","7","8","9","0"};

		for (String character: letters) {
			if (input.contains(character)) {
				return true;
			}
		}
		return false;
	}
}
