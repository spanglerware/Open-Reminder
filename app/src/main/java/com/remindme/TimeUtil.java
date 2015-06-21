package com.remindme;

/**
 * Created by Scott on 6/14/2015.
 */
public class TimeUtil {

    private TimeUtil () {}

    public static String FloatTimeToString(float timeValue) {
        boolean flag = false;
        String timeText;

        if (timeValue == 24) { return "12:00 am"; }

        int hour = (int) Math.floor(timeValue);
        int minute = (int) Math.round((timeValue - hour) * 6) * 10;
        if (minute == 60) {
            minute = 0;
            hour += 1;
        }
        if (hour == 12) {
            flag = true;
        } else if (hour > 12) {
            hour -= 12;
            flag = true;
        }

        timeText = String.format("%01d:%02d", hour, minute);
        if (flag) {
            timeText += " pm";
        } else {
            timeText += " am";
        }

        return timeText;
    }

    public static int FloatTimeToMilliseconds(float timeValue) {
        return (int) (timeValue * 60.0f * 60.0f * 1000.0f);
    }

    public static float MillisecondsToFloatTime(long timeValue) {
        return (float) timeValue / 3600000.0f;
    }

    public static String FloatTimeToStringHMS(float timeValue) {
        int hours, minutes, seconds;
        hours = (int) Math.floor(timeValue);
        minutes = (int) Math.floor((timeValue - hours) * 60);
        seconds = (int) Math.floor((timeValue - hours - (minutes / 60.0f)) * 3600);
        if (seconds > 60) {
            seconds -= 60;
            minutes += 1;
        }
        if (minutes > 60) {
            minutes -= 60;
            hours += 1;
        }

        String formattedFreq = "";
        if (hours != 0) { formattedFreq = String.valueOf(hours) + ":"; }
        formattedFreq += String.format("%02d:%02d", minutes, seconds);

        return formattedFreq;
    }


} //end of TimeUtil class
