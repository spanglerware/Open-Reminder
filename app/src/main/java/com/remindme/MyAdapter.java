package com.remindme;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Scott on 4/26/2015.
 */

public class MyAdapter extends BaseAdapter implements View.OnTouchListener {
    private LayoutInflater layoutInflater;
    //private ArrayList<Reminder> mItems;
    private final Context mContext;
    private HashMap<Integer,TextView> counterMap;
    protected ListView mListView;
    private boolean mStarted;
    private ReminderCallbacks reminderCallbacks;

    public static int selectedId = -1;

    static class TestViewHolder {
        public TextView tvHolderReminder;
        public TextView tvHolderFrequency;
        public TextView tvHolderRowId;
        public TextView tvHolderCounter;
        public TextView tvHolderDays;
        public TextView tvHolderTimes;
//        public TextView tvHolderNotType;
        public Button  btnStart;
        public Button btnEdit;
        public Button btnDelete;
        public LinearLayout llSecondary;
        public LinearLayout llAll;
    }

//todo use constructor for multiple TVs
    public MyAdapter(Context context, ListView listView, ReminderCallbacks reminderCBs) {
        super();
        //super(context, data, resource, from, to);
        layoutInflater = LayoutInflater.from(context);
        //mItems = SingletonDataArray.getInstance().getDataArray();
        mContext = context;
        counterMap = new HashMap<Integer,TextView>();
        mListView = listView;
        mStarted = false;
        reminderCallbacks = reminderCBs;
    }

    public int getCount() {
        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        return mItems.size();
    }

    public Reminder getItem(int position) {
        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

//    @Override
//    public void notifyDataSetChanged() {
//        mItems = SingletonDataArray.getInstance().getDataArray();
//        super.notifyDataSetChanged();
//    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_reminder, parent, false);
            final TestViewHolder viewHolder = new TestViewHolder();
            viewHolder.tvHolderRowId = (TextView) rowView.findViewById(R.id.list_item_reminder_id);
            viewHolder.tvHolderReminder = (TextView) rowView.findViewById(R.id.list_item_reminder_text);
            viewHolder.tvHolderCounter = (TextView) rowView.findViewById(R.id.list_item_reminder_counter);
            viewHolder.tvHolderFrequency = (TextView) rowView.findViewById(R.id.list_item_reminder_frequency);
            viewHolder.tvHolderDays = (TextView) rowView.findViewById(R.id.list_item_reminder_days);
            viewHolder.tvHolderTimes = (TextView) rowView.findViewById(R.id.list_item_reminder_times);
//            viewHolder.tvHolderNotType = (TextView) rowView.findViewById(R.id.list_item_reminder_notification_type);
            viewHolder.btnEdit = (Button) rowView.findViewById(R.id.button_edit);
            viewHolder.btnEdit.setOnClickListener(mOnEditClickListener);
            viewHolder.btnStart = (Button) rowView.findViewById(R.id.button_start);
            viewHolder.btnStart.setOnClickListener(mOnStartClickListener);
            viewHolder.btnDelete = (Button) rowView.findViewById(R.id.button_delete);
            viewHolder.btnDelete.setOnClickListener(mOnDeleteClickListener);
            viewHolder.llSecondary = (LinearLayout) rowView.findViewById(R.id.container_secondary);
            viewHolder.llAll = (LinearLayout) rowView.findViewById(R.id.container_all);

            AssetManager assetManager = mContext.getAssets();
            Typeface typeface = Typeface.createFromAsset(assetManager, "fonts/comicsans.ttf");

            int color = mContext.getResources().getColor(R.color.black);
            viewHolder.tvHolderReminder.setTypeface(typeface);
            viewHolder.tvHolderReminder.setTextColor(color);
            viewHolder.tvHolderFrequency.setTypeface(typeface);
            viewHolder.tvHolderFrequency.setTextColor(color);
            viewHolder.tvHolderDays.setTypeface(typeface);
            viewHolder.tvHolderDays.setTextColor(color);
//            viewHolder.tvHolderNotType.setTypeface(typeface);
//            viewHolder.tvHolderNotType.setTextColor(color);
            viewHolder.tvHolderTimes.setTypeface(typeface);
            viewHolder.tvHolderTimes.setTextColor(color);
            viewHolder.tvHolderCounter.setTypeface(typeface);
            viewHolder.tvHolderCounter.setTextColor(color);

            rowView.setTag(viewHolder);
        }

