package com.lifeknight.combatanalysis.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Predicate;

import static com.lifeknight.combatanalysis.mod.Core.THREAD_POOL;

public class Logger {
    private File logFile;
    private final File logFolder;
    private final List<File> logFiles = new ArrayList<>();
    private boolean doLog = true;
    private final boolean doLogTime;
    private String currentLog = "";
    private String previousLog = "";
    private final List<String> logs = new ArrayList<>();

    public Logger(File folder) {
        this.logFolder = folder;
        this.logFile = new File(this.logFolder + "/" + Text.getCurrentDateString().replace("/", ".") + ".txt");

        if (this.logFolder.exists()) {
            for (File file : Objects.requireNonNull(this.logFolder.listFiles())) {
                THREAD_POOL.submit(() -> {
                    this.logs.add(logToString(file));
                });
            }
        } else {
            this.logFolder.mkdirs();
        }

        try {
            if (this.logFile.exists()) this.updatePreviousLog();
        } catch (Exception exception) {
            Miscellaneous.logError("An error occurred while attempting to check for the current log files creation/instantiation: %s", exception.getMessage());
        }
        this.doLogTime = true;

        try {
            this.logFiles.addAll(Arrays.asList(Objects.requireNonNull(this.logFolder.listFiles())));
        } catch (Exception exception) {
            this.logFiles.add(this.logFile);
            Miscellaneous.logError("An error occurred while attempting to collect the files in " + this.logFolder + ": %s", exception.getMessage());
        }

    }

    public void updatePreviousLog() {
        this.previousLog = logToString(this.logFile);
    }

    private static String logToString(File log) {
        try {
            Scanner reader = new Scanner(log);

            StringBuilder logContent = new StringBuilder();

            while (reader.hasNextLine()) {
                logContent.append(reader.nextLine()).append(System.getProperty("line.separator"));
            }

            reader.close();

            return logContent.toString();

        } catch (Exception exception) {
            Miscellaneous.logError("An error occurred while attempting to convert a log to a string: %s", exception.getMessage());
        }
        return "";
    }

    public void log(String input) {
        if (this.doLog) {
            if (this.doLogTime) {
                this.currentLog += String.format("[%s] %s" + System.getProperty("line.separator"), Text.getCurrentTimeString(), input);
            }
            this.writeLogToFile();
        }
    }

    public void plainLog(String input) {
        if (this.doLog) {
            this.currentLog += input + System.getProperty("line.separator");
            this.writeLogToFile();
        }
    }

