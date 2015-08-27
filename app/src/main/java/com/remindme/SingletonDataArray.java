package com.remindme;

import java.util.ArrayList;

/**
 * Created by Scott on 6/8/2015.
 */

//singleton pattern used to manage reminder data
public class SingletonDataArray {
    private ArrayList<Reminder> dataArray;
    private static SingletonDataArray instance;

    //private constructor
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
        dataArray.set(position, reminder);
    }

    public void addReminder(Reminder reminder) {
        dataArray.add(reminder);
    }

    public void removeReminder(int position) {
        dataArray.remove(position);
    }

    public Reminder getReminderWithId (long rowId) {
        for (Reminder reminder : dataArray) {
            if (reminder.getRowId() == rowId) { return reminder; }
        }
        return null;
    }

    public int getPositionWithId (long rowId) {
        for (int i = 0; i < dataArray.size(); i++) {
            if (dataArray.get(i).getRowId() == rowId) { return i; }
        }
        return -1;
    }

    public int getSize() {
        return dataArray.size();
    }


}  //end of SingletonDataArray class
