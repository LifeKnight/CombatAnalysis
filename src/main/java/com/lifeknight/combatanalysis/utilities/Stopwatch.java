package com.lifeknight.combatanalysis.utilities;


public class Stopwatch {
    private long startTime = 0L;
    private long lastPauseTime = 0L;
    private long timeSpentPaused = 0L;
    private long totalMilliseconds = 0L;
    private boolean running = false;
    private boolean ended = false;

    public void start() {
        running = true;
        ended = false;
        startTime = System.currentTimeMillis();
        lastPauseTime = 0L;
        timeSpentPaused = 0L;
    }

    public void pause() {
        if (ended) throw new IllegalArgumentException("Cannot pause stopped stopwatch.");
        running = false;
        lastPauseTime = System.currentTimeMillis();
        totalMilliseconds = System.currentTimeMillis() - startTime - timeSpentPaused;
    }

    public void resume() {
        if (ended) throw new IllegalArgumentException("Cannot resume stopped stopwatch.");
        running = true;
        timeSpentPaused += System.currentTimeMillis() - lastPauseTime;
    }

    public void stop() {
        pause();
        totalMilliseconds = getTotalMilliseconds();
        ended = true;
    }

    public long getTotalMilliseconds() {
        return !running ? totalMilliseconds : System.currentTimeMillis() - startTime - timeSpentPaused;
    }

    public void reset() {
        totalMilliseconds = 0L;
    }

    public boolean isRunning() {
        return running;
    }

    public String getFormattedTime() {
        long days;
        long hours;
        long minutes;
        long seconds;
        long millisecondsLeft = this.getTotalMilliseconds();
        days = millisecondsLeft / 86400000;
        millisecondsLeft %= 86400000;
        hours = millisecondsLeft / 3600000;
        millisecondsLeft %= 3600000;
        minutes = millisecondsLeft / 60000;
        millisecondsLeft %= 60000;
        seconds = millisecondsLeft / 1000;
        millisecondsLeft %= 1000;

        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(":");
            result.append(appendTime(hours)).append(":");
        } else {
            result.append(hours).append(":");
        }

        result.append(appendTime(minutes)).append(":");

        result.append(appendTime(seconds)).append(".");

        result.append(formatMilliseconds(millisecondsLeft));

        return result.toString();
    }

    public String getTextualFormattedTime(boolean includeMilliseconds) {
        long days;
        long hours;
        long minutes;
        long seconds;
        long millisecondsLeft = this.getTotalMilliseconds();
        days = millisecondsLeft / 86400000;
        millisecondsLeft %= 86400000;
        hours = millisecondsLeft / 3600000;
        millisecondsLeft %= 3600000;
        minutes = millisecondsLeft / 60000;
        millisecondsLeft %= 60000;
        seconds = millisecondsLeft / 1000;
        millisecondsLeft %= 1000;

        String result = "";
        if (includeMilliseconds) {
            if (millisecondsLeft > 0) {
                if (millisecondsLeft != 1) {
                    result = millisecondsLeft + " milliseconds";
                } else {
                    result = "1 millisecond";
                }
            }
        }

        if (seconds > 0) {
            if (seconds != 1) {
                if (result.length() != 0) {
                    result = seconds + " seconds, " + result;
                } else {
                    result = seconds + " seconds";
                }
            } else {
                if (result.length() != 0) {
                    result = "1 second, " + result;
                } else {
                    result = "1 second";
                }
            }
        }

        if (minutes > 0) {
            if (minutes != 1) {
                if (result.length() != 0) {
                    result = minutes + " minutes, " + result;
                } else {
                    result = minutes + " minutes";
                }
            } else {
                if (result.length() != 0) {
                    result = "1 minute, " + result;
                } else {
                    result = "1 minute";
                }
            }
        }

        if (hours > 0) {
            if (hours != 1) {
                if (result.length() != 0) {
                    result = hours + " hours, " + result;
                } else {
                    result = hours + " hours";
                }
            } else {
                if (result.length() != 0) {
                    result = "1 hour, " + result;
                } else {
                    result = "1 hour";
                }
            }
        }

        if (days > 0) {
            if (days != 1) {
                if (result.length() != 0) {
                    result = days + " days, " + result;
                } else {
                    result = days + " days";
                }
            } else {
                if (result.length() != 0) {
                    result = "1 day, " + result;
                } else {
                    result = "1 day";
                }
            }
        }

        if (result.contains(",")) {
            char[] asChars = result.toCharArray();

            int lastCommaIndex = result.lastIndexOf(",");
            for (int i = 0; i < asChars.length; i++) {
                if (i == lastCommaIndex) {
                    asChars[i] = '.';
                }
            }

            result = new String(asChars).replace(".", " and");
        }

        return result;
    }

    private String appendTime(long timeValue) {
        StringBuilder result = new StringBuilder();
        if (timeValue > 9) {
            result.append(timeValue);
        } else {
            result.append("0").append(timeValue);
        }
        return result.toString();
    }

    private String formatMilliseconds(long milliseconds) {
        String asString = String.valueOf(milliseconds);

        if (asString.length() == 1) {
            return "00" + milliseconds;
        } else if (asString.length() == 2) {
            return "0" + milliseconds;
        }
        return asString;
    }

    public long getSeconds() {
        long millisecondsLeft = this.getTotalMilliseconds();
        millisecondsLeft %= 86400000;
        millisecondsLeft %= 3600000;
        millisecondsLeft %= 60000;
        return millisecondsLeft / 1000;
    }

    public long getMinutes() {
        long millisecondsLeft = this.getTotalMilliseconds();
        return millisecondsLeft / 60000;
    }

    public long getHours() {
        long millisecondsLeft = this.getTotalMilliseconds();
        millisecondsLeft %= 86400000;
        return millisecondsLeft / 3600000;
    }

    public long getDays() {
        long millisecondsLeft = this.getTotalMilliseconds();
        return millisecondsLeft / 86400000;
    }
}
