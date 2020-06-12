package com.lifeknight.combatanalysis.utilities;

import static com.lifeknight.combatanalysis.mod.Core.THREAD_POOL;

public class Timer {
    private int milliseconds = 0;
    private int seconds;
    private int minutes;
    private int hours;
    private int days;
    private boolean running = false;
    private boolean ended = false;

    public Timer(int seconds, int minutes, int hours, int days) {
        int secondsLeft = seconds + minutes * 60 + hours * 3600 + days * 86400;
        this.days = secondsLeft / 86400;
        secondsLeft %= 86400;
        this.hours = secondsLeft / 3600;
        secondsLeft %= 3600;
        this.minutes = secondsLeft / 60;
        secondsLeft %= 60;
        this.seconds = secondsLeft;
    }

    public Timer(int seconds, int minutes, int hours) {
        int secondsLeft = seconds + minutes * 60 + hours * 3600;
        this.days = secondsLeft / 86400;
        secondsLeft %= 86400;
        this.hours = secondsLeft / 3600;
        secondsLeft %= 3600;
        this.minutes = secondsLeft / 60;
        secondsLeft %= 60;
        this.seconds = secondsLeft;
    }

    public Timer(int seconds, int minutes) {
        int secondsLeft = seconds + minutes * 60;
        this.days = secondsLeft / 86400;
        secondsLeft %= 86400;
        this.hours = secondsLeft / 3600;
        secondsLeft %= 3600;
        this.minutes = secondsLeft / 60;
        secondsLeft %= 60;
        this.seconds = secondsLeft;
    }

    public Timer(int seconds) {
        int secondsLeft = seconds;
        this.days = secondsLeft / 86400;
        secondsLeft %= 86400;
        this.hours = secondsLeft / 3600;
        secondsLeft %= 3600;
        this.minutes = secondsLeft / 60;
        secondsLeft %= 60;
        this.seconds = secondsLeft;
    }

    public void start() {
        running = true;
        count();
    }

    public void count() {
        THREAD_POOL.submit(() -> {
           while (running) {
               if (ended) {
                   break;
               }
                   try {
                       Thread.sleep(5);
                   } catch (Exception e) {
                       e.printStackTrace();
                   }

                   milliseconds -= 5;

                   if (milliseconds == -5) {
                       milliseconds = 990;
                       seconds--;
                   }

                   if (seconds == -1) {
                       seconds = 59;
                       minutes--;
                   }

                   if (minutes == -1) {
                       minutes = 59;
                       hours--;
                   }

                   if (hours == -1) {
                       hours = 23;
                       days--;
                   }

                   if (milliseconds == 0 && seconds == 0 && minutes == 0 && hours == 0 && days == 0) {
                       end();
                       break;
                   }
               }
        });
    }

    public void end() {
        ended = true;
    }

    public void toggle() {
        running = !running;

        if (running) {
            count();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean hasEnded() {
        return ended;
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

    private String appendTime(int timeValue) {
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
