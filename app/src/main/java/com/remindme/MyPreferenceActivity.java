package com.remindme;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import java.util.List;

/**
 * Created by Scott on 5/10/2015.
 */
public class MyPreferenceActivity extends android.preference.PreferenceActivity {

    /*   save/read examples
SharedPreferences SP = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
String strUserName = SP.getString("username", "NA");
boolean bAppUpdates = SP.getBoolean("applicationUpdates",false);
String downloadType = SP.getString("downloadType","1");
     */
/*
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.headers_preferences, target);
    }

    public boolean isValidFragment(String fragmentName) {
        return MyPreferenceFragment.class.getName().equals(fragmentName);
    }

*/
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new MyPreferenceFragment()).commit();

    }




}
