package com.remindme;

import java.util.ArrayList;

/**
 * Created by Scott on 6/8/2015.
 */
public class SingletonDataArray {
    private ArrayList<Reminder> dataArray;

    private static SingletonDataArray instance;

    private SingletonDataArray() {
        dataArray = new ArrayList<Reminder>();
    }

    public static SingletonDataArray getInstance() {
        if (instance == null) {
            instance = new SingletonDataArray();
        }
        return instance;
    }

    public ArrayList<Reminder> getDataArray() {
        return dataArray;
    }

    public void updateReminder(Reminder reminder, int position) {
        //todo getReminderId position does not update when the list changes
        dataArray.set(position, reminder);
    }

    public void addReminder(Reminder reminder) {
        dataArray.add(reminder);
    }

    public void removeReminder(int position) {
        dataArray.remove(position);
    }

}
