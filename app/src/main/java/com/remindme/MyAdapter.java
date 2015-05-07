package com.remindme;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Scott on 4/26/2015.
 */

public class MyAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private final ArrayList<Reminder> myItems;
    private final Context myContext;
    private HashMap<Integer,TextView> counterMap;

    static class TestViewHolder {
        public TextView tvHolderReminder;
        public TextView tvHolderFrequency;
        public TextView tvHolderRowId;
    }

//todo use constructor for multiple TVs
    public MyAdapter(Context context, final ArrayList<Reminder> data) {
        super();
        //super(context, data, resource, from, to);
        layoutInflater = LayoutInflater.from(context);
        myItems = data;
        myContext = context;
        counterMap = new HashMap<Integer,TextView>();
    }

    public int getCount() {
        return myItems.size();
    }

    public Reminder getItem(int position) {
        return myItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.item_layout, parent, false);
            final TestViewHolder viewHolder = new TestViewHolder();
            viewHolder.tvHolderReminder = (TextView) rowView.findViewById(R.id.textViewItemTask);
            viewHolder.tvHolderFrequency = (TextView) rowView.findViewById(R.id.textViewItemFrequency);
            viewHolder.tvHolderRowId = (TextView) rowView.findViewById(R.id.textViewItemNumber);
            rowView.setTag(viewHolder);
        }

        TestViewHolder holder = (TestViewHolder) rowView.getTag();
        final TextView tvF = holder.tvHolderFrequency;
        final TextView tvR = holder.tvHolderReminder;
        final TextView tvRow = holder.tvHolderRowId;
        //final TextView tvF = (TextView) rowView.findViewById(R.id.textViewItemFrequency);
        //final TextView tvR = (TextView) rowView.findViewById(R.id.textViewItemTask);
        //final TextView tvRow = (TextView) rowView.findViewById(R.id.textViewItemNumber);

        Reminder item = myItems.get(position);

        tvR.setText(item.getReminder());
        tvF.setText(item.getFrequency());
        tvRow.setText(String.valueOf(item.getRowId()));
        if (item.isActive()) {
            //myCustomTimer.setTimer(tvF, (Long.parseLong(item.getFrequency()) * 360000));
            tvF.setText(item.getCounterAsString());
            tvF.setBackgroundResource(R.drawable.active_spinner);
            tvR.setBackgroundResource(R.drawable.active_spinner);
            counterMap.put(position,tvF);
        } else {
            tvF.setBackgroundResource(R.drawable.border_style);
            tvR.setBackgroundResource(R.drawable.border_style);
        }
        return rowView;
    }

    public boolean reduceCounters(long interval) {
        int listSize = myItems.size();
        boolean complete = false;
        Reminder item;

        for (int i = 0; i < listSize; i++) {
            item = myItems.get(i);
            if (item.isActive()) {
                if (item.reduceCounter(interval)) { complete = true; }
            }
        }
        return complete;
    }

    public void removeItem(int position) {
        myItems.remove(position);
    }



    //end of MyAdapter class
}
