package com.lifeknight.combatanalysis.utilities;

import java.util.ArrayList;

public class Text {

	public static ArrayList<String> returnStartingEntries(ArrayList<String> arrayList, String input) {
		ArrayList<String> result = new ArrayList<>();
		if (!input.equals("") && arrayList != null) {
			for (String element: arrayList) {
				try {
					if (element.toLowerCase().startsWith(input.toLowerCase())) {
						result.add(element);
					}
				} catch (Exception ignored) {

				}
			}
		} else {
			result.addAll(arrayList);
		}
		return result;
	}

	public static String removeAllPunctuation(String text) {
		char[] textAsArray = text.toCharArray();

		StringBuilder editedText = new StringBuilder();

		char[] nonPunctuationChars = {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0','_'};

		for (char character: textAsArray) {
			for (char nonPunctuationChar: nonPunctuationChars) {
				if (character == nonPunctuationChar) {
					editedText.append(character);
				}
			}
		}

		return editedText.toString();
	}

	public static int countWords(String msg) {
		int count = 0;
		for (int x = 0; x < msg.length(); x++) {
			if (msg.charAt(x) == ' ') {
				count++;
			}
		}
		return ++count;
	}
	
	public static String removeAll(String msg, String rmv) {
		msg = msg.replaceAll(rmv, "");
		return msg;
	}

	public static String removeFormattingCodes(String input) {
		String formattingSymbol = "";
		formattingSymbol += '\u00A7';

		input = removeAll(input, formattingSymbol + "0");
		input = removeAll(input, formattingSymbol + "1");
		input = removeAll(input, formattingSymbol + "2");
		input = removeAll(input, formattingSymbol + "3");
		input = removeAll(input, formattingSymbol + "4");
		input = removeAll(input, formattingSymbol + "5");
		input = removeAll(input, formattingSymbol + "6");
		input = removeAll(input, formattingSymbol + "7");
		input = removeAll(input, formattingSymbol + "8");
		input = removeAll(input, formattingSymbol + "9");
		input = removeAll(input, formattingSymbol + "a");
		input = removeAll(input, formattingSymbol + "b");
		input = removeAll(input, formattingSymbol + "c");
		input = removeAll(input, formattingSymbol + "d");
		input = removeAll(input, formattingSymbol + "e");
		input = removeAll(input, formattingSymbol + "f");
		input = removeAll(input, formattingSymbol + "k");
		input = removeAll(input, formattingSymbol + "l");
		input = removeAll(input, formattingSymbol + "m");
		input = removeAll(input, formattingSymbol + "n");
		input = removeAll(input, formattingSymbol + "o");
		input = removeAll(input, formattingSymbol + "r");

		return input;
	}
	
	public static String replaceAllTabooWords(String msg) {
		msg = msg.replaceAll("(?i)fuck", "frick");
		msg = msg.replaceAll("(?i)fuk", "frick");
		msg = msg.replaceAll("(?i)shit", "crap");
		msg = msg.replaceAll("(?i)sh1t", "crap");
		msg = msg.replaceAll("(?i)shlt", "crap");
		msg = msg.replaceAll("(?i)nigger", "black person");
		msg = msg.replaceAll("(?i)nigga", "black person");
		msg = msg.replaceAll("(?i)bitches", "female dogs");
		msg = msg.replaceAll("(?i)b1tches", "female dogs");
		msg = msg.replaceAll("(?i)bltches", "female dogs");
		msg = msg.replaceAll("(?i)bitch3s", "female dogs");
		msg = msg.replaceAll("(?i)b1tch3s", "female dogs");
		msg = msg.replaceAll("(?i)bltch3s", "female dogs");
		msg = msg.replaceAll("(?i)b1tch", "female dog");
		msg = msg.replaceAll("(?i)bltch", "female dog");
		msg = msg.replaceAll("(?i)ass", "donkey");
		msg = msg.replaceAll("(?i)dick", "phallus");
		msg = msg.replaceAll("(?i)d1ck", "phallus");
		msg = msg.replaceAll("(?i)dlck", "phallus");
		msg = msg.replaceAll("(?i)pussy", "vagina");
		msg = msg.replaceAll("(?i)cunt", "vagina");
		
		return msg;
	}

	public static String parseTextToIndexOfTextAfter(String text, String firstIndexText, String secondIndexText) {
        
        if (text.contains(firstIndexText) && text.contains(secondIndexText)) {
			return text.substring((firstIndexText.indexOf(firstIndexText) + firstIndexText.length() + 1), (text.indexOf(secondIndexText) - 1));
        }
        return null;
	}

	public static String shortenDouble(double value, int decimalDigits) {
		String afterDecimal = String.valueOf(value).substring(String.valueOf(value).indexOf(".") + 1);

		if (afterDecimal.contains("E")) {
			return "0.0";
		}

		if (decimalDigits == 0) {
			return String.valueOf(Math.round(value));
		}
		if (afterDecimal.length() <= decimalDigits) {
			return String.valueOf(value);
		} else {
			return String.valueOf(value).substring(0, String.valueOf(value).indexOf(".") + decimalDigits + 1);
		}
	}
}
