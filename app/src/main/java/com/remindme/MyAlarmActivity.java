package com.remindme;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Scott on 5/1/2015.
 */

public class MyAlarmActivity extends Activity {
    private MediaPlayer mediaPlayer;
    private List<Integer> songId = Arrays.asList(R.raw.alarm1, R.raw.alarm2, R.raw.alarm3, R.raw.alarm4,
            R.raw.alarm5, R.raw.alarm6, R.raw.alarm7, R.raw.alarm8);

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.alarm_activity_layout);

        //todo need to pass in message ID and reminder text
        //load interface with data in case of an edit
        Intent intent = getIntent();
        int messageId = intent.getIntExtra("messageId", 1);
        String reminder = intent.getStringExtra("reminder");

        TextView textView = (TextView) findViewById(R.id.textViewAlarmReminder);
        textView.setText(reminder);

        messageId = songId.get(messageId);
        mediaPlayer = MediaPlayer.create(this, messageId);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void goStopAlarm(View view) {
        mediaPlayer.stop();
        mediaPlayer.release();
        finish();
    }



    //end of MyAlarmActivity class
}
