package com.msl.utaastu.Utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.msl.utaastu.Application.MyApplication;

/**
 * Created by Malek Shefat on 6/14/2017.
 */

public class SharedPreferencesRef {

    public static boolean getBooleanSharedPreferences(String name, String key) {
        SharedPreferences sharedPreferences = MyApplication.getAppContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, false);
    }

    public static void setBooleanSharedPreferences(String name, String key, boolean b) {
        SharedPreferences sharedPreferences = MyApplication.getAppContext().getSharedPreferences(name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

}
