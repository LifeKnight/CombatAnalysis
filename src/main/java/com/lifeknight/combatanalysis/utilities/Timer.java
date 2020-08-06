package com.lifeknight.combatanalysis.utilities;

import static com.lifeknight.combatanalysis.mod.Core.THREAD_POOL;

public class Timer {
    private long endTime = 0L;
    private long lastPauseTime = 0L;
    private long timeSpentPaused = 0L;
    private long millisecondsRemaining = 0L;
    private boolean running = false;
    private boolean ended = false;
    private ITimerListener onEnd;

    public Timer(int seconds, int minutes, int hours, int days) {
        int secondsLeft = seconds + minutes * 60 + hours * 3600 + days * 86400;
        endTime = System.currentTimeMillis() + secondsLeft * 1000;
    }

    public Timer(int seconds, int minutes, int hours) {
        int secondsLeft = seconds + minutes * 60 + hours * 3600;
        endTime = System.currentTimeMillis() + secondsLeft * 1000;
    }

    public Timer(int seconds, int minutes) {
        int secondsLeft = seconds + minutes * 60;
        endTime = System.currentTimeMillis() + secondsLeft * 1000;
    }

    public Timer(int seconds) {
        endTime = System.currentTimeMillis() + seconds * 1000;
    }

    public void onEnd(ITimerListener onEnd) {
        this.onEnd = onEnd;
    }

    public void start() {
        running = true;
        ended = false;
        lastPauseTime = 0L;
        timeSpentPaused = 0L;
        THREAD_POOL.submit(this::waitForEnd);
    }

    private void waitForEnd() {
        while (getTotalMilliseconds() != 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        running = false;
        ended = true;
        if (onEnd != null) onEnd.onTimerEnd();
    }

    public void pause() {
        if (ended) throw new IllegalArgumentException("Cannot pause stopped timer.");
        millisecondsRemaining = this.getTotalMilliseconds();
        running = false;
        lastPauseTime = System.currentTimeMillis();
    }

    public void resume() {
        if (ended) throw new IllegalArgumentException("Cannot resume stopped timer.");
        running = true;
        timeSpentPaused += System.currentTimeMillis() - lastPauseTime;
    }

    public void stop() {
        pause();
        ended = true;
        millisecondsRemaining = 0L;
    }

    public long getTotalMilliseconds() {
        return !running ? millisecondsRemaining : endTime - System.currentTimeMillis() + timeSpentPaused;
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

    public interface ITimerListener {
        public void onTimerEnd();
    }
}
