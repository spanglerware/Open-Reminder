package com.remindme;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//HelpTopicActivity displays a help screen for the topic selected in HelpActivity
public class HelpTopicActivity extends Activity {

    private int mTextResourceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_topic);

        // Read the arguments from the Intent object.
        Intent in = getIntent ();
        mTextResourceId = in.getIntExtra (HelpActivity.HELP_ID_KEY, 0);
        if (mTextResourceId <= 0) mTextResourceId = R.string.help_error;
        TextView textView = (TextView) findViewById (R.id.topic_text);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText (Html.fromHtml(getString(mTextResourceId)));
    }


//end of HelpTopicActivity class
}