        TestViewHolder holder = (TestViewHolder) rowView.getTag();
        final TextView tvRow = holder.tvHolderRowId;
        final TextView tvR = holder.tvHolderReminder;
        final TextView tvC = holder.tvHolderCounter;
        final TextView tvF = holder.tvHolderFrequency;
        final TextView tvD = holder.tvHolderDays;
        final TextView tvT = holder.tvHolderTimes;
//        final TextView tvN = holder.tvHolderNotType;

        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        Reminder item = mItems.get(position);

        tvRow.setText(String.valueOf(item.getRowId()));
        tvR.setText(item.getReminder());
        tvC.setText(item.getCounterAsString());
        String label = "";
        if (item.getRecurring()) {
            label = "Timer: ";
            tvF.setText(label + item.getFormattedFrequency());
            tvT.setVisibility(View.VISIBLE);
            tvT.setText("Time: " + item.getTimeFromAsString() + " - " + item.getTimeToAsString());
            //holder.llAll.setOrientation(LinearLayout.HORIZONTAL);
        } else {
            label = "Time: ";
            tvF.setText(label + TimeUtil.FloatTimeToStringExact(item.getFloatFrequency()));
            tvT.setText("");
            tvT.setVisibility(View.GONE);
        }

        tvD.setText("Days: " + item.getDaysAsString());
//        tvN.setText("Type: " + (item.getNotificationType() ? "Alarm" : "Notification"));

        rowView.setTag(holder);
        return rowView;
    }

    public boolean reduceCounters(long interval) {
        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        int listSize = mItems.size();
        boolean complete = false;
        Reminder item;

        for (int i = 0; i < listSize; i++) {
            item = mItems.get(i);
            if (item.isActive()) {
                if (item.reduceCounter(interval)) { complete = true; }
            }
        }
        return complete;
    }

//    public void removeItem(int position) {
//        mItems.remove(position);
//    }

    private View.OnClickListener mOnStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        final int position = mListView.getPositionForView((View) view.getParent());
        Reminder reminder = mItems.get(position);

        if (mStarted) {
            setButtonImage(true, view);
            mStarted = false;
            reminder.setActive(false);
            reminderCallbacks.cancelReminderCallBack(reminder);
        } else {
            setButtonImage(false, view);
            reminder.setActive(true);
            mStarted = true;
            reminderCallbacks.startReminderCallBack(reminder);
        }

        notifyDataSetChanged();
        }
    };

    private View.OnClickListener mOnEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
            View tempView = ((View) (view.getParent()).getParent());
            final int position = mListView.getPositionForView((View) view.getParent());
            Reminder reminder = mItems.get(position);
            TestViewHolder viewHolder = (TestViewHolder) tempView.getTag();

            reminder.setActive(false);
            setButtonImage(true, viewHolder.btnStart);
            reminderCallbacks.editReminderCallBack(position);
        }
    };

    private View.OnClickListener mOnDeleteClickListener = (new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int position = mListView.getPositionForView((View) view.getParent());
            reminderCallbacks.deleteReminderCallBack(position);
        }
    });

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        //vh.row is the convertView in getView or you may call it the row item itself

        if (view instanceof Button) {
            Button button = (Button) view;
            button.setFocusable(true);
            button.setFocusableInTouchMode(true);
        } else {
            TestViewHolder viewHolder = (TestViewHolder) view.getTag();
            viewHolder.btnEdit.setFocusableInTouchMode(false);
            viewHolder.btnEdit.setFocusable(false);
            viewHolder.btnStart.setFocusable(false);
            viewHolder.btnStart.setFocusableInTouchMode(false);
            viewHolder.btnDelete.setFocusable(false);
            viewHolder.btnDelete.setFocusableInTouchMode(false);
        }
        return false;
    }

    private void setButtonImage(boolean toStart, View view) {
        Button startButton = (Button) view;

        if (toStart) {
            startButton.setBackgroundResource(R.drawable.play2);
        } else {
            startButton.setBackgroundResource(R.drawable.stop);
        }
    }

    public void refresh() {
        //mItems = SingletonDataArray.getInstance().getDataArray();
        notifyDataSetChanged();
    }

    //end of MyAdapter class
}
