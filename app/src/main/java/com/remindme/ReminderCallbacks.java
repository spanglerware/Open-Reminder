package com.remindme;

/**
 * Created by Scott on 6/15/2015.
 */
public interface ReminderCallbacks {

    void startReminderCallBack(int position);

    void cancelReminderCallBack(int position);

    void deleteReminderCallBack(int position);

    void editReminderCallBack(int position);

    void notificationCallBack(String reminderText);
}
