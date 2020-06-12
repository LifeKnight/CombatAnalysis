package com.lifeknight.combatanalysis.utilities;


import static com.lifeknight.combatanalysis.mod.Core.THREAD_POOL;

public class Stopwatch {
    private long totalSeconds = 0L;
    private int milliseconds = 0;
    private int seconds = 0;
    private int minutes = 0;
    private int hours = 0;
    private int days = 0;
    private boolean running = false;
    private final boolean started = false;
    private boolean ended = false;

    public void start() {
        running = true;
        count();
    }

    public void count() {
        THREAD_POOL.submit(() -> {
           while (running) {
                   try {
                       Thread.sleep(5);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }

                   milliseconds += 5;

                   if (milliseconds == 1000) {
                       milliseconds = 0;
                       seconds++;
                       totalSeconds++;
                   }

                   if (seconds == 60) {
                       seconds = 0;
                       minutes++;
                   }

                   if (minutes == 60) {
                       minutes = 0;
                       hours++;
                   }

                   if (hours == 24) {
                       hours = 0;
                       days++;
                   }
                   if (ended) {
                       break;
                   }
               }
        });
    }

    public void toggle() {
        running = !running;

        if (running) {
            count();
        }
    }

    public void stop() {
        ended = true;
    }

    public void reset() {
        milliseconds = 0;
        seconds = 0;
        minutes = 0;
        hours = 0;
        days = 0;
        totalSeconds = 0L;
    }

    public boolean isRunning() {
        return running;
    }

    public String getFormattedTime() {
        StringBuilder result = new StringBuilder();

        if (days > 0) {
            result.append(days).append(":");
            result.append(appendTime(hours)).append(":");
        } else {
            result.append(hours).append(":");
        }

        result.append(appendTime(minutes)).append(":");

        result.append(appendTime(seconds)).append(".");

        result.append(formatMilliseconds());

        return result.toString();
    }

    public String getTextualFormattedTime(boolean includeMilliseconds) {
        String result = "";
        if (includeMilliseconds) {
            if (milliseconds > 0) {
                if (milliseconds != 1) {
                    result = milliseconds + " milliseconds";
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

    public String appendTime(int timeValue) {
        StringBuilder result = new StringBuilder();
        if (timeValue > 9) {
            result.append(timeValue);
        } else {
            result.append("0").append(timeValue);
        }
        return result.toString();
    }

    private String formatMilliseconds() {
        String asString = String.valueOf(milliseconds);

        if (asString.length() == 1) {
            return "00" + milliseconds;
        } else if (asString.length() == 2) {
            return "0" + milliseconds;
        }
        return asString;
    }


    public long getTotalSeconds() {
        return totalSeconds;
    }

    public int getMilliseconds() {
        return milliseconds;
    }

    public int getSeconds() {
        return seconds;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getHours() {
        return hours;
    }

    public int getDays() {
        return days;
    }

    public void setMilliseconds(int milliseconds) {
        this.milliseconds = milliseconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
