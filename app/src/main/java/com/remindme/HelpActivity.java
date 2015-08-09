package com.remindme;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

public class HelpActivity extends Activity {

    public static final String HELP_ID_KEY = "helpIdKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView textView = (TextView) findViewById (R.id.mainTextView);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText (Html.fromHtml(getString(R.string.help_main_text)));
    }

    public void onClickHelp (View view)
    {
        int id = view.getId ();
        int textId = -1;
        switch (id) {
            case R.id.help_button1 :
                //general help for opening, closing, creating, using back button to navigate
                textId = R.string.help_general;
                break;
            case R.id.help_button2 :
                //help for using the create page
                textId = R.string.help_create;
                break;
            case R.id.help_button3 :
                //help for starting and stopping, include phone restart issue
                textId = R.string.help_start;
                break;
            case R.id.help_button4 :
                //help with editing, using back button
                textId = R.string.help_edit;
                break;
            case R.id.help_button5 :
                //help with deleting, necessary?
                textId = R.string.help_delete;
                break;
            default:
                break;
        }
        if (textId >= 0) startInfoActivity(textId);
//        else toast ("Detailed Help for that topic is not available.", true);
    }

    public void startInfoActivity (int textId)
    {
        if (textId >= 0) {
            Intent intent = (new Intent(this, HelpTopicActivity.class));
            intent.putExtra (HELP_ID_KEY, textId);
            startActivity (intent);
        }
    }
}
