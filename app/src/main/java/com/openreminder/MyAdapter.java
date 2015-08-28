package com.openreminder;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.openreminder.R;

import java.util.ArrayList;

/**
 * Created by Scott on 4/26/2015.
 */

//MyAdapter provides reminder data for the list view in MainActivity and implements callbacks for
    //events triggered from within the list
public class MyAdapter extends BaseAdapter implements View.OnTouchListener {

    private final Context mContext;
    protected ListView mListView;
    private ReminderCallbacks reminderCallbacks;

//view holder is used to store controls for improved performance
    static class ReminderViewHolder {
        public TextView tvHolderReminder;
        public TextView tvHolderFrequency;
        public TextView tvHolderRowId;
        public TextView tvHolderCounter;
        public TextView tvHolderDays;
        public TextView tvHolderTimes;
        public Button  btnStart;
        public Button btnEdit;
        public Button btnDelete;
        public LinearLayout llSecondary;
        public LinearLayout llAll;
    }

    //public constructor
    public MyAdapter(Context context, ListView listView, ReminderCallbacks reminderCBs) {
        super();
        mContext = context;
        mListView = listView;
        reminderCallbacks = reminderCBs;
    }

    //MyAdapter uses following methods to provide layer of abstraction between MainActivity and the Singleton
    public int getCount() {
        return SingletonDataArray.getInstance().getSize();
    }

    public Reminder getItem(int position) {
        ArrayList<Reminder> mItems = SingletonDataArray.getInstance().getDataArray();
        return mItems.get(position);
    }

    public void removeItem(int position) {
        SingletonDataArray.getInstance().removeReminder(position);
    }

    public long getItemId(int position) {
        return position;
    }

    //getView implements the view holder for improved performance
    //this method also controls visibility of list items' expanded details
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        ReminderViewHolder viewHolder;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.list_item_reminder, parent, false);

            viewHolder = new ReminderViewHolder();
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
            viewHolder.tvHolderTimes.setTypeface(typeface);
            viewHolder.tvHolderTimes.setTextColor(color);
            viewHolder.tvHolderCounter.setTypeface(typeface);
            viewHolder.tvHolderCounter.setTextColor(color);

            rowView.setTag(viewHolder);
        } else {
            viewHolder = (ReminderViewHolder) rowView.getTag();
        }

        Reminder item = getItem(position);
        //Log.d("myadapter getview", "position = " + position);

        viewHolder.tvHolderRowId.setText(String.valueOf(item.getRowId()));
        viewHolder.tvHolderReminder.setText(item.getReminder());
        viewHolder.tvHolderCounter.setText(item.getCounterAsString());
        String label;
        if (item.getRecurring()) {
            label = "Timer: ";
            viewHolder.tvHolderFrequency.setText(label + item.getFormattedFrequency());
            viewHolder.tvHolderTimes.setVisibility(View.VISIBLE);
            viewHolder.tvHolderTimes.setText("Time: " + item.getTimeFromAsString() + " - " + item.getTimeToAsString());
        } else {
            label = "Time: ";
            viewHolder.tvHolderFrequency.setText(label + TimeUtil.FloatTimeToStringExact(item.getFloatFrequency()));
            viewHolder.tvHolderTimes.setText("");
            viewHolder.tvHolderTimes.setVisibility(View.GONE);
        }

        viewHolder.tvHolderDays.setText("Days: " + item.getDaysAsString());
//        Log.d("myadapter getview", "days = " + item.getDaysAsString());
//        Log.d("myadapter getview", "visibility = " + item.visibility);

        viewHolder.llSecondary.setVisibility(item.visibility);
        if (item.visibility == View.GONE) {
            viewHolder.llAll.setBackgroundResource(R.color.transparent);
        } else {
            viewHolder.llAll.setBackgroundResource(R.color.light_grey);
        }
        setButtonImage(!item.isActive(), viewHolder.btnStart);

        return rowView;
    } //end of getView method

    //reduceCounters is used to decrease the countdown timers for all active reminders
    public boolean reduceCounters(long interval) {
        int listSize = getCount();
        boolean complete = false;
        Reminder item;

        for (int i = 0; i < listSize; i++) {
            item = getItem(i);
            if (item.isActive()) {
                if (item.reduceCounter(interval)) { complete = true; }
            }
        }
        return complete;
    }

    //event listener for the start/stop control
    private View.OnClickListener mOnStartClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
        final int position = mListView.getPositionForView((View) view.getParent());
            Reminder reminder = getItem(position);

        if (reminder.isActive()) {
            setButtonImage(true, view);
            reminderCallbacks.cancelReminderCallBack(position);
        } else {
            setButtonImage(false, view);
            reminderCallbacks.startReminderCallBack(position);
        }

        notifyDataSetChanged();
        }
    };

    //event listener for the edit control
    private View.OnClickListener mOnEditClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            View tempView = ((View) (view.getParent()).getParent());
            final int position = mListView.getPositionForView((View) view.getParent());
            ReminderViewHolder viewHolder = (ReminderViewHolder) tempView.getTag();

            setButtonImage(true, viewHolder.btnStart);
            reminderCallbacks.editReminderCallBack(position);
        }
    };

    //event listener for edit control
    private View.OnClickListener mOnDeleteClickListener = (new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            final int position = mListView.getPositionForView((View) view.getParent());
            reminderCallbacks.deleteReminderCallBack(position);
        }
    });

    //onTouch event used to set the focus in order to prevent multiple reminders from being
        //expanded at the same time
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if (view instanceof Button) {
            Button button = (Button) view;
            button.setFocusable(true);
            button.setFocusableInTouchMode(true);
        } else {
            ReminderViewHolder viewHolder = (ReminderViewHolder) view.getTag();
            viewHolder.btnEdit.setFocusableInTouchMode(false);
            viewHolder.btnEdit.setFocusable(false);
            viewHolder.btnStart.setFocusable(false);
            viewHolder.btnStart.setFocusableInTouchMode(false);
            viewHolder.btnDelete.setFocusable(false);
            viewHolder.btnDelete.setFocusableInTouchMode(false);
        }
        return false;
    }

    //setButtonImage alternates the image of the start/stop control
    private void setButtonImage(boolean toStart, View view) {
        Button startButton = (Button) view;

        if (toStart) {
            startButton.setBackgroundResource(R.drawable.play_green);
        } else {
            startButton.setBackgroundResource(R.drawable.stop);
        }
    }


    //end of MyAdapter class
}
