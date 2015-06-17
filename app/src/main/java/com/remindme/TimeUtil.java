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
        if (hour >= 13) {
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
        return (int) timeValue * 60 * 60 * 1000;
    }

} //end of TimeUtil class
