package com.msl.utaastu.Activities;

import android.app.FragmentTransaction;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.msl.utaastu.Application.MyApplication;
import com.msl.utaastu.R;
import com.msl.utaastu.Services.AlarmManagerHelper;
import com.msl.utaastu.UserData.ProfileDialog;

import java.text.ParseException;
import java.util.List;

/**
 * Created by Malek Shefat on 7/11/2017.
 */

public class SettingsActivity extends AppCompatActivity {

    private static FragmentManager v4manager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        v4manager = getSupportFragmentManager();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
        SettingsScreen settingsScreen;
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (savedInstanceState == null) {
            settingsScreen = new SettingsScreen();
            transaction.replace(R.id.container, settingsScreen, "settings-fragment");
            transaction.commit();
        } else {
            settingsScreen = (SettingsScreen) getFragmentManager().findFragmentByTag("settings-fragment");
            transaction.replace(R.id.container, settingsScreen);
            transaction.commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }

    public static class SettingsScreen extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

        private FirebaseUser user = MyApplication.getUser();
        private Preference dialogPreference;
        private PreferenceCategory notifications_topics;
        private SwitchPreference auto_silence, general_notifications;
        private List<String> departments = MyApplication.getWritableDepartmentsDatabase().getAllStrings();
        private boolean[] department_notifications = new boolean[departments.size()];

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_screen);
            dialogPreference = getPreferenceScreen().findPreference("profile-dialog");
            notifications_topics = (PreferenceCategory) getPreferenceScreen().findPreference("notifications-topics");
            auto_silence = (SwitchPreference) getPreferenceScreen().findPreference("auto-silence");
            general_notifications = (SwitchPreference) getPreferenceScreen().findPreference("general-notifications");
            addDepartmentCheckBoxes(notifications_topics);
            notifications_topics.setEnabled(general_notifications.isChecked());
            dialogPreference.setOnPreferenceClickListener(this);
            auto_silence.setOnPreferenceChangeListener(this);
            general_notifications.setOnPreferenceChangeListener(this);
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            switch (preference.getKey()) {
                case "profile-dialog":
                    ProfileDialog profileDialog = new ProfileDialog();
                    profileDialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                    profileDialog.show(v4manager, "profile");
                    break;
            }
            return true;
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            switch (preference.getKey()) {
                case "auto-silence":
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !notificationManager.isNotificationPolicyAccessGranted()
                            && (boolean) newValue) {
                        //  ask permission to Access Do Not Disturb state !
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                        startActivity(intent);
                        ((SwitchPreference) preference).setChecked(false);
                    } else {
                        try {
                            MyApplication.alarmSet((boolean) newValue);
                            AlarmManagerHelper.setAlarm(getActivity(), !(boolean) newValue);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case "general-notifications":
                    enableNotifications("general", (boolean) newValue);
                    enableNotifications(user.getUid(), (boolean) newValue);
                    notifications_topics.setEnabled((boolean) newValue);
                    for (int i = 0; i < department_notifications.length && i < departments.size(); i++)
                        enableNotifications(departments.get(i), (boolean) newValue && department_notifications[i]);
                    break;
                default:
                    for (int i = 0; i < department_notifications.length && i < departments.size(); i++) {
                        if (preference.getKey().equals(departments.get(i))) {
                            enableNotifications(departments.get(i), (boolean) newValue);
                            department_notifications[i] = (boolean) newValue;
                            break;
                        }
                    }
                    break;
            }
            return true;
        }

        private void enableNotifications(String id, boolean enabled) {
            if (enabled) {
                FirebaseMessaging.getInstance().subscribeToTopic(id.contains(" ") ? id.split(" ")[0] : id);
                return;
            }
            FirebaseMessaging.getInstance().unsubscribeFromTopic(id.contains(" ") ? id.split(" ")[0] : id);
        }

        private void addDepartmentCheckBoxes(PreferenceCategory category) {
            for (int i = 0; i < departments.size() && i < department_notifications.length; i++) {
                CheckBoxPreference checkBoxPref = new CheckBoxPreference(getActivity());
                checkBoxPref.setDefaultValue(departments.get(i).equalsIgnoreCase(MyApplication.readDepartment()));
                checkBoxPref.setTitle(departments.get(i));
                checkBoxPref.setKey(departments.get(i));
                checkBoxPref.setOnPreferenceChangeListener(this);
                category.addPreference(checkBoxPref);
                department_notifications[i] = checkBoxPref.isChecked();
            }
        }
    }
}
