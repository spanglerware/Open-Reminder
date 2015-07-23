package com.remindme;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.ArrayList;

/**
 * Created by Scott on 6/14/2015.
 */
public class TimeUtil {

    private TimeUtil () {}

    //todo re-write FloatTimeToString functions into one method with parameters

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

    public static String FloatTimeToStringExact(float timeValue) {
        boolean flag = false;
        String timeText = "";

        if (timeValue == 24) { return "12:00 am"; }

        int hours = (int) Math.floor(timeValue);
        int minutes = (int) Math.floor((timeValue - hours) * 60);
        int seconds = (int) Math.floor((timeValue - hours - (minutes / 60.0f)) * 3600);
        if (seconds > 60) {
            seconds -= 60;
            minutes += 1;
        }
        if (minutes > 60) {
            minutes -= 60;
            hours += 1;
        }
        if (hours == 12) {
            flag = true;
        } else if (hours > 12) {
            hours -= 12;
            flag = true;
        }

        timeText = String.format("%01d:%02d:%02d", hours, minutes, seconds);
        if (flag) {
            timeText += " pm";
        } else {
            timeText += " am";
        }

        return timeText;
    }

    public static long scheduleAlarm(boolean useType, float frequency,
                                         ArrayList<Integer> days, float timeFrom,
                                         float timeTo) {
        //first calculate next time for today, then check if it is in time range, then check for next days
        LocalTime localTime = LocalTime.now();
        LocalDate localDate = LocalDate.now();

        float currentTime = TimeUtil.MillisecondsToFloatTime(localTime.getMillisOfDay());
        float alarmNextTime;

        int today = localDate.getDayOfWeek();
        int tomorrow = localDate.getDayOfWeek() + 1;
        if (tomorrow > 7) { tomorrow = 1; }
        boolean found = false;
        boolean exit = false;
        int daysFromToday = 0;
        float nextTime = 0;  //this value will be the next alarm time in float time

        if (!useType) {
            if (frequency >= currentTime) {
                nextTime = frequency;
            } else if (days.isEmpty()) {
                exit = true;
            } else {
                for (int i = tomorrow; i < 8; i++) {
                    if (days.contains(i)) { found = true; daysFromToday = i - tomorrow + 1;  break; }
                }
                if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                    for (int i = 1; i < tomorrow; i++) {
                        if (days.contains(i)) { found = true; daysFromToday = 7 - tomorrow + i + 1;  break; }
                    }
                }
                nextTime = (frequency + (24 * daysFromToday));
            }
        } else {
            alarmNextTime = currentTime + frequency;

            //check if frequency fits within timefrom - timeto interval, if not then there will be no repeat
            //also check if no days were selected for repeating
            if (frequency > (timeTo - timeFrom) || days.isEmpty()) {
                exit = true;
            } else {

                //first check for today
                if (days.contains(today) && alarmNextTime < timeTo) {
                    if (alarmNextTime <= timeFrom) {
                        nextTime = timeFrom + frequency;
                    } else {
                        //schedule alarm normally
                        nextTime = alarmNextTime;
                    }
                } else {  //check for next day to run alarm
                    for (int i = tomorrow; i < 8; i++) {
                        if (days.contains(i)) {
                            found = true;
                            daysFromToday = i - tomorrow + 1;
                            break;
                        }
                    }
                    if (!found && tomorrow > 1) {  //if tomorrow = 1 then it has already been covered by loop above
                        for (int i = 1; i < tomorrow; i++) {
                            if (days.contains(i)) {
                                found = true;
                                daysFromToday = 7 - tomorrow + i + 1;
                                break;
                            }
                        }
                    }
                    if (found) {
                        nextTime = (daysFromToday * 24) + timeFrom + frequency;
                    }
                }
            }
        }

        if (exit) return 0;

        nextTime -= currentTime;
        return TimeUtil.FloatTimeToMilliseconds(nextTime);
    }



} //end of TimeUtil class