    public void writeLogToFile() {
        try {
            this.logFile = new File(this.logFolder + "/" + Text.getCurrentDateString().replace("/", ".") + ".txt");

            if (this.logFile.createNewFile()) {
                this.logFiles.add(this.logFile);
                this.logs.add(this.currentLog);
                this.previousLog = "";
                this.currentLog = "";
            }

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.logFile), StandardCharsets.UTF_8));
            writer.write(this.previousLog + this.currentLog);
            writer.close();
        } catch (Exception exception) {
            Miscellaneous.logError("An error occurred while attempting to write to a log file: %s", exception.getMessage());
        }
    }

    public void deleteCurrentLog() {
        this.currentLog = "";
    }

    public void deletePreviousLog() {
        this.previousLog = "";
    }

    public String getLog() {
        return this.currentLog;
    }

    public String getPreviousLog() {
        return this.previousLog;
    }

    public boolean deleteLogFile(String date) {
        date = date.replace("/", ".");

        File file = new File(this.logFolder + "/" + date + ".txt");

        return file.delete();
    }

    public String getLogOfDate(String date) {
        date = date.replace("/", ".");
        for (File log : this.logFiles) {
            if (log.getName().equals(date + ".txt")) return logToString(log);
        }
        return null;
    }

    public boolean replaceLogContent(String date, String textToReplace, String replacement) {
        String originalContent = this.getLogOfDate(date);
        if (originalContent == null) {
            Miscellaneous.logError("Tried to replace log content, no file found: [%s] %s", this.logFolder.getName(), date);
            return false;
        }

        String newContent = originalContent.replaceAll(textToReplace, replacement);

        if (originalContent.equals(newContent)) {
            Miscellaneous.logWarn("Tried to replace log content, replaced and original are the same: %s, %s, %s", date, textToReplace, replacement);
            return false;
        }

        return this.writeToLog(date, newContent);
    }

    public boolean replaceLogsContent(String textToReplace, String replacement) {
        boolean changesMade = false;

        for (File file : this.logFiles) {
            if (this.replaceLogContent(Text.removeAll(file.getName(), ".txt"), textToReplace, replacement)) changesMade = true;
        }

        return changesMade;
    }

    public boolean changeContentOfLog(String date, String newContent) {
        String originalContent = this.getLogOfDate(date);
        if (originalContent == null) {
            Miscellaneous.logWarn("Tried to update the content of a log, log does not exist: %s", this.logFolder, date);
            return false;
        }

        if (originalContent.equals(newContent)) {
            Miscellaneous.logWarn("Attempted to change content of log, new and original are the same: %s, %s, %s", this.logFolder, date, Text.shortenText(newContent));
            return false;
        }

        return this.writeToLog(date, newContent);
    }

    public boolean deleteLinesOfLogThat(String date, Predicate<String> condition) {
        String originalContent = this.getLogOfDate(date);
        if (originalContent == null) {
            Miscellaneous.logError("Tried to delete line of logs that contain a certain string, no log found: %s", date);
            return false;
        }

        StringBuilder newContent = new StringBuilder();

        Scanner scanner = new Scanner(originalContent);

        while (scanner.hasNextLine()) {
            String nextLine = scanner.nextLine();
            if (!condition.test(nextLine)) {
                newContent.append(nextLine).append(System.getProperty("line.separator"));
            }
        }

        if (originalContent.equals(newContent.toString())) {
            Miscellaneous.logWarn("Tried to delete log of line that contains text, new and original are the same: %s, %s, %s", date, Text.shortenText(originalContent), Text.shortenText(newContent.toString()));
        }

        return this.writeToLog(date, newContent.toString());
    }

    public boolean deleteLinesOfLogsThat(Predicate<String> filter) {
        boolean changesMade = false;

        for (File file : this.logFiles) {
            if (this.deleteLinesOfLogThat(Text.removeAll(file.getName(), ".txt"), filter)) changesMade = true;
        }

        return changesMade;
    }

    public boolean deleteLinesOfLogThatContain(String date, String containedText, boolean ignoreCapitalization) {
        if (ignoreCapitalization) containedText = containedText.toLowerCase();
        String finalContainedText = containedText;
        return this.deleteLinesOfLogThat(date, string -> ((ignoreCapitalization && string.toLowerCase().contains(finalContainedText)) || (!ignoreCapitalization && string.contains(finalContainedText))));
    }

    public boolean deleteLinesOfLogsThatContain(String containedText, boolean ignoreCapitalization) {
        boolean changesMade = false;

        for (File file : this.logFiles) {
            if (this.deleteLinesOfLogThatContain(Text.removeAll(file.getName(), ".txt"), containedText, ignoreCapitalization)) changesMade = true;
        }

        return changesMade;
    }

    private boolean writeToLog(String date, String content) {
        date = date.replace("/", ".");
        File file = new File(this.logFolder + "/" + date + ".txt");

        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
            writer.write(content);
            writer.close();
        } catch (Exception exception) {
            Miscellaneous.logError("Tried to log content, error occurred: %s, %s", date, exception.getMessage());
            return false;
        }
        return true;
    }

    public List<String> getLogs() {
        return this.logs;
    }

    public File getLogFile() {
        return this.logFile;
    }

    public List<File> getLogFiles() {
        return this.logFiles;
    }

    public void toggleLog() {
        if (this.doLog) {
            this.log("Pausing logs.");
            this.doLog = false;
        } else {
            this.doLog = true;
            this.log("Resuming logs.");
        }
    }

    public boolean isRunning() {
        return this.doLog;
    }
}