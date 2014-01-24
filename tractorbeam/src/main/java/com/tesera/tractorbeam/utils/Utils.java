package com.tesera.tractorbeam.utils;


import android.content.Context;

public class Utils {

    public static String getStringFromPrefs(Context context, String key) {
        return context.getSharedPreferences(Consts.PREF_NAME, Context.MODE_PRIVATE).getString(key, null);
    }

    public static void setStringToPrefs(Context context, String key, String value) {
        context.getSharedPreferences(Consts.PREF_NAME, Context.MODE_PRIVATE).edit().putString(key, value).commit();
    }
}
