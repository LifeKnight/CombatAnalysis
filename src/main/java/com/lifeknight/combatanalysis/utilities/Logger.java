package com.lifeknight.combatanalysis.utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.lifeknight.combatanalysis.mod.Core.THREAD_POOL;

public class Logger {
    private File logFile;
    private final File logFolder;
    private List<File> logFiles;
    private boolean doLog = true;
    private final boolean doLogTime;
    private String currentLog = "";
    private final List<String> logs = new ArrayList<>();

    public Logger(File folder) {
        logFolder = folder;
        logFile = new File(logFolder + "/" + Miscellaneous.getCurrentDateString().replace("/", ".") + ".txt");

        boolean logCreateFolder = logFolder.mkdirs();

        for (File file : logFolder.listFiles()) {
            THREAD_POOL.submit(() -> {
                logs.add(logToString(file));
            });
        }

        try {
            if (!logFile.createNewFile()) getPreviousLog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.doLogTime = true;

        try {
            logFiles = Arrays.asList(logFolder.listFiles());
        } catch (Exception e) {
            logFiles = Collections.singletonList(logFile);
            e.printStackTrace();
        }

    }

    public void getPreviousLog() {
        currentLog = logToString(logFile);
    }

    public String logToString(File log) {
        try {
            Scanner reader = new Scanner(log);

            StringBuilder logContent = new StringBuilder();

            while (reader.hasNextLine()) {
                logContent.append(reader.nextLine()).append(System.getProperty("line.separator"));
            }

            reader.close();

            return logContent.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public void log(String input) {
        if (doLog) {
            if (doLogTime) {
                currentLog += "[" + Miscellaneous.getCurrentTimeString() + "] " + input + System.getProperty("line.separator");
            }
            writeLogToFile();
        }
    }

    public void plainLog(String input) {
        if (doLog) {
            currentLog += input + System.getProperty("line.separator");
            writeLogToFile();
        }
    }

    public void writeLogToFile() {
        try {
            logFile = new File(logFolder + "/" + Miscellaneous.getCurrentDateString().replace("/", ".") + ".txt");

            if (this.logFile.createNewFile()) {
                logFiles.add(logFile);
                logs.add(currentLog);
                currentLog = "";
            }

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(logFile), StandardCharsets.UTF_8));

            writer.write(currentLog);

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLog() {
        return currentLog;
    }

    public List<String> getLogs() {
        return logs;
    }

    public File getLogFile() {
        return logFile;
    }

    public List<File> getLogFiles() {
        return logFiles;
    }

    public void toggleLog() {
        if (doLog) {
            log("Pausing logs.");
            doLog = false;
        } else {
            doLog = true;
            log("Resuming logs.");
        }
    }

    public boolean isRunning() {
        return doLog;
    }
}
