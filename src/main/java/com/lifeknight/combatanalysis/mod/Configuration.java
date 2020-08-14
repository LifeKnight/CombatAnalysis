package com.lifeknight.combatanalysis.mod;

import com.google.gson.*;
import com.lifeknight.combatanalysis.utilities.Chat;
import com.lifeknight.combatanalysis.utilities.Miscellaneous;
import com.lifeknight.combatanalysis.variables.*;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.lifeknight.combatanalysis.mod.Core.MOD_ID;
import static com.lifeknight.combatanalysis.mod.Core.MOD_NAME;

public class Configuration {
	private JsonObject configurationAsJsonObject = new JsonObject();
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public Configuration() {
		if (configExists()) {
			this.updateVariablesFromConfiguration();
		}
		this.updateConfigurationFromVariables();
	}

	private void updateVariablesFromConfiguration() {
		this.getConfigurationContent();
		for (int i = 0; i < LifeKnightVariable.getVariables().size(); i++) {
			LifeKnightVariable variable = LifeKnightVariable.getVariables().get(i);
			if (variable.isStoreValue()) {
				try {
					if (variable instanceof LifeKnightBoolean) {
						((LifeKnightBoolean) variable).setValue(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsBoolean());
					} else if (variable instanceof LifeKnightString) {
						((LifeKnightString) variable).setValue(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsString());
					} else if (variable instanceof LifeKnightNumber) {
						((LifeKnightNumber) variable).setValue(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsNumber());
					} else if (variable instanceof LifeKnightList<?>) {
						((LifeKnightList<?>) variable).setValueFromJsonArray(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonArray());
					} else if (variable instanceof LifeKnightCycle) {
						((LifeKnightCycle) variable).setCurrentValue(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsInt());
					} else if (variable instanceof LifeKnightObject) {
						((LifeKnightObject) variable).setValueFromJsonObject(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonObject());
					} else if (variable instanceof LifeKnightObjectList) {
						((LifeKnightObjectList) variable).setValueFromJsonArray(this.configurationAsJsonObject.getAsJsonObject(variable.getGroupForConfiguration()).get(variable.getNameForConfiguration()).getAsJsonArray());
					}
				} catch (Exception exception) {
					Chat.queueChatMessageForConnection(EnumChatFormatting.RED + "An error occurred while extracting the value of \"" + variable.getName() + "\" from the config; the value will be interpreted as " + variable.getValue() + ".");
				}
			}
		}
	}

	public void updateConfigurationFromVariables() {
		JsonObject configAsJsonReplacement = new JsonObject();

		List<String> groups = new ArrayList<>();

		for (LifeKnightVariable variable : LifeKnightVariable.getVariables()) {
			if (!groups.contains(variable.getGroupForConfiguration()) && variable.isStoreValue()) {
				groups.add(variable.getGroupForConfiguration());
			}
		}

		for (String group : groups) {
			JsonObject jsonObject = new JsonObject();
			for (LifeKnightVariable variable : LifeKnightVariable.getVariables()) {
				if (variable.isStoreValue() && variable.getGroupForConfiguration().equals(group)) {
					if (variable instanceof LifeKnightBoolean) {
						jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightBoolean) variable).getValue()));
					} else if (variable instanceof LifeKnightString) {
						jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightString) variable).getValue()));
					} else if (variable instanceof LifeKnightNumber) {
						jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightNumber) variable).getValue()));
					} else if (variable instanceof LifeKnightList<?>) {
						jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightList<?>) variable).toJsonArray());
					} else if (variable instanceof LifeKnightCycle) {
						jsonObject.add(variable.getNameForConfiguration(), new JsonPrimitive(((LifeKnightCycle) variable).getValue()));
					} else if (variable instanceof LifeKnightObject) {
						jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightObject) variable).getAsJsonObject());
					} else if (variable instanceof LifeKnightObjectList) {
						jsonObject.add(variable.getNameForConfiguration(), ((LifeKnightObjectList) variable).toJsonArray());
					}
				}
			}
			configAsJsonReplacement.add(group, jsonObject);
		}

		this.configurationAsJsonObject = configAsJsonReplacement;

		this.writeToConfigurationFile();
	}

	private boolean configExists() {
		try {
			return !new File("config/" + MOD_ID + ".json").createNewFile();
		} catch (Exception exception) {
			Miscellaneous.logError("An error occurred while attempting to check for the configuration file's existence: %s", exception.getMessage());
			return false;
		}
	}

	private void writeToConfigurationFile() {
		try {
			PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream("config/" + MOD_ID + ".json"), StandardCharsets.UTF_8));

			writer.write(gson.toJson(this.configurationAsJsonObject));

			writer.close();
		} catch (Exception exception) {
			Miscellaneous.logError("Could not write to configuration file: %s", exception.getMessage());
		}
	}

	private void getConfigurationContent() {
		try {
			Scanner reader = new Scanner(new File("config/" + MOD_ID + ".json"));
			StringBuilder configContent = new StringBuilder();

			while (reader.hasNextLine()) {
				configContent.append(reader.nextLine()).append(System.getProperty("line.separator"));
			}

			reader.close();

			this.configurationAsJsonObject = new JsonParser().parse(configContent.toString()).getAsJsonObject();
		} catch (Exception exception) {
			Miscellaneous.logError("Could not read configuration file: %s", exception.getMessage());
		}
	}

	public JsonObject getConfigurationAsJsonObject() {
		return this.configurationAsJsonObject;
	}
}