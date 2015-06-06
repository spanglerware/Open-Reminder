package com.remindme;

import android.content.Context;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

/**
 * Created by Scott on 5/10/2015.
 */
public class MyPreferenceFragment extends android.preference.PreferenceFragment {

//todo may be able to set preferences for each reminder by setting PreferenceManager.
//todo  .setDefaultValues(this, R.xml.pref, TRUE), and then changing default values for each one

    //todo could use headers for days and then checkbox each day

    //todo can override onSetInitialValue to set the value from something other than default?

    //todo also may be able to use onGetDefaultValue, may not work multiple times though,
    //todo may need to instantiate pref objects each time called?

    PreferenceScreen preferenceScreen;
    private Context context = getActivity();

    @Override
    public void onCreate(final Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.preferences);
//        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
//        preferenceScreen = (PreferenceScreen) getPreferenceManager()
//                .findPreference("preferenceReminders");
//                    .createPreferenceScreen(this);
//            setPreferenceScreen(preferenceScreen);
//        preferenceScreen.removeAll();
//        fillPreferenceScreen();
    }
/*
    private void fillPreferenceScreen() {
        EditTextPreference editTextPref = new EditTextPreference(context);
        editTextPref.setDialogTitle("Enter Reminder");
        editTextPref.setKey("editTextPreference");
        editTextPref.setTitle("Reminder");
        editTextPref.setSummary("Enter a reminder");
        preferenceScreen.addPreference(editTextPref);
    }
*/
}
