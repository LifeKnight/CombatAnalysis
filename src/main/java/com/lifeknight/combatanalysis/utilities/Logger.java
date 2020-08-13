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
        this.logFolder = folder;
        this.logFile = new File(this.logFolder + "/" + Miscellaneous.getCurrentDateString().replace("/", ".") + ".txt");

        this.logFolder.mkdirs();

        for (File file : this.logFolder.listFiles()) {
            THREAD_POOL.submit(() -> {
                this.logs.add(logToString(file));
            });
        }

        try {
            if (this.logFile.exists()) this.getPreviousLog();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.doLogTime = true;

        try {
            this.logFiles = Arrays.asList(this.logFolder.listFiles());
        } catch (Exception e) {
            this.logFiles = Collections.singletonList(this.logFile);
            e.printStackTrace();
        }

    }

    public void getPreviousLog() {
        this.currentLog = logToString(this.logFile);
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
        if (this.doLog) {
            if (this.doLogTime) {
                this.currentLog += "[" + Miscellaneous.getCurrentTimeString() + "] " + input + System.getProperty("line.separator");
            }
            writeLogToFile();
        }
    }

    public void plainLog(String input) {
        if (this.doLog) {
            this.currentLog += input + System.getProperty("line.separator");
            writeLogToFile();
        }
    }

    public void writeLogToFile() {
        try {
            this.logFile = new File(this.logFolder + "/" + Miscellaneous.getCurrentDateString().replace("/", ".") + ".txt");

            if (this.logFile.createNewFile()) {
                this.logFiles.add(this.logFile);
                this.logs.add(this.currentLog);
                this.currentLog = "";
            }

            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.logFile), StandardCharsets.UTF_8));

            writer.write(this.currentLog);

            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getLog() {
        return this.currentLog;
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