package org.andstatus.todoagenda.prefs;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

import org.andstatus.todoagenda.R;

public class EventDetailsPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences_event_details);
    }
}