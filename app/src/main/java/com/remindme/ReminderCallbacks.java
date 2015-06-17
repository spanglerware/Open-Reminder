package com.remindme;

/**
 * Created by Scott on 6/15/2015.
 */
public interface ReminderCallbacks {

    public void startReminderCallBack(Reminder reminder);

    public void cancelReminderCallBack(Reminder reminder);

    public void deleteReminderCallBack(int position);

    public void editReminderCallBack(int position);

    public void notificationCallBack(String reminderText);
}
