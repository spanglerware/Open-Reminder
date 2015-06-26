package com.remindme;

/**
 * Created by Scott on 6/15/2015.
 */
public interface ReminderCallbacks {

    public void startReminderCallBack(int position);

    public void cancelReminderCallBack(int position);

    public void deleteReminderCallBack(int position);

    public void editReminderCallBack(int position);

    public void notificationCallBack(String reminderText);
}
