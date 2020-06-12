package com.lifeknight.combatanalysis.mod;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.lifeknight.combatanalysis.utilities.*;
import com.lifeknight.combatanalysis.variables.*;
import net.minecraft.util.EnumChatFormatting;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

import static com.lifeknight.combatanalysis.mod.Core.modId;
import static com.lifeknight.combatanalysis.variables.LifeKnightVariable.variables;

public class Configuration {
	private JsonObject configAsJson = new JsonObject();

	public Configuration() {
		if (configExists()) {
			updateVariablesFromConfig();
		}
		updateConfigFromVariables();
	}

	private void updateVariablesFromConfig() {
		getConfigContent();
		for (LifeKnightVariable variable: variables) {
			if (variable.isStoreValue()) {
				try {
					if (variable instanceof LifeKnightBoolean) {
						((LifeKnightBoolean) variable).setValue(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsBoolean());
					} else if (variable instanceof LifeKnightString) {
						((LifeKnightString) variable).setValue(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsString());
					} else if (variable instanceof LifeKnightInteger) {
						((LifeKnightInteger) variable).setValue(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsInt());
					} else if (variable instanceof LifeKnightDouble) {
						((LifeKnightDouble) variable).setValue(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsDouble());
					} else if (variable instanceof LifeKnightStringList) {
						((LifeKnightStringList) variable).setValueFromCSV(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsString());
					} else if (variable instanceof LifeKnightCycle) {
						((LifeKnightCycle) variable).setCurrentValue(configAsJson.getAsJsonObject(variable.getLowerCaseGroup()).get(variable.getLowerCaseName()).getAsInt());
					}
				} catch (Exception e) {
					e.printStackTrace();
					Chat.queueChatMessageForConnection(EnumChatFormatting.RED + "An error occurred while extracting the value of \"" + variable.getLowerCaseName() + "\" from the config; the value will be interpreted as " + variable.getValue() + ".");
				}
			}
		}
	}

	public void updateConfigFromVariables() {
		JsonObject configAsJsonReplacement = new JsonObject();

		ArrayList<String> groups = new ArrayList<>();

		for (LifeKnightVariable variable: variables) {
			if (!groups.contains(variable.getLowerCaseGroup())) {
				groups.add(variable.getLowerCaseGroup());
			}
		}

		for (String group: groups) {
			JsonObject jsonObject = new JsonObject();
			for (LifeKnightVariable variable: variables) {
				if (variable.isStoreValue() && variable.getLowerCaseGroup().equals(group)) {
					if (variable instanceof LifeKnightBoolean) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightBoolean) variable).getValue());
					} else if (variable instanceof LifeKnightString) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightString) variable).getValue());
					} else if (variable instanceof LifeKnightInteger) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightInteger) variable).getValue());
					} else if (variable instanceof LifeKnightDouble) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightDouble) variable).getValue());
					} else if (variable instanceof LifeKnightStringList) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightStringList) variable).toCSV());
					} else if (variable instanceof LifeKnightCycle) {
						jsonObject.addProperty(variable.getLowerCaseName(), ((LifeKnightCycle) variable).getValue());
					}
				}
			}
			configAsJsonReplacement.add(group, jsonObject);
		}

		configAsJson = configAsJsonReplacement;

		writeToConfig(configAsJson.toString());
	}

	private boolean configExists() {
		try {
			return !new File("config/" + modId + ".cfg").createNewFile();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private void writeToConfig(String text) {
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("config/" + modId + ".cfg"), StandardCharsets.UTF_8));

			writer.write(text);

			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not write in config");
		}
	}

	private void getConfigContent() {
		try {
			Scanner reader = new Scanner(new File("config/" + modId + ".cfg"));
			StringBuilder configContent = new StringBuilder();

			while (reader.hasNextLine()) {
				configContent.append(reader.nextLine());
				configContent.append(System.getProperty("line.separator"));
			}

			reader.close();

			configAsJson = new JsonParser().parse(configContent.toString()).getAsJsonObject();
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Could not read");
		}
	}

	public JsonObject getConfigAsJson() {
		return configAsJson;
	}
}
