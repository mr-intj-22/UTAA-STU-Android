package com.msl.utaastu.Application;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.msl.utaastu.Database.CoursesDatabase;
import com.msl.utaastu.Database.DepartmentsDatabase;
import com.msl.utaastu.Database.TimesDatabase;
import com.msl.utaastu.R;

import static com.msl.utaastu.Firebase.FirebaseConstants.BUS_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.CONTACTS_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.EXAMS_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.FOOD_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.LAST_UPDATE_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.NOTIFICATIONS_KEY;
import static com.msl.utaastu.Firebase.FirebaseConstants.USERS_NODE;

/**
 * Created by malek_000 on 8/4/2015.
 */

public class MyApplication extends Application {

    private static MyApplication instance;

    private static Bitmap profileImage = null;

    private static FirebaseStorage storage;
    private static FirebaseDatabase database;
    private static FirebaseAuth auth;

    private static CoursesDatabase courses;
    private static DepartmentsDatabase departments;
    private static TimesDatabase lectures_times;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = FirebaseDatabase.getInstance();
        database.setPersistenceEnabled(true);
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();
        keepSynced();
    }

    private void keepSynced() {
        DatabaseReference examsRef = database.getReference(EXAMS_KEY);
        examsRef.keepSynced(true);
        DatabaseReference notifyRef = database.getReference(NOTIFICATIONS_KEY);
        notifyRef.keepSynced(true);
        DatabaseReference foodRef = database.getReference(FOOD_KEY);
        foodRef.keepSynced(true);
        DatabaseReference busRef = database.getReference(BUS_KEY);
        busRef.keepSynced(true);
        DatabaseReference contactsRef = database.getReference(CONTACTS_KEY);
        contactsRef.keepSynced(true);
        DatabaseReference lastUpdateRef = database.getReference(LAST_UPDATE_KEY);
        lastUpdateRef.keepSynced(true);
        DatabaseReference usersRef = database.getReference(USERS_NODE);
        usersRef.keepSynced(true);
    }

    public static Context getAppContext() {
        return instance.getApplicationContext();
    }

    /*  Databases   */

    public synchronized static FirebaseStorage getStorage() {
        return storage;
    }

    public synchronized static FirebaseDatabase getDatabase() {
        return database;
    }

    public synchronized static FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

    public synchronized static CoursesDatabase getWritableCoursesDatabase() {
        if (courses == null) {
            courses = new CoursesDatabase(getAppContext());
        }
        return courses;
    }

    public synchronized static DepartmentsDatabase getWritableDepartmentsDatabase() {
        if (departments == null) {
            departments = new DepartmentsDatabase(getAppContext());
        }
        return departments;
    }

    public synchronized static TimesDatabase getWritableLecturesTimesDatabase() {
        if (lectures_times == null) {
            lectures_times = new TimesDatabase(getAppContext());
        }
        return lectures_times;
    }

    /*  SharedPreferences   */

    public synchronized static void storeDepartment(String department) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putString("stnemtraped", department);
        edit.apply();
    }

    public synchronized static String readDepartment() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getString("stnemtraped", getAppContext().getResources().getString(R.string.selectDepartment));
    }

    public synchronized static void alarmSet(boolean audio_changed) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean("audio_changer", audio_changed);
        edit.apply();
    }

    public synchronized static boolean isAlarmSet() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getBoolean("audio_changer", true);
    }

    public synchronized static void setGifs(String key, boolean audio_changed) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putBoolean(key, audio_changed);
        edit.apply();
    }

    public synchronized static boolean showGifs(String key) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, true);
    }

    public synchronized static void setLastPdf(int lastPdf) {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        SharedPreferences.Editor edit = sharedPreferences.edit();
        edit.putInt("last_pdf", lastPdf);
        edit.apply();
    }

    public synchronized static int getLastPdf() {
        SharedPreferences sharedPreferences = getAppContext().getSharedPreferences(getAppContext().getString(R.string.app_name), MODE_PRIVATE);
        return sharedPreferences.getInt("last_pdf", 0);
    }

    /*  ProfileImage    */

    public synchronized static Bitmap getProfileImage() {
        return profileImage;
    }

    public synchronized static void setProfileImage(Bitmap bitmap) {
        profileImage = bitmap;
    }
}