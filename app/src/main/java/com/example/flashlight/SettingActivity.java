package com.example.flashlight;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

public class SettingActivity extends AppCompatActivity {
    private EditTextPreference usernamePreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
// Get the EditTextPreference by its key
        usernamePreference = findPreference(sharedPreferences, "pref_username");
        // Set the listener to update the preference value
        // Update the summary based on custom logic
        String username = sharedPreferences.getString("pref_username", "");
        updateUsernameSummary(username);
//        usernamePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//            @Override
//            public boolean onPreferenceChange(Preference preference, Object newValue) {
//                Log.e("GGGGGGGGGGGGGGGGGGGGGGGGGGG","bHADAL GAYA HAI BHAI");
//
//                // Update the preference value with the entered name
//                usernamePreference.setSummary((String) newValue);
//                return true;
//            }
//        });
        Fragment fragment = new SettingsScreen();
        // Get the fragment manager

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        if(savedInstanceState==null){
            fragmentTransaction.add(R.id.fragment_container,fragment,"fragment_settingfragment");
            fragmentTransaction.commit();

        }
        else{
            fragment=getFragmentManager().findFragmentByTag("fragment_settingfragment");
        }



    }


    public static class SettingsScreen extends PreferenceFragment{
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
        }
    }
    private EditTextPreference findPreference(SharedPreferences sharedPreferences, String key) {
        EditTextPreference preference = new EditTextPreference(this);
        preference.setKey(key);
        preference.setTitle(sharedPreferences.getString(key, ""));
        return preference;
    }
    private void updateUsernameSummary(String username) {
        if (username.isEmpty()) {
            usernamePreference.setSummary("Enter your username");
        } else {
            usernamePreference.setSummary("Username: " + username);
        }
    }
}
