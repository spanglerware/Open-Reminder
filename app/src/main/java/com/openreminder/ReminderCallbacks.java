package com.openreminder;

/**
 * Created by Scott on 6/15/2015.
 */

//callback interface
public interface ReminderCallbacks {

    void startReminderCallBack(int position);

    void cancelReminderCallBack(int position);

    void deleteReminderCallBack(int position);

    void editReminderCallBack(int position);

    void notificationCallBack(String reminderText);
}
